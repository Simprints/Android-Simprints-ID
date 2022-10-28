package com.simprints.id.activities.settings

import androidx.test.core.app.ActivityScenario
import com.adevinta.android.barista.assertion.BaristaRecyclerViewAssertions.assertRecyclerViewItemCount
import com.adevinta.android.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import com.adevinta.android.barista.assertion.BaristaVisibilityAssertions.assertNotDisplayed
import com.adevinta.android.barista.interaction.BaristaListInteractions.clickListItem
import com.simprints.id.R
import com.simprints.infra.config.ConfigManager
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import org.junit.Test

class ModuleSelectionActivityAndroidTest {


    lateinit var configManager: ConfigManager

    private val moduleIdsOptions = listOf("a", "b", "c", "d", "e")
    private val selectedModuleIds = listOf("b")


    @Test
    fun shouldLoadOnlyUnselectedModules() {
        launchWithModulesSelected()
        assertRecyclerViewItemCount(R.id.rvModules, 4)
    }

    @Test
    fun whenSelectingModules_noModulesSelectedTextShouldNotBeVisible() {
        launchWithModulesSelected()
        clickListItem(R.id.rvModules, FIRST_MODULE_INDEX)
        assertNotDisplayed(R.id.txtNoModulesSelected)

    }

    @Test
    fun whenSelectingAModule_shouldBeRemovedFromList() {
        launchWithModulesSelected()
        clickListItem(R.id.rvModules, 0)
        assertRecyclerViewItemCount(R.id.rvModules, 3)
    }

    @Test
    fun withSelectedModules_shouldDisplaySelectedModulesText() {
        launchWithModulesSelected()
        assertDisplayed(R.id.txtSelectedModules)
    }

    @Test
    fun withSelectedModules_shouldNotDisplayNoModulesSelectedText() {
        launchWithModulesSelected()
        assertNotDisplayed(R.id.txtNoModulesSelected)
    }

    @Test
    fun withoutSelectedModules_shouldDisplayNoModulesSelectedText() {
        launchWithoutModulesSelected()

        assertDisplayed(R.id.txtNoModulesSelected)
    }

    @Test
    fun withoutSelectedModules_shouldNotDisplaySelectedModulesText() {
        launchWithoutModulesSelected()

        assertNotDisplayed(R.id.txtSelectedModules)
    }


    private fun launchWithModulesSelected() {
        coEvery { configManager.getProjectConfiguration() } returns mockk {
            every { synchronization } returns mockk {
                every { down } returns mockk {
                    every { moduleOptions } returns moduleIdsOptions
                    every { maxNbOfModules } returns 6
                }
            }
        }

        coEvery { configManager.getDeviceConfiguration() } returns mockk {
            every { selectedModules } returns selectedModuleIds
        }

        ActivityScenario.launch(ModuleSelectionActivity::class.java)
    }

    private fun launchWithoutModulesSelected() {
        coEvery { configManager.getProjectConfiguration() } returns mockk {
            every { synchronization } returns mockk {
                every { down } returns mockk {
                    every { moduleOptions } returns moduleIdsOptions
                    every { maxNbOfModules } returns 6
                }
            }
        }

        coEvery { configManager.getDeviceConfiguration() } returns mockk {
            every { selectedModules } returns listOf()
        }

        ActivityScenario.launch(ModuleSelectionActivity::class.java)
    }

    companion object {
        private const val FIRST_MODULE_INDEX = 0
    }
}
