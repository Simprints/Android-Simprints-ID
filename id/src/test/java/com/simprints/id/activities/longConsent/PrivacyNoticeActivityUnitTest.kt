package com.simprints.id.activities.longConsent

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.id.R
import com.simprints.id.data.consent.longconsent.LongConsentFetchResult
import com.simprints.id.data.consent.longconsent.LongConsentRepository
import com.simprints.id.data.prefs.IdPreferencesManager
import com.simprints.id.testtools.TestApplication
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.id.testtools.di.TestAppModule
import com.simprints.id.testtools.di.TestPreferencesModule
import com.simprints.id.testtools.di.TestViewModelModule
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.coroutines.TestDispatcherProvider
import com.simprints.testtools.common.di.DependencyRule
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import com.simprints.testtools.unit.robolectric.createAndStartActivity
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class PrivacyNoticeActivityUnitTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private val longConsentRepository: LongConsentRepository = mockk(relaxed = true)
    private val preferencesManager: IdPreferencesManager = mockk(relaxed = true)
    private val dispatcherProvider = TestDispatcherProvider(testCoroutineRule)

    private val viewModelModule by lazy {
        TestViewModelModule(
            privacyViewModelFactoryRule = DependencyRule.ReplaceRule {
                PrivacyNoticeViewModelFactory(longConsentRepository, preferencesManager, dispatcherProvider)
            }
        )
    }

    private val app = ApplicationProvider.getApplicationContext() as TestApplication

    private val preferencesModule by lazy {
        TestPreferencesModule(
            settingsPreferencesManagerRule = DependencyRule.SpykRule
        )
    }

    private val module by lazy {
        TestAppModule(
            app,
            dbManagerRule = DependencyRule.MockkRule,
            sessionEventsLocalDbManagerRule = DependencyRule.MockkRule
        )
    }

    @Before
    fun setUp() {
        UnitTestConfig(
            module,
            preferencesModule,
            viewModelModule = viewModelModule
        ).fullSetup().inject(this)
    }

    @Test
    fun withSuccessConfirmViews() {
        runBlocking {
            coEvery {
                longConsentRepository.getLongConsentResultForLanguage(any())
            } returns flowOf(
                LongConsentFetchResult.InProgress("en"),
                LongConsentFetchResult.Succeed("en", "some consent")
            )

            createAndStartActivity<PrivacyNoticeActivity>()

            onView(withId(R.id.errorCard)).check(
                matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE))
            )
            onView(withId(R.id.errorTextView)).check(
                matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE))
            )
        }
    }

    @Test
    fun withBackendMaintenanceErrorConfirmViews() {
        runBlocking {
            coEvery {
                longConsentRepository.getLongConsentResultForLanguage(any())
            } returns flowOf(
                LongConsentFetchResult.InProgress("en"),
                LongConsentFetchResult.FailedBecauseBackendMaintenance("en", Throwable())
            )

            createAndStartActivity<PrivacyNoticeActivity>()

            onView(withId(R.id.errorCard)).check(
                matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))
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
        runBlocking {
            coEvery {
                longConsentRepository.getLongConsentResultForLanguage(any())
            } returns flowOf(
                LongConsentFetchResult.InProgress("en"),
                LongConsentFetchResult.FailedBecauseBackendMaintenance("en", Throwable(), 600L)
            )
            createAndStartActivity<PrivacyNoticeActivity>()

            onView(withId(R.id.errorCard)).check(
                matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))
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
    }
}
