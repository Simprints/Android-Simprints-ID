package com.simprints.feature.dashboard.settings.syncinfo.moduleselection

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.feature.dashboard.settings.syncinfo.moduleselection.exceptions.NoModuleSelectedException
import com.simprints.feature.dashboard.settings.syncinfo.moduleselection.exceptions.TooManyModulesSelectedException
import com.simprints.feature.dashboard.settings.syncinfo.moduleselection.repository.Module
import com.simprints.feature.dashboard.settings.syncinfo.moduleselection.repository.ModuleRepository
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.SettingsPasswordConfig
import com.simprints.infra.eventsync.EventSyncManager
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.livedata.getOrAwaitValue
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.*
import io.mockk.impl.annotations.MockK
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

    @MockK(relaxed = true)
    private lateinit var repository: ModuleRepository

    @MockK(relaxed = true)
    private lateinit var eventSyncManager: EventSyncManager

    @MockK(relaxed = true)
    private lateinit var configManager: ConfigManager

    private lateinit var viewModel: ModuleSelectionViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        coEvery { repository.getModules() } returns listOf(
            Module("a", false),
            Module("b", false),
            Module("c", true),
            Module("d", false)
        )
        coEvery { repository.getMaxNumberOfModules() } returns 2
        coEvery { configManager.getProjectConfiguration() } returns mockk {
            every { general.settingsPassword } returns SettingsPasswordConfig.Locked("1234")
        }

        viewModel = ModuleSelectionViewModel(
            repository,
            eventSyncManager,
            configManager,
            CoroutineScope(testCoroutineRule.testCoroutineDispatcher),
        )
    }

    @Test
    fun `should initialize the live data with the correct values`() {
        assertThat(viewModel.modulesList.getOrAwaitValue()).isEqualTo(
            listOf(
                Module("a", false),
                Module("b", false),
                Module("c", true),
                Module("d", false)
            )
        )
    }

    @Test
    fun `updateModuleSelection should throw a NoModuleSelectedException if trying to unselect the last selected module`() {
        assertThrows<NoModuleSelectedException> {
            viewModel.updateModuleSelection(Module("c", true))
        }
    }

    @Test
    fun `updateModuleSelection should throw a TooManyModulesSelectedException if trying to select more than the maximum number of modules`() {
        viewModel.updateModuleSelection(Module("b", false))

        val exception = assertThrows<TooManyModulesSelectedException> {
            viewModel.updateModuleSelection(Module("a", false))
        }

        assertThat(exception.maxNumberOfModules).isEqualTo(2)
    }

    @Test
    fun `updateModuleSelection should update the module selection correctly otherwise`() {
        val expectedModules = listOf(
            Module("a", false),
            Module("b", true),
            Module("c", true),
            Module("d", false)
        )
        viewModel.updateModuleSelection(Module("b", false))

        assertThat(viewModel.modulesList.getOrAwaitValue()).isEqualTo(expectedModules)
    }

    @Test
    fun `hasSelectionChanged should return true if the selection has changed`() {
        viewModel.updateModuleSelection(Module("a", false))

        assertThat(viewModel.hasSelectionChanged()).isEqualTo(true)
    }

    @Test
    fun `hasSelectionChanged should return false if the selection hasn't changed`() {
        viewModel.updateModuleSelection(Module("a", false))
        viewModel.updateModuleSelection(Module("a", true))

        assertThat(viewModel.hasSelectionChanged()).isEqualTo(false)
    }

    @Test
    fun `saveModules should save the modules and trigger the sync`() {
        val updatedModules = listOf(
            Module("a", true),
            Module("b", false),
            Module("c", true),
            Module("d", false)
        )
        viewModel.updateModuleSelection(Module("a", false))
        viewModel.saveModules()

        coVerify(exactly = 1) { repository.saveModules(updatedModules) }
        coVerify(exactly = 1) { eventSyncManager.stop() }
        coVerify(exactly = 1) { eventSyncManager.sync() }
    }

    @Test
    fun `should initialize password settings when called`() {
        viewModel.loadPasswordSettings()

        assertThat(viewModel.screenLocked.getOrAwaitValue()).isEqualTo(
            SettingsPasswordConfig.Locked("1234")
        )
    }

    @Test
    fun `unlockScreens marks screen as unlocked`() {
        viewModel.unlockScreen()

        assertThat(viewModel.screenLocked.getOrAwaitValue()).isEqualTo(
            SettingsPasswordConfig.Unlocked
        )
    }
}
