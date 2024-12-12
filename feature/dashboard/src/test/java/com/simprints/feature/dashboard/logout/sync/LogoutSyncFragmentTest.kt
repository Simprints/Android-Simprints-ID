package com.simprints.feature.dashboard.logout.sync

import androidx.lifecycle.Observer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.simprints.feature.dashboard.R
import com.simprints.feature.dashboard.logout.LogoutSyncViewModel
import com.simprints.feature.dashboard.main.sync.SyncViewModel
import com.simprints.feature.dashboard.views.SyncCardState
import com.simprints.testtools.hilt.launchFragmentInHiltContainer
import com.simprints.testtools.hilt.testNavController
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.hamcrest.core.IsNot.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import com.simprints.infra.resources.R as IDR

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
@Config(application = HiltTestApplication::class)
internal class LogoutSyncFragmentTest {
    companion object {
        private const val LAST_SYNC_TIME = "2022-10-10"
    }

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @BindValue
    @JvmField
    internal val syncViewModel = mockk<SyncViewModel>(relaxed = true)

    @BindValue
    @JvmField
    internal val logoutSyncViewModel = mockk<LogoutSyncViewModel>(relaxed = true)

    private val context = InstrumentationRegistry.getInstrumentation().context
    private val navController = testNavController(R.navigation.graph_dashboard)

    @Test
    fun `should not hide the sync card view if it can't sync to BFSID`() {
        mockSyncToBFSIDAllowed(false)
        launchFragmentInHiltContainer<LogoutSyncFragment>(navController = navController)

        onView(withId(R.id.logoutSyncCard)).check(matches(isDisplayed()))
    }

    @Test
    fun `should display the correct sync card view for the SyncDefault state`() {
        mockSyncToBFSIDAllowed(true)
        mockSyncCardLiveData(SyncCardState.SyncDefault(LAST_SYNC_TIME))

        launchFragmentInHiltContainer<LogoutSyncFragment>(navController = navController)

        checkHiddenViews(
            listOf(
                R.id.sync_card_failed_message,
                R.id.sync_card_select_no_modules,
                R.id.sync_card_offline,
                R.id.sync_card_progress,
                R.id.sync_card_try_again,
                R.id.logoutButton,
            ),
        )
        val lastSyncText = context.getString(
            IDR.string.dashboard_sync_card_last_sync,
            LAST_SYNC_TIME,
        )
        onView(withId(R.id.sync_card_last_sync))
            .check(matches(withText(lastSyncText)))
        onView(withId(R.id.sync_card_default_items_to_upload)).check(
            matches(withText(context.getString(IDR.string.dashboard_sync_card_records_uploaded))),
        )
        onView(withId(R.id.sync_card_default_state_sync_button))
            .check(matches(isDisplayed()))
            .perform(scrollTo(), click())
        verify(exactly = 1) { syncViewModel.sync() }
    }

    @Test
    fun `should display the correct sync card view for the SyncPendingUpload state`() {
        mockSyncToBFSIDAllowed(true)
        mockSyncCardLiveData(SyncCardState.SyncPendingUpload(LAST_SYNC_TIME, 2))

        launchFragmentInHiltContainer<LogoutSyncFragment>(navController = navController)

        checkHiddenViews(
            listOf(
                R.id.sync_card_failed_message,
                R.id.sync_card_select_no_modules,
                R.id.sync_card_offline,
                R.id.sync_card_progress,
                R.id.sync_card_try_again,
                R.id.logoutButton,
            ),
        )
        val lastSyncText = context.getString(
            IDR.string.dashboard_sync_card_last_sync,
            LAST_SYNC_TIME,
        )
        onView(withId(R.id.sync_card_last_sync))
            .check(matches(withText(lastSyncText)))
        onView(withId(R.id.sync_card_default_items_to_upload)).check(
            matches(
                withText(
                    context.resources.getQuantityString(
                        com.simprints.infra.resources.R.plurals.dashboard_sync_card_records_to_upload,
                        2,
                        2,
                    ),
                ),
            ),
        )
        onView(withId(R.id.sync_card_default_state_sync_button))
            .check(matches(isDisplayed()))
            .perform(scrollTo(), click())
        verify(exactly = 1) { syncViewModel.sync() }
    }

