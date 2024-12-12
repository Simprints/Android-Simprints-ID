package com.simprints.feature.dashboard.logout.syncdecline

import androidx.lifecycle.Observer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.feature.dashboard.R
import com.simprints.feature.dashboard.logout.LogoutSyncViewModel
import com.simprints.infra.config.store.models.SettingsPasswordConfig
import com.simprints.testtools.hilt.launchFragmentInHiltContainer
import com.simprints.testtools.hilt.testNavController
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
@Config(application = HiltTestApplication::class)
internal class LogoutSyncDeclineFragmentTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @BindValue
    @JvmField
    internal val viewModel = mockk<LogoutSyncViewModel>(relaxed = true)

    @Test
    fun `should call viewModel_logout when logout without sync confirmation button is pressed and no logout password is set`() {
        mockSettingsPassword(SettingsPasswordConfig.NotSet)
        val navController =
            testNavController(R.navigation.graph_dashboard, R.id.logOutSyncDeclineFragment)
        launchFragmentInHiltContainer<LogoutSyncDeclineFragment>(navController = navController)

        onView(withId(R.id.logoutWithoutSyncConfirmButton)).perform(click())
        onView(withId(android.R.id.button1))
            .inRoot(RootMatchers.isDialog())
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            .perform(click())
        verify(exactly = 1) { viewModel.logout() }
    }

    @Test
    fun `should close the logout dialog when logout without sync confirmation button is pressed and cancel is pressed`() {
        mockSettingsPassword(SettingsPasswordConfig.NotSet)
        val navController =
            testNavController(R.navigation.graph_dashboard, R.id.logOutSyncDeclineFragment)
        launchFragmentInHiltContainer<LogoutSyncDeclineFragment>(navController = navController)

        onView(withId(R.id.logoutWithoutSyncConfirmButton)).perform(click())
        onView(withId(android.R.id.button2))
            .inRoot(RootMatchers.isDialog())
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            .perform(click())
        verify(exactly = 0) { viewModel.logout() }
    }

    @Test
    fun `should call viewModel_logout when logout without sync confirmation button is pressed and correct password is entered`() {
        val password = "password123"
        mockSettingsPassword(SettingsPasswordConfig.Locked(password = password))
        val navController =
            testNavController(R.navigation.graph_dashboard, R.id.logOutSyncDeclineFragment)
        launchFragmentInHiltContainer<LogoutSyncDeclineFragment>(navController = navController)

        onView(withId(R.id.logoutWithoutSyncConfirmButton)).perform(click())
        onView(withId(R.id.password_input_field))
            .inRoot(RootMatchers.isDialog())
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            .perform(ViewActions.replaceText(password))
        verify(exactly = 1) { viewModel.logout() }
    }

    @Test
    fun `should close the logout dialog when logout without sync confirmation is password-protected and cancel is pressed`() {
        val password = "password123"
        mockSettingsPassword(SettingsPasswordConfig.Locked(password = password))
        val navController =
            testNavController(R.navigation.graph_dashboard, R.id.logOutSyncDeclineFragment)
        launchFragmentInHiltContainer<LogoutSyncDeclineFragment>(navController = navController)

        onView(withId(R.id.logoutWithoutSyncConfirmButton)).perform(click())
        onView(withId(android.R.id.button2))
            .inRoot(RootMatchers.isDialog())
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            .perform(click())
        verify(exactly = 0) { viewModel.logout() }
    }

    @Test
    fun `should navigate to requestLoginFragment when logout confirmation button is pressed`() {
        mockSettingsPassword(SettingsPasswordConfig.NotSet)
        val navController =
            testNavController(R.navigation.graph_dashboard, R.id.logOutSyncDeclineFragment)
        launchFragmentInHiltContainer<LogoutSyncDeclineFragment>(navController = navController)

        onView(withId(R.id.logoutWithoutSyncConfirmButton)).perform(click())
        onView(withId(android.R.id.button1))
            .inRoot(RootMatchers.isDialog())
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            .perform(click())
        assertThat(navController.currentDestination?.id)
            .isEqualTo(R.id.requestLoginFragment)
    }

    @Test
    fun `should navigate to requestLoginFragment when correct logout password is entered`() {
        val password = "password123"
        mockSettingsPassword(SettingsPasswordConfig.Locked(password = password))
        val navController =
            testNavController(R.navigation.graph_dashboard, R.id.logOutSyncDeclineFragment)
        launchFragmentInHiltContainer<LogoutSyncDeclineFragment>(navController = navController)

        onView(withId(R.id.logoutWithoutSyncConfirmButton)).perform(click())
        onView(withId(R.id.password_input_field))
            .inRoot(RootMatchers.isDialog())
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            .perform(ViewActions.replaceText(password))
        assertThat(navController.currentDestination?.id)
            .isEqualTo(R.id.requestLoginFragment)
    }

    private fun mockSettingsPassword(lock: SettingsPasswordConfig) {
        every { viewModel.settingsLocked } returns mockk {
            every { observe(any(), any()) } answers {
                secondArg<Observer<LiveDataEventWithContent<SettingsPasswordConfig>?>>()
                    .onChanged(LiveDataEventWithContent(lock))
            }
        }
    }
}
