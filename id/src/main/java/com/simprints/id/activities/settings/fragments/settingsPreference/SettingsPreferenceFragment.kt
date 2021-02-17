package com.simprints.id.activities.settings.fragments.settingsPreference

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.viewModels
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.simprints.core.tools.extentions.getStringArray
import com.simprints.core.tools.extentions.removeAnimationsToNextActivity
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.settings.SettingsActivity
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.modality.Modality
import com.simprints.id.tools.extensions.enablePreference
import com.simprints.id.tools.extensions.runOnUiThreadIfStillRunning
import com.simprints.id.tools.extensions.setChangeListener
import javax.inject.Inject

class SettingsPreferenceFragment : PreferenceFragmentCompat() {

    @Inject
    lateinit var preferencesManager: PreferencesManager

    @Inject
    lateinit var settingsPreferenceViewModelFactory: SettingsPreferenceViewModelFactory

    val settingsPreferenceViewModel by viewModels<SettingsPreferenceViewModel> { settingsPreferenceViewModelFactory }

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
        configureAvailableLanguageEntriesFromProjectLanguages(getPreferenceForLanguage(), getLanguageCodeAndNamePairs())
        loadPreferenceValuesAndBindThemToChangeListeners()
        enableSettingsBasedOnModalities(getPreferenceForDefaultFingers())
    }

    private fun loadPreferenceValuesAndBindThemToChangeListeners() {
        loadValueAndBindChangeListener(getPreferenceForLanguage())
        loadValueAndBindChangeListener(getPreferenceForDefaultFingers())
        loadValueAndBindChangeListener(getPreferenceForAbout())
        loadValueAndBindChangeListener(getPreferenceForSyncInformation())
    }

    private fun setTextInLayout() {
        getPreferenceForGeneralCategory()?.title = getString(R.string.settings_general)
        getPreferenceForAppDetailsCategory()?.title = getString(R.string.settings_app_details)
    }

    private fun initTextInLayout() {
        getPreferenceForLanguage()?.title = getString(R.string.preference_select_language_title)
        getPreferenceForDefaultFingers()?.apply {
            title = getString(R.string.preference_select_fingers_title)
            summary = getString(R.string.preference_summary_settings_fingers)
        }

        getPreferenceForSyncInformation()?.apply {
            title = getString(R.string.preference_sync_information_title)
            summary = getString(R.string.preference_summary_sync_information)
        }

        getPreferenceForAbout()?.title = getString(R.string.preference_app_details_title)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            startActivity(Intent(activity, SettingsActivity::class.java))
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    fun loadValueAndBindChangeListener(preference: Preference?) {
        when (preference?.key) {
            getKeyForLanguagePreference() -> {
                loadLanguagePreference(preference as ListPreference)
                preference.setChangeListener { value: String ->
                    handleLanguagePreferenceChanged(preference, value)
                    clearActivityStackAndRelaunchApp()
                }
            }
            getKeyForDefaultFingersPreference() -> {
                preference.setOnPreferenceClickListener {
                    openFingerSelectionActivity()
                    true
                }
            }
            getKeyForSyncInfoPreference() -> {
                preference.setOnPreferenceClickListener {
                    openSyncInfoActivity()
                    true
                }
            }
            getKeyForAboutPreference() -> {
                preference.setOnPreferenceClickListener {
                    openSettingAboutActivity()
                    true
                }
            }
        }
    }

    private fun getLanguageCodeAndNamePairs(): Map<String, String> {
        val languageCodes = requireActivity().getStringArray(R.array.language_values)
        val languageNames = requireActivity().getStringArray(R.array.language_array)
        return languageCodes.zip(languageNames).toMap()
    }

    private fun getPreferenceForGeneralCategory(): Preference? =
        findPreference(getKeyForGeneralPreferenceCategory())

    private fun getKeyForGeneralPreferenceCategory() =
        getString(R.string.preferences_general_key)

    private fun getPreferenceForAppDetailsCategory(): Preference? =
        findPreference(getKeyForAppDetailsPreferenceCategory())

    private fun getKeyForAppDetailsPreferenceCategory() =
        getString(R.string.preferences_app_details_key)

    fun getPreferenceForLanguage(): Preference? =
        findPreference(getKeyForLanguagePreference())

    private fun getPreferenceForDefaultFingers(): Preference? =
        findPreference(getKeyForDefaultFingersPreference())

    fun getKeyForLanguagePreference(): String =
        getString(R.string.preference_select_language_key)

    private fun getKeyForDefaultFingersPreference(): String =
        getString(R.string.preference_select_fingers_key)

    fun getPreferenceForAbout(): Preference? =
        findPreference(getKeyForAboutPreference())

    private fun getPreferenceForSyncInformation(): Preference? =
        findPreference(getKeyForSyncInfoPreference())

    fun getKeyForAboutPreference(): String =
        getString(R.string.preference_app_details_key)

    private fun getKeyForSyncInfoPreference(): String =
        getString(R.string.preference_sync_info_key)

    private fun loadLanguagePreference(preference: ListPreference) {
        preference.value = preferencesManager.language
        val index = preference.findIndexOfValue(preference.value)
        preference.summary = if (index >= 0) {
            preference.entries[index]
        } else {
            null
        }
    }

    private fun enableSettingsBasedOnModalities(defaultFingersPref: Preference?) {
        preferencesManager.modalities.forEach {
            when (it) {
                Modality.FINGER -> enableFingerprintSettings(defaultFingersPref)
                Modality.FACE -> enableFaceSettings()
            }
        }
    }

    private fun enableFingerprintSettings(defaultFingersPref: Preference?) {
        defaultFingersPref?.enablePreference()
    }

    private fun enableFaceSettings() {
        // No face-specific settings yet
    }

    private fun configureAvailableLanguageEntriesFromProjectLanguages(prefForLanguage: Preference?, languageCodeToName: Map<String, String>) {
        val availableLanguages = preferencesManager.projectLanguages

        val (availableLanguageNames, availableLanguageCodes) = computeAvailableLanguageNamesAndCodes(availableLanguages, languageCodeToName)

        val preference = prefForLanguage as ListPreference
        preference.entries = availableLanguageNames.toTypedArray()
        preference.entryValues = availableLanguageCodes.toTypedArray()
    }

    private fun handleLanguagePreferenceChanged(listPreference: ListPreference, stringValue: String): Boolean {
        val index = listPreference.findIndexOfValue(stringValue)
        preferencesManager.language = stringValue
        settingsPreferenceViewModel.logMessageForCrashReport("Language set to ${preferencesManager.language}")

        listPreference.summary = if (index >= 0) {
            listPreference.entries[index]
        } else {
            null
        }
        return true
    }

    private fun computeAvailableLanguageNamesAndCodes(availableLanguages: Array<String>, languageCodeToName: Map<String, String>): Pair<MutableList<String>, MutableList<String>> {
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

    fun openSettingAboutActivity() {
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
