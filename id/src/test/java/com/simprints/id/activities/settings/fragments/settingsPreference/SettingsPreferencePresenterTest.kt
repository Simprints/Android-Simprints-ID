package com.simprints.id.activities.settings.fragments.settingsPreference

import android.preference.ListPreference
import android.preference.MultiSelectListPreference
import android.preference.Preference
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockito_kotlin.*
import com.simprints.id.activities.ShadowAndroidXMultiDex
import com.simprints.id.di.DaggerForTests
import com.simprints.id.testUtils.base.RxJavaTest
import com.simprints.id.testUtils.roboletric.TestApplication
import junit.framework.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
import org.robolectric.annotation.Config

@Suppress("UsePropertyAccessSyntax")
@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class SettingsPreferencePresenterTest: RxJavaTest, DaggerForTests() {

    companion object {
        const val PREFERENCE_KEY_FOR_MODULE = "select_modules"
        const val PREFERENCE_KEY_FOR_LANGUAGES = "select_language"
        const val PREFERENCE_KEY_FOR_FINGERS = "select_fingers"
        const val PREFERENCE_KEY_FOR_SYNC_AND_SEARCH = "select_sync_and_search"
        const val PREFERENCE_KEY_FOR_APP_VERSION = "app_version"
        const val PREFERENCE_KEY_FOR_SCANNER_VERSION = "scanner_version"
        const val PREFERENCE_KEY_FOR_LOGOUT = "finishSettings"
        const val PREFERENCE_KEY_FOR_DEVICE_ID = "device_id"
    }

    lateinit var presenter: SettingsPreferencePresenter
    private val viewMock: SettingsPreferenceContract.View = mock(SettingsPreferenceContract.View::class.java)

    @Before
    override fun setUp() {
        presenter = spy(SettingsPreferencePresenter(viewMock, mock()))
    }

    @Test
    fun languagePreference_bindChangeListener_preferenceShouldHaveListenerBoundedAndValues() {
        val mockPreference = mock(ListPreference::class.java)
        whenever(viewMock.getPreferenceForLanguage()).thenReturn(mockPreference)
        whenever(viewMock.getKeyForLanguagePreference()).thenReturn(PREFERENCE_KEY_FOR_LANGUAGES)
        whenever(mockPreference.key).thenReturn(PREFERENCE_KEY_FOR_LANGUAGES)
        doNothing().whenever(presenter).loadLanguagePreference(any())

        presenter.loadValueAndBindChangeListener(mockPreference)

        verify(mockPreference, times(1)).setOnPreferenceChangeListener(any())
        verify(presenter, times(1)).loadLanguagePreference(any())
    }

    @Test
    fun defaultFingersPreference_bindChangeListener_preferenceShouldHaveListenerBoundedAndValues() {
        val mockPreference = mock(MultiSelectListPreference::class.java)
        whenever(viewMock.getPreferenceForDefaultFingers()).thenReturn(mockPreference)
        whenever(viewMock.getKeyForDefaultFingersPreference()).thenReturn(PREFERENCE_KEY_FOR_FINGERS)
        whenever(mockPreference.key).thenReturn(PREFERENCE_KEY_FOR_FINGERS)
        doNothing().whenever(presenter).loadDefaultFingersPreference(any())

        presenter.loadValueAndBindChangeListener(mockPreference)

        verify(mockPreference, times(1)).setOnPreferenceChangeListener(any())
        verify(presenter, times(1)).loadDefaultFingersPreference(any())
    }

    @Test
    fun modulePreference_loadValueAndBindChangeListener_preferenceShouldHaveListenerBoundedAndValues() {
        val mockPreference = mock(MultiSelectListPreference::class.java)
        whenever(viewMock.getPreferenceForSelectModules()).thenReturn(mockPreference)
        whenever(viewMock.getKeyForSelectModulesPreference()).thenReturn(PREFERENCE_KEY_FOR_MODULE)
        whenever(mockPreference.key).thenReturn(PREFERENCE_KEY_FOR_MODULE)
        doNothing().whenever(presenter).loadSelectModulesPreference(any())

        presenter.loadValueAndBindChangeListener(mockPreference)

        verify(mockPreference, times(1)).setOnPreferenceChangeListener(any())
        verify(presenter, times(1)).loadSelectModulesPreference(any())
    }

    @Test
    fun syncAndSearchPreference_loadValue_preferenceShouldHaveValues() {
        val mockPreference = mock(Preference::class.java)
        whenever(viewMock.getKeyForSyncAndSearchConfigurationPreference()).thenReturn(PREFERENCE_KEY_FOR_SYNC_AND_SEARCH)
        whenever(mockPreference.key).thenReturn(PREFERENCE_KEY_FOR_SYNC_AND_SEARCH)
        doNothing().whenever(presenter).loadSyncAndSearchConfigurationPreference(any())

        presenter.loadValueAndBindChangeListener(mockPreference)

        verify(presenter, times(1)).loadSyncAndSearchConfigurationPreference(any())
    }


    @Test
    fun appVersionPreference_loadValue_preferenceShouldHaveValues() {
        val mockPreference = mock(Preference::class.java)
        whenever(viewMock.getKeyForAppVersionPreference()).thenReturn(PREFERENCE_KEY_FOR_APP_VERSION)
        whenever(mockPreference.key).thenReturn(PREFERENCE_KEY_FOR_APP_VERSION)
        doNothing().whenever(presenter).loadAppVersionInPreference(any())

        presenter.loadValueAndBindChangeListener(mockPreference)

        verify(presenter, times(1)).loadAppVersionInPreference(any())
    }

    @Test
    fun scannerVersionPreference_loadValue_preferenceShouldHaveValues() {
        val mockPreference = mock(Preference::class.java)
        whenever(viewMock.getScannerVersionPreference()).thenReturn(mockPreference)
        whenever(viewMock.getKeyForScannerVersionPreference()).thenReturn(PREFERENCE_KEY_FOR_SCANNER_VERSION)
        whenever(mockPreference.key).thenReturn(PREFERENCE_KEY_FOR_SCANNER_VERSION)
        doNothing().whenever(presenter).loadScannerVersionInPreference(any())

        presenter.loadValueAndBindChangeListener(mockPreference)

        verify(presenter, times(1)).loadScannerVersionInPreference(any())
    }

    @Test
    fun deviceIdPreference_loadValue_preferenceShouldHaveValues() {
        val mockPreference = mock(Preference::class.java)
        whenever(viewMock.getDeviceIdPreference()).thenReturn(mockPreference)
        whenever(viewMock.getKeyForDeviceIdPreference()).thenReturn(PREFERENCE_KEY_FOR_DEVICE_ID)
        whenever(mockPreference.key).thenReturn(PREFERENCE_KEY_FOR_DEVICE_ID)
        doNothing().whenever(presenter).loadDeviceIdInPreference(any())

        presenter.loadValueAndBindChangeListener(mockPreference)

        verify(presenter, times(1)).loadDeviceIdInPreference(any())
    }

    @Test
    fun logoutPreference_bindClickListener_shouldHaveListenerBounded() {
        val mockPreference = mock(Preference::class.java)
        whenever(viewMock.getLogoutPreference()).thenReturn(mockPreference)
        whenever(viewMock.getKeyForLogoutPreference()).thenReturn(PREFERENCE_KEY_FOR_LOGOUT)
        whenever(mockPreference.key).thenReturn(PREFERENCE_KEY_FOR_LOGOUT)

        presenter.loadValueAndBindChangeListener(mockPreference)

        verify(mockPreference, times(1)).setOnPreferenceClickListener (any())
    }

    @Test
    fun logoutPreference_userClicksOnIt_viewShouldShowDialog() {
        val mockPreference = mock(Preference::class.java)
        whenever(viewMock.getLogoutPreference()).thenReturn(mockPreference)
        whenever(viewMock.getKeyForLogoutPreference()).thenReturn(PREFERENCE_KEY_FOR_LOGOUT)
        whenever(mockPreference.key).thenReturn(PREFERENCE_KEY_FOR_LOGOUT)
        var actionForLogoutPreference: Preference.OnPreferenceClickListener? = null
        Mockito.doAnswer {
            actionForLogoutPreference = it.arguments.first() as Preference.OnPreferenceClickListener
            null
        }.whenever(mockPreference).setOnPreferenceClickListener(any())

        presenter.loadValueAndBindChangeListener(mockPreference)

        actionForLogoutPreference?.let {
            it.onPreferenceClick(mockPreference)
            verify(viewMock, times(1)).showConfirmationDialogForLogout()
        } ?: fail("Action for logout preference not set.")
    }
}
