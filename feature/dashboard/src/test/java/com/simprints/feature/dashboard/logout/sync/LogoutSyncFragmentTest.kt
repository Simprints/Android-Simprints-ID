package com.simprints.feature.dashboard.logout.sync

import android.content.Context
import android.widget.ImageButton
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.Visibility
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Configuration
import androidx.work.testing.WorkManagerTestInitHelper
import com.google.common.truth.Truth.assertThat
import com.simprints.feature.dashboard.R
import com.simprints.feature.dashboard.tools.MviStubs
import com.simprints.feature.dashboard.tools.stubMvi
import com.simprints.testtools.hilt.launchFragmentInHiltContainer
import com.simprints.testtools.hilt.testNavController
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.mockk
import org.hamcrest.CoreMatchers.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
internal class LogoutSyncFragmentTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @BindValue
    @JvmField
    internal val logoutSyncViewModel = mockk<LogoutSyncViewModel>(relaxed = true)

    private lateinit var mvi: MviStubs<LogoutSyncUiState, LogoutSyncUiEffect>

    private val navController = testNavController(R.navigation.graph_dashboard)

    @Before
    fun setUp() {
        hiltRule.inject()
        val ctx: Context = getApplicationContext()
        WorkManagerTestInitHelper.initializeTestWorkManager(ctx, Configuration.Builder().build())
        mvi = logoutSyncViewModel.stubMvi(LogoutSyncUiState())
    }

    @Test
    fun `instant logout button and instructions are visible when ready to be seen`() {
        mvi.state.value = LogoutSyncUiState(isLogoutWithoutSyncVisible = true)
        launchFragmentInHiltContainer<LogoutSyncFragment>(navController = navController)

        onView(withId(R.id.logout_sync_info)).check(matches(isDisplayed()))
        onView(withId(R.id.logoutWithoutSyncButton)).check(matches(isDisplayed()))
    }

    @Test
    fun `instant logout button and instructions are not visible when not ready to be seen`() {
        mvi.state.value = LogoutSyncUiState(isLogoutWithoutSyncVisible = false)
        launchFragmentInHiltContainer<LogoutSyncFragment>(navController = navController)

        // INVISIBLE, not GONE: the layout keeps its space
        assertLogoutWithoutSyncVisibility(Visibility.INVISIBLE)
    }

    @Test
    fun `logout without sync button opens the decline screen`() {
        mvi.state.value = LogoutSyncUiState(isLogoutWithoutSyncVisible = true)
        val navController = navControllerOnLogoutSyncFromAbout()
        launchFragmentInHiltContainer<LogoutSyncFragment>(navController = navController)

        onView(withId(R.id.logoutWithoutSyncButton)).check(matches(isDisplayed())).perform(click())

        assertThat(navController.currentDestination?.id).isEqualTo(R.id.logOutSyncDeclineFragment)
    }

    @Test
    fun `visibility is re-rendered on every value including visible to hidden and back`() {
        mvi.state.value = LogoutSyncUiState(isLogoutWithoutSyncVisible = true)
        launchFragmentInHiltContainer<LogoutSyncFragment>(navController = navController)
        assertLogoutWithoutSyncVisibility(Visibility.VISIBLE)

        mvi.state.value = LogoutSyncUiState(isLogoutWithoutSyncVisible = false)
        assertLogoutWithoutSyncVisibility(Visibility.INVISIBLE)

        mvi.state.value = LogoutSyncUiState(isLogoutWithoutSyncVisible = true)
        assertLogoutWithoutSyncVisibility(Visibility.VISIBLE)
    }

    @Test
    fun `before the first value the button keeps its visible and the info text its invisible xml default`() {
        launchFragmentInHiltContainer<LogoutSyncFragment>(navController = navController)

        onView(withId(R.id.logoutWithoutSyncButton)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withId(R.id.logout_sync_info)).check(matches(withEffectiveVisibility(Visibility.INVISIBLE)))
    }

    @Test
    fun `state is rendered once the view is started without waiting for resume`() {
        mvi.state.value = LogoutSyncUiState(isLogoutWithoutSyncVisible = true)
        launchFragmentInHiltContainer<LogoutSyncFragment>(navController = navController, initialState = Lifecycle.State.STARTED)

        assertLogoutWithoutSyncVisibility(Visibility.VISIBLE)
    }

    @Test
    fun `toolbar up pops one level`() {
        val navController = navControllerOnLogoutSyncFromAbout()
        launchFragmentInHiltContainer<LogoutSyncFragment>(navController = navController)

        onView(
            allOf(
                isAssignableFrom(ImageButton::class.java),
                withParent(withId(R.id.logoutSyncToolbar)),
            ),
        ).perform(click())

        assertThat(navController.currentDestination?.id).isEqualTo(R.id.aboutFragment)
    }

    private fun assertLogoutWithoutSyncVisibility(visibility: Visibility) {
        onView(withId(R.id.logout_sync_info)).check(matches(withEffectiveVisibility(visibility)))
        onView(withId(R.id.logoutWithoutSyncButton)).check(matches(withEffectiveVisibility(visibility)))
    }

    private fun navControllerOnLogoutSyncFromAbout() = testNavController(R.navigation.graph_dashboard, R.id.aboutFragment).also {
        it.navigate(R.id.action_aboutFragment_to_logout_navigation)
        assertThat(it.currentDestination?.id).isEqualTo(R.id.logOutSyncFragment)
    }
}
