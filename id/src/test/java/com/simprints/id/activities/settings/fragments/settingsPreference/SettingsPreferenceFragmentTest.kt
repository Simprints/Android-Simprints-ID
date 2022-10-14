package com.simprints.id.activities.settings.fragments.settingsPreference

import androidx.fragment.app.testing.launchFragment
import androidx.lifecycle.Observer
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.id.R
import com.simprints.id.testtools.TestApplication
import com.simprints.infra.config.domain.models.GeneralConfiguration
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import com.simprints.infra.resources.R as IDR

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class)
class SettingsPreferenceFragmentTest {

    companion object {
        private const val LANGUAGE_1 = "fr"
        private const val LANGUAGE_2 = "en"
    }

    private val generalConfiguration = mockk<GeneralConfiguration>()
    private val viewModel = mockk<SettingsPreferenceViewModel>()

    @Before
    fun setup() {

        every { generalConfiguration.languageOptions } returns listOf(LANGUAGE_1, LANGUAGE_2)
        every { viewModel.generalConfiguration } returns mockk {
            every { observe(any(), any()) } answers {
                secondArg<Observer<GeneralConfiguration>>().onChanged(generalConfiguration)
            }
        }
        every { viewModel.languagePreference } returns mockk {
            every { observe(any(), any()) } answers {
                secondArg<Observer<String>>().onChanged(LANGUAGE_1)
            }
        }
    }

    @Test
    fun `should display the correct preferences`() {
        launchFragment<SettingsPreferenceFragment>().onFragment { fragment ->
            val generalCategoryPref =
                fragment.findPreference<Preference>(fragment.getString(IDR.string.preferences_general_key))
            assertThat(generalCategoryPref?.title).isEqualTo(fragment.getString(IDR.string.settings_general))

            val appDetailsCategoryPref =
                fragment.findPreference<Preference>(fragment.getString(IDR.string.preferences_app_details_key))
            assertThat(appDetailsCategoryPref?.title).isEqualTo(fragment.getString(IDR.string.settings_app_details))

            val languagePref =
                fragment.findPreference<ListPreference>(fragment.getString(R.string.preference_select_language_key))
            assertThat(languagePref?.title).isEqualTo(fragment.getString(IDR.string.preference_select_language_title))
            assertThat(languagePref?.entries).isEqualTo(arrayOf("French", "English"))
            assertThat(languagePref?.entryValues).isEqualTo(arrayOf("fr", "en"))
            assertThat(languagePref?.value).isEqualTo("fr")
            assertThat(languagePref?.summary).isEqualTo("French")

            val syncInfoPref =
                fragment.findPreference<Preference>(fragment.getString(R.string.preference_sync_info_key))
            assertThat(syncInfoPref?.title).isEqualTo(fragment.getString(IDR.string.preference_sync_information_title))
            assertThat(syncInfoPref?.summary).isEqualTo(fragment.getString(IDR.string.preference_summary_sync_information))

            val aboutPref =
                fragment.findPreference<Preference>(fragment.getString(R.string.preference_app_details_key))
            assertThat(aboutPref?.title).isEqualTo(fragment.getString(IDR.string.preference_app_details_title))
        }
    }

    @Test
    fun `should display the scanner preference for Fingerprint`() {
        every { generalConfiguration.modalities } returns listOf(GeneralConfiguration.Modality.FINGERPRINT)

        launchFragment<SettingsPreferenceFragment>().onFragment { fragment ->
            val scannerPref =
                fragment.findPreference<Preference>(fragment.getString(R.string.preference_select_fingers_key))
            assertThat(scannerPref?.summary).isEqualTo(fragment.getString(IDR.string.preference_summary_settings_fingers))
            assertThat(scannerPref?.title).isEqualTo(fragment.getString(IDR.string.preference_select_fingers_title))
            assertThat(scannerPref?.isEnabled).isTrue()
        }
    }

    @Test
    fun `should not display the scanner preference for Face`() {
        every { generalConfiguration.modalities } returns listOf(GeneralConfiguration.Modality.FACE)

        launchFragment<SettingsPreferenceFragment>().onFragment { fragment ->
            val scannerPref =
                fragment.findPreference<Preference>(fragment.getString(R.string.preference_select_fingers_key))
            assertThat(scannerPref?.isEnabled).isFalse()
        }
    }
}
