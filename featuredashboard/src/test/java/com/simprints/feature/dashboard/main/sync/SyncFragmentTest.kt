package com.simprints.feature.dashboard.main.sync

import androidx.lifecycle.Observer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth
import com.simprints.feature.dashboard.R
import com.simprints.feature.dashboard.tools.launchFragmentInHiltContainer
import com.simprints.feature.dashboard.tools.testNavController
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
class SyncFragmentTest {

    companion object {
        private const val LAST_SYNC_TIME = "2022-10-10"
    }

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @BindValue
    @JvmField
    internal val viewModel = mockk<SyncViewModel>(relaxed = true)

    private val context = InstrumentationRegistry.getInstrumentation().context

    @Test
    fun `should hide the sync card view if it can't sync to BFSID`() {
        mockSyncToBFSIDAllowed(false)

        launchFragmentInHiltContainer<SyncFragment>()

        onView(withId(R.id.dashboard_sync_card)).check(matches(not(isDisplayed())))
    }

    @Test
    fun `should display the sync card view if it can sync to BFSID`() {
        mockSyncToBFSIDAllowed(true)

        launchFragmentInHiltContainer<SyncFragment>()

        onView(withId(R.id.dashboard_sync_card)).check(matches(isDisplayed()))
    }

    @Test
    fun `should display the correct sync card view for the SyncDefault state`() {
        mockSyncToBFSIDAllowed(true)
        mockSyncCardLiveData(DashboardSyncCardState.SyncDefault(LAST_SYNC_TIME))

        launchFragmentInHiltContainer<SyncFragment>()

        checkHiddenViews(
            listOf(
                R.id.dashboard_sync_card_failed_message,
                R.id.dashboard_sync_card_select_no_modules,
                R.id.dashboard_sync_card_offline,
                R.id.dashboard_sync_card_progress,
                R.id.dashboard_sync_card_try_again,
            )
        )
        val lastSyncText = context.getString(IDR.string.dashboard_card_sync_last_sync, LAST_SYNC_TIME)
        onView(withId(R.id.dashboard_sync_card_last_sync)).check(matches(withText(lastSyncText)))
        onView(withId(R.id.dashboard_sync_card_default_state_sync_button))
            .check(matches(isDisplayed()))
            .perform(click())
        verify(exactly = 1) { viewModel.sync() }
    }

    @Test
    fun `should display the correct sync card view for the SyncFailed state`() {
        mockSyncToBFSIDAllowed(true)
        mockSyncCardLiveData(DashboardSyncCardState.SyncFailed(LAST_SYNC_TIME))

        launchFragmentInHiltContainer<SyncFragment>()

        checkHiddenViews(
            listOf(
                R.id.dashboard_sync_card_default_state_sync_button,
                R.id.dashboard_sync_card_select_no_modules,
                R.id.dashboard_sync_card_offline,
                R.id.dashboard_sync_card_progress,
                R.id.dashboard_sync_card_try_again,
            )
        )

        val lastSyncText = context.getString(IDR.string.dashboard_card_sync_last_sync, LAST_SYNC_TIME)
        onView(withId(R.id.dashboard_sync_card_last_sync)).check(matches(withText(lastSyncText)))
        onView(withId(R.id.dashboard_sync_card_failed_message)).check(matches(withText(IDR.string.dashboard_sync_card_failed_message)))
    }

    @Test
    fun `should display the correct sync card view for the SyncFailedBackendMaintenance state without estimated outage`() {
        mockSyncToBFSIDAllowed(true)
        mockSyncCardLiveData(DashboardSyncCardState.SyncFailedBackendMaintenance(LAST_SYNC_TIME))

        launchFragmentInHiltContainer<SyncFragment>()

        checkHiddenViews(
            listOf(
                R.id.dashboard_sync_card_default_state_sync_button,
                R.id.dashboard_sync_card_select_no_modules,
                R.id.dashboard_sync_card_offline,
                R.id.dashboard_sync_card_progress,
                R.id.dashboard_sync_card_try_again,
            )
        )
        val lastSyncText = context.getString(IDR.string.dashboard_card_sync_last_sync, LAST_SYNC_TIME)
        onView(withId(R.id.dashboard_sync_card_last_sync)).check(matches(withText(lastSyncText)))
        onView(withId(R.id.dashboard_sync_card_failed_message)).check(matches(withText(IDR.string.error_backend_maintenance_message)))
    }

