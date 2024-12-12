package com.simprints.feature.dashboard.settings.syncinfo.moduleselection

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.feature.dashboard.settings.syncinfo.moduleselection.exceptions.NoModuleSelectedException
import com.simprints.feature.dashboard.settings.syncinfo.moduleselection.exceptions.TooManyModulesSelectedException
import com.simprints.feature.dashboard.settings.syncinfo.moduleselection.repository.Module
import com.simprints.feature.dashboard.settings.syncinfo.moduleselection.repository.ModuleRepository
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.SettingsPasswordConfig
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.config.store.tokenization.TokenizationProcessor
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.eventsync.EventSyncManager
import com.simprints.infra.sync.SyncOrchestrator
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.livedata.getOrAwaitValue
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ModuleSelectionViewModelTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var repository: ModuleRepository

    @MockK
    private lateinit var eventSyncManager: EventSyncManager

    @MockK
    private lateinit var syncOrchestrator: SyncOrchestrator

    @MockK
    private lateinit var configManager: ConfigManager

    @MockK
    private lateinit var tokenizationProcessor: TokenizationProcessor

    @MockK
    private lateinit var authStore: AuthStore

    @MockK
    private lateinit var project: Project

    private lateinit var viewModel: ModuleSelectionViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        val modulesDefault = listOf(
            Module("a".asTokenizableEncrypted(), false),
            Module("b".asTokenizableEncrypted(), false),
            Module("c".asTokenizableEncrypted(), true),
            Module("d".asTokenizableEncrypted(), false),
        )
        coEvery { repository.getModules() } returns modulesDefault
        coEvery { repository.getMaxNumberOfModules() } returns 2
        coEvery { configManager.getProjectConfiguration() } returns mockk {
            every { general.settingsPassword } returns SettingsPasswordConfig.Locked("1234")
        }
        every { authStore.signedInProjectId } returns PROJECT_ID
        coEvery { configManager.getProject(PROJECT_ID) } returns project
        modulesDefault.forEach {
            coEvery {
                tokenizationProcessor.decrypt(
                    encrypted = it.name as TokenizableString.Tokenized,
                    tokenKeyType = TokenKeyType.ModuleId,
                    project = project,
                )
            } returns it.name.value.asTokenizableRaw()
        }

        viewModel = ModuleSelectionViewModel(
            authStore = authStore,
            moduleRepository = repository,
            syncOrchestrator = syncOrchestrator,
            configManager = configManager,
            tokenizationProcessor = tokenizationProcessor,
            externalScope = CoroutineScope(testCoroutineRule.testCoroutineDispatcher),
        )
    }

    @Test
    fun `should initialize the live data with the correct values`() {
        assertThat(viewModel.modulesList.getOrAwaitValue()).isEqualTo(
            listOf(
                Module("a".asTokenizableRaw(), false),
                Module("b".asTokenizableRaw(), false),
                Module("c".asTokenizableRaw(), true),
                Module("d".asTokenizableRaw(), false),
            ),
        )
    }

    @Test
    fun `updateModuleSelection should allow unselecting the last selected module`() {
        viewModel.updateModuleSelection(Module("c".asTokenizableRaw(), true))
        assertThat(viewModel.modulesList.getOrAwaitValue().none(Module::isSelected)).isTrue()
    }

    @Test
    fun `updateModuleSelection should throw a TooManyModulesSelectedException if trying to select more than the maximum number of modules`() {
        viewModel.updateModuleSelection(Module("b".asTokenizableRaw(), false))

        val exception = assertThrows<TooManyModulesSelectedException> {
            viewModel.updateModuleSelection(Module("a".asTokenizableRaw(), false))
        }

        assertThat(exception.maxNumberOfModules).isEqualTo(2)
    }

    @Test
    fun `updateModuleSelection should update the module selection correctly otherwise`() {
        val expectedModules = listOf(
            Module("a".asTokenizableRaw(), false),
            Module("b".asTokenizableRaw(), true),
            Module("c".asTokenizableRaw(), true),
            Module("d".asTokenizableRaw(), false),
        )
        viewModel.updateModuleSelection(Module("b".asTokenizableRaw(), false))

        assertThat(viewModel.modulesList.getOrAwaitValue()).isEqualTo(expectedModules)
    }

    @Test
    fun `hasSelectionChanged should return true if the selection has changed`() {
        viewModel.updateModuleSelection(Module("a".asTokenizableRaw(), false))

        assertThat(viewModel.hasSelectionChanged()).isEqualTo(true)
    }

    @Test
    fun `hasSelectionChanged should return false if the selection hasn't changed`() {
        viewModel.updateModuleSelection(Module("a".asTokenizableRaw(), false))
        viewModel.updateModuleSelection(Module("a".asTokenizableRaw(), true))

        assertThat(viewModel.hasSelectionChanged()).isEqualTo(false)
    }

    @Test
    fun `saveModules should throw NoModuleSelectedException if no modules is selected`() {
        viewModel.updateModuleSelection(Module("c".asTokenizableRaw(), true))
        assertThrows<NoModuleSelectedException> {
            viewModel.saveModules()
        }
    }

    @Test
    fun `saveModules should save the modules and trigger the sync`() {
        val updatedModules = listOf(
            Module("a".asTokenizableRaw(), true),
            Module("b".asTokenizableRaw(), false),
            Module("c".asTokenizableRaw(), true),
            Module("d".asTokenizableRaw(), false),
        )
        updatedModules.forEach { module ->
            every {
                tokenizationProcessor.encrypt(
                    decrypted = module.name as TokenizableString.Raw,
                    tokenKeyType = TokenKeyType.ModuleId,
                    project = project,
                )
            } returns module.name.value.asTokenizableEncrypted()
        }
        viewModel.updateModuleSelection(Module("a".asTokenizableRaw(), false))
        viewModel.saveModules()

        coVerify(exactly = 1) { repository.saveModules(updatedModules) }
        coVerify(exactly = 1) { syncOrchestrator.stopEventSync() }
        coVerify(exactly = 1) { syncOrchestrator.startEventSync() }
    }

    @Test
    fun `should initialize password settings when called`() {
        viewModel.loadPasswordSettings()

        assertThat(viewModel.screenLocked.getOrAwaitValue()).isEqualTo(
            SettingsPasswordConfig.Locked("1234"),
        )
    }

    @Test
    fun `unlockScreens marks screen as unlocked`() {
        viewModel.unlockScreen()

        assertThat(viewModel.screenLocked.getOrAwaitValue()).isEqualTo(
            SettingsPasswordConfig.Unlocked,
        )
    }

    companion object {
        private const val PROJECT_ID = "projectId"
    }
}
