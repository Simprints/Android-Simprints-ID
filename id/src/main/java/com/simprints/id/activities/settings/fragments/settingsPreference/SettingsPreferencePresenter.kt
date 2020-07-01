package com.simprints.id.activities.settings.fragments.settingsPreference

import android.preference.ListPreference
import android.preference.MultiSelectListPreference
import android.preference.Preference
import com.simprints.core.tools.utils.LanguageHelper
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.crashreport.CrashReportTag
import com.simprints.id.data.analytics.crashreport.CrashReportTrigger
import com.simprints.id.data.db.subject.domain.FingerIdentifier
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.di.AppComponent
import com.simprints.id.services.scheduledSync.SyncManager
import javax.inject.Inject


class SettingsPreferencePresenter(private val view: SettingsPreferenceContract.View,
                                  component: AppComponent) :
    SettingsPreferenceContract.Presenter {

    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var syncSchedulerHelper: SyncManager
    @Inject lateinit var crashReportManager: CrashReportManager

    init {
        component.inject(this)
    }

    override fun start() {
        configureAvailableLanguageEntriesFromProjectLanguages()
        loadPreferenceValuesAndBindThemToChangeListeners()
    }

    private fun configureAvailableLanguageEntriesFromProjectLanguages() {
        val availableLanguages = preferencesManager.projectLanguages
        val languageCodeToName = view.getLanguageCodeAndNamePairs()

        val (availableLanguageNames, availableLanguageCodes) = computeAvailableLanguageNamesAndCodes(availableLanguages, languageCodeToName)

        val preference = view.getPreferenceForLanguage() as ListPreference
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

    private fun loadPreferenceValuesAndBindThemToChangeListeners() {
        loadValueAndBindChangeListener(view.getPreferenceForLanguage())
        loadValueAndBindChangeListener(view.getPreferenceForDefaultFingers())
        loadValueAndBindChangeListener(view.getPreferenceForAbout())
        loadValueAndBindChangeListener(view.getPreferenceForSyncInformation())
    }

    internal fun loadValueAndBindChangeListener(preference: Preference) {
        when (preference.key) {
            view.getKeyForLanguagePreference() -> {
                loadLanguagePreference(preference as ListPreference)
                preference.setChangeListener { value: String ->
                    handleLanguagePreferenceChanged(preference, value)
                    view.clearActivityStackAndRelaunchApp()
                }
            }

            view.getKeyForDefaultFingersPreference() -> {
                loadDefaultFingersPreference(preference as MultiSelectListPreference)
                preference.setChangeListener { value: HashSet<String> -> handleDefaultFingersChanged(preference, value) }
            }
            view.getKeyForSyncInfoPreference() -> {
                preference.setOnPreferenceClickListener {
                    view.openSyncInfoActivity()
                    true
                }
            }
            view.getKeyForAboutPreference() -> {
                preference.setOnPreferenceClickListener {
                    view.openSettingAboutActivity()
                    true
                }
            }
        }
    }

    private inline fun <reified V : Any> Preference.setChangeListener(crossinline listener: (V) -> Unit) {
        onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, value ->
            listener(value as V)
            true
        }
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

    internal fun loadDefaultFingersPreference(preference: MultiSelectListPreference) {
        preference.values = getHashSetFromFingersMap(preferencesManager.fingerStatus).toHashSet()
    }

    private fun handleLanguagePreferenceChanged(listPreference: ListPreference, stringValue: String): Boolean {
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

    private fun handleDefaultFingersChanged(preference: MultiSelectListPreference,
                                            fingersHash: HashSet<String>): Boolean {
        if (selectionContainsDefaultFingers(fingersHash)) {
            preferencesManager.fingerStatus = getMapFromFingersHash(fingersHash)
            logMessageForCrashReport("Default fingers set to ${preferencesManager.fingerStatus}")
            setCrashlyticsKeyForFingers()
        } else {
            view.showToastForInvalidSelectionOfFingers()
            fingersHash.clear()
            fingersHash.addAll(preference.values)
        }
        return true
    }

    private fun selectionContainsDefaultFingers(fingersHash: HashSet<String>): Boolean =
        fingersHash.containsAll(getHashSetFromFingersMap(preferencesManager.getRemoteConfigFingerStatus()))

    private fun getHashSetFromFingersMap(fingersMap: Map<FingerIdentifier, Boolean>) =
        fingersMap.filter { it.value }.keys.map { it.toString() }.toHashSet()

    private fun getMapFromFingersHash(fingersHash: HashSet<String>): Map<FingerIdentifier, Boolean> =
        mutableMapOf<FingerIdentifier, Boolean>().apply {
            fingersHash.map { FingerIdentifier.valueOf(it) }.forEach { this[it] = true }
        }

    private fun logMessageForCrashReport(message: String) {
        crashReportManager.logMessageForCrashReport(CrashReportTag.SETTINGS, CrashReportTrigger.UI, message = message)
    }

    private fun setCrashlyticsKeyForFingers() {
        crashReportManager.setFingersSelectedCrashlyticsKey(preferencesManager.fingerStatus)
    }


}