    @Test
    fun `should display the correct sync card view for the SyncFailed state`() {
        mockSyncToBFSIDAllowed(true)
        mockSyncCardLiveData(SyncCardState.SyncFailed(LAST_SYNC_TIME))

        launchFragmentInHiltContainer<LogoutSyncFragment>(navController = navController)

        checkHiddenViews(
            listOf(
                R.id.sync_card_default_state_sync_button,
                R.id.sync_card_select_no_modules,
                R.id.sync_card_offline,
                R.id.sync_card_progress,
                R.id.sync_card_try_again,
                R.id.logoutButton,
            ),
        )

        val lastSyncText = context.getString(
            IDR.string.dashboard_sync_card_last_sync,
            LAST_SYNC_TIME,
        )
        onView(withId(R.id.sync_card_last_sync))
            .check(matches(withText(lastSyncText)))
        onView(withId(R.id.sync_card_failed_message))
            .check(matches(withText(IDR.string.dashboard_sync_card_failed_message)))
    }

    @Test
    fun `should display the correct sync card view for the SyncFailedBackendMaintenance state without estimated outage`() {
        mockSyncToBFSIDAllowed(true)
        mockSyncCardLiveData(SyncCardState.SyncFailedBackendMaintenance(LAST_SYNC_TIME))

        launchFragmentInHiltContainer<LogoutSyncFragment>(navController = navController)

        checkHiddenViews(
            listOf(
                R.id.sync_card_default_state_sync_button,
                R.id.sync_card_select_no_modules,
                R.id.sync_card_offline,
                R.id.sync_card_progress,
                R.id.sync_card_try_again,
                R.id.logoutButton,
            ),
        )
        val lastSyncText = context.getString(
            IDR.string.dashboard_sync_card_last_sync,
            LAST_SYNC_TIME,
        )
        onView(withId(R.id.sync_card_last_sync))
            .check(matches(withText(lastSyncText)))
        onView(withId(R.id.sync_card_failed_message))
            .check(matches(withText(IDR.string.error_backend_maintenance_message)))
    }

    @Test
    fun `should display the correct sync card view for the SyncFailedBackendMaintenance state with estimated outage`() {
        mockSyncToBFSIDAllowed(true)
        mockSyncCardLiveData(
            SyncCardState.SyncFailedBackendMaintenance(
                LAST_SYNC_TIME,
                10L,
            ),
        )

        launchFragmentInHiltContainer<LogoutSyncFragment>(navController = navController)

        checkHiddenViews(
            listOf(
                R.id.sync_card_default_state_sync_button,
                R.id.sync_card_select_no_modules,
                R.id.sync_card_offline,
                R.id.sync_card_progress,
                R.id.sync_card_try_again,
                R.id.logoutButton,
            ),
        )

        val lastSyncText = context.getString(
            IDR.string.dashboard_sync_card_last_sync,
            LAST_SYNC_TIME,
        )
        onView(withId(R.id.sync_card_last_sync))
            .check(matches(withText(lastSyncText)))
        val text =
            context.getString(
                IDR.string.error_backend_maintenance_with_time_message,
                "10 seconds",
            )
        onView(withId(R.id.sync_card_failed_message))
            .check(matches(withText(text)))
    }

    @Test
    fun `should display the correct sync card view for the SyncTooManyRequests state`() {
        mockSyncToBFSIDAllowed(true)
        mockSyncCardLiveData(SyncCardState.SyncTooManyRequests(LAST_SYNC_TIME))

        launchFragmentInHiltContainer<LogoutSyncFragment>(navController = navController)

        checkHiddenViews(
            listOf(
                R.id.sync_card_default_state_sync_button,
                R.id.sync_card_select_no_modules,
                R.id.sync_card_offline,
                R.id.sync_card_progress,
                R.id.sync_card_try_again,
                R.id.logoutButton,
            ),
        )

        val lastSyncText = context.getString(
            IDR.string.dashboard_sync_card_last_sync,
            LAST_SYNC_TIME,
        )
        onView(withId(R.id.sync_card_last_sync))
            .check(matches(withText(lastSyncText)))
        onView(withId(R.id.sync_card_failed_message))
            .check(matches(withText(IDR.string.dashboard_sync_card_too_many_modules_message)))
    }

