package com.simprints.id.activities.settings.fragments.settingsPreference

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.viewModels
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.simprints.core.tools.extentions.removeAnimationsToNextActivity
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.settings.SettingsActivity
import com.simprints.id.tools.extensions.enablePreference
import com.simprints.id.tools.extensions.runOnUiThreadIfStillRunning
import com.simprints.id.tools.extensions.setChangeListener
import com.simprints.infra.config.domain.models.GeneralConfiguration
import javax.inject.Inject
import com.simprints.infra.resources.R as IDR

class SettingsPreferenceFragment : PreferenceFragmentCompat() {

    @Inject
    lateinit var settingsPreferenceViewModelFactory: SettingsPreferenceViewModelFactory

    private val settingsPreferenceViewModel by viewModels<SettingsPreferenceViewModel> { settingsPreferenceViewModelFactory }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_general)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        val component = (requireActivity().application as Application).component
        component.inject(this)

        setTextInLayout()

        initTextInLayout()

        settingsPreferenceViewModel.generalConfiguration.observe(this) {
            configureAvailableLanguageEntriesFromProjectLanguages(
                it,
                getPreferenceForLanguage(),
                getLanguageCodeAndNamePairs()
            )
            enableSettingsBasedOnModalities(it, getPreferenceForDefaultFingers())
        }
        settingsPreferenceViewModel.languagePreference.observe(this) {
            loadPreferenceValuesAndBindThemToChangeListeners(it)
        }
    }

    private fun loadPreferenceValuesAndBindThemToChangeListeners(language: String) {
        getPreferenceForLanguage()?.apply {
            loadLanguagePreference(language, this)
            this.setChangeListener { value: String ->
                handleLanguagePreferenceChanged(this, value)
                clearActivityStackAndRelaunchApp()
            }
        }
        getPreferenceForDefaultFingers()?.setOnPreferenceClickListener {
            openFingerSelectionActivity()
            true
        }

        getPreferenceForSyncInformation()?.setOnPreferenceClickListener {
            openSyncInfoActivity()
            true
        }

        getPreferenceForAbout()?.setOnPreferenceClickListener {
            openSettingAboutActivity()
            true
        }
    }

    private fun setTextInLayout() {
        getPreferenceForGeneralCategory()?.title = getString(IDR.string.settings_general)
        getPreferenceForAppDetailsCategory()?.title = getString(IDR.string.settings_app_details)
    }

    private fun initTextInLayout() {
        getPreferenceForLanguage()?.title = getString(IDR.string.preference_select_language_title)
        getPreferenceForDefaultFingers()?.apply {
            title = getString(IDR.string.preference_select_fingers_title)
            summary = getString(IDR.string.preference_summary_settings_fingers)
        }

        getPreferenceForSyncInformation()?.apply {
            title = getString(IDR.string.preference_sync_information_title)
            summary = getString(IDR.string.preference_summary_sync_information)
        }

        getPreferenceForAbout()?.title = getString(IDR.string.preference_app_details_title)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            startActivity(Intent(activity, SettingsActivity::class.java))
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getLanguageCodeAndNamePairs(): Map<String, String> {
        val languageCodes = requireActivity().resources.getStringArray(R.array.language_values)
        val languageNames = requireActivity().resources.getStringArray(R.array.language_array)
        return languageCodes.zip(languageNames).toMap()
    }

    private fun getPreferenceForGeneralCategory(): Preference? =
        findPreference(getString(IDR.string.preferences_general_key))

    private fun getKeyForGeneralPreferenceCategory() =
        getString(IDR.string.preferences_general_key)

    private fun getPreferenceForAppDetailsCategory(): Preference? =
        findPreference(getString(IDR.string.preferences_app_details_key))

    private fun getKeyForAppDetailsPreferenceCategory() =
        getString(IDR.string.preferences_app_details_key)

    private fun getPreferenceForLanguage(): ListPreference? =
        findPreference(getString(R.string.preference_select_language_key))

    private fun getPreferenceForDefaultFingers(): Preference? =
        findPreference(getString(R.string.preference_select_fingers_key))

    private fun getPreferenceForAbout(): Preference? =
        findPreference(getString(R.string.preference_app_details_key))

    private fun getPreferenceForSyncInformation(): Preference? =
        findPreference(getString(R.string.preference_sync_info_key))

    private fun loadLanguagePreference(
        language: String,
        preference: ListPreference
    ) {
        preference.value = language
        val index = preference.findIndexOfValue(preference.value)
        preference.summary = if (index >= 0) {
            preference.entries[index]
        } else {
            null
        }
    }

    private fun enableSettingsBasedOnModalities(
        generalConfiguration: GeneralConfiguration,
        defaultFingersPref: Preference?
    ) {
        generalConfiguration.modalities.forEach {
            when (it) {
                GeneralConfiguration.Modality.FINGERPRINT -> enableFingerprintSettings(
                    defaultFingersPref
                )
                GeneralConfiguration.Modality.FACE -> enableFaceSettings()
            }
        }
    }

    private fun enableFingerprintSettings(defaultFingersPref: Preference?) {
        defaultFingersPref?.enablePreference()
    }

    private fun enableFaceSettings() {
        // No face-specific settings yet
    }

    private fun configureAvailableLanguageEntriesFromProjectLanguages(
        generalConfiguration: GeneralConfiguration,
        prefForLanguage: Preference?,
        languageCodeToName: Map<String, String>
    ) {
        val availableLanguages = generalConfiguration.languageOptions

        val (availableLanguageNames, availableLanguageCodes) = computeAvailableLanguageNamesAndCodes(
            availableLanguages,
            languageCodeToName
        )

        val preference = prefForLanguage as ListPreference
        preference.entries = availableLanguageNames.toTypedArray()
        preference.entryValues = availableLanguageCodes.toTypedArray()
    }

    private fun handleLanguagePreferenceChanged(
        listPreference: ListPreference,
        stringValue: String
    ): Boolean {
        val index = listPreference.findIndexOfValue(stringValue)
        settingsPreferenceViewModel.updateLanguagePreference(stringValue)

        listPreference.summary = if (index >= 0) {
            listPreference.entries[index]
        } else {
            null
        }
        return true
    }

    private fun computeAvailableLanguageNamesAndCodes(
        availableLanguages: List<String>,
        languageCodeToName: Map<String, String>
    ): Pair<MutableList<String>, MutableList<String>> {
        val availableLanguageNames = mutableListOf<String>()
        val availableLanguageCodes = mutableListOf<String>()
        availableLanguages.forEach { code ->
            val name = languageCodeToName[code]
            if (name != null) {
                availableLanguageNames.add(name)
                availableLanguageCodes.add(code)
            }
        }

        if (availableLanguageNames.isEmpty()) {
            availableLanguageNames.addAll(languageCodeToName.values)
            availableLanguageCodes.addAll(languageCodeToName.keys)
        }
        return Pair(availableLanguageNames, availableLanguageCodes)
    }

    private fun openFingerSelectionActivity() {
        activity?.runOnUiThreadIfStillRunning {
            (activity as SettingsActivity).openFingerSelectionActivity()
        }
    }

    private fun openSettingAboutActivity() {
        activity?.runOnUiThreadIfStillRunning {
            (activity as SettingsActivity).openSettingAboutActivity()
        }
    }

    private fun openSyncInfoActivity() {
        activity?.runOnUiThreadIfStillRunning {
            (activity as SettingsActivity).openSyncInformationActivity()
        }
    }

    private fun clearActivityStackAndRelaunchApp() {
        activity?.runOnUiThreadIfStillRunning {
            activity?.finishAffinity()
            (activity as SettingsActivity).openCheckLoginFromMainLauncherActivity()
            activity?.removeAnimationsToNextActivity()
        }
    }
}
