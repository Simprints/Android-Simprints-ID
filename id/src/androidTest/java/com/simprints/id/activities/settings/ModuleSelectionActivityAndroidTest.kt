package com.simprints.id.activities.settings

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.rule.ActivityTestRule
import br.com.concretesolutions.kappuccino.actions.ClickActions.click
import br.com.concretesolutions.kappuccino.assertions.VisibilityAssertions.displayed
import br.com.concretesolutions.kappuccino.assertions.VisibilityAssertions.notDisplayed
import br.com.concretesolutions.kappuccino.custom.recyclerView.RecyclerViewInteractions.recyclerView
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.commontesttools.di.TestPreferencesModule
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

    @Before
    fun setUp() {
        AndroidTestConfig(this, null, preferencesModule).fullSetup()
        configureMock()
        rule.launchActivity(Intent())
    }

    @Test
    fun shouldLoadOnlyUnselectedModules() {
        recyclerView(R.id.rvModules) {
            sizeIs(4)
        }
    }

    @Test
    fun whenSelectingModules_noModulesSelectedTextShouldNotBeVisible() {
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
    fun whenDeselectingAllModules_noModulesSelectedTextShouldBeVisible() {
        click {
            id(ID_CHIP_CLOSE_BUTTON)
        }

        displayed {
            id(R.id.txtNoModulesSelected)
        }
    }

    @Test
    fun whenSelectingAModule_shouldBeRemovedFromList() {
        recyclerView(R.id.rvModules) {
            atPosition(0) {
                click()
            }

            sizeIs(3)
        }
    }

    private fun configureMock() {
        val preferencesManager = app.component.getPreferencesManager()

        whenever {
            preferencesManager.moduleIdOptions
        } thenReturn setOf("a", "b", "c", "d", "e")

        whenever {
            preferencesManager.selectedModules
        } thenReturn setOf("b")
    }

    private companion object {
        const val ID_CHIP_CLOSE_BUTTON = 1
    }

}
