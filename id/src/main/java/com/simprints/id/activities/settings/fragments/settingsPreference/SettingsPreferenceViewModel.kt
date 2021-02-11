package com.simprints.id.activities.settings.fragments.settingsPreference

import androidx.lifecycle.ViewModel
import androidx.preference.ListPreference
import androidx.preference.Preference
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.crashreport.CrashReportTag
import com.simprints.id.data.analytics.crashreport.CrashReportTrigger
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.modality.Modality

class SettingsPreferenceViewModel(
    val preferencesManager: PreferencesManager,
    val crashReportManager: CrashReportManager) : ViewModel() {

    fun enableSettingsBasedOnModalities(defaultFingersPref: Preference?) {
        preferencesManager.modalities.forEach {
            when (it) {
                Modality.FINGER -> enableFingerprintSettings(defaultFingersPref)
                Modality.FACE -> enableFaceSettings()
            }
        }
    }

    private fun enableFingerprintSettings(defaultFingersPref: Preference?) {
        enablePreference(defaultFingersPref)
    }

    private fun enableFaceSettings() {
        // No face-specific settings yet
    }

    fun configureAvailableLanguageEntriesFromProjectLanguages(prefForLanguage: Preference?, languageCodeToName: Map<String, String>) {
        val availableLanguages = preferencesManager.projectLanguages

        val (availableLanguageNames, availableLanguageCodes) = computeAvailableLanguageNamesAndCodes(availableLanguages, languageCodeToName)

        val preference = prefForLanguage as ListPreference
        preference.entries = availableLanguageNames.toTypedArray()
        preference.entryValues = availableLanguageCodes.toTypedArray()
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

    internal fun loadLanguagePreference(preference: ListPreference) {
        preference.value = preferencesManager.language
        val index = preference.findIndexOfValue(preference.value)
        preference.summary = if (index >= 0) {
            preference.entries[index]
        } else {
            null
        }
    }

    fun handleLanguagePreferenceChanged(listPreference: ListPreference, stringValue: String): Boolean {
        val index = listPreference.findIndexOfValue(stringValue)
        preferencesManager.language = stringValue
        logMessageForCrashReport("Language set to ${preferencesManager.language}")

        listPreference.summary = if (index >= 0) {
            listPreference.entries[index]
        } else {
            null
        }
        return true
    }

    private fun logMessageForCrashReport(message: String) {
        crashReportManager.logMessageForCrashReport(CrashReportTag.SETTINGS, CrashReportTrigger.UI, message = message)
    }

    private fun enablePreference(preference: Preference?) {
        preference?.isEnabled = true
    }
}
