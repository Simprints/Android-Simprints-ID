package com.simprints.id.activities.settings.fragments.settingsAbout

import android.preference.Preference
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.id.testtools.TestApplication
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import io.mockk.*
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@Suppress("UsePropertyAccessSyntax")
@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class SettingsAboutPresenterTest {

    companion object {
        const val PREFERENCE_KEY_FOR_SYNC_AND_SEARCH = "select_sync_and_search"
        const val PREFERENCE_KEY_FOR_APP_VERSION = "app_version"
        const val PREFERENCE_KEY_FOR_SCANNER_VERSION = "scanner_version"
        const val PREFERENCE_KEY_FOR_LOGOUT = "finishSettings"
        const val PREFERENCE_KEY_FOR_DEVICE_ID = "device_id"
    }

    private lateinit var presenter: SettingsAboutPresenter
    private val viewMock: SettingsAboutContract.View = mockk(relaxed = true)

    @Before
    fun setUp() {
        presenter = SettingsAboutPresenter(viewMock, mockk(relaxed = true))
    }

    @Test
    fun syncAndSearchPreference_loadValue_preferenceShouldHaveValues() {
        presenter = spyk(SettingsAboutPresenter(viewMock, mockk(relaxed = true)))
        val mockPreference: Preference = mockk(relaxed = true)
        every { viewMock.getKeyForSyncAndSearchConfigurationPreference() } returns PREFERENCE_KEY_FOR_SYNC_AND_SEARCH
        every { mockPreference.key } returns PREFERENCE_KEY_FOR_SYNC_AND_SEARCH
        every { presenter.loadSyncAndSearchConfigurationPreference(any()) } returns Unit

        presenter.loadValueAndBindChangeListener(mockPreference)

        verify(exactly = 1) { presenter.loadSyncAndSearchConfigurationPreference(any()) }
    }

    @Test
    fun appVersionPreference_loadValue_preferenceShouldHaveValues() {
        presenter = spyk(SettingsAboutPresenter(viewMock, mockk(relaxed = true)))
        val mockPreference: Preference = mockk(relaxed = true)
        every { viewMock.getKeyForAppVersionPreference() } returns PREFERENCE_KEY_FOR_APP_VERSION
        every { mockPreference.key } returns PREFERENCE_KEY_FOR_APP_VERSION
        every { presenter.loadAppVersionInPreference(any()) } returns Unit

        presenter.loadValueAndBindChangeListener(mockPreference)

        verify(exactly = 1) { presenter.loadAppVersionInPreference(any()) }
    }

    @Test
    fun scannerVersionPreference_loadValue_preferenceShouldHaveValues() {
        presenter = spyk(SettingsAboutPresenter(viewMock, mockk(relaxed = true)))
        val mockPreference: Preference = mockk(relaxed = true)
        every { viewMock.getScannerVersionPreference() } returns mockPreference
        every { viewMock.getKeyForScannerVersionPreference() } returns PREFERENCE_KEY_FOR_SCANNER_VERSION
        every { mockPreference.key } returns PREFERENCE_KEY_FOR_SCANNER_VERSION
        every { presenter.loadScannerVersionInPreference(any()) } returns Unit

        presenter.loadValueAndBindChangeListener(mockPreference)

        verify(exactly = 1) { presenter.loadScannerVersionInPreference(any()) }
    }

    @Test
    fun deviceIdPreference_loadValue_preferenceShouldHaveValues() {
        presenter = spyk(SettingsAboutPresenter(viewMock, mockk(relaxed = true)))
        val mockPreference: Preference = mockk(relaxed = true)
        every { viewMock.getDeviceIdPreference() } returns mockPreference
        every { viewMock.getKeyForDeviceIdPreference() } returns PREFERENCE_KEY_FOR_DEVICE_ID
        every { mockPreference.key } returns PREFERENCE_KEY_FOR_DEVICE_ID
        every { presenter.loadDeviceIdInPreference(any()) } returns Unit

        presenter.loadValueAndBindChangeListener(mockPreference)

        verify(exactly = 1) { presenter.loadDeviceIdInPreference(any()) }
    }

    @Test
    fun logoutPreference_bindClickListener_shouldHaveListenerBounded() {
        val mockPreference: Preference = mockk(relaxed = true)
        every { viewMock.getLogoutPreference() } returns mockPreference
        every { viewMock.getKeyForLogoutPreference() } returns PREFERENCE_KEY_FOR_LOGOUT
        every { mockPreference.key } returns PREFERENCE_KEY_FOR_LOGOUT

        presenter.loadValueAndBindChangeListener(mockPreference)

        verify(exactly = 1) { mockPreference.setOnPreferenceClickListener(any()) }
    }

    @Test
    fun logoutPreference_userClicksOnIt_viewShouldShowDialog() {
        val mockPreference: Preference = mockk(relaxed = true)
        every { viewMock.getLogoutPreference() } returns mockPreference
        every { viewMock.getKeyForLogoutPreference() } returns PREFERENCE_KEY_FOR_LOGOUT
        every { mockPreference.key } returns PREFERENCE_KEY_FOR_LOGOUT
        var actionForLogoutPreference: Preference.OnPreferenceClickListener? = null
        every { mockPreference.setOnPreferenceClickListener(any()) } answers {
            actionForLogoutPreference = this.args.first() as Preference.OnPreferenceClickListener
            null
        }

        presenter.loadValueAndBindChangeListener(mockPreference)

        actionForLogoutPreference?.let {
            it.onPreferenceClick(mockPreference)
            verify(exactly = 1) { viewMock.showConfirmationDialogForLogout() }
        } ?: fail("Action for logout preference not set.")
    }

    @Test
    fun presenterLogout_dbManagerShouldSignOut() = runBlockingTest {
        mockDepsForLogout(presenter)

        presenter.logout()

        coVerify { presenter.signerManager.signOut() }
    }

    @Test
    fun presenterLogout_downSyncWorkersAreCancelled() = runBlockingTest {
        mockDepsForLogout(presenter)

        presenter.logout()

        coVerify { presenter.syncSchedulerHelper.cancelBackgroundSyncs() }
    }

    @Test
    fun presenterLogout_longConsentsAreDeleted() = runBlockingTest {
        mockDepsForLogout(presenter)

        presenter.logout()

        verify(exactly = 1) { presenter.longConsentManager.deleteLongConsents() }
    }

    @Test
    fun presenterLogout_sessionsManagerSignsOut() = runBlockingTest {
        mockDepsForLogout(presenter)

        presenter.logout()

        verify(exactly = 1) { presenter.sessionEventManager.signOut() }
    }

    private fun mockDepsForLogout(presenter: SettingsAboutPresenter) {
        presenter.signerManager = mockk(relaxed = true)
        presenter.syncSchedulerHelper = mockk(relaxed = true)
        presenter.longConsentManager = mockk(relaxed = true)
        presenter.sessionEventManager = mockk(relaxed = true)
    }
}
