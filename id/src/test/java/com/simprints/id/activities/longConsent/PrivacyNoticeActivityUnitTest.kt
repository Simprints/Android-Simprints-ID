package com.simprints.id.activities.longConsent

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.id.R
import com.simprints.id.data.consent.longconsent.LongConsentFetchResult
import com.simprints.id.data.consent.longconsent.LongConsentRepository
import com.simprints.id.testtools.TestApplication
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.DeviceConfiguration
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import com.simprints.testtools.unit.robolectric.createAndStartActivity
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class PrivacyNoticeActivityUnitTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private val longConsentRepository: LongConsentRepository = mockk(relaxed = true)
    private val configManager = mockk<ConfigManager> {
        coEvery { getDeviceConfiguration() } returns DeviceConfiguration(
            LANGUAGE,
            listOf(),
            listOf(),
            ""
        )
    }

    @Test
    fun withSuccessConfirmViews() {
        runTest {
            coEvery {
                longConsentRepository.getLongConsentResultForLanguage(LANGUAGE)
            } returns flowOf(
                LongConsentFetchResult.InProgress("en"),
                LongConsentFetchResult.Succeed("en", "some consent")
            )

            createAndStartActivity<PrivacyNoticeActivity>()

            onView(withId(R.id.errorCard)).check(
                matches(withEffectiveVisibility(Visibility.GONE))
            )
            onView(withId(R.id.errorTextView)).check(
                matches(withEffectiveVisibility(Visibility.GONE))
            )
        }
    }

    @Test
    fun withBackendMaintenanceErrorConfirmViews() {
        runTest {
            coEvery {
                longConsentRepository.getLongConsentResultForLanguage(LANGUAGE)
            } returns flowOf(
                LongConsentFetchResult.InProgress("en"),
                LongConsentFetchResult.FailedBecauseBackendMaintenance("en", Throwable())
            )

            createAndStartActivity<PrivacyNoticeActivity>()

            onView(withId(R.id.errorCard)).check(
                matches(withEffectiveVisibility(Visibility.VISIBLE))
            )
            onView(withId(R.id.errorTextView)).check(
                matches(
                    withText(SYNC_CARD_FAILED_BACKEND_MAINTENANCE_STATE_MESSAGE)
                )
            )
        }
    }

    @Test
    fun withTimedBackendMaintenanceErrorConfirmViews() {
        runTest {
            coEvery {
                longConsentRepository.getLongConsentResultForLanguage(LANGUAGE)
            } returns flowOf(
                LongConsentFetchResult.InProgress("en"),
                LongConsentFetchResult.FailedBecauseBackendMaintenance("en", Throwable(), 600L)
            )
            createAndStartActivity<PrivacyNoticeActivity>()

            onView(withId(R.id.errorCard)).check(
                matches(withEffectiveVisibility(Visibility.VISIBLE))
            )
            onView(withId(R.id.errorTextView)).check(
                matches(
                    withText(SYNC_CARD_FAILED_BACKEND_MAINTENANCE_STATE_TIMED_MESSAGE)
                )
            )
        }
    }

    companion object {
        private const val SYNC_CARD_FAILED_BACKEND_MAINTENANCE_STATE_TIMED_MESSAGE =
            "The system is currently offline for maintenance. Please try again after 10 minutes, 00 seconds"
        private const val SYNC_CARD_FAILED_BACKEND_MAINTENANCE_STATE_MESSAGE =
            "The system is currently offline for maintenance. Please try again later."
        private const val LANGUAGE = "en"
    }
}
