package com.simprints.id.activities.login

import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.eventsystem.event.domain.models.AuthenticationEvent
import com.simprints.id.R
import com.simprints.id.activities.login.request.LoginActivityRequest
import com.simprints.id.activities.login.viewmodel.LoginViewModelFactory
import com.simprints.id.data.prefs.IdPreferencesManager
import com.simprints.id.secure.AuthenticationHelper
import com.simprints.id.secure.AuthenticationHelperImpl
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
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class LoginActivityTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private val authenticationHelper: AuthenticationHelper = mockk(relaxed = true)
    private val dispatcherProvider = TestDispatcherProvider(testCoroutineRule)
    private val preferencesManager: IdPreferencesManager = mockk(relaxed = true)

    private val viewModelModule by lazy {
        TestViewModelModule(
            loginViewModelFactoryRule = DependencyRule.ReplaceRule {
                LoginViewModelFactory(authenticationHelper, dispatcherProvider)
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
                authenticationHelper.authenticateSafely(any(), "project_id", any(), any())
            } returns AuthenticationEvent.AuthenticationPayload.Result.AUTHENTICATED

            createAndStartActivity<LoginActivity>(loginBundle)

            onView(withId(R.id.loginEditTextProjectSecret)).perform(typeText("loginEditTextProjectSecret"))
            onView(withId(R.id.loginEditTextProjectId)).perform(typeText("project_id"))
            onView(withId(R.id.loginButtonSignIn)).perform(click())
            onView(withId(R.id.errorCard)).check(
                matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE))
            )
            onView(withId(R.id.errorTextView)).check(
                matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE))
            )
        }
    }

    @Test
    fun withOfflineConfirmViews() {
        runBlocking {
            coEvery {
                authenticationHelper.authenticateSafely(any(), "project_id", any(), any())
            } returns AuthenticationEvent.AuthenticationPayload.Result.OFFLINE

            createAndStartActivity<LoginActivity>(loginBundle)

            onView(withId(R.id.loginEditTextProjectSecret)).perform(typeText("loginEditTextProjectSecret"))
            onView(withId(R.id.loginEditTextProjectId)).perform(typeText("project_id"))
            onView(withId(R.id.loginButtonSignIn)).perform(click())
            onView(withId(R.id.errorCard)).check(
                matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE))
            )
            onView(withId(R.id.errorTextView)).check(
                matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE))
            )
        }
    }

    @Test
    fun withBadCredentialsConfirmViews() {
        runBlocking {
            coEvery {
                authenticationHelper.authenticateSafely(any(), "project_id", any(), any())
            } returns AuthenticationEvent.AuthenticationPayload.Result.BAD_CREDENTIALS

            createAndStartActivity<LoginActivity>(loginBundle)

            onView(withId(R.id.loginEditTextProjectSecret)).perform(typeText("loginEditTextProjectSecret"))
            onView(withId(R.id.loginEditTextProjectId)).perform(typeText("project_id"))
            onView(withId(R.id.loginButtonSignIn)).perform(click())
            onView(withId(R.id.errorCard)).check(
                matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE))
            )
            onView(withId(R.id.errorTextView)).check(
                matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE))
            )
        }
    }

    @Test
    fun withTechnicalErrorConfirmViews() {
        runBlocking {
            coEvery {
                authenticationHelper.authenticateSafely(any(), "project_id", any(), any())
            } returns AuthenticationEvent.AuthenticationPayload.Result.TECHNICAL_FAILURE

            createAndStartActivity<LoginActivity>(loginBundle)

            onView(withId(R.id.loginEditTextProjectSecret)).perform(typeText("loginEditTextProjectSecret"))
            onView(withId(R.id.loginEditTextProjectId)).perform(typeText("project_id"))
            onView(withId(R.id.loginButtonSignIn)).perform(click())
            onView(withId(R.id.errorCard)).check(
                matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE))
            )
            onView(withId(R.id.errorTextView)).check(
                matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE))
            )
        }
    }

    @Test
    fun withUnknownErrorConfirmViews() {
        runBlocking {
            coEvery {
                authenticationHelper.authenticateSafely(any(), "project_id", any(), any())
            } returns AuthenticationEvent.AuthenticationPayload.Result.UNKNOWN

            createAndStartActivity<LoginActivity>(loginBundle)

            onView(withId(R.id.loginEditTextProjectSecret)).perform(typeText("loginEditTextProjectSecret"))
            onView(withId(R.id.loginEditTextProjectId)).perform(typeText("project_id"))
            onView(withId(R.id.loginButtonSignIn)).perform(click())
            onView(withId(R.id.errorCard)).check(
                matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE))
            )
            onView(withId(R.id.errorTextView)).check(
                matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE))
            )
        }
    }

    @Test
    fun withSafetyNetInvalidErrorConfirmViews() {
        runBlocking {
            coEvery {
                authenticationHelper.authenticateSafely(any(), "project_id", any(), any())
            } returns AuthenticationEvent.AuthenticationPayload.Result.SAFETYNET_INVALID_CLAIM

            createAndStartActivity<LoginActivity>(loginBundle)

            onView(withId(R.id.loginEditTextProjectSecret)).perform(typeText("loginEditTextProjectSecret"))
            onView(withId(R.id.loginEditTextProjectId)).perform(typeText("project_id"))
            onView(withId(R.id.loginButtonSignIn)).perform(click())
            onView(withId(R.id.errorCard)).check(
                matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE))
            )
            onView(withId(R.id.errorTextView)).check(
                matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE))
            )
        }
    }

    @Test
    fun withSafetyNetUnavailableErrorConfirmViews() {
        runBlocking {
            coEvery {
                authenticationHelper.authenticateSafely(any(), "project_id", any(), any())
            } returns AuthenticationEvent.AuthenticationPayload.Result.SAFETYNET_UNAVAILABLE

            createAndStartActivity<LoginActivity>(loginBundle)

            onView(withId(R.id.loginEditTextProjectSecret)).perform(typeText("loginEditTextProjectSecret"))
            onView(withId(R.id.loginEditTextProjectId)).perform(typeText("project_id"))
            onView(withId(R.id.loginButtonSignIn)).perform(click())
            onView(withId(R.id.errorCard)).check(
                matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE))
            )
            onView(withId(R.id.errorTextView)).check(
                matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE))
            )
        }
    }

    @Test
    fun withBackendMaintenanceErrorConfirmViews() {
        runBlocking {
            coEvery {
                authenticationHelper.authenticateSafely(any(), "project_id", any(), any())
            } returns AuthenticationEvent.AuthenticationPayload.Result.BACKEND_MAINTENANCE_ERROR

            createAndStartActivity<LoginActivity>(loginBundle)

            onView(withId(R.id.loginEditTextProjectSecret)).perform(typeText("loginEditTextProjectSecret"))
            onView(withId(R.id.loginEditTextProjectId)).perform(typeText("project_id"))
            onView(withId(R.id.loginButtonSignIn)).perform(click())
            onView(withId(R.id.errorCard)).check(
                matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))
            )
            onView(withId(R.id.errorTextView)).check(
                matches(
                    ViewMatchers.withText(SYNC_CARD_FAILED_BACKEND_MAINTENANCE_STATE_MESSAGE)
                )
            )
        }
    }

    @Test
    fun withTimedBackendMaintenanceErrorConfirmViews() {
        runBlocking {
            coEvery {
                authenticationHelper.authenticateSafely(any(), "project_id", any(), any())
            } returns AuthenticationEvent.AuthenticationPayload.Result.BACKEND_MAINTENANCE_ERROR

            every { preferencesManager.getSharedPreference(AuthenticationHelperImpl.PREFS_ESTIMATED_OUTAGE,0L) } returns 600L

            createAndStartActivity<LoginActivity>(loginBundle).apply {
                idPreferencesManager = preferencesManager
            }

            onView(withId(R.id.loginEditTextProjectSecret)).perform(typeText("loginEditTextProjectSecret"))
            onView(withId(R.id.loginEditTextProjectId)).perform(typeText("project_id"))
            onView(withId(R.id.loginButtonSignIn)).perform(click())
            onView(withId(R.id.errorCard)).check(
                matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))
            )
            onView(withId(R.id.errorTextView)).check(
                matches(
                    ViewMatchers.withText(SYNC_CARD_FAILED_BACKEND_MAINTENANCE_STATE_TIMED_MESSAGE)
                )
            )
        }
    }

    private val loginBundle = Bundle().apply {
        putParcelable(
            LoginActivityRequest.BUNDLE_KEY,
            LoginActivityRequest("project_id", "user_id")
        )
    }

    companion object {
        private const val SYNC_CARD_FAILED_BACKEND_MAINTENANCE_STATE_TIMED_MESSAGE =
            "The system is currently offline for maintenance. Please try again after 10 minutes, 00 seconds"
        private const val SYNC_CARD_FAILED_BACKEND_MAINTENANCE_STATE_MESSAGE =
            "The system is currently offline for maintenance. Please try again later."
    }
}
