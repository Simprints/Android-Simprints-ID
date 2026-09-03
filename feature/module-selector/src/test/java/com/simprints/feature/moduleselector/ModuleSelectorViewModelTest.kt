package com.simprints.feature.moduleselector

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.google.common.truth.Truth.*
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.feature.moduleselector.ModuleSelectorState.SelectionError
import com.simprints.feature.moduleselector.adapter.ModuleSelectorItem
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.SettingsPasswordConfig
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.config.store.tokenization.TokenizationProcessor
import com.simprints.infra.eventsync.module.ModuleSelectionRepository
import com.simprints.infra.eventsync.module.SelectableModule
import com.simprints.infra.sync.OneTime
import com.simprints.infra.sync.SyncOrchestrator
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

internal class ModuleSelectorViewModelTest {
    @get:Rule
    val instantRule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var moduleRepository: ModuleSelectionRepository

    @MockK
    private lateinit var syncOrchestrator: SyncOrchestrator

    @MockK
    private lateinit var configRepository: ConfigRepository

    @MockK
    private lateinit var tokenizationProcessor: TokenizationProcessor

    @MockK
    private lateinit var project: Project

    private lateinit var viewModel: ModuleSelectorViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        coEvery { configRepository.getProject() } returns project
        coEvery {
            configRepository.getProjectConfiguration().general.settingsPassword
        } returns SettingsPasswordConfig.NotSet

