package com.simprints.feature.dashboard.settings

import android.os.Looper
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceFragmentCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.*
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.contrib.*
import androidx.test.espresso.matcher.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.*
import com.google.common.truth.Truth.*
import com.simprints.feature.dashboard.DashboardActivity
import com.simprints.feature.dashboard.R
import com.simprints.feature.dashboard.tools.MviStubs
import com.simprints.feature.dashboard.tools.enterSettingsPassword
import com.simprints.feature.dashboard.tools.hostedFragment
import com.simprints.feature.dashboard.tools.launchRecreatableInHiltContainer
import com.simprints.feature.dashboard.tools.preference
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
import org.robolectric.shadows.ShadowDialog
import org.robolectric.shadows.ShadowToast
import com.simprints.infra.resources.R as IDR

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class SettingsFragmentTest {
    companion object {
        private val LANGUAGE_OPTIONS = listOf("en", "fr", "pt")
        private const val LANGUAGE = "en"
        private const val PASSWORD = "1234"
        private const val LAST_UPDATED = "5 minutes ago"
        private const val OTHER_LAST_UPDATED = "0 minutes ago"
        private const val LATER_LAST_UPDATED = "7 minutes ago"
    }

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @BindValue
    @JvmField
    internal val viewModel = mockk<SettingsViewModel>(relaxed = true)

    private lateinit var mvi: MviStubs<SettingsUiState, SettingsUiEffect>

    @Before
    fun setUp() {
        mvi = viewModel.stubMvi(SettingsUiState())
    }

    @Test
    fun `should display the toolbar`() {
        givenLoaded(isFingerprint = true)

        launchFragmentInHiltContainer<SettingsFragment>()

        onView(withId(R.id.settingsToolbar)).check(matches(isDisplayed()))
    }

    @Test
    fun `should hide the fingerprint preference if the modalities doesn't contain Fingerprint`() {
        givenLoaded(isFingerprint = false)

        launchFragmentInHiltContainer<SettingsFragment>()

        onView(withText(IDR.string.dashboard_preference_view_fingers_title)).check(doesNotExist())
    }

    @Test
    fun `should display the fingerprint preference if the modalities contains Fingerprint`() {
        givenLoaded(isFingerprint = true)

        launchFragmentInHiltContainer<SettingsFragment>()

        onView(withText(IDR.string.dashboard_preference_view_fingers_title)).check(matches(isDisplayed()))
    }

    @Test
    fun `should redirect to the sync info fragment when clicking on the sync info preference`() {
        givenLoaded(isFingerprint = true)
        val navController = testNavController(R.navigation.graph_dashboard, R.id.settingsFragment)

        launchFragmentInHiltContainer<SettingsFragment>(navController = navController)

        onView(withText(IDR.string.dashboard_preference_sync_information_title)).perform(click())
        assertThat(navController.currentDestination?.id).isEqualTo(R.id.syncInfoFragment)
    }

    @Test
    fun `should redirect to the finger selection fragment when clicking on the finger selection preference`() {
        givenLoaded(isFingerprint = true)
        val navController = testNavController(R.navigation.graph_dashboard, R.id.settingsFragment)

        launchFragmentInHiltContainer<SettingsFragment>(navController = navController)

        onView(withText(IDR.string.dashboard_preference_view_fingers_title)).perform(click())
        assertThat(navController.currentDestination?.id).isEqualTo(R.id.fingerSelectionFragment)
    }

    @Test
    fun `should redirect to the about fragment when clicking on the about preference`() {
        givenLoaded(isFingerprint = false)
        val navController = testNavController(R.navigation.graph_dashboard, R.id.settingsFragment)

        launchFragmentInHiltContainer<SettingsFragment>(navController = navController)

        onView(withId(androidx.preference.R.id.recycler_view)).perform(
            RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                hasDescendant(withText(IDR.string.dashboard_preference_app_details_title)),
            ),
        )
        onView(withText(IDR.string.dashboard_preference_app_details_title)).perform(click())
        assertThat(navController.currentDestination?.id).isEqualTo(R.id.aboutFragment)
    }

    @Test
    fun `should add the selected language as the summary of the language preference`() {
        givenLoaded(isFingerprint = false)

        launchFragmentInHiltContainer<SettingsFragment>()

        onView(withText("English")).check(matches(isDisplayed()))
    }

    // ST-8
    @Test
    fun `toolbar up goes back to the main screen`() {
        givenLoaded(isFingerprint = false)
        val navController = testNavController(R.navigation.graph_dashboard, R.id.mainFragment).also {
            it.navigate(R.id.action_mainFragment_to_settingsFragment)
            assertThat(it.currentDestination?.id).isEqualTo(R.id.settingsFragment)
        }
        launchFragmentInHiltContainer<SettingsFragment>(navController = navController)

        onView(allOf(isAssignableFrom(ImageButton::class.java), withParent(withId(R.id.settingsToolbar)))).perform(click())

        assertThat(navController.currentDestination?.id).isEqualTo(R.id.mainFragment)
    }

    // D-3
    @Test
    fun `nothing loaded yet keeps every preference at its xml default`() {
        lateinit var fragment: SettingsFragment
        launchFragmentInHiltContainer<SettingsFragment> { fragment = this as SettingsFragment }

        assertThat(fragment.preference(R.string.preference_select_fingers_key).isVisible).isFalse()
        assertThat(fragment.preference(R.string.preference_enable_audio_on_scan_complete_key).isVisible).isTrue()
        assertThat(fragment.preference(R.string.preference_enable_face_auto_capture).isVisible).isFalse()
        assertThat(fragment.preference(R.string.preference_select_language_key).summary).isNull()
        assertThat(fragment.updateConfigSummary()).isEqualTo(fragment.getString(IDR.string.dashboard_preference_summary_update_config))
    }

    // ST-1
    @Test
    fun `face auto capture preference is shown only when the face configuration has auto capture`() {
        givenLoaded(isFingerprint = false)
        lateinit var fragment: SettingsFragment
        launchFragmentInHiltContainer<SettingsFragment> { fragment = this as SettingsFragment }

        mvi.state.update { it.copy(isFaceAutoCaptureVisible = true) }
        assertThat(fragment.preference(R.string.preference_enable_face_auto_capture).isVisible).isTrue()
        onView(withText(IDR.string.dashboard_preference_enable_auto_capture)).check(matches(isDisplayed()))

        mvi.state.update { it.copy(isFaceAutoCaptureVisible = false) }
        assertThat(fragment.preference(R.string.preference_enable_face_auto_capture).isVisible).isFalse()
        onView(withText(IDR.string.dashboard_preference_enable_auto_capture)).check(doesNotExist())
    }

    // ST-2
    @Test
    fun `audio alert preference follows the fingerprint modality`() {
        givenLoaded(isFingerprint = false)
        lateinit var fragment: SettingsFragment
        launchFragmentInHiltContainer<SettingsFragment> { fragment = this as SettingsFragment }

        // XML default is visible: no fingerprint hides it.
        assertThat(fragment.preference(R.string.preference_enable_audio_on_scan_complete_key).isVisible).isFalse()
        onView(withText(IDR.string.dashboard_preference_enable_audio_on_scan_complete)).check(doesNotExist())

        mvi.state.update { it.copy(isFingerprintSettingsVisible = true) }
        shadowOf(Looper.getMainLooper()).idle()

        assertThat(fragment.preference(R.string.preference_enable_audio_on_scan_complete_key).isVisible).isTrue()
        scrollToPreference(IDR.string.dashboard_preference_enable_audio_on_scan_complete)
        onView(withText(IDR.string.dashboard_preference_enable_audio_on_scan_complete)).check(matches(isDisplayed()))
    }

    // ST-11
    @Test
    fun `losing the fingerprint modality on a re-render hides finger selection and audio alert`() {
        givenLoaded(isFingerprint = true)
        lateinit var fragment: SettingsFragment
        launchFragmentInHiltContainer<SettingsFragment> { fragment = this as SettingsFragment }
        assertThat(fragment.preference(R.string.preference_select_fingers_key).isVisible).isTrue()
        assertThat(fragment.preference(R.string.preference_enable_audio_on_scan_complete_key).isVisible).isTrue()

        mvi.state.update { it.copy(isFingerprintSettingsVisible = false) }
        shadowOf(Looper.getMainLooper()).idle()

        assertThat(fragment.preference(R.string.preference_select_fingers_key).isVisible).isFalse()
        assertThat(fragment.preference(R.string.preference_enable_audio_on_scan_complete_key).isVisible).isFalse()
        onView(withText(IDR.string.dashboard_preference_view_fingers_title)).check(doesNotExist())
    }

    // ST-3
    @Test
    fun `language without a password shows the configured languages and sends the selection without relaunching`() {
        givenLoaded(isFingerprint = false)
        every { viewModel.onAction(SettingsUiAction.LanguageClicked) } answers {
            mvi.state.update { it.copy(languageOptions = listOf("en", "fr", "xx"), isLanguageDialogVisible = true) }
        }
        lateinit var fragment: SettingsFragment
        launchFragmentInHiltContainer<SettingsFragment> { fragment = this as SettingsFragment }

        onView(withText(IDR.string.dashboard_preference_select_language_title)).perform(click())
        shadowOf(Looper.getMainLooper()).idle()

        val dialog = languageDialogs().single()
        assertThat(dialog.items()).containsExactly("English", "French").inOrder()
        assertThat(dialog.listView.checkedItemPosition).isEqualTo(0)

        onView(withText("French")).inRoot(RootMatchers.isDialog()).perform(click())
        shadowOf(Looper.getMainLooper()).idle()

        // KI-3 fix: the summary and the relaunch wait for the save, which the ViewModel confirms through state and RelaunchApp.
        verify(exactly = 1) { viewModel.onAction(SettingsUiAction.LanguageSelected("fr")) }
        assertThat(dialog.isShowing).isFalse()
        assertThat(fragment.preference(R.string.preference_select_language_key).summary).isEqualTo("English")
        val activity = fragment.requireActivity()
        assertThat(activity.isFinishing).isFalse()
        assertThat(shadowOf(activity).nextStartedActivity).isNull()
        verifyOrder {
            viewModel.onAction(SettingsUiAction.ScreenStarted)
            viewModel.onAction(SettingsUiAction.LanguageClicked)
            viewModel.onAction(SettingsUiAction.LanguageSelected("fr"))
        }
        // The selection is the only report: closing the dialog on a pick is not a cancel
        verify(exactly = 0) { viewModel.onAction(SettingsUiAction.LanguageDialogDismissed) }
        verify(exactly = 3) { viewModel.onAction(any()) }
    }

    @Test
    fun `the language dialog follows the state and a cancel reports the dismissal`() {
        givenLoaded(isFingerprint = false)
        launchFragmentInHiltContainer<SettingsFragment>()
        assertThat(languageDialogs()).isEmpty()

        mvi.state.update { it.copy(isLanguageDialogVisible = true) }
        shadowOf(Looper.getMainLooper()).idle()
        val dialog = languageDialogs().single()
        assertThat(dialog.items()).containsExactly("English", "French").inOrder()

        mvi.state.update { it.copy(isLanguageDialogVisible = false) }
        shadowOf(Looper.getMainLooper()).idle()
        assertThat(languageDialogs()).isEmpty()
        // Hidden by the state, not by the user: no dismissal is reported
        verify(exactly = 0) { viewModel.onAction(SettingsUiAction.LanguageDialogDismissed) }

        mvi.state.update { it.copy(isLanguageDialogVisible = true) }
        shadowOf(Looper.getMainLooper()).idle()
        languageDialogs().single().cancel() // back or a touch outside
        shadowOf(Looper.getMainLooper()).idle()

        assertThat(languageDialogs()).isEmpty()
        verify(exactly = 1) { viewModel.onAction(SettingsUiAction.LanguageDialogDismissed) }
        verify(exactly = 0) { viewModel.onAction(ofType<SettingsUiAction.LanguageSelected>()) }
    }

    @Test
    fun `the language dialog is shown again after the activity is re-created`() {
        givenLoaded(isFingerprint = false)
        mvi.state.update { it.copy(isLanguageDialogVisible = true) }
        val scenario = launchRecreatableSettings()
        val first = languageDialogs().single()

        scenario.recreate()
        shadowOf(Looper.getMainLooper()).idle()

        assertThat(first.isShowing).isFalse()
        val recreated = languageDialogs().single()
        assertThat(recreated).isNotSameInstanceAs(first)
        assertThat(recreated.items()).containsExactly("English", "French").inOrder()
        // Closing the old window with the view is not a user dismissal
        verify(exactly = 0) { viewModel.onAction(SettingsUiAction.LanguageDialogDismissed) }
        scenario.close()
    }

    // KI-3 fix
    @Test
    fun `saved language is shown and the app relaunches only on the relaunch effect`() {
        givenLoaded(isFingerprint = false)
        lateinit var fragment: SettingsFragment
        launchFragmentInHiltContainer<SettingsFragment> { fragment = this as SettingsFragment }
        val activity = fragment.requireActivity()
        assertThat(fragment.preference(R.string.preference_select_language_key).summary).isEqualTo("English")

        mvi.state.update { it.copy(languageCode = "fr") }
        shadowOf(Looper.getMainLooper()).idle()

        assertThat(fragment.preference(R.string.preference_select_language_key).summary).isEqualTo("French")
        assertThat(activity.isFinishing).isFalse()

        mvi.effects.trySend(SettingsUiEffect.RelaunchApp)
        shadowOf(Looper.getMainLooper()).idle()

        assertThat(activity.isFinishing).isTrue()
        assertThat(shadowOf(activity).nextStartedActivity.component?.className).isEqualTo(DashboardActivity::class.java.name)
    }

    // ST-4
    @Test
    fun `language with a password asks for it and the correct password unlocks and opens the language dialog`() {
        givenLoaded(isFingerprint = false)
        languageClickAnswersWith(SettingsUiEffect.ShowPasswordDialog(PASSWORD, SettingsGatedAction.LANGUAGE))
        every { viewModel.onAction(SettingsUiAction.PasswordUnlocked(SettingsGatedAction.LANGUAGE)) } answers {
            mvi.state.update { it.copy(isLanguageDialogVisible = true) }
        }
        launchFragmentInHiltContainer<SettingsFragment>()

        onView(withText(IDR.string.dashboard_preference_select_language_title)).perform(click())

        onView(withText(IDR.string.dashboard_password_lock_title_default))
            .inRoot(RootMatchers.isDialog())
            .check(matches(isDisplayed()))
        assertThat(languageDialogs()).isEmpty()

        enterSettingsPassword(PASSWORD)
        shadowOf(Looper.getMainLooper()).idle()

        verify(exactly = 1) { viewModel.onAction(SettingsUiAction.PasswordUnlocked(SettingsGatedAction.LANGUAGE)) }
        assertThat(languageDialogs().single().items()).containsExactly("English", "French").inOrder()
    }

    // ST-4
    @Test
    fun `language with a password and a wrong password does not unlock or open the language dialog`() {
        givenLoaded(isFingerprint = false)
        languageClickAnswersWith(SettingsUiEffect.ShowPasswordDialog(PASSWORD, SettingsGatedAction.LANGUAGE))
        launchFragmentInHiltContainer<SettingsFragment>()

        onView(withText(IDR.string.dashboard_preference_select_language_title)).perform(click())
        enterSettingsPassword("1111")
        shadowOf(Looper.getMainLooper()).idle()

        onView(withText(IDR.string.dashboard_password_lock_wrong_pin))
            .inRoot(RootMatchers.isDialog())
            .check(matches(isDisplayed()))
        assertThat(languageDialogs()).isEmpty()
        verify(exactly = 1) { viewModel.onAction(SettingsUiAction.LanguageClicked) }
        verify(exactly = 0) { viewModel.onAction(ofType<SettingsUiAction.PasswordUnlocked>()) }
        verify(exactly = 0) { viewModel.onAction(ofType<SettingsUiAction.LanguageSelected>()) }
    }

    // ST-5
    @Test
    fun `update config shows scheduled then the finished toast and the last updated time`() {
        givenLoaded(isFingerprint = false)
        every { viewModel.onAction(SettingsUiAction.UpdateConfigClicked) } answers {
            mvi.state.update { it.copy(isConfigUpdateScheduled = true) }
        }
        lateinit var fragment: SettingsFragment
        launchFragmentInHiltContainer<SettingsFragment> { fragment = this as SettingsFragment }

        onView(withText(IDR.string.dashboard_preference_update_config_title)).perform(click())

        verify(exactly = 1) { viewModel.onAction(SettingsUiAction.UpdateConfigClicked) }
        assertThat(fragment.updateConfigSummary()).isEqualTo(fragment.getString(IDR.string.dashboard_preference_update_config_scheduled))
        assertThat(ShadowToast.shownToastCount()).isEqualTo(0)

        mvi.effects.trySend(SettingsUiEffect.ConfigUpdated)
        mvi.state.update { it.copy(configLastUpdated = OTHER_LAST_UPDATED, isConfigUpdateScheduled = false) }

        assertThat(ShadowToast.getTextOfLatestToast()).isEqualTo(fragment.getString(IDR.string.dashboard_preference_update_config_finished))
        assertThat(ShadowToast.shownToastCount()).isEqualTo(1)
        assertThat(fragment.updateConfigSummary()).isEqualTo(fragment.lastUpdatedSummary(OTHER_LAST_UPDATED))
    }

    // ST-9 (KI-2 fix)
    @Test
    fun `first render shows the last updated text and asks for a fresh one`() {
        givenLoaded(isFingerprint = false)
        lateinit var fragment: SettingsFragment
        launchFragmentInHiltContainer<SettingsFragment> { fragment = this as SettingsFragment }

        assertThat(fragment.updateConfigSummary()).isEqualTo(fragment.lastUpdatedSummary(LAST_UPDATED))
        verify(exactly = 1) { viewModel.onAction(SettingsUiAction.ScreenStarted) }
        verify(exactly = 1) { viewModel.onAction(any()) }
    }

    // ST-9 (KI-2 fix)
    @Test
    fun `after a back stack round trip the last updated text is still shown and up to date`() {
        givenLoaded(isFingerprint = false)
        lateinit var fragment: SettingsFragment
        lateinit var firstView: View
        lateinit var recreatedView: View
        lateinit var summaryBeforeReturn: String
        launchFragmentInHiltContainer<SettingsFragment> {
            fragment = this as SettingsFragment
            firstView = requireView()
            parentFragmentManager
                .beginTransaction()
                .replace(android.R.id.content, Fragment())
                .addToBackStack(null)
                .commit()
            parentFragmentManager.executePendingTransactions()
            summaryBeforeReturn = fragment.updateConfigSummary()
            // Time passes while the sub-screen is shown: the next ScreenStarted reads a newer text.
            every { viewModel.onAction(SettingsUiAction.ScreenStarted) } answers {
                mvi.state.update { it.copy(configLastUpdated = LATER_LAST_UPDATED) }
            }
            parentFragmentManager.popBackStackImmediate()
            recreatedView = requireView()
        }
        shadowOf(Looper.getMainLooper()).idle()

        assertThat(recreatedView).isNotSameInstanceAs(firstView)
        assertThat(summaryBeforeReturn).isEqualTo(fragment.lastUpdatedSummary(LAST_UPDATED))
        assertThat(fragment.updateConfigSummary()).isEqualTo(fragment.lastUpdatedSummary(LATER_LAST_UPDATED))
        verify(exactly = 2) { viewModel.onAction(SettingsUiAction.ScreenStarted) }
        verify(exactly = 2) { viewModel.onAction(any()) }
    }

    // ST-9 (KI-2 fix)
    @Test
    fun `after the activity is re-created the last updated text is rendered from the surviving state`() {
        givenLoaded(isFingerprint = false)
        val scenario = launchRecreatableSettings()
        val first = scenario.hostedFragment<SettingsFragment>()
        assertThat(first.updateConfigSummary()).isEqualTo(first.lastUpdatedSummary(LAST_UPDATED))

        scenario.recreate()
        shadowOf(Looper.getMainLooper()).idle()

        val recreated = scenario.hostedFragment<SettingsFragment>()
        assertThat(recreated).isNotSameInstanceAs(first)
        assertThat(recreated.updateConfigSummary()).isEqualTo(recreated.lastUpdatedSummary(LAST_UPDATED))
        verify(exactly = 2) { viewModel.onAction(SettingsUiAction.ScreenStarted) }
        scenario.close()
    }

    // ST-9 (KI-2 fix)
    @Test
    fun `a new last updated value is rendered`() {
        givenLoaded(isFingerprint = false)
        lateinit var fragment: SettingsFragment
        launchFragmentInHiltContainer<SettingsFragment> { fragment = this as SettingsFragment }
        assertThat(fragment.updateConfigSummary()).isEqualTo(fragment.lastUpdatedSummary(LAST_UPDATED))

        mvi.state.update { it.copy(configLastUpdated = OTHER_LAST_UPDATED) }

        assertThat(fragment.updateConfigSummary()).isEqualTo(fragment.lastUpdatedSummary(OTHER_LAST_UPDATED))
        assertThat(ShadowToast.shownToastCount()).isEqualTo(0)
    }

    private fun givenLoaded(isFingerprint: Boolean) {
        mvi.state.value = SettingsUiState(
            isFingerprintSettingsVisible = isFingerprint,
            isFaceAutoCaptureVisible = false,
            languageCode = LANGUAGE,
            languageOptions = LANGUAGE_OPTIONS,
            configLastUpdated = LAST_UPDATED,
        )
    }

    // Stands in for the ViewModel: the language click answers with the effect the real ViewModel would emit.
    private fun languageClickAnswersWith(effect: SettingsUiEffect) {
        every { viewModel.onAction(SettingsUiAction.LanguageClicked) } answers { mvi.effects.trySend(effect) }
    }

    private fun scrollToPreference(titleRes: Int) {
        onView(withId(androidx.preference.R.id.recycler_view)).perform(
            RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(hasDescendant(withText(titleRes))),
        )
    }

    private fun launchRecreatableSettings() = launchRecreatableInHiltContainer { SettingsFragment() }

    private fun PreferenceFragmentCompat.updateConfigSummary(): String =
        preference(R.string.preference_update_config_key).summary.toString()

    private fun Fragment.lastUpdatedSummary(sinceLastUpdate: String) =
        getString(IDR.string.dashboard_preference_summary_update_config_last_updated, sinceLastUpdate)

    // The language dialog is the only dialog with a list; the password dialog has none.
    private fun languageDialogs() = ShadowDialog
        .getShownDialogs()
        .filterIsInstance<AlertDialog>()
        .filter { it.isShowing && it.listView != null }

    private fun AlertDialog.items() = (0 until listView.adapter.count).map { listView.adapter.getItem(it).toString() }
}
