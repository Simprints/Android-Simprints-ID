package com.simprints.id.activities.settings

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.rule.ActivityTestRule
import br.com.concretesolutions.kappuccino.assertions.VisibilityAssertions.displayed
import br.com.concretesolutions.kappuccino.assertions.VisibilityAssertions.notDisplayed
import br.com.concretesolutions.kappuccino.custom.recyclerView.RecyclerViewInteractions.recyclerView
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.commontesttools.di.TestPreferencesModule
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.testtools.AndroidTestConfig
import com.simprints.testtools.common.di.DependencyRule
import com.simprints.testtools.common.syntax.whenever
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ModuleSelectionActivityAndroidTest {

    @Rule
    @JvmField
    val rule = ActivityTestRule<ModuleSelectionActivity>(
        ModuleSelectionActivity::class.java, true, false
    )

    private val app = ApplicationProvider.getApplicationContext<Application>()
    private val preferencesModule = TestPreferencesModule(
        settingsPreferencesManagerRule = DependencyRule.MockRule
    )

    private lateinit var preferencesManager: PreferencesManager

    @Before
    fun setUp() {
        AndroidTestConfig(this, null, preferencesModule).fullSetup()
        preferencesManager = app.component.getPreferencesManager()
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
            atPosition(0) {
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
        whenever {
            preferencesManager.moduleIdOptions
        } thenReturn setOf("a", "b", "c", "d", "e")

        whenever {
            preferencesManager.selectedModules
        } thenReturn setOf("b")

        rule.launchActivity(Intent())
    }

    private fun launchWithoutModulesSelected() {
        whenever {
            preferencesManager.moduleIdOptions
        } thenReturn setOf("a", "b", "c", "d", "e")

        whenever {
            preferencesManager.selectedModules
        } thenReturn emptySet()

        rule.launchActivity(Intent())
    }

}