    @Test
    fun `should display the correct sync card view for the SyncTryAgain state`() {
        mockSyncToBFSIDAllowed(true)
        mockSyncCardLiveData(SyncCardState.SyncTryAgain(LAST_SYNC_TIME))

        launchFragmentInHiltContainer<LogoutSyncFragment>(navController = navController)

        checkHiddenViews(
            listOf(
                R.id.sync_card_default_state_sync_button,
                R.id.sync_card_failed_message,
                R.id.sync_card_select_no_modules,
                R.id.sync_card_offline,
                R.id.sync_card_progress,
                R.id.logoutButton,
            ),
        )

        val lastSyncText = context.getString(
            IDR.string.dashboard_sync_card_last_sync,
            LAST_SYNC_TIME,
        )
        onView(withId(R.id.sync_card_last_sync))
            .check(matches(withText(lastSyncText)))
        onView(withId(R.id.sync_card_try_again_sync_button))
            .check(matches(isDisplayed()))
            .perform(scrollTo(), click())
        verify(exactly = 1) { syncViewModel.sync() }
    }

    @Test
    fun `should display the correct sync card view for the SyncHasNoModules state`() {
        mockSyncToBFSIDAllowed(true)
        mockSyncCardLiveData(SyncCardState.SyncHasNoModules(LAST_SYNC_TIME))

        val navController = testNavController(R.navigation.graph_dashboard, R.id.logOutSyncFragment)

        launchFragmentInHiltContainer<LogoutSyncFragment>(navController = navController)

        checkHiddenViews(
            listOf(
                R.id.sync_card_default_state_sync_button,
                R.id.sync_card_failed_message,
                R.id.sync_card_try_again,
                R.id.sync_card_offline,
                R.id.sync_card_progress,
                R.id.logoutButton,
            ),
        )

        val lastSyncText = context.getString(
            IDR.string.dashboard_sync_card_last_sync,
            LAST_SYNC_TIME,
        )
        onView(withId(R.id.sync_card_last_sync))
            .check(matches(withText(lastSyncText)))
        onView(withId(R.id.sync_card_select_no_modules_button))
            .check(matches(isDisplayed()))
            .perform(click())
        assertThat(navController.currentDestination?.id)
            .isEqualTo(R.id.moduleSelectionFragment)
    }

    @Test
    fun `should display the correct sync card view for the SyncProgress state`() {
        mockSyncToBFSIDAllowed(true)
        mockSyncCardLiveData(SyncCardState.SyncProgress(LAST_SYNC_TIME, 20, 40))

        launchFragmentInHiltContainer<LogoutSyncFragment>(navController = navController)

        checkHiddenViews(
            listOf(
                R.id.sync_card_default_state_sync_button,
                R.id.sync_card_failed_message,
                R.id.sync_card_try_again,
                R.id.sync_card_select_no_modules_button,
                R.id.sync_card_offline,
                R.id.logoutButton,
            ),
        )

        val lastSyncText = context.getString(
            IDR.string.dashboard_sync_card_last_sync,
            LAST_SYNC_TIME,
        )
        onView(withId(R.id.sync_card_last_sync))
            .check(matches(withText(lastSyncText)))

        onView(withId(R.id.sync_card_progress_sync_progress_bar)).check(
            matches(
                isDisplayed(),
            ),
        )

        val text = context.getString(IDR.string.dashboard_sync_card_progress, "50%")
        onView(withId(R.id.sync_card_progress_message))
            .check(matches(withText(text)))
    }

    @Test
    fun `should display the correct sync card view for the SyncConnecting state`() {
        mockSyncToBFSIDAllowed(true)
        mockSyncCardLiveData(SyncCardState.SyncConnecting(LAST_SYNC_TIME, 20, 40))

        launchFragmentInHiltContainer<LogoutSyncFragment>(navController = navController)

        checkHiddenViews(
            listOf(
                R.id.sync_card_default_state_sync_button,
                R.id.sync_card_failed_message,
                R.id.sync_card_try_again,
                R.id.sync_card_select_no_modules_button,
                R.id.sync_card_offline,
                R.id.logoutButton,
            ),
        )

        val lastSyncText = context.getString(
            IDR.string.dashboard_sync_card_last_sync,
            LAST_SYNC_TIME,
        )
        onView(withId(R.id.sync_card_last_sync))
            .check(matches(withText(lastSyncText)))

        onView(withId(R.id.sync_card_progress_sync_progress_bar)).check(
            matches(isDisplayed()),
        )

        onView(withId(R.id.sync_card_progress_message))
            .check(matches(withText(IDR.string.dashboard_sync_card_connecting)))
    }

