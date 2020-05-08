package com.simprints.id.activities.settings

import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import br.com.concretesolutions.kappuccino.assertions.VisibilityAssertions.displayed
import br.com.concretesolutions.kappuccino.assertions.VisibilityAssertions.notDisplayed
import br.com.concretesolutions.kappuccino.custom.recyclerView.RecyclerViewInteractions.recyclerView
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.commontesttools.di.TestPreferencesModule
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.testtools.AndroidTestConfig
import com.simprints.testtools.common.di.DependencyRule
import io.mockk.every
import org.junit.Before
import org.junit.Test

class ModuleSelectionActivityAndroidTest {

    private val app = ApplicationProvider.getApplicationContext<Application>()
    private val preferencesModule = TestPreferencesModule(
        settingsPreferencesManagerRule = DependencyRule.SpykRule
    )

    private lateinit var preferencesManagerSpy: PreferencesManager

    private val moduleOptions = setOf("a", "b", "c", "d", "e")
    private val selectedModules = setOf("b")

    @Before
    fun setUp() {
        AndroidTestConfig(this, null, preferencesModule = preferencesModule).fullSetup()
        preferencesManagerSpy = app.component.getPreferencesManager()
    }

    @Test
    fun shouldLoadOnlyUnselectedModules() {
        launchWithModulesSelected()

        recyclerView(R.id.rvModules) {
            sizeIs(4)
        }
    }

    @Test
    fun whenSelectingModules_noModulesSelectedTextShouldNotBeVisible() {
        launchWithModulesSelected()

        recyclerView(R.id.rvModules) {
            atPosition(FIRST_MODULE_INDEX) {
                click()
            }
        }

        notDisplayed {
            id(R.id.txtNoModulesSelected)
        }
    }

    @Test
    fun whenSelectingAModule_shouldBeRemovedFromList() {
        launchWithModulesSelected()

        recyclerView(R.id.rvModules) {
            atPosition(0) {
                click()
            }

            sizeIs(3)
        }
    }

    @Test
    fun withSelectedModules_shouldDisplaySelectedModulesText() {
        launchWithModulesSelected()

        displayed {
            id(R.id.txtSelectedModules)
        }
    }

    @Test
    fun withSelectedModules_shouldNotDisplayNoModulesSelectedText() {
        launchWithModulesSelected()

        notDisplayed {
            id(R.id.txtNoModulesSelected)
        }
    }

    @Test
    fun withoutSelectedModules_shouldDisplayNoModulesSelectedText() {
        launchWithoutModulesSelected()

        displayed {
            id(R.id.txtNoModulesSelected)
        }
    }

    @Test
    fun withoutSelectedModules_shouldNotDisplaySelectedModulesText() {
        launchWithoutModulesSelected()

        notDisplayed {
            id(R.id.txtSelectedModules)
        }
    }


    private fun launchWithModulesSelected() {
        every {
            preferencesManagerSpy.moduleIdOptions
        } returns  moduleOptions

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
