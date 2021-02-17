package com.simprints.id.activities.settings.fragments.settingsPreference

import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.id.activities.settings.SettingsActivity
import com.simprints.id.commontesttools.di.TestAppModule
import com.simprints.id.testtools.TestApplication
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config

@Suppress("UsePropertyAccessSyntax")
@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
@ExperimentalCoroutinesApi
class SettingsPreferenceFragmentTest {

    companion object {
        const val PREFERENCE_KEY_FOR_LANGUAGES = "select_language"
        const val PREFERENCE_KEY_FOR_ABOUT_APP = "about_app"
    }

    private val app = ApplicationProvider.getApplicationContext() as TestApplication
    private lateinit var settingsActivity: SettingsActivity
    private lateinit var fragment: SettingsPreferenceFragment

    private val module by lazy {
        TestAppModule(app)
    }

    @Before
    fun setUp() {
        UnitTestConfig(this, module).fullSetup()

        settingsActivity = Robolectric.buildActivity(SettingsActivity::class.java)
            .create()
            .start()
            .resume()
            .get()

        val fragmentManager = settingsActivity.supportFragmentManager
        fragment = fragmentManager.findFragmentById(com.simprints.id.R.id.prefContent) as SettingsPreferenceFragment
    }

    @Test
    fun languagePreference_bindChangeListener_preferenceShouldHaveListenerBoundedAndValues() {
        val mockPreference = mockk<ListPreference>(relaxed = true)
        every { fragment.getPreferenceForLanguage() } returns mockPreference
        every { fragment.getKeyForLanguagePreference() } returns PREFERENCE_KEY_FOR_LANGUAGES
        every { mockPreference.key } returns PREFERENCE_KEY_FOR_LANGUAGES
        every { fragment.loadLanguagePreference(any()) } just Runs

        fragment.loadValueAndBindChangeListener(mockPreference)

        verify(atLeast = 1) { mockPreference.setOnPreferenceChangeListener(any()) }
        verify(atLeast = 1) { fragment.loadLanguagePreference(any()) }
    }

    @Test
    fun aboutPreference_userClicksOnIt_shouldStartAboutActivity() {
        val mockPreference = mockk<Preference>()
        every { fragment.getPreferenceForAbout() } returns mockPreference
        every { fragment.getKeyForAboutPreference() } returns PREFERENCE_KEY_FOR_ABOUT_APP
        every { mockPreference.key } returns PREFERENCE_KEY_FOR_ABOUT_APP
        var actionForAboutAppPreference: Preference.OnPreferenceClickListener? = null
        every { mockPreference.setOnPreferenceClickListener(any()) } answers {
            actionForAboutAppPreference = this.args.first() as Preference.OnPreferenceClickListener?
            null
        }

        fragment.loadValueAndBindChangeListener(mockPreference)

        actionForAboutAppPreference?.let {
            it.onPreferenceClick(mockPreference)
            verify(atLeast = 1) { fragment.openSettingAboutActivity() }
        } ?: Assert.fail("Action for open About Settings preference not set.")
    }
}