        every {
            tokenizationProcessor.untokenizeIfNecessary(
                tokenizableString = any(),
                tokenKeyType = TokenKeyType.ModuleId,
                project = any(),
            )
        } answers { firstArg<TokenizableString>() }
    }

    @Test
    fun `loads modules on init and applies lock state`() = runTest {
        coEvery { moduleRepository.getMaxNumberOfModules() } returns 2
        coEvery { moduleRepository.getModules() } returns listOf(
            SelectableModule(name = "module-a".asTokenizableRaw(), isSelected = true),
            SelectableModule(name = "module-b".asTokenizableRaw(), isSelected = false),
        )
        coEvery {
            configRepository.getProjectConfiguration().general.settingsPassword
        } returns SettingsPasswordConfig.Locked("1234")

        viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertThat(state.isScreenLocked).isTrue()
        assertThat(state.isConfirmEnabled).isTrue()
        assertThat(state.selectionError).isNull()
        assertThat(state.modules)
            .containsExactly(
                ModuleSelectorItem.Module("module-a", "module-a".asTokenizableRaw(), true),
                ModuleSelectorItem.Module("module-b", "module-b".asTokenizableRaw(), false),
            ).inOrder()
    }

    @Test
    fun `displays no modules found item when project is not available`() = runTest {
        coEvery { configRepository.getProject() } returns null
        coEvery { moduleRepository.getMaxNumberOfModules() } returns 2
        coEvery { moduleRepository.getModules() } returns listOf(
            SelectableModule(name = "module-a".asTokenizableRaw(), isSelected = true),
        )

        viewModel = createViewModel()
        advanceUntilIdle()

        assertThat(viewModel.state.value.modules).containsExactly(ModuleSelectorItem.NoResult)
    }

    @Test
    fun `search filters modules case-insensitively`() = runTest {
        coEvery { moduleRepository.getMaxNumberOfModules() } returns 2
        coEvery { moduleRepository.getModules() } returns listOf(
            SelectableModule(name = "Alpha Module".asTokenizableRaw(), isSelected = true),
            SelectableModule(name = "Beta".asTokenizableRaw(), isSelected = false),
        )

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(ModuleSelectorAction.SearchQueryChanged("alp"))

        assertThat(viewModel.state.value.modules).containsExactly(
            ModuleSelectorItem.Module("Alpha Module", "Alpha Module".asTokenizableRaw(), true),
        )
    }

    @Test
    fun `adds no result item when search has no matches`() = runTest {
        coEvery { moduleRepository.getMaxNumberOfModules() } returns 2
        coEvery { moduleRepository.getModules() } returns listOf(
            SelectableModule(name = "Alpha".asTokenizableRaw(), isSelected = true),
            SelectableModule(name = "Beta".asTokenizableRaw(), isSelected = false),
        )

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(ModuleSelectorAction.SearchQueryChanged("zzz"))

        assertThat(viewModel.state.value.modules).containsExactly(ModuleSelectorItem.NoResult)
    }

    @Test
    fun `only selected filter keeps selected modules only`() = runTest {
        coEvery { moduleRepository.getMaxNumberOfModules() } returns 2
        coEvery { moduleRepository.getModules() } returns listOf(
            SelectableModule(name = "Alpha".asTokenizableRaw(), isSelected = true),
            SelectableModule(name = "Beta".asTokenizableRaw(), isSelected = false),
        )

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(ModuleSelectorAction.OnlySelectedChanged(true))

        assertThat(viewModel.state.value.modules).containsExactly(
            ModuleSelectorItem.Module("Alpha", "Alpha".asTokenizableRaw(), true),
        )
        assertThat(viewModel.state.value.onlySelected).isTrue()
    }

    @Test
    fun `updates validation when selection exceeds configured limit`() = runTest {
        coEvery { moduleRepository.getMaxNumberOfModules() } returns 1
        coEvery { moduleRepository.getModules() } returns listOf(
            SelectableModule(name = "Alpha".asTokenizableRaw(), isSelected = true),
            SelectableModule(name = "Beta".asTokenizableRaw(), isSelected = false),
        )

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(
            ModuleSelectorAction.ModuleClicked(
                ModuleSelectorItem.Module("Beta", "Beta".asTokenizableRaw(), false),
            ),
        )

        val state = viewModel.state.value
        assertThat(state.isConfirmEnabled).isFalse()
        assertThat(state.selectionError).isEqualTo(SelectionError.TooManyModulesSelected(1))
    }

    @Test
    fun `shows no module selected validation when all modules are unselected`() = runTest {
        coEvery { moduleRepository.getMaxNumberOfModules() } returns 2
        coEvery { moduleRepository.getModules() } returns listOf(
            SelectableModule(name = "Alpha".asTokenizableRaw(), isSelected = true),
        )

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(
            ModuleSelectorAction.ModuleClicked(
                ModuleSelectorItem.Module("Alpha", "Alpha".asTokenizableRaw(), true),
            ),
        )

        val state = viewModel.state.value
        assertThat(state.isConfirmEnabled).isFalse()
        assertThat(state.selectionError).isEqualTo(SelectionError.NoModuleSelected)
    }

    @Test
    fun `cancel action emits dismiss effect`() = runTest {
        coEvery { moduleRepository.getMaxNumberOfModules() } returns 2
        coEvery { moduleRepository.getModules() } returns emptyList()
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.effects.test {
            viewModel.onAction(ModuleSelectorAction.CancelClicked)
            assertThat(awaitItem()).isEqualTo(ModuleSelectorEffects.Dismiss)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `save action persists modules and triggers sync then emits dismiss effect`() = runTest {
        coEvery { moduleRepository.getMaxNumberOfModules() } returns 2
        coEvery { moduleRepository.getModules() } returns listOf(
            SelectableModule(name = "Alpha".asTokenizableRaw(), isSelected = true),
            SelectableModule(name = "Beta".asTokenizableRaw(), isSelected = false),
        )
        coJustRun { moduleRepository.saveModules(any()) }

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.effects.test {
            viewModel.onAction(ModuleSelectorAction.SaveClicked)
            assertThat(awaitItem()).isEqualTo(ModuleSelectorEffects.Confirmed)
            cancelAndIgnoreRemainingEvents()
        }
        advanceUntilIdle()

        val saveSlot = slot<List<SelectableModule>>()
        coVerify(exactly = 1) { moduleRepository.saveModules(capture(saveSlot)) }
        coVerify(exactly = 1) { syncOrchestrator.execute(OneTime.Events.restart()) }
        assertThat(saveSlot.captured)
            .containsExactly(
                SelectableModule(name = "Alpha".asTokenizableRaw(), isSelected = true),
                SelectableModule(name = "Beta".asTokenizableRaw(), isSelected = false),
            ).inOrder()
    }

    @Test
    fun `lock overlay click emits password dialog effect when locked`() = runTest {
        coEvery { moduleRepository.getMaxNumberOfModules() } returns 2
        coEvery { moduleRepository.getModules() } returns emptyList()
        coEvery {
            configRepository.getProjectConfiguration().general.settingsPassword
        } returns SettingsPasswordConfig.Locked("1234")

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.effects.test {
            viewModel.onAction(ModuleSelectorAction.LockOverlayClicked)
            assertThat(awaitItem()).isEqualTo(ModuleSelectorEffects.ShowPassword("1234"))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `unlock action removes lock and lock click no longer emits effect`() = runTest {
        coEvery { moduleRepository.getMaxNumberOfModules() } returns 2
        coEvery { moduleRepository.getModules() } returns emptyList()
        coEvery {
            configRepository.getProjectConfiguration().general.settingsPassword
        } returns SettingsPasswordConfig.Locked("1234")

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.effects.test {
            viewModel.onAction(ModuleSelectorAction.UnlockScreen)
            viewModel.onAction(ModuleSelectorAction.LockOverlayClicked)
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }

        assertThat(viewModel.state.value.isScreenLocked).isFalse()
    }

    private fun createViewModel() = ModuleSelectorViewModel(
        moduleRepository = moduleRepository,
        syncOrchestrator = syncOrchestrator,
        configRepository = configRepository,
        tokenizationProcessor = tokenizationProcessor,
        externalScope = CoroutineScope(testCoroutineRule.testCoroutineDispatcher),
    )
}
