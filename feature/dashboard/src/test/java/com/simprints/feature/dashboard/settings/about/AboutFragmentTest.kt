package com.simprints.feature.dashboard.settings.about

import android.content.ClipboardManager
import android.content.Context
import android.os.Looper
import android.widget.ImageButton
import androidx.test.espresso.Espresso.*
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.matcher.RootMatchers.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.*
import com.google.common.truth.Truth.*
import com.simprints.core.DeviceID
import com.simprints.core.PackageVersionName
import com.simprints.feature.dashboard.R
import com.simprints.feature.dashboard.tools.MviStubs
import com.simprints.feature.dashboard.tools.enterSettingsPassword
import com.simprints.feature.dashboard.tools.hostedFragment
import com.simprints.feature.dashboard.tools.launchRecreatableInHiltContainer
import com.simprints.feature.dashboard.tools.preference
import com.simprints.feature.dashboard.tools.showingAlertDialogs
import com.simprints.feature.dashboard.tools.stubMvi
import com.simprints.infra.config.store.models.SettingsPasswordConfig
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
import org.robolectric.shadows.ShadowToast
import javax.inject.Inject
import com.simprints.feature.troubleshooting.R as TroubleshootingR
import com.simprints.infra.resources.R as IDR

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class AboutFragmentTest {
    companion object {
        private const val SCANNER_VERSION = "SP424242"
        private const val SYNC = "PROJECT"
        private const val SEARCH = "MODULE"
        private const val PASSWORD = "1234"
    }

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    @DeviceID
    lateinit var deviceId: String

    @Inject
    @PackageVersionName
    lateinit var packageVersionName: String

    @BindValue
    @JvmField
    internal val viewModel = mockk<AboutViewModel>(relaxed = true)

    private lateinit var mvi: MviStubs<AboutUiState, AboutUiEffect>

    @Before
    fun setup() {
        hiltRule.inject()
        mvi = viewModel.stubMvi(AboutUiState())
    }

    @Test
    fun `should hide the fingerprint preference if the modalities doesn't contain Fingerprint`() {
        givenLoaded(isScannerVersionVisible = false)

        launchFragmentInHiltContainer<AboutFragment>()

        onView(withText(IDR.string.dashboard_preference_scanner_version_title)).check(doesNotExist())
        onView(withText(SCANNER_VERSION)).check(doesNotExist())
    }

    @Test
    fun `should display the fingerprint preference if the modalities contains Fingerprint`() {
        givenLoaded(isScannerVersionVisible = true)

        launchFragmentInHiltContainer<AboutFragment>()

        onView(withText(IDR.string.dashboard_preference_scanner_version_title)).check(matches(isDisplayed()))
        onView(withText(SCANNER_VERSION)).check(matches(isDisplayed()))
    }

    // AB-9
    @Test
    fun `losing the fingerprint modality on a re-render hides the scanner version`() {
        givenLoaded(isScannerVersionVisible = true)
        lateinit var fragment: AboutFragment
        launchFragmentInHiltContainer<AboutFragment> { fragment = this as AboutFragment }
        assertThat(fragment.preference(R.string.preference_scanner_version_key).isVisible).isTrue()
        onView(withText(SCANNER_VERSION)).check(matches(isDisplayed()))

        mvi.state.update { it.copy(isScannerVersionVisible = false) }
        shadowOf(Looper.getMainLooper()).idle()

        assertThat(fragment.preference(R.string.preference_scanner_version_key).isVisible).isFalse()
        onView(withText(IDR.string.dashboard_preference_scanner_version_title)).check(doesNotExist())
        onView(withText(SCANNER_VERSION)).check(doesNotExist())
    }

    // D-3
    @Test
    fun `nothing loaded yet keeps the loaded preferences at their xml defaults and sends no action`() {
        lateinit var fragment: AboutFragment
        launchFragmentInHiltContainer<AboutFragment> { fragment = this as AboutFragment }

        assertThat(fragment.preference(R.string.preference_app_version_key).summary).isEqualTo(packageVersionName)
        assertThat(fragment.preference(R.string.preference_device_id_key).summary).isEqualTo(deviceId)
        assertThat(fragment.preference(R.string.preference_scanner_version_key).isVisible).isFalse()
        assertThat(fragment.preference(R.string.preference_scanner_version_key).summary).isNull()
        assertThat(fragment.preference(R.string.preference_sync_and_search_key).summary).isNull()
        verify(exactly = 0) { viewModel.onAction(any()) }
    }

    @Test
    fun `should display the toolbar`() {
        givenLoaded(isScannerVersionVisible = true)

        launchFragmentInHiltContainer<AboutFragment>()

        onView(withId(R.id.settingsAboutToolbar)).check(matches(isDisplayed()))
    }

    @Test
    fun `should init the preferences correctly`() {
        givenLoaded(isScannerVersionVisible = false)

        launchFragmentInHiltContainer<AboutFragment>()

        onView(withText(deviceId)).check(matches(isDisplayed()))
        onView(withText(packageVersionName)).check(matches(isDisplayed()))
        onView(withText("Project Sync - Module Search")).check(matches(isDisplayed()))
    }

    // AB-6
    @Test
    fun `clicking the device id copies it to the clipboard and shows a toast`() {
        givenLoaded(isScannerVersionVisible = false)
        lateinit var fragment: AboutFragment
        launchFragmentInHiltContainer<AboutFragment> { fragment = this as AboutFragment }

        onView(withText(deviceId)).perform(click())

        val clipboard = fragment.requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        assertThat(
            clipboard.primaryClip
                ?.getItemAt(0)
                ?.text
                .toString(),
        ).isEqualTo(deviceId)
        assertThat(ShadowToast.getTextOfLatestToast()).isEqualTo(fragment.getString(IDR.string.dashboard_preference_copied_to_clipboard))
        assertThat(ShadowToast.shownToastCount()).isEqualTo(1)
        verify(exactly = 0) { viewModel.onAction(any()) }
    }

    // AB-8
    @Test
    fun `toolbar up goes back to the settings screen`() {
        givenLoaded(isScannerVersionVisible = false)
        val navController = testNavController(R.navigation.graph_dashboard, R.id.settingsFragment).also {
            it.navigate(R.id.action_settingsFragment_to_aboutFragment)
            assertThat(it.currentDestination?.id).isEqualTo(R.id.aboutFragment)
        }
        launchFragmentInHiltContainer<AboutFragment>(navController = navController)

        onView(allOf(isAssignableFrom(ImageButton::class.java), withParent(withId(R.id.settingsAboutToolbar)))).perform(click())

        assertThat(navController.currentDestination?.id).isEqualTo(R.id.settingsFragment)
        verify(exactly = 0) { viewModel.onAction(any()) }
    }

    @Test
    fun `should process logout when no password and clicking on logout`() {
        givenLoaded(isScannerVersionVisible = false)
        logoutClickShowsTheConfirmation()
        val navController = testNavController(R.navigation.graph_dashboard, R.id.aboutFragment)

        launchFragmentInHiltContainer<AboutFragment>(navController = navController)
        onView(withText(IDR.string.dashboard_preference_logout_title)).perform(click())
        onView(withId(android.R.id.button1))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
            .perform(click())

        shadowOf(Looper.getMainLooper()).idle()

        verifyOrder {
            viewModel.onAction(AboutUiAction.LogoutClicked)
            viewModel.onAction(AboutUiAction.LogoutConfirmed)
        }
        verify(exactly = 2) { viewModel.onAction(any()) }
    }

    @Test
    fun `should navigate to logout sync screen when the logout sync effect is received`() {
        givenLoaded(isScannerVersionVisible = false)
        mvi.effects.trySend(AboutUiEffect.NavigateToLogoutSync)
        val navController = testNavController(R.navigation.graph_dashboard, R.id.aboutFragment)

        launchFragmentInHiltContainer<AboutFragment>(navController = navController)

        assertThat(navController.currentDestination?.id).isEqualTo(R.id.logOutSyncFragment)
    }

    // AB-3: without pending data the ViewModel logs out and sends no effect; navigation comes from the logout broadcast.
    @Test
    fun `confirmed logout without a navigation effect stays on about`() {
        givenLoaded(isScannerVersionVisible = false)
        logoutClickShowsTheConfirmation()
        val navController = testNavController(R.navigation.graph_dashboard, R.id.aboutFragment)

        launchFragmentInHiltContainer<AboutFragment>(navController = navController)
        onView(withText(IDR.string.dashboard_preference_logout_title)).perform(click())
        onView(withId(android.R.id.button1)).inRoot(isDialog()).perform(click())
        shadowOf(Looper.getMainLooper()).idle()

        verify(exactly = 1) { viewModel.onAction(AboutUiAction.LogoutConfirmed) }
        assertThat(navController.currentDestination?.id).isEqualTo(R.id.aboutFragment)
    }

    @Test
    fun `should not logout when no password and refusing on the alert dialog`() {
        givenLoaded(isScannerVersionVisible = false)
        logoutClickShowsTheConfirmation()
        val navController = testNavController(R.navigation.graph_dashboard, R.id.aboutFragment)

        launchFragmentInHiltContainer<AboutFragment>(navController = navController)
        onView(withText(IDR.string.dashboard_preference_logout_title)).perform(click())
        onView(withId(android.R.id.button2))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
            .perform(click())

        shadowOf(Looper.getMainLooper()).idle()

        verify(exactly = 1) { viewModel.onAction(AboutUiAction.LogoutClicked) }
        assertThat(navController.currentDestination?.id).isEqualTo(R.id.aboutFragment)
        verify(exactly = 1) { viewModel.onAction(AboutUiAction.LogoutConfirmationDismissed) }
        verify(exactly = 0) { viewModel.onAction(AboutUiAction.LogoutConfirmed) }
    }

    @Test
    fun `the logout confirmation follows the state`() {
        givenLoaded(isScannerVersionVisible = false)
        launchFragmentInHiltContainer<AboutFragment>()
        assertThat(showingAlertDialogs()).isEmpty()

        mvi.state.update { it.copy(isLogoutConfirmationVisible = true) }
        shadowOf(Looper.getMainLooper()).idle()
        onView(withText(IDR.string.dashboard_logout_confirmation_message))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
        assertThat(showingAlertDialogs()).hasSize(1)

        mvi.state.update { it.copy(isLogoutConfirmationVisible = false) }
        shadowOf(Looper.getMainLooper()).idle()
        assertThat(showingAlertDialogs()).isEmpty()
        verify(exactly = 0) { viewModel.onAction(AboutUiAction.LogoutConfirmationDismissed) }
    }

    @Test
    fun `the logout confirmation is shown again after the activity is re-created`() {
        givenLoaded(isScannerVersionVisible = false)
        mvi.state.update { it.copy(isLogoutConfirmationVisible = true) }
        val scenario = launchRecreatableInHiltContainer { AboutFragment() }
        val first = showingAlertDialogs().single()

        scenario.recreate()
        shadowOf(Looper.getMainLooper()).idle()

        assertThat(scenario.hostedFragment<AboutFragment>()).isNotNull()
        assertThat(first.isShowing).isFalse()
        assertThat(showingAlertDialogs().single()).isNotSameInstanceAs(first)
        verify(exactly = 0) { viewModel.onAction(AboutUiAction.LogoutConfirmationDismissed) }
        scenario.close()
    }

    // AB-5 (Fragment half): the unlock and the single logout run in the ViewModel on PasswordUnlocked(LOGOUT).
    @Test
    fun `should prompt password input when has password and clicking on logout`() {
        givenLoaded(isScannerVersionVisible = false)
        logoutClickAnswersWith(AboutUiEffect.ShowPasswordDialog(PASSWORD, AboutGatedAction.LOGOUT))
        val navController = testNavController(R.navigation.graph_dashboard, R.id.aboutFragment)

        launchFragmentInHiltContainer<AboutFragment>(navController = navController)
        onView(withText(IDR.string.dashboard_preference_logout_title)).perform(click())
        onView(withText(IDR.string.dashboard_password_lock_title_default))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
        enterSettingsPassword(PASSWORD)

        verify(exactly = 1) { viewModel.onAction(AboutUiAction.PasswordUnlocked(AboutGatedAction.LOGOUT)) }
        verifyOrder {
            viewModel.onAction(AboutUiAction.LogoutClicked)
            viewModel.onAction(AboutUiAction.PasswordUnlocked(AboutGatedAction.LOGOUT))
        }
        verify(exactly = 2) { viewModel.onAction(any()) }
    }

    @Test
    fun `should not log out when prompted password and it was incorrect`() {
        givenLoaded(isScannerVersionVisible = false)
        logoutClickAnswersWith(AboutUiEffect.ShowPasswordDialog(PASSWORD, AboutGatedAction.LOGOUT))
        val navController = testNavController(R.navigation.graph_dashboard, R.id.aboutFragment)

        launchFragmentInHiltContainer<AboutFragment>(navController = navController)
        onView(withText(IDR.string.dashboard_preference_logout_title)).perform(click())
        enterSettingsPassword("1111")

        onView(withText(IDR.string.dashboard_password_lock_wrong_pin))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
        assertThat(navController.currentDestination?.id).isEqualTo(R.id.aboutFragment)
        verify(exactly = 1) { viewModel.onAction(AboutUiAction.LogoutClicked) }
        verify(exactly = 1) { viewModel.onAction(any()) }
    }

    // AB-4
    @Test
    fun `clicking sync and search sends a troubleshooting click each time`() {
        givenLoaded(isScannerVersionVisible = false)
        launchFragmentInHiltContainer<AboutFragment>()

        onView(withText(IDR.string.dashboard_preference_sync_and_search_title)).perform(click())
        verify(exactly = 1) { viewModel.onAction(AboutUiAction.TroubleshootingClicked) }

        onView(withText(IDR.string.dashboard_preference_sync_and_search_title)).perform(click())
        verify(exactly = 2) { viewModel.onAction(AboutUiAction.TroubleshootingClicked) }
        verify(exactly = 2) { viewModel.onAction(any()) }
    }

    // AB-4
    @Test
    fun `troubleshooting signal without a password opens troubleshooting`() {
        givenLoaded(isScannerVersionVisible = false)
        mvi.effects.trySend(AboutUiEffect.NavigateToTroubleshooting)
        val navController = testNavController(R.navigation.graph_dashboard, R.id.aboutFragment)

        launchFragmentInHiltContainer<AboutFragment>(navController = navController)

        assertThat(navController.currentDestination?.id).isEqualTo(TroubleshootingR.id.troubleshootingFragment)
        assertThat(navController.previousBackStackEntry?.destination?.id).isEqualTo(R.id.aboutFragment)
    }

    // AB-4
    @Test
    fun `troubleshooting signal with a password asks for it and the correct password unlocks and opens troubleshooting`() {
        givenLoaded(isScannerVersionVisible = false)
        every { viewModel.onAction(AboutUiAction.PasswordUnlocked(AboutGatedAction.TROUBLESHOOTING)) } answers {
            mvi.effects.trySend(AboutUiEffect.NavigateToTroubleshooting)
        }
        mvi.effects.trySend(AboutUiEffect.ShowPasswordDialog(PASSWORD, AboutGatedAction.TROUBLESHOOTING))
        val navController = testNavController(R.navigation.graph_dashboard, R.id.aboutFragment)

        launchFragmentInHiltContainer<AboutFragment>(navController = navController)
        onView(withText(IDR.string.dashboard_password_lock_title_default))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
        assertThat(navController.currentDestination?.id).isEqualTo(R.id.aboutFragment)

        enterSettingsPassword(PASSWORD)
        shadowOf(Looper.getMainLooper()).idle()

        verify(exactly = 1) { viewModel.onAction(AboutUiAction.PasswordUnlocked(AboutGatedAction.TROUBLESHOOTING)) }
        assertThat(navController.currentDestination?.id).isEqualTo(TroubleshootingR.id.troubleshootingFragment)
        verify(exactly = 1) { viewModel.onAction(any()) }
    }

    // AB-4
    @Test
    fun `troubleshooting signal with a wrong password stays on about`() {
        givenLoaded(isScannerVersionVisible = false)
        mvi.effects.trySend(AboutUiEffect.ShowPasswordDialog(PASSWORD, AboutGatedAction.TROUBLESHOOTING))
        val navController = testNavController(R.navigation.graph_dashboard, R.id.aboutFragment)

        launchFragmentInHiltContainer<AboutFragment>(navController = navController)
        enterSettingsPassword("1111")

        onView(withText(IDR.string.dashboard_password_lock_wrong_pin))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
        assertThat(navController.currentDestination?.id).isEqualTo(R.id.aboutFragment)
        verify(exactly = 0) { viewModel.onAction(any()) }
    }

    private fun givenLoaded(isScannerVersionVisible: Boolean) {
        mvi.state.value = AboutUiState(
            syncAndSearchConfig = SyncAndSearchConfig(SYNC, SEARCH),
            isScannerVersionVisible = isScannerVersionVisible,
            lastScannerVersion = SCANNER_VERSION,
            passwordConfig = SettingsPasswordConfig.NotSet,
        )
    }

    // Stands in for the ViewModel: the logout click answers with the effect the real ViewModel would emit.
    private fun logoutClickAnswersWith(effect: AboutUiEffect) {
        every { viewModel.onAction(AboutUiAction.LogoutClicked) } answers { mvi.effects.trySend(effect) }
    }

    // Stands in for the ViewModel without a password: the logout click shows the confirmation through the state.
    private fun logoutClickShowsTheConfirmation() {
        every { viewModel.onAction(AboutUiAction.LogoutClicked) } answers {
            mvi.state.update { it.copy(isLogoutConfirmationVisible = true) }
        }
    }
}
