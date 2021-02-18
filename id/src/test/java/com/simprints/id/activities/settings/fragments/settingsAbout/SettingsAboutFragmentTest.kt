package com.simprints.id.activities.settings.fragments.settingsAbout

import android.content.DialogInterface.BUTTON_POSITIVE
import androidx.preference.Preference
import androidx.test.core.app.ApplicationProvider
import com.simprints.id.activities.settings.SettingsAboutActivity
import com.simprints.id.commontesttools.di.TestAppModule
import com.simprints.id.testtools.TestApplication
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.robolectric.Robolectric
import org.robolectric.annotation.Config

@Suppress("UsePropertyAccessSyntax")
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
@ExperimentalCoroutinesApi
class SettingsAboutFragmentTest {

    companion object {
        const val PREFERENCE_KEY_FOR_SYNC_AND_SEARCH = "select_sync_and_search"
        const val PREFERENCE_KEY_FOR_APP_VERSION = "app_version"
        const val PREFERENCE_KEY_FOR_SCANNER_VERSION = "scanner_version"
        const val PREFERENCE_KEY_FOR_LOGOUT = "finishSettings"
        const val PREFERENCE_KEY_FOR_DEVICE_ID = "device_id"
    }

    private val app = ApplicationProvider.getApplicationContext() as TestApplication

    lateinit var settingsAboutActivity: SettingsAboutActivity
    lateinit var fragment: SettingsAboutFragment

    private val module by lazy {
        TestAppModule(app)
    }

    @Before
    fun setUp() {
        UnitTestConfig(this, module).fullSetup()

        settingsAboutActivity = Robolectric.buildActivity(SettingsAboutActivity::class.java)
            .create()
            .start()
            .resume()
            .get()

        val fragmentManager = settingsAboutActivity.supportFragmentManager
        fragment = fragmentManager.findFragmentById(com.simprints.id.R.id.prefContent) as SettingsAboutFragment
    }

    @Test
    fun logoutDialogShown_userClicksOk_presenterShouldPerformLogout() = runBlocking {
        fragment.settingsAboutViewModel = spyk(fragment.settingsAboutViewModel)
        coEvery { fragment.settingsAboutViewModel.logout() } coAnswers { }
        val dialog = fragment.buildConfirmationDialogForLogout()

        dialog.show()
        dialog.getButton(BUTTON_POSITIVE).performClick()

        coVerify { fragment.settingsAboutViewModel.logout() }
    }

    @Test
    fun logoutPreference_userClicksOnIt_viewShouldShowDialog() {
        val mockPreference: Preference = mockk(relaxed = true)
        every { fragment.getLogoutPreference() } returns mockPreference
        every { fragment.getKeyForLogoutPreference() } returns PREFERENCE_KEY_FOR_LOGOUT
        every { mockPreference.key } returns PREFERENCE_KEY_FOR_LOGOUT
        var actionForLogoutPreference: Preference.OnPreferenceClickListener? = null
        every { mockPreference.setOnPreferenceClickListener(any()) } answers {
            actionForLogoutPreference = this.args.first() as Preference.OnPreferenceClickListener
        }

        fragment.loadValueAndBindChangeListener(mockPreference)

        actionForLogoutPreference?.let {
            it.onPreferenceClick(mockPreference)
            verify(exactly = 1) { fragment.showConfirmationDialogForLogout() }
        } ?: Assert.fail("Action for logout preference not set.")
    }

    @Test
    fun syncAndSearchPreference_loadValue_preferenceShouldHaveValues() {
        val mockPreference: Preference = mockk(relaxed = true)
        every { fragment.getKeyForSyncAndSearchConfigurationPreference() } returns PREFERENCE_KEY_FOR_SYNC_AND_SEARCH
        every { mockPreference.key } returns PREFERENCE_KEY_FOR_SYNC_AND_SEARCH
        every { fragment.loadSyncAndSearchConfigurationPreference(any()) } returns Unit

        fragment.loadValueAndBindChangeListener(mockPreference)

        verify(exactly = 1) { fragment.loadSyncAndSearchConfigurationPreference(any()) }
    }

    @Test
    fun appVersionPreference_loadValue_preferenceShouldHaveValues() {
        val mockPreference: Preference = mockk(relaxed = true)
        every { fragment.getKeyForAppVersionPreference() } returns PREFERENCE_KEY_FOR_APP_VERSION
        every { mockPreference.key } returns PREFERENCE_KEY_FOR_APP_VERSION
        every { fragment.loadAppVersionInPreference(any(), any()) } returns Unit

        fragment.loadValueAndBindChangeListener(mockPreference)

        verify(exactly = 1) { fragment.loadAppVersionInPreference(any(), any()) }
    }

    @Test
    fun scannerVersionPreference_loadValue_preferenceShouldHaveValues() {
        val mockPreference: Preference = mockk(relaxed = true)
        every { fragment.getScannerVersionPreference() } returns mockPreference
        every { fragment.getKeyForScannerVersionPreference() } returns PREFERENCE_KEY_FOR_SCANNER_VERSION
        every { mockPreference.key } returns PREFERENCE_KEY_FOR_SCANNER_VERSION
        every { fragment.loadScannerVersionInPreference(any()) } returns Unit

        fragment.loadValueAndBindChangeListener(mockPreference)

        verify(exactly = 1) { fragment.loadScannerVersionInPreference(any()) }
    }

    @Test
    fun deviceIdPreference_loadValue_preferenceShouldHaveValues() {
        val mockPreference: Preference = mockk(relaxed = true)
        every { fragment.getDeviceIdPreference() } returns mockPreference
        every { fragment.getKeyForDeviceIdPreference() } returns PREFERENCE_KEY_FOR_DEVICE_ID
        every { mockPreference.key } returns PREFERENCE_KEY_FOR_DEVICE_ID
        every { fragment.loadDeviceIdInPreference(any(), any()) } returns Unit

        fragment.loadValueAndBindChangeListener(mockPreference)

        verify(exactly = 1) { fragment.loadDeviceIdInPreference(any(), any()) }
    }

    @Test
    fun logoutPreference_bindClickListener_shouldHaveListenerBounded() {
        val mockPreference: Preference = mockk(relaxed = true)
        every { fragment.getLogoutPreference() } returns mockPreference
        every { fragment.getKeyForLogoutPreference() } returns PREFERENCE_KEY_FOR_LOGOUT
        every { mockPreference.key } returns PREFERENCE_KEY_FOR_LOGOUT

        fragment.loadValueAndBindChangeListener(mockPreference)

        verify(exactly = 1) { mockPreference.setOnPreferenceClickListener(any()) }
    }
}
