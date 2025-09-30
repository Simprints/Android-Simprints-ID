package com.simprints.feature.dashboard.logout.sync

import androidx.lifecycle.Observer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
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
import io.mockk.every
import io.mockk.mockk
import org.hamcrest.core.IsNot.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
@Config(application = HiltTestApplication::class)
internal class LogoutSyncFragmentTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @BindValue
    @JvmField
    internal val logoutSyncViewModel = mockk<LogoutSyncViewModel>(relaxed = true)

    private val navController = testNavController(R.navigation.graph_dashboard)

    @Test
    fun `instant logout button and instructions are visible when ready to be seen`() {
        every { logoutSyncViewModel.isLogoutWithoutSyncVisibleLiveData } returns mockk {
            every { observe(any(), any()) } answers {
                secondArg<Observer<Boolean>>().onChanged(true)
            }
        }
        launchFragmentInHiltContainer<LogoutSyncFragment>(navController = navController)

        onView(withId(R.id.logout_sync_info)).check(matches(isDisplayed()))
        onView(withId(R.id.logoutWithoutSyncButton)).check(matches(isDisplayed()))
    }

    @Test
    fun `instant logout button and instructions are not visible when not ready to be seen`() {
        every { logoutSyncViewModel.isLogoutWithoutSyncVisibleLiveData } returns mockk {
            every { observe(any(), any()) } answers {
                secondArg<Observer<Boolean>>().onChanged(false)
            }
        }
        launchFragmentInHiltContainer<LogoutSyncFragment>(navController = navController)

        onView(withId(R.id.logout_sync_info)).check(matches(not(isDisplayed())))
        onView(withId(R.id.logoutWithoutSyncButton)).check(matches(not(isDisplayed())))
    }
}
