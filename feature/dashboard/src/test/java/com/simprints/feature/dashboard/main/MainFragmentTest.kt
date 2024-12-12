package com.simprints.feature.dashboard.main

import androidx.lifecycle.Observer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openContextualActionModeOverflowMenu
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.feature.dashboard.R
import com.simprints.feature.dashboard.main.dailyactivity.DailyActivityViewModel
import com.simprints.feature.dashboard.main.projectdetails.ProjectDetailsViewModel
import com.simprints.feature.dashboard.main.sync.SyncViewModel
import com.simprints.testtools.hilt.launchFragmentInHiltContainer
import com.simprints.testtools.hilt.testNavController
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import io.mockk.every
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import com.simprints.feature.consent.R as PrivacyR
import com.simprints.infra.resources.R as IDR

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
@Config(application = HiltTestApplication::class)
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

    @BindValue
    @JvmField
    internal val syncViewModel = mockk<SyncViewModel>(relaxed = true)

    private val navController = testNavController(R.navigation.graph_dashboard, R.id.mainFragment)

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

    private fun mockConsentRequired(required: Boolean) {
        every { mainViewModel.consentRequired } returns mockk {
            every { observe(any(), any()) } answers {
                secondArg<Observer<Boolean>>().onChanged(required)
            }
        }
    }
}
