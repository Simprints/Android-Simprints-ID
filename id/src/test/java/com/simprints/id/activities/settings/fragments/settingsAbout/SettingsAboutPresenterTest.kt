package com.simprints.id.activities.settings.fragments.settingsAbout

import android.preference.Preference
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.id.testtools.TestApplication
import com.simprints.testframework.common.syntax.anyNotNull
import com.simprints.testframework.common.syntax.mock
import com.simprints.testframework.common.syntax.verifyOnce
import com.simprints.testframework.common.syntax.whenever
import com.simprints.testframework.unit.robolectric.ShadowAndroidXMultiDex
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
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
    private val viewMock: SettingsAboutContract.View = mock(SettingsAboutContract.View::class.java)

    @Before
    fun setUp() {
        presenter = spy(SettingsAboutPresenter(viewMock, mock()))
    }

    @Test
    fun syncAndSearchPreference_loadValue_preferenceShouldHaveValues() {
        val mockPreference = mock(Preference::class.java)
        whenever(viewMock.getKeyForSyncAndSearchConfigurationPreference()).thenReturn(PREFERENCE_KEY_FOR_SYNC_AND_SEARCH)
        whenever(mockPreference.key).thenReturn(PREFERENCE_KEY_FOR_SYNC_AND_SEARCH)
        whenever(presenter) { loadSyncAndSearchConfigurationPreference(anyNotNull()) } thenDoNothing {}

        presenter.loadValueAndBindChangeListener(mockPreference)

        verifyOnce(presenter) { loadSyncAndSearchConfigurationPreference(anyNotNull()) }
    }

    @Test
    fun appVersionPreference_loadValue_preferenceShouldHaveValues() {
        val mockPreference = mock(Preference::class.java)
        whenever(viewMock.getKeyForAppVersionPreference()).thenReturn(PREFERENCE_KEY_FOR_APP_VERSION)
        whenever(mockPreference.key).thenReturn(PREFERENCE_KEY_FOR_APP_VERSION)
        whenever(presenter) { loadAppVersionInPreference(anyNotNull()) } thenDoNothing {}

        presenter.loadValueAndBindChangeListener(mockPreference)

        verifyOnce(presenter) { loadAppVersionInPreference(anyNotNull()) }
    }

    @Test
    fun scannerVersionPreference_loadValue_preferenceShouldHaveValues() {
        val mockPreference = mock(Preference::class.java)
        whenever(viewMock.getScannerVersionPreference()).thenReturn(mockPreference)
        whenever(viewMock.getKeyForScannerVersionPreference()).thenReturn(PREFERENCE_KEY_FOR_SCANNER_VERSION)
        whenever(mockPreference.key).thenReturn(PREFERENCE_KEY_FOR_SCANNER_VERSION)
        whenever(presenter) { loadScannerVersionInPreference(anyNotNull()) } thenDoNothing {}

        presenter.loadValueAndBindChangeListener(mockPreference)

        verifyOnce(presenter) { loadScannerVersionInPreference(anyNotNull()) }
    }

    @Test
    fun deviceIdPreference_loadValue_preferenceShouldHaveValues() {
        val mockPreference = mock(Preference::class.java)
        whenever(viewMock.getDeviceIdPreference()).thenReturn(mockPreference)
        whenever(viewMock.getKeyForDeviceIdPreference()).thenReturn(PREFERENCE_KEY_FOR_DEVICE_ID)
        whenever(mockPreference.key).thenReturn(PREFERENCE_KEY_FOR_DEVICE_ID)
        whenever(presenter) { loadDeviceIdInPreference(anyNotNull()) } thenDoNothing {}

        presenter.loadValueAndBindChangeListener(mockPreference)

        verifyOnce(presenter) { loadDeviceIdInPreference(anyNotNull()) }
    }

    @Test
    fun logoutPreference_bindClickListener_shouldHaveListenerBounded() {
        val mockPreference = mock(Preference::class.java)
        whenever(viewMock.getLogoutPreference()).thenReturn(mockPreference)
        whenever(viewMock.getKeyForLogoutPreference()).thenReturn(PREFERENCE_KEY_FOR_LOGOUT)
        whenever(mockPreference.key).thenReturn(PREFERENCE_KEY_FOR_LOGOUT)

        presenter.loadValueAndBindChangeListener(mockPreference)

        verifyOnce(mockPreference) { setOnPreferenceClickListener(anyNotNull()) }
    }

    @Test
    fun logoutPreference_userClicksOnIt_viewShouldShowDialog() {
        val mockPreference = mock(Preference::class.java)
        whenever(viewMock.getLogoutPreference()).thenReturn(mockPreference)
        whenever(viewMock.getKeyForLogoutPreference()).thenReturn(PREFERENCE_KEY_FOR_LOGOUT)
        whenever(mockPreference.key).thenReturn(PREFERENCE_KEY_FOR_LOGOUT)
        var actionForLogoutPreference: Preference.OnPreferenceClickListener? = null
        whenever(mockPreference) { setOnPreferenceClickListener(anyNotNull()) } thenAnswer {
            actionForLogoutPreference = it.arguments.first() as Preference.OnPreferenceClickListener
            null
        }

        presenter.loadValueAndBindChangeListener(mockPreference)

        actionForLogoutPreference?.let {
            it.onPreferenceClick(mockPreference)
            verifyOnce(viewMock) { showConfirmationDialogForLogout() }
        } ?: fail("Action for logout preference not set.")
    }
}
