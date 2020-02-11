package com.simprints.id.activities.dashboard

import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.matcher.ViewMatchers.withText
import br.com.concretesolutions.kappuccino.assertions.VisibilityAssertions.displayed
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.commontesttools.di.TestDashboardActivityModule
import com.simprints.id.commontesttools.di.TestPreferencesModule
import com.simprints.id.data.prefs.settings.SettingsPreferencesManager
import com.simprints.id.testtools.AndroidTestConfig
import com.simprints.testtools.common.di.DependencyRule
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import org.junit.Before
import org.junit.Test

class DashboardActivityAndroidTest {

    @MockK lateinit var mockPreferencesManager: SettingsPreferencesManager
    @MockK lateinit var mockViewModelFactory: DashboardViewModelFactory

    private val app = ApplicationProvider.getApplicationContext<Application>()

    private val preferencesModule by lazy {
        TestPreferencesModule(settingsPreferencesManagerRule = DependencyRule.ReplaceRule {
            mockPreferencesManager
        })
    }

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        every {
            mockViewModelFactory.create<DashboardViewModel>(any())
        } returns mockk(relaxed = true)

        app.component = AndroidTestConfig(this, preferencesModule = preferencesModule)
            .componentBuilder()
            .dashboardActivityModule(buildDashboardActivityModule())
            .build()

        ActivityScenario.launch(DashboardActivity::class.java)
    }

    @Test
    fun withConsentRequiredDisabled_shouldNotShowPrivacyNoticeMenuItem() {
        every { mockPreferencesManager.consentRequired } returns false

        openActionBarOverflowOrOptionsMenu(app.applicationContext)

        onView(withText(R.string.menu_privacy_notice)).check(doesNotExist())
    }

    @Test
    fun withConsentRequiredEnabled_shouldShowPrivacyNoticeMenuItem() {
        every { mockPreferencesManager.consentRequired } returns true

        openActionBarOverflowOrOptionsMenu(app.applicationContext)

        displayed {
            text(R.string.menu_privacy_notice)
        }
    }

    private fun buildDashboardActivityModule() = TestDashboardActivityModule(
        viewModelFactoryRule = DependencyRule.ReplaceRule {
            mockViewModelFactory
        }
    )

}
