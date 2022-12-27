package com.simprints.id.activities.login

import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.id.R
import com.simprints.id.activities.login.request.LoginActivityRequest
import com.simprints.id.secure.AuthenticationHelper
import com.simprints.id.secure.models.AuthenticateDataResult
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.coroutines.TestDispatcherProvider
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import com.simprints.testtools.unit.robolectric.createAndStartActivity
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(shadows = [ShadowAndroidXMultiDex::class])
class LoginActivityTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private val authenticationHelper: AuthenticationHelper = mockk(relaxed = true)
    private val dispatcherProvider = TestDispatcherProvider(testCoroutineRule)

    @Test
    fun withSuccessConfirmViews() {
        runBlocking {
            coEvery {
                authenticationHelper.authenticateSafely(any(), "project_id", any(), any())
            } returns AuthenticateDataResult.Authenticated

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
            } returns AuthenticateDataResult.Offline

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
            } returns AuthenticateDataResult.BadCredentials

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
            } returns AuthenticateDataResult.TechnicalFailure

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
            } returns AuthenticateDataResult.Unknown

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
    fun withIntegrityInvalidErrorConfirmViews() {
        runBlocking {
            coEvery {
                authenticationHelper.authenticateSafely(any(), "project_id", any(), any())
            } returns AuthenticateDataResult.PlayIntegrityInvalidClaim

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
    fun withIntegrityUnavailableErrorConfirmViews() {
        runBlocking {
            coEvery {
                authenticationHelper.authenticateSafely(any(), "project_id", any(), any())
            } returns AuthenticateDataResult.PlayIntegrityUnavailable

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
            } returns AuthenticateDataResult.BackendMaintenanceError()

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
            } returns AuthenticateDataResult.BackendMaintenanceError(600L)

            createAndStartActivity<LoginActivity>(loginBundle)

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
