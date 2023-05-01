package com.simprints.feature.dashboard.logout.syncdecline

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.feature.dashboard.R
import com.simprints.feature.dashboard.logout.LogoutSyncViewModel
import com.simprints.testtools.hilt.launchFragmentInHiltContainer
import com.simprints.testtools.hilt.testNavController
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
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
    fun `should call viewModel_logout when logout without sync confirmation button is pressed`() {
        val navController =
            testNavController(R.navigation.graph_dashboard, R.id.logOutSyncDeclineFragment)
        launchFragmentInHiltContainer<LogoutSyncDeclineFragment>(navController = navController)

        onView(withId(R.id.logoutWithoutSyncConfirmButton)).perform(click())
        verify(exactly = 1) { viewModel.logout() }
    }

    @Test
    fun `should navigate to requestLoginFragment when logout without sync confirmation button is pressed`() {
        val navController =
            testNavController(R.navigation.graph_dashboard, R.id.logOutSyncDeclineFragment)
        launchFragmentInHiltContainer<LogoutSyncDeclineFragment>(navController = navController)

        onView(withId(R.id.logoutWithoutSyncConfirmButton)).perform(click())
        assertThat(navController.currentDestination?.id)
            .isEqualTo(R.id.requestLoginFragment)
    }
}