    @Test
    fun `should display the correct sync card view for the SyncComplete state`() {
        mockSyncToBFSIDAllowed(true)
        mockSyncCardLiveData(SyncCardState.SyncComplete(LAST_SYNC_TIME))

        launchFragmentInHiltContainer<LogoutSyncFragment>(navController = navController)

        checkHiddenViews(
            listOf(
                R.id.sync_card_default_state_sync_button,
                R.id.sync_card_failed_message,
                R.id.sync_card_try_again,
                R.id.sync_card_select_no_modules_button,
                R.id.sync_card_offline,
            ),
        )

        val lastSyncText = context.getString(
            IDR.string.dashboard_sync_card_last_sync,
            LAST_SYNC_TIME,
        )
        onView(withId(R.id.sync_card_last_sync))
            .check(matches(withText(lastSyncText)))

        onView(withId(R.id.sync_card_progress_sync_progress_bar)).check(
            matches(isDisplayed()),
        )

        onView(withId(R.id.sync_card_progress_message))
            .check(matches(withText(IDR.string.dashboard_sync_card_complete)))

        onView(withId(R.id.logoutButton))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
    }

    @Test
    fun `should navigate to requestLoginFragment when logout button is pressed`() {
        mockSyncToBFSIDAllowed(true)
        mockSyncCardLiveData(SyncCardState.SyncComplete(LAST_SYNC_TIME))
        val navController = testNavController(R.navigation.graph_dashboard, R.id.logout_navigation)
        launchFragmentInHiltContainer<LogoutSyncFragment>(navController = navController)

        onView(withId(R.id.logoutButton)).perform(scrollTo(), click())
        assertThat(navController.currentDestination?.id)
            .isEqualTo(R.id.requestLoginFragment)
    }

    @Test
    fun `logout button is not visible when records are not synchronized`() {
        mockSyncToBFSIDAllowed(true)
        mockSyncCardLiveData(SyncCardState.SyncProgress(LAST_SYNC_TIME, 20, 40))

        launchFragmentInHiltContainer<LogoutSyncFragment>(navController = navController)
        onView(withId(R.id.logoutButton)).check(matches(not(isDisplayed())))
        onView(withId(R.id.logout_sync_info)).check(matches(isDisplayed()))
        onView(withId(R.id.logoutWithoutSyncButton)).check(matches(isDisplayed()))
    }

    @Test
    fun `logout button is visible when records are synchronized`() {
        mockSyncToBFSIDAllowed(true)
        mockSyncCardLiveData(SyncCardState.SyncComplete(LAST_SYNC_TIME))

        launchFragmentInHiltContainer<LogoutSyncFragment>(navController = navController)
        onView(withId(R.id.logoutButton)).check(matches(isDisplayed()))
        onView(withId(R.id.logout_sync_info)).check(matches(not(isDisplayed())))
        onView(withId(R.id.logoutWithoutSyncButton)).check(matches(not(isDisplayed())))
    }

    private fun mockSyncCardLiveData(state: SyncCardState) {
        every { syncViewModel.syncCardLiveData } returns mockk {
            every { observe(any(), any()) } answers {
                secondArg<Observer<SyncCardState>>().onChanged(state)
            }
        }
    }

    private fun mockSyncToBFSIDAllowed(allowed: Boolean) {
        every { syncViewModel.syncToBFSIDAllowed } returns mockk {
            every { observe(any(), any()) } answers {
                secondArg<Observer<Boolean>>().onChanged(allowed)
            }
        }
    }

    private fun checkHiddenViews(views: List<Int>) {
        views.forEach {
            onView(withId(it))
                .check(matches(not(isDisplayed())))
        }
    }
}
