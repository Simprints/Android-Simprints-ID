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
import junit.framework.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.annotation.Config


@Suppress("UsePropertyAccessSyntax")
@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class SettingsPreferencePresenterTest: RxJavaTest, DaggerForTests() {

    companion object {
        const val PREFERENCE_KEY_FOR_MODULE = "select_modules"
        const val PREFERENCE_KEY_FOR_LANGUAGES = "select_language"
        const val PREFERENCE_KEY_FOR_FINGERS = "select_fingers"
        const val PREFERENCE_KEY_FOR_ABOUT_APP = "about_app"
    }

    private lateinit var presenter: SettingsPreferencePresenter
    private val viewMock: SettingsPreferenceContract.View = Mockito.mock(SettingsPreferenceContract.View::class.java)

    @Before
    override fun setUp() {
        presenter = Mockito.spy(SettingsPreferencePresenter(viewMock, mock()))
    }

    @Test
    fun languagePreference_bindChangeListener_preferenceShouldHaveListenerBoundedAndValues() {
        val mockPreference = Mockito.mock(ListPreference::class.java)
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
        val mockPreference = Mockito.mock(MultiSelectListPreference::class.java)
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
        val mockPreference = Mockito.mock(MultiSelectListPreference::class.java)
        whenever(viewMock.getPreferenceForSelectModules()).thenReturn(mockPreference)
        whenever(viewMock.getKeyForSelectModulesPreference()).thenReturn(PREFERENCE_KEY_FOR_MODULE)
        whenever(mockPreference.key).thenReturn(PREFERENCE_KEY_FOR_MODULE)
        doNothing().whenever(presenter).loadSelectModulesPreference(any())

        presenter.loadValueAndBindChangeListener(mockPreference)

        verify(mockPreference, times(1)).setOnPreferenceChangeListener(any())
        verify(presenter, times(1)).loadSelectModulesPreference(any())
    }

    @Test
    fun aboutPreference_userClicksOnIt_shouldStartAboutActivity() {
        val mockPreference = Mockito.mock(Preference::class.java)
        whenever(viewMock.getPreferenceForAbout()).thenReturn(mockPreference)
        whenever(viewMock.getKeyForAboutPreference()).thenReturn(PREFERENCE_KEY_FOR_ABOUT_APP)
        whenever(mockPreference.key).thenReturn(PREFERENCE_KEY_FOR_ABOUT_APP)
        var actionForAboutAppPreference: Preference.OnPreferenceClickListener? = null
        Mockito.doAnswer {
            actionForAboutAppPreference = it.arguments.first() as Preference.OnPreferenceClickListener
            null
        }.whenever(mockPreference).setOnPreferenceClickListener(any())

        presenter.loadValueAndBindChangeListener(mockPreference)

        actionForAboutAppPreference?.let {
            it.onPreferenceClick(mockPreference)
            verify(viewMock, times(1)).openSettingAboutActivity()
        } ?: Assert.fail("Action for open About Settings preference not set.")
    }
}