    @Test
    fun `should display the correct sync card view for the SyncFailedBackendMaintenance state with estimated outage`() {
        mockSyncToBFSIDAllowed(true)
        mockSyncCardLiveData(
            DashboardSyncCardState.SyncFailedBackendMaintenance(
                LAST_SYNC_TIME,
                10L
            )
        )

        launchFragmentInHiltContainer<SyncFragment>()

        checkHiddenViews(
            listOf(
                R.id.dashboard_sync_card_default_state_sync_button,
                R.id.dashboard_sync_card_select_no_modules,
                R.id.dashboard_sync_card_offline,
                R.id.dashboard_sync_card_progress,
                R.id.dashboard_sync_card_try_again,
            )
        )

        val lastSyncText = context.getString(IDR.string.dashboard_card_sync_last_sync, LAST_SYNC_TIME)
        onView(withId(R.id.dashboard_sync_card_last_sync)).check(matches(withText(lastSyncText)))
        val text =
            context.getString(IDR.string.error_backend_maintenance_with_time_message, "10 seconds")
        onView(withId(R.id.dashboard_sync_card_failed_message)).check(matches(withText(text)))
    }

    @Test
    fun `should display the correct sync card view for the SyncTooManyRequests state`() {
        mockSyncToBFSIDAllowed(true)
        mockSyncCardLiveData(DashboardSyncCardState.SyncTooManyRequests(LAST_SYNC_TIME))

        launchFragmentInHiltContainer<SyncFragment>()

        checkHiddenViews(
            listOf(
                R.id.dashboard_sync_card_default_state_sync_button,
                R.id.dashboard_sync_card_select_no_modules,
                R.id.dashboard_sync_card_offline,
                R.id.dashboard_sync_card_progress,
                R.id.dashboard_sync_card_try_again,
            )
        )

        val lastSyncText = context.getString(IDR.string.dashboard_card_sync_last_sync, LAST_SYNC_TIME)
        onView(withId(R.id.dashboard_sync_card_last_sync)).check(matches(withText(lastSyncText)))
        onView(withId(R.id.dashboard_sync_card_failed_message)).check(matches(withText(IDR.string.dashboard_sync_card_too_many_modules_message)))
    }

    @Test
    fun `should display the correct sync card view for the SyncTryAgain state`() {
        mockSyncToBFSIDAllowed(true)
        mockSyncCardLiveData(DashboardSyncCardState.SyncTryAgain(LAST_SYNC_TIME))

        launchFragmentInHiltContainer<SyncFragment>()

        checkHiddenViews(
            listOf(
                R.id.dashboard_sync_card_default_state_sync_button,
                R.id.dashboard_sync_card_failed_message,
                R.id.dashboard_sync_card_select_no_modules,
                R.id.dashboard_sync_card_offline,
                R.id.dashboard_sync_card_progress,
            )
        )

        val lastSyncText = context.getString(IDR.string.dashboard_card_sync_last_sync, LAST_SYNC_TIME)
        onView(withId(R.id.dashboard_sync_card_last_sync)).check(matches(withText(lastSyncText)))
        onView(withId(R.id.dashboard_sync_card_try_again_sync_button))
            .check(matches(isDisplayed()))
            .perform(click())
        verify(exactly = 1) { viewModel.sync() }
    }

    @Test
    fun `should display the correct sync card view for the SyncHasNoModules state`() {
        mockSyncToBFSIDAllowed(true)
        mockSyncCardLiveData(DashboardSyncCardState.SyncHasNoModules(LAST_SYNC_TIME))

        val navController = testNavController(R.navigation.graph_dashboard, R.id.mainFragment)

        launchFragmentInHiltContainer<SyncFragment>(navController = navController)

        checkHiddenViews(
            listOf(
                R.id.dashboard_sync_card_default_state_sync_button,
                R.id.dashboard_sync_card_failed_message,
                R.id.dashboard_sync_card_try_again,
                R.id.dashboard_sync_card_offline,
                R.id.dashboard_sync_card_progress,
            )
        )

        val lastSyncText = context.getString(IDR.string.dashboard_card_sync_last_sync, LAST_SYNC_TIME)
        onView(withId(R.id.dashboard_sync_card_last_sync)).check(matches(withText(lastSyncText)))
        onView(withId(R.id.dashboard_sync_card_select_no_modules_button))
            .check(matches(isDisplayed()))
            .perform(click())
        Truth.assertThat(navController.currentDestination?.id)
            .isEqualTo(R.id.moduleSelectionFragment)
    }

    @Test
    fun `should display the correct sync card view for the SyncOffline state`() {
        mockSyncToBFSIDAllowed(true)
        mockSyncCardLiveData(DashboardSyncCardState.SyncOffline(LAST_SYNC_TIME))

        launchFragmentInHiltContainer<SyncFragment>()

        checkHiddenViews(
            listOf(
                R.id.dashboard_sync_card_default_state_sync_button,
                R.id.dashboard_sync_card_failed_message,
                R.id.dashboard_sync_card_try_again,
                R.id.dashboard_sync_card_select_no_modules_button,
                R.id.dashboard_sync_card_progress,
            )
        )

        val lastSyncText = context.getString(IDR.string.dashboard_card_sync_last_sync, LAST_SYNC_TIME)
        onView(withId(R.id.dashboard_sync_card_last_sync)).check(matches(withText(lastSyncText)))
        onView(withId(R.id.dashboard_sync_card_offline_button))
            .check(matches(isDisplayed()))
            .perform(click())
    }

