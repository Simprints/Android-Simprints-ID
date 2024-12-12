package com.simprints.feature.consent.screens.privacy

import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.simprints.feature.consent.R
import com.simprints.testtools.hilt.launchFragmentInHiltContainer
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
    }

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @BindValue
    @JvmField
    internal val viewModel = mockk<PrivacyNoticeViewModel>(relaxed = true)

    private val context = InstrumentationRegistry.getInstrumentation().context

    @Test
    fun `should go back when clicking on the back navigation`() {
        val navController = mockk<NavController>(relaxed = true)

        launchFragmentInHiltContainer<PrivacyNoticeFragment>(navController = navController)

        onView(withContentDescription("back")).perform(click())

        verify(exactly = 1) { navController.popBackStack() }
    }

    @Test
    fun `should display the privacy notice when available`() {
        mockPrivacyNoticeState(PrivacyNoticeState.ConsentAvailable(PRIVACY_NOTICE))

        launchFragmentInHiltContainer<PrivacyNoticeFragment>()

        onView(withId(R.id.privacyDownloadButton)).check(matches(not(isDisplayed())))
        onView(withId(R.id.privacyHeader)).check(matches(not(isDisplayed())))
        onView(withId(R.id.privacyProgress)).check(matches(not(isDisplayed())))
        onView(withId(R.id.privacyErrorCard)).check(matches(not(isDisplayed())))

        onView(withId(R.id.privacyText)).check(matches(withText(PRIVACY_NOTICE)))
    }

    @Test
    fun `should display the correct error message when the privacy notice is not available`() {
        mockPrivacyNoticeState(PrivacyNoticeState.ConsentNotAvailable)

        launchFragmentInHiltContainer<PrivacyNoticeFragment>()

        onView(withId(R.id.privacyText)).check(matches(not(isDisplayed())))
        onView(withId(R.id.privacyErrorCard)).check(matches(not(isDisplayed())))
        onView(withId(R.id.privacyHeader)).check(matches(not(isDisplayed())))
        onView(withId(R.id.privacyProgress)).check(matches(not(isDisplayed())))

        onView(withId(R.id.privacyDownloadButton)).check(matches(isDisplayed()))

        ShadowToast.showedToast(context?.getString(IDR.string.consent_privacy_notice_failed_to_download))
    }

    @Test
    fun `should display the correct error message when the privacy notice is not available because of maintenance with estimated outage`() {
        mockPrivacyNoticeState(PrivacyNoticeState.BackendMaintenance("10"))

        launchFragmentInHiltContainer<PrivacyNoticeFragment>()

        onView(withId(R.id.privacyText)).check(matches(not(isDisplayed())))
        onView(withId(R.id.privacyHeader)).check(matches(not(isDisplayed())))
        onView(withId(R.id.privacyProgress)).check(matches(not(isDisplayed())))

        onView(withId(R.id.privacyDownloadButton)).check(matches(isDisplayed()))
        onView(withId(R.id.privacyErrorCard)).check(matches(isDisplayed()))
        onView(
            withId(R.id.privacyErrorText),
        ).check(matches(withText(context.getString(IDR.string.error_backend_maintenance_with_time_message, "10"))))
    }

    @Test
    fun `should display the correct error message when the privacy notice is not available because of maintenance without estimated outage`() {
        mockPrivacyNoticeState(PrivacyNoticeState.BackendMaintenance(null))

        launchFragmentInHiltContainer<PrivacyNoticeFragment>()

        onView(withId(R.id.privacyText)).check(matches(not(isDisplayed())))
        onView(withId(R.id.privacyHeader)).check(matches(not(isDisplayed())))
        onView(withId(R.id.privacyProgress)).check(matches(not(isDisplayed())))

        onView(withId(R.id.privacyDownloadButton)).check(matches(isDisplayed()))
        onView(withId(R.id.privacyErrorCard)).check(matches(isDisplayed()))
        onView(withId(R.id.privacyErrorText)).check(matches(withText(context.getString(IDR.string.error_backend_maintenance_message))))
    }

    private fun mockPrivacyNoticeState(state: PrivacyNoticeState) {
        every { viewModel.viewState } returns mockk {
            every { observe(any(), any()) } answers {
                secondArg<Observer<PrivacyNoticeState>>().onChanged(state)
            }
        }
    }
}
