package com.simprints.id.activities.settings

import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.adevinta.android.barista.assertion.BaristaRecyclerViewAssertions.assertRecyclerViewItemCount
import com.adevinta.android.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import com.adevinta.android.barista.assertion.BaristaVisibilityAssertions.assertNotDisplayed
import com.adevinta.android.barista.interaction.BaristaListInteractions.clickListItem
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.data.prefs.IdPreferencesManager
import com.simprints.id.testtools.AndroidTestConfig
import com.simprints.id.testtools.di.TestPreferencesModule
import com.simprints.testtools.common.di.DependencyRule
import io.mockk.every
import org.junit.Before
import org.junit.Test

class ModuleSelectionActivityAndroidTest {

    private val app = ApplicationProvider.getApplicationContext<Application>()
    private val preferencesModule = TestPreferencesModule(
        settingsPreferencesManagerRule = DependencyRule.SpykRule
    )

    private lateinit var preferencesManagerSpy: IdPreferencesManager

    private val moduleOptions = setOf("a", "b", "c", "d", "e")
    private val selectedModules = setOf("b")

    @Before
    fun setUp() {
        AndroidTestConfig(preferencesModule = preferencesModule).fullSetup().inject(this)
        preferencesManagerSpy = app.component.getIdPreferencesManager()
    }

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
        every {
            preferencesManagerSpy.moduleIdOptions
        } returns moduleOptions

        every {
            preferencesManagerSpy.selectedModules
        } returns selectedModules

        ActivityScenario.launch(ModuleSelectionActivity::class.java)
    }

    private fun launchWithoutModulesSelected() {
        every {
            preferencesManagerSpy.moduleIdOptions
        } returns moduleOptions

        every {
            preferencesManagerSpy.selectedModules
        } returns emptySet()

        ActivityScenario.launch(ModuleSelectionActivity::class.java)
    }

    companion object {
        private const val FIRST_MODULE_INDEX = 0
    }
}
