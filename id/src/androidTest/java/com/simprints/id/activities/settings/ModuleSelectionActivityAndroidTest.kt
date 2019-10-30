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
    fun shouldLoadModules() {
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
    fun whenNoModulesAreSelected_noModulesSelectedTextShouldBeVisible() {
        displayed {
            id(R.id.txtNoModulesSelected)
        }
    }

    @Test
    fun whenDeselectingAllModules_noModulesSelectedTextShouldBeVisible() {
        recyclerView(R.id.rvModules) {
            atPosition(0) {
                click()
            }
        }

        notDisplayed {
            id(R.id.txtNoModulesSelected)
        }

        recyclerView(R.id.rvModules) {
            atPosition(0) {
                click()
            }
        }

        displayed {
            id(R.id.txtNoModulesSelected)
        }
    }

    private fun configureMock() {
        val preferencesManager = app.component.getPreferencesManager()
        whenever {
            preferencesManager.moduleIdOptions
        } thenReturn setOf("a", "b", "c", "d")
        whenever {
            preferencesManager.selectedModules
        } thenReturn emptySet()
    }

}
