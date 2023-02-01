package com.simprints.feature.dashboard.privacynotices

import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.simprints.feature.dashboard.R
import com.simprints.feature.dashboard.tools.launchFragmentInHiltContainer
import com.simprints.feature.dashboard.privacynotices.PrivacyNoticeState.*
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.hamcrest.core.IsNot.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowToast
import com.simprints.infra.resources.R as IDR

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
@Config(application = HiltTestApplication::class)
class PrivacyNoticesFragmentTest {

    companion object {
        private const val PRIVACY_NOTICE = "privacy notice"
        private const val LANGUAGE = "fr"
    }

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @BindValue
    @JvmField
    internal val viewModel = mockk<PrivacyNoticesViewModel>(relaxed = true)

    private val context = InstrumentationRegistry.getInstrumentation().context

    @Test
    fun `should go back when clicking on the back navigation`() {
        val navController = mockk<NavController>(relaxed = true)

        launchFragmentInHiltContainer<PrivacyNoticesFragment>(navController = navController)

        onView(withContentDescription("back")).perform(click())

        verify(exactly = 1) { navController.popBackStack() }
    }

    @Test
    fun `should display the privacy notice when available`() {
        mockPrivacyNoticeState(Available(LANGUAGE, PRIVACY_NOTICE))

        launchFragmentInHiltContainer<PrivacyNoticesFragment>()

        onView(withId(R.id.privacyNotice_downloadButton)).check(matches(not(isDisplayed())))
        onView(withId(R.id.privacyNotice_header)).check(matches(not(isDisplayed())))
        onView(withId(R.id.privacyNotice_downloadProgressBar)).check(matches(not(isDisplayed())))
        onView(withId(R.id.errorCard)).check(matches(not(isDisplayed())))

        onView(withId(R.id.privacyNotice_TextView)).check(matches(withText(PRIVACY_NOTICE)))
    }

    @Test
    fun `should display the correct error message when the privacy notice is not available`() {
        mockPrivacyNoticeState(NotAvailable(LANGUAGE))

        launchFragmentInHiltContainer<PrivacyNoticesFragment>()

        onView(withId(R.id.privacyNotice_TextView)).check(matches(not(isDisplayed())))
        onView(withId(R.id.errorCard)).check(matches(not(isDisplayed())))
        onView(withId(R.id.privacyNotice_header)).check(matches(not(isDisplayed())))
        onView(withId(R.id.privacyNotice_downloadProgressBar)).check(matches(not(isDisplayed())))

        onView(withId(R.id.privacyNotice_downloadButton)).check(matches(isDisplayed()))

        ShadowToast.showedToast(context?.getString(IDR.string.long_consent_failed_to_download))
    }

    @Test
    fun `should display the correct error message when the privacy notice is not available because of maintenance with estimated outage`() {
        mockPrivacyNoticeState(NotAvailableBecauseBackendMaintenance(LANGUAGE, 10))

        launchFragmentInHiltContainer<PrivacyNoticesFragment>()

        onView(withId(R.id.privacyNotice_TextView)).check(matches(not(isDisplayed())))
        onView(withId(R.id.privacyNotice_header)).check(matches(not(isDisplayed())))
        onView(withId(R.id.privacyNotice_downloadProgressBar)).check(matches(not(isDisplayed())))

        onView(withId(R.id.privacyNotice_downloadButton)).check(matches(isDisplayed()))
        onView(withId(R.id.errorCard)).check(matches(isDisplayed()))
        onView(withId(R.id.errorTextView)).check(matches(withText(context.getString(IDR.string.error_backend_maintenance_with_time_message, "10 seconds"))))
    }

    @Test
    fun `should display the correct error message when the privacy notice is not available because of maintenance without estimated outage`() {
        mockPrivacyNoticeState(NotAvailableBecauseBackendMaintenance(LANGUAGE, null))

        launchFragmentInHiltContainer<PrivacyNoticesFragment>()

        onView(withId(R.id.privacyNotice_TextView)).check(matches(not(isDisplayed())))
        onView(withId(R.id.privacyNotice_header)).check(matches(not(isDisplayed())))
        onView(withId(R.id.privacyNotice_downloadProgressBar)).check(matches(not(isDisplayed())))

        onView(withId(R.id.privacyNotice_downloadButton)).check(matches(isDisplayed()))
        onView(withId(R.id.errorCard)).check(matches(isDisplayed()))
        onView(withId(R.id.errorTextView)).check(matches(withText(context.getString(IDR.string.error_backend_maintenance_message))))
    }

    @Test
    fun `should display the correct error message when there is not connectivity`() {
        mockPrivacyNoticeState(NotConnectedToInternet(LANGUAGE))

        launchFragmentInHiltContainer<PrivacyNoticesFragment>()

        onView(withId(R.id.privacyNotice_TextView)).check(matches(not(isDisplayed())))
        onView(withId(R.id.errorCard)).check(matches(not(isDisplayed())))
        onView(withId(R.id.privacyNotice_header)).check(matches(not(isDisplayed())))
        onView(withId(R.id.privacyNotice_downloadProgressBar)).check(matches(not(isDisplayed())))

        onView(withId(R.id.privacyNotice_downloadButton)).check(matches(isDisplayed()))

        ShadowToast.showedToast(context?.getString(IDR.string.login_no_network))
    }

    private fun mockPrivacyNoticeState(state: PrivacyNoticeState) {
        every { viewModel.privacyNoticeState } returns mockk {
            every { observe(any(), any()) } answers {
                secondArg<Observer<PrivacyNoticeState>>().onChanged(state)
            }
        }
    }
}
