package com.simprints.id.activities.settings.fragments.settingsPreference

import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.id.testtools.TestApplication
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
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
    private val viewMock: SettingsPreferenceContract.View = mockk(relaxed = true)

    @Before
    fun setUp() {
        presenter = spyk(SettingsPreferencePresenter(viewMock, mockk(relaxed = true)))
    }

    @Test
    fun languagePreference_bindChangeListener_preferenceShouldHaveListenerBoundedAndValues() {
        val mockPreference = mockk<ListPreference>(relaxed = true)
        every { viewMock.getPreferenceForLanguage() } returns mockPreference
        every { viewMock.getKeyForLanguagePreference() } returns PREFERENCE_KEY_FOR_LANGUAGES
        every { mockPreference.key } returns PREFERENCE_KEY_FOR_LANGUAGES
        every { presenter.loadLanguagePreference(any()) } just Runs

        presenter.loadValueAndBindChangeListener(mockPreference)

        verify(atLeast = 1) { mockPreference.setOnPreferenceChangeListener(any()) }
        verify(atLeast = 1) { presenter.loadLanguagePreference(any()) }
    }

    @Test
    fun aboutPreference_userClicksOnIt_shouldStartAboutActivity() {
        val mockPreference = mockk<Preference>()
        every { viewMock.getPreferenceForAbout() } returns mockPreference
        every { viewMock.getKeyForAboutPreference() } returns PREFERENCE_KEY_FOR_ABOUT_APP
        every { mockPreference.key } returns PREFERENCE_KEY_FOR_ABOUT_APP
        var actionForAboutAppPreference: Preference.OnPreferenceClickListener? = null
        every { mockPreference.setOnPreferenceClickListener(any()) } answers {
            actionForAboutAppPreference = this.args.first() as Preference.OnPreferenceClickListener?
            null
        }

        presenter.loadValueAndBindChangeListener(mockPreference)

        actionForAboutAppPreference?.let {
            it.onPreferenceClick(mockPreference)
            verify(atLeast = 1) { viewMock.openSettingAboutActivity() }
        } ?: Assert.fail("Action for open About Settings preference not set.")
    }
}
