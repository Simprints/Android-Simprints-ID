package com.simprints.id.activities.settings.fragments.settingsPreference

import android.preference.ListPreference
import android.preference.MultiSelectListPreference
import android.preference.Preference
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.id.testtools.TestApplication
import com.simprints.testtools.common.syntax.*
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.annotation.Config

@Suppress("UsePropertyAccessSyntax")
@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class SettingsPreferencePresenterTest {

    companion object {
        const val PREFERENCE_KEY_FOR_MODULE = "select_modules"
        const val PREFERENCE_KEY_FOR_LANGUAGES = "select_language"
        const val PREFERENCE_KEY_FOR_FINGERS = "select_fingers"
        const val PREFERENCE_KEY_FOR_ABOUT_APP = "about_app"
    }

    private lateinit var presenter: SettingsPreferencePresenter
    private val viewMock: SettingsPreferenceContract.View = Mockito.mock(SettingsPreferenceContract.View::class.java)

    @Before
    fun setUp() {
        presenter = spy(SettingsPreferencePresenter(viewMock, mock()))
    }

    @Test
    fun languagePreference_bindChangeListener_preferenceShouldHaveListenerBoundedAndValues() {
        val mockPreference = Mockito.mock(ListPreference::class.java)
        whenever(viewMock.getPreferenceForLanguage()).thenReturn(mockPreference)
        whenever(viewMock.getKeyForLanguagePreference()).thenReturn(PREFERENCE_KEY_FOR_LANGUAGES)
        whenever(mockPreference.key).thenReturn(PREFERENCE_KEY_FOR_LANGUAGES)
        whenever(presenter) { loadLanguagePreference(anyNotNull()) } thenDoNothing {}

        presenter.loadValueAndBindChangeListener(mockPreference)

        verifyOnce(mockPreference) { setOnPreferenceChangeListener(anyNotNull()) }
        verifyOnce(presenter) { loadLanguagePreference(anyNotNull()) }
    }

    @Test
    fun defaultFingersPreference_bindChangeListener_preferenceShouldHaveListenerBoundedAndValues() {
        val mockPreference = Mockito.mock(MultiSelectListPreference::class.java)
        whenever(viewMock.getPreferenceForDefaultFingers()).thenReturn(mockPreference)
        whenever(viewMock.getKeyForDefaultFingersPreference()).thenReturn(PREFERENCE_KEY_FOR_FINGERS)
        whenever(mockPreference.key).thenReturn(PREFERENCE_KEY_FOR_FINGERS)
        whenever(presenter) { loadDefaultFingersPreference(anyNotNull()) } thenDoNothing {}

        presenter.loadValueAndBindChangeListener(mockPreference)

        verifyOnce(mockPreference) { setOnPreferenceChangeListener(anyNotNull()) }
        verifyOnce(presenter) { loadDefaultFingersPreference(anyNotNull()) }
    }

    @Test
    fun modulePreference_loadValueAndBindChangeListener_preferenceShouldHaveListenerBoundedAndValues() {
        val mockPreference = Mockito.mock(MultiSelectListPreference::class.java)
        whenever(viewMock.getPreferenceForSelectModules()).thenReturn(mockPreference)
        whenever(viewMock.getKeyForSelectModulesPreference()).thenReturn(PREFERENCE_KEY_FOR_MODULE)
        whenever(mockPreference.key).thenReturn(PREFERENCE_KEY_FOR_MODULE)
        whenever(presenter) { loadSelectModulesPreference(anyNotNull()) } thenDoNothing {}

        presenter.loadValueAndBindChangeListener(mockPreference)

        verifyOnce(mockPreference) { setOnPreferenceChangeListener(anyNotNull()) }
        verifyOnce(presenter) { loadSelectModulesPreference(anyNotNull()) }
    }

    @Test
    fun aboutPreference_userClicksOnIt_shouldStartAboutActivity() {
        val mockPreference = Mockito.mock(Preference::class.java)
        whenever(viewMock.getPreferenceForAbout()).thenReturn(mockPreference)
        whenever(viewMock.getKeyForAboutPreference()).thenReturn(PREFERENCE_KEY_FOR_ABOUT_APP)
        whenever(mockPreference.key).thenReturn(PREFERENCE_KEY_FOR_ABOUT_APP)
        var actionForAboutAppPreference: Preference.OnPreferenceClickListener? = null
        whenever(mockPreference) { setOnPreferenceClickListener(anyNotNull()) } thenAnswer {
            actionForAboutAppPreference = it.arguments.first() as Preference.OnPreferenceClickListener
            null
        }

        presenter.loadValueAndBindChangeListener(mockPreference)

        actionForAboutAppPreference?.let {
            it.onPreferenceClick(mockPreference)
            verifyOnce(viewMock) { openSettingAboutActivity() }
        } ?: Assert.fail("Action for open About Settings preference not set.")
    }
}
