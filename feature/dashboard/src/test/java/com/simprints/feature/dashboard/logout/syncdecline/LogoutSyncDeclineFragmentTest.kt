package com.simprints.feature.dashboard.logout.syncdecline

import android.os.Looper
import android.widget.ImageButton
import androidx.test.espresso.Espresso.*
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.*
import androidx.test.espresso.matcher.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.*
import com.google.common.truth.Truth.*
import com.simprints.feature.dashboard.R
import com.simprints.feature.dashboard.tools.MviStubs
import com.simprints.feature.dashboard.tools.enterSettingsPassword
import com.simprints.feature.dashboard.tools.hostedFragment
import com.simprints.feature.dashboard.tools.launchRecreatableInHiltContainer
import com.simprints.feature.dashboard.tools.showingAlertDialogs
import com.simprints.feature.dashboard.tools.stubMvi
import com.simprints.testtools.hilt.launchFragmentInHiltContainer
import com.simprints.testtools.hilt.testNavController
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.*
import kotlinx.coroutines.flow.update
import org.hamcrest.CoreMatchers.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import com.simprints.infra.resources.R as IDR

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
internal class LogoutSyncDeclineFragmentTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @BindValue
    @JvmField
    internal val viewModel = mockk<LogoutSyncDeclineViewModel>(relaxed = true)

    private lateinit var mvi: MviStubs<LogoutSyncDeclineUiState, LogoutSyncDeclineUiEffect>

    @Before
    fun setUp() {
        mvi = viewModel.stubMvi(LogoutSyncDeclineUiState())
    }

    @Test
    fun `every confirm click sends confirm clicked and nothing else`() {
        launchFragmentInHiltContainer<LogoutSyncDeclineFragment>(navController = declineNavController())

        onView(withId(R.id.logoutWithoutSyncConfirmButton)).perform(click())
        verify(exactly = 1) { viewModel.onAction(LogoutSyncDeclineUiAction.ConfirmClicked) }

        onView(withId(R.id.logoutWithoutSyncConfirmButton)).perform(click())
        verify(exactly = 2) { viewModel.onAction(LogoutSyncDeclineUiAction.ConfirmClicked) }
        verify(exactly = 0) { viewModel.onAction(LogoutSyncDeclineUiAction.LogoutConfirmed) }
    }

    @Test
    fun `without a password the confirmation dialog log out button sends logout confirmed`() {
        confirmClickShowsTheConfirmation()
        launchFragmentInHiltContainer<LogoutSyncDeclineFragment>(navController = declineNavController())

        onView(withId(R.id.logoutWithoutSyncConfirmButton)).perform(click())
        onView(withText(IDR.string.dashboard_logout_confirmation_message))
            .inRoot(RootMatchers.isDialog())
            .check(ViewAssertions.matches(isDisplayed()))
        onView(withId(android.R.id.button1))
            .inRoot(RootMatchers.isDialog())
            .check(ViewAssertions.matches(isDisplayed()))
            .perform(click())
        shadowOf(Looper.getMainLooper()).idle()

        verify(exactly = 1) { viewModel.onAction(LogoutSyncDeclineUiAction.LogoutConfirmed) }
        verify(exactly = 0) { viewModel.onAction(LogoutSyncDeclineUiAction.LogoutConfirmationDismissed) }
    }

    @Test
    fun `without a password cancelling the confirmation dialog reports the dismissal and does not log out`() {
        confirmClickShowsTheConfirmation()
        val navController = declineNavController()
        launchFragmentInHiltContainer<LogoutSyncDeclineFragment>(navController = navController)

        onView(withId(R.id.logoutWithoutSyncConfirmButton)).perform(click())
        onView(withId(android.R.id.button2))
            .inRoot(RootMatchers.isDialog())
            .check(ViewAssertions.matches(isDisplayed()))
            .perform(click())

        shadowOf(Looper.getMainLooper()).idle()
        assertThat(navController.currentDestination?.id).isEqualTo(R.id.logOutSyncDeclineFragment)
        verify(exactly = 1) { viewModel.onAction(LogoutSyncDeclineUiAction.LogoutConfirmationDismissed) }
        verify(exactly = 0) { viewModel.onAction(LogoutSyncDeclineUiAction.LogoutConfirmed) }
    }

    @Test
    fun `the confirmation dialog follows the state`() {
        launchFragmentInHiltContainer<LogoutSyncDeclineFragment>(navController = declineNavController())
        assertThat(showingAlertDialogs()).isEmpty()

        mvi.state.update { it.copy(isLogoutConfirmationVisible = true) }
        shadowOf(Looper.getMainLooper()).idle()
        onView(withText(IDR.string.dashboard_logout_confirmation_message))
            .inRoot(RootMatchers.isDialog())
            .check(ViewAssertions.matches(isDisplayed()))
        assertThat(showingAlertDialogs()).hasSize(1)

        mvi.state.update { it.copy(isLogoutConfirmationVisible = false) }
        shadowOf(Looper.getMainLooper()).idle()
        assertThat(showingAlertDialogs()).isEmpty()
        // Hidden by the state, not by the user: no dismissal is reported
        verify(exactly = 0) { viewModel.onAction(LogoutSyncDeclineUiAction.LogoutConfirmationDismissed) }
    }

    @Test
    fun `the confirmation dialog is shown again after the activity is re-created`() {
        mvi.state.update { it.copy(isLogoutConfirmationVisible = true) }
        val scenario = launchRecreatableInHiltContainer { LogoutSyncDeclineFragment() }
        val first = showingAlertDialogs().single()

        scenario.recreate()
        shadowOf(Looper.getMainLooper()).idle()

        assertThat(scenario.hostedFragment<LogoutSyncDeclineFragment>()).isNotNull()
        assertThat(first.isShowing).isFalse()
        assertThat(showingAlertDialogs().single()).isNotSameInstanceAs(first)
        // Closing the old window with the view is not a user dismissal
        verify(exactly = 0) { viewModel.onAction(LogoutSyncDeclineUiAction.LogoutConfirmationDismissed) }
        scenario.close()
    }

    @Test
    fun `with a password the correct password sends logout confirmed`() {
        confirmClickAnswersWith(LogoutSyncDeclineUiEffect.ShowPasswordDialog(PASSWORD))
        launchFragmentInHiltContainer<LogoutSyncDeclineFragment>(navController = declineNavController())

        onView(withId(R.id.logoutWithoutSyncConfirmButton)).perform(click())
        onView(withText(IDR.string.dashboard_password_lock_title_logout))
            .inRoot(RootMatchers.isDialog())
            .check(ViewAssertions.matches(isDisplayed()))
        enterSettingsPassword(PASSWORD)

        verify(exactly = 1) { viewModel.onAction(LogoutSyncDeclineUiAction.LogoutConfirmed) }
    }

    @Test
    fun `with a password cancelling the password dialog does not log out`() {
        confirmClickAnswersWith(LogoutSyncDeclineUiEffect.ShowPasswordDialog(PASSWORD))
        val navController = declineNavController()
        launchFragmentInHiltContainer<LogoutSyncDeclineFragment>(navController = navController)

        onView(withId(R.id.logoutWithoutSyncConfirmButton)).perform(click())
        onView(withId(android.R.id.button2))
            .inRoot(RootMatchers.isDialog())
            .check(ViewAssertions.matches(isDisplayed()))
            .perform(click())

        assertThat(navController.currentDestination?.id).isEqualTo(R.id.logOutSyncDeclineFragment)
        verify(exactly = 0) { viewModel.onAction(LogoutSyncDeclineUiAction.LogoutConfirmed) }
    }

    @Test
    fun `wrong password does not log out`() {
        confirmClickAnswersWith(LogoutSyncDeclineUiEffect.ShowPasswordDialog("1234"))
        val navController = declineNavController()
        launchFragmentInHiltContainer<LogoutSyncDeclineFragment>(navController = navController)

        onView(withId(R.id.logoutWithoutSyncConfirmButton)).perform(click())
        enterSettingsPassword("1111")

        onView(withText(IDR.string.dashboard_password_lock_wrong_pin))
            .inRoot(RootMatchers.isDialog())
            .check(ViewAssertions.matches(isDisplayed()))
        assertThat(navController.currentDestination?.id).isEqualTo(R.id.logOutSyncDeclineFragment)
        verify(exactly = 0) { viewModel.onAction(LogoutSyncDeclineUiAction.LogoutConfirmed) }
    }

    @Test
    fun `cancel button goes back to the logout sync screen without logging out`() {
        val navController = navControllerOnDeclineFromLogoutSync()
        launchFragmentInHiltContainer<LogoutSyncDeclineFragment>(navController = navController)

        onView(withId(R.id.logoutWithoutSyncCancelButton)).perform(click())

        assertThat(navController.currentDestination?.id).isEqualTo(R.id.logOutSyncFragment)
        verify(exactly = 0) { viewModel.onAction(any()) }
    }

    @Test
    fun `toolbar up goes back to the logout sync screen without logging out`() {
        val navController = navControllerOnDeclineFromLogoutSync()
        launchFragmentInHiltContainer<LogoutSyncDeclineFragment>(navController = navController)

        onView(
            allOf(
                isAssignableFrom(ImageButton::class.java),
                withParent(withId(R.id.logoutSyncDeclineToolbar)),
            ),
        ).perform(click())

        assertThat(navController.currentDestination?.id).isEqualTo(R.id.logOutSyncFragment)
        verify(exactly = 0) { viewModel.onAction(any()) }
    }

    private fun declineNavController() = testNavController(R.navigation.graph_dashboard, R.id.logOutSyncDeclineFragment)

    private fun navControllerOnDeclineFromLogoutSync() = testNavController(R.navigation.graph_dashboard, R.id.logOutSyncFragment).also {
        it.navigate(R.id.action_logoutSyncFragment_to_logoutSyncDeclineFragment)
        assertThat(it.currentDestination?.id).isEqualTo(R.id.logOutSyncDeclineFragment)
    }

    // Stands in for the ViewModel: the confirm click answers with the effect the real ViewModel would emit.
    private fun confirmClickAnswersWith(effect: LogoutSyncDeclineUiEffect) {
        every { viewModel.onAction(LogoutSyncDeclineUiAction.ConfirmClicked) } answers { mvi.effects.trySend(effect) }
    }

    // Stands in for the ViewModel without a password: the confirm click shows the confirmation through the state.
    private fun confirmClickShowsTheConfirmation() {
        every { viewModel.onAction(LogoutSyncDeclineUiAction.ConfirmClicked) } answers {
            mvi.state.update { it.copy(isLogoutConfirmationVisible = true) }
        }
    }

    private companion object {
        const val PASSWORD = "password123"
    }
}