    @Test
    fun `should display the correct sync card view for the SyncProgress state`() {
        mockSyncToBFSIDAllowed(true)
        mockSyncCardLiveData(DashboardSyncCardState.SyncProgress(LAST_SYNC_TIME, 20, 40))

        launchFragmentInHiltContainer<SyncFragment>()

        checkHiddenViews(
            listOf(
                R.id.dashboard_sync_card_default_state_sync_button,
                R.id.dashboard_sync_card_failed_message,
                R.id.dashboard_sync_card_try_again,
                R.id.dashboard_sync_card_select_no_modules_button,
                R.id.dashboard_sync_card_offline,
            )
        )

        val lastSyncText = context.getString(IDR.string.dashboard_card_sync_last_sync, LAST_SYNC_TIME)
        onView(withId(R.id.dashboard_sync_card_last_sync)).check(matches(withText(lastSyncText)))
        onView(withId(R.id.dashboard_sync_card_progress_indeterminate_progress_bar)).check(
            matches(
                not(isDisplayed())
            )
        )

        onView(withId(R.id.dashboard_sync_card_progress_sync_progress_bar)).check(
            matches(
                isDisplayed()
            )
        )

        val text = context.getString(IDR.string.dashboard_sync_card_progress, "50%")
        onView(withId(R.id.dashboard_sync_card_progress_message)).check(matches(withText(text)))
    }

    @Test
    fun `should display the correct sync card view for the SyncConnecting state`() {
        mockSyncToBFSIDAllowed(true)
        mockSyncCardLiveData(DashboardSyncCardState.SyncConnecting(LAST_SYNC_TIME, 20, 40))

        launchFragmentInHiltContainer<SyncFragment>()

        checkHiddenViews(
            listOf(
                R.id.dashboard_sync_card_default_state_sync_button,
                R.id.dashboard_sync_card_failed_message,
                R.id.dashboard_sync_card_try_again,
                R.id.dashboard_sync_card_select_no_modules_button,
                R.id.dashboard_sync_card_offline,
            )
        )

        val lastSyncText = context.getString(IDR.string.dashboard_card_sync_last_sync, LAST_SYNC_TIME)
        onView(withId(R.id.dashboard_sync_card_last_sync)).check(matches(withText(lastSyncText)))
        onView(withId(R.id.dashboard_sync_card_progress_indeterminate_progress_bar)).check(
            matches(isDisplayed())
        )

        onView(withId(R.id.dashboard_sync_card_progress_sync_progress_bar)).check(
            matches(isDisplayed())
        )

        onView(withId(R.id.dashboard_sync_card_progress_message)).check(matches(withText(IDR.string.dashboard_sync_card_connecting)))
    }

    @Test
    fun `should display the correct sync card view for the SyncComplete state`() {
        mockSyncToBFSIDAllowed(true)
        mockSyncCardLiveData(DashboardSyncCardState.SyncComplete(LAST_SYNC_TIME))

        launchFragmentInHiltContainer<SyncFragment>()

        checkHiddenViews(
            listOf(
                R.id.dashboard_sync_card_default_state_sync_button,
                R.id.dashboard_sync_card_failed_message,
                R.id.dashboard_sync_card_try_again,
                R.id.dashboard_sync_card_select_no_modules_button,
                R.id.dashboard_sync_card_offline,
            )
        )

        val lastSyncText = context.getString(IDR.string.dashboard_card_sync_last_sync, LAST_SYNC_TIME)
        onView(withId(R.id.dashboard_sync_card_last_sync)).check(matches(withText(lastSyncText)))
        onView(withId(R.id.dashboard_sync_card_progress_indeterminate_progress_bar)).check(
            matches(not(isDisplayed()))
        )

        onView(withId(R.id.dashboard_sync_card_progress_sync_progress_bar)).check(
            matches(isDisplayed())
        )

        onView(withId(R.id.dashboard_sync_card_progress_message)).check(matches(withText(IDR.string.dashboard_sync_card_complete)))
    }

    private fun mockSyncToBFSIDAllowed(allowed: Boolean) {
        every { viewModel.syncToBFSIDAllowed } returns mockk {
            every { observe(any(), any()) } answers {
                secondArg<Observer<Boolean>>().onChanged(allowed)
            }
        }
    }

    private fun mockSyncCardLiveData(state: DashboardSyncCardState) {
        every { viewModel.syncCardLiveData } returns mockk {
            every { observe(any(), any()) } answers {
                secondArg<Observer<DashboardSyncCardState>>().onChanged(state)
            }
        }
    }

    private fun checkHiddenViews(views: List<Int>) {
        views.forEach {
            onView(withId(it)).check(matches(not(isDisplayed())))
        }
    }
}
