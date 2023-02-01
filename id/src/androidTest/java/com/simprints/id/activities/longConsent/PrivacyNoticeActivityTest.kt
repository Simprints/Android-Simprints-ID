package com.simprints.id.activities.longConsent

import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import com.simprints.id.R
import com.simprints.id.activities.login.SYNC_CARD_FAILED_BACKEND_MAINTENANCE_STATE_MESSAGE
import com.simprints.id.activities.login.SYNC_CARD_FAILED_BACKEND_MAINTENANCE_STATE_TIMED_MESSAGE
import com.simprints.id.tools.device.DeviceManager
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Test

class PrivacyNoticeActivityTest {

    private val language = "en"

    @MockK
    lateinit var deviceManager: DeviceManager

    @MockK
    lateinit var viewModel: PrivacyNoticeViewModel


    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

    }

    @Test
    fun shouldNot_retrievePrivacyNotice_wheneverPrivacyNotice_buttonIsClicked_andNetwork_isNotConnected() {
        every { deviceManager.isConnected() } returns false

        every { viewModel.getPrivacyNoticeViewStateLiveData() } returns
            MutableLiveData(PrivacyNoticeState.ConsentNotAvailable(language))

        ActivityScenario.launch(PrivacyNoticeActivity::class.java)
        onView(withId(R.id.longConsent_downloadButton)).perform(click())

        verify(exactly = 1) { deviceManager.isConnected() }
        verify(exactly = 1) { viewModel.retrievePrivacyNotice() }
    }

    @Test
    fun downloading_notice_should_show_correct_error_when_backend_maintenance_error() {
        every { viewModel.getPrivacyNoticeViewStateLiveData() } returns
            MutableLiveData(
                PrivacyNoticeState.ConsentNotAvailableBecauseBackendMaintenance(
                    language
                )
            )

        ActivityScenario.launch(PrivacyNoticeActivity::class.java)

        onView(withId(R.id.errorTextView)).check(
            matches(
                withText(
                    SYNC_CARD_FAILED_BACKEND_MAINTENANCE_STATE_MESSAGE
                )
            )
        )
        onView(withId(R.id.errorCard)).check(matches(isDisplayed()))
        onView(withId(R.id.longConsent_TextView)).check(matches(not(isDisplayed())))
        onView(withId(R.id.longConsent_downloadProgressBar)).check(matches(not(isDisplayed())))
        onView(withId(R.id.longConsent_header)).check(matches(not(isDisplayed())))
        onView(withId(R.id.longConsent_downloadButton)).check(matches(isDisplayed()))
    }

    @Test
    fun downloading_notice_should_show_correct_timed_error_when_backend_maintenance_error() {
        every { viewModel.getPrivacyNoticeViewStateLiveData() } returns
            MutableLiveData(
                PrivacyNoticeState.ConsentNotAvailableBecauseBackendMaintenance(
                    language,
                    600L
                )
            )

        ActivityScenario.launch(PrivacyNoticeActivity::class.java)

        onView(withId(R.id.errorTextView)).check(
            matches(
                withText(
                    SYNC_CARD_FAILED_BACKEND_MAINTENANCE_STATE_TIMED_MESSAGE
                )
            )
        )
        onView(withId(R.id.errorCard)).check(matches(isDisplayed()))
        onView(withId(R.id.longConsent_TextView)).check(matches(not(isDisplayed())))
        onView(withId(R.id.longConsent_downloadProgressBar)).check(matches(not(isDisplayed())))
        onView(withId(R.id.longConsent_header)).check(matches(not(isDisplayed())))
        onView(withId(R.id.longConsent_downloadButton)).check(matches(isDisplayed()))
    }

    @Test
    fun should_retrievePrivacyNotice_wheneverPrivacyNotice_buttonIsClicked_andNetwork_isConnected() {
        val longConsent = "some consent to be displayed"
        every { deviceManager.isConnected() } returns true

        val privacyNoticeMutableData = MutableLiveData<PrivacyNoticeState>(
            PrivacyNoticeState.ConsentNotAvailable(language)
        )

        every { viewModel.getPrivacyNoticeViewStateLiveData() } returns privacyNoticeMutableData


        ActivityScenario.launch(PrivacyNoticeActivity::class.java)
        onView(withId(R.id.longConsent_downloadButton)).perform(click())
        privacyNoticeMutableData.postValue(
            PrivacyNoticeState.ConsentAvailable(
                language, longConsent
            )
        )


        onView(withId(R.id.longConsent_TextView)).check(matches(isDisplayed()))
        onView(withId(R.id.longConsent_TextView)).check(matches(withText(longConsent)))
        onView(withId(R.id.longConsent_downloadButton)).check(matches(not(isDisplayed())))

        verify(exactly = 1) { deviceManager.isConnected() }
        verify(exactly = 2) { viewModel.retrievePrivacyNotice() }
    }
}
