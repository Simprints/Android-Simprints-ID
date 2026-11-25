package com.simprints.feature.dashboard.main

import android.content.Context
import android.view.View
import androidx.fragment.app.commitNow
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openContextualActionModeOverflowMenu
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.Visibility
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Configuration
import androidx.work.testing.WorkManagerTestInitHelper
import com.google.common.truth.Truth.assertThat
import com.simprints.feature.dashboard.R
import com.simprints.feature.dashboard.main.dailyactivity.DailyActivityUiState
import com.simprints.feature.dashboard.main.dailyactivity.DailyActivityViewModel
import com.simprints.feature.dashboard.main.projectdetails.ProjectDetailsUiState
import com.simprints.feature.dashboard.main.projectdetails.ProjectDetailsViewModel
import com.simprints.feature.dashboard.tools.MviStubs
import com.simprints.feature.dashboard.tools.stubMvi
import com.simprints.testtools.hilt.launchFragmentInHiltContainer
import com.simprints.testtools.hilt.testNavController
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.simprints.feature.consent.R as PrivacyR
import com.simprints.infra.resources.R as IDR

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class MainFragmentTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @BindValue
    @JvmField
    internal val mainViewModel = mockk<MainViewModel>(relaxed = true)

    @BindValue
    @JvmField
    internal val projectDetailsViewModel = mockk<ProjectDetailsViewModel>(relaxed = true)

    @BindValue
    @JvmField
    internal val dailyActivityViewModel = mockk<DailyActivityViewModel>(relaxed = true)

    private lateinit var mvi: MviStubs<MainUiState, MainUiEffect>

    private val navController = testNavController(R.navigation.graph_dashboard, R.id.mainFragment)

    @Before
    fun setUp() {
        hiltRule.inject()
        mvi = mainViewModel.stubMvi(MainUiState())
        projectDetailsViewModel.stubMvi(ProjectDetailsUiState())
        dailyActivityViewModel.stubMvi(DailyActivityUiState())
        val ctx: Context = getApplicationContext()
        WorkManagerTestInitHelper.initializeTestWorkManager(ctx, Configuration.Builder().build())
    }

    @Test
    fun `should hide the privacy notice menu if the consent is not required`() {
        mockConsentRequired(false)

        launchFragmentInHiltContainer<MainFragment>(navController = navController)

        openContextualActionModeOverflowMenu()
        onView(withText("Privacy Notice")).check(doesNotExist())
    }

    @Test
    fun `should display the privacy notice menu if the consent is required`() {
        mockConsentRequired(true)

        launchFragmentInHiltContainer<MainFragment>(navController = navController)

        openContextualActionModeOverflowMenu()
        onView(withText("Privacy Notice")).check(matches(isDisplayed()))
    }

    @Test
    fun `should redirect to the settings fragment when clicking on settings`() {
        mockConsentRequired(true)

        launchFragmentInHiltContainer<MainFragment>(navController = navController)

        openContextualActionModeOverflowMenu()
        onView(withText(IDR.string.dashboard_menu_settings)).perform(click())
        assertThat(navController.currentDestination?.id).isEqualTo(R.id.settingsFragment)
    }

    @Test
    fun `should redirect to the privacy notices fragment when clicking on privacy notices`() {
        mockConsentRequired(true)

        launchFragmentInHiltContainer<MainFragment>(navController = navController)

        openContextualActionModeOverflowMenu()
        onView(withText(IDR.string.dashboard_menu_privacy_notice)).perform(click())
        assertThat(navController.currentDestination?.id).isEqualTo(PrivacyR.id.privacyNoticeFragment)
    }

    @Test
    fun `should redirect to the debug fragment when clicking on debug`() {
        mockConsentRequired(true)

        launchFragmentInHiltContainer<MainFragment>(navController = navController)

        openContextualActionModeOverflowMenu()
        onView(withText("Debug")).perform(click())
        assertThat(navController.currentDestination?.id).isEqualTo(R.id.debugFragment)
    }

    @Test
    fun `no consent state yet keeps the privacy notice menu xml default`() {
        launchFragmentInHiltContainer<MainFragment>(navController = navController)

        openContextualActionModeOverflowMenu()
        onView(withText("Privacy Notice")).check(matches(isDisplayed()))
    }

    @Test
    fun `rooted device shows the rooted device overlay`() {
        mvi.state.value = MainUiState(isConsentRequired = true, isRootedDeviceDetected = true)

        launchFragmentInHiltContainer<MainFragment>(navController = navController)

        onView(withId(R.id.rootedDeviceError)).check(matches(isDisplayed()))
    }

    @Test
    fun `rooted device state arriving after the screen is shown displays the overlay`() {
        mvi.state.value = MainUiState(isConsentRequired = true)
        launchFragmentInHiltContainer<MainFragment>(navController = navController)
        onView(withId(R.id.rootedDeviceError)).check(matches(withEffectiveVisibility(Visibility.GONE)))

        mvi.state.value = MainUiState(isConsentRequired = true, isRootedDeviceDetected = true)

        onView(withId(R.id.rootedDeviceError)).check(matches(isDisplayed()))
    }

    @Test
    fun `rooted device overlay is still shown after the view is re-created`() {
        mvi.state.value = MainUiState(isConsentRequired = true, isRootedDeviceDetected = true)
        lateinit var firstView: View
        lateinit var recreatedView: View

        launchFragmentInHiltContainer<MainFragment>(navController = navController) {
            firstView = requireView()
            // Destroys and re-creates the view while the Fragment (and its ViewModel) stays, as when returning from the back stack.
            parentFragmentManager.commitNow { detach(this@launchFragmentInHiltContainer) }
            parentFragmentManager.commitNow { attach(this@launchFragmentInHiltContainer) }
            recreatedView = requireView()
        }

        assertThat(recreatedView).isNotSameInstanceAs(firstView)
        onView(withId(R.id.rootedDeviceError)).check(matches(isDisplayed()))
        // The only load is the ViewModel's init one: the Fragment never reloads, also not for a new view.
        verify(exactly = 0) { mainViewModel.onAction(any()) }
    }

    @Test
    fun `safe device keeps the rooted device overlay hidden`() {
        mvi.state.value = MainUiState(isConsentRequired = true, isRootedDeviceDetected = false)

        launchFragmentInHiltContainer<MainFragment>(navController = navController)

        onView(withId(R.id.dashboardToolbar)).check(matches(isDisplayed()))
        onView(withId(R.id.rootedDeviceError)).check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    private fun mockConsentRequired(required: Boolean) {
        mvi.state.value = MainUiState(isConsentRequired = required)
    }
}
