package com.simprints.id.activities.settings.fragments.settingsPreference

import android.preference.*
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.di.AppComponent
import com.simprints.libsimprints.FingerIdentifier
import javax.inject.Inject


class SettingsPreferencePresenter(private val view: SettingsPreferenceContract.View,
                                  component: AppComponent) :
    SettingsPreferenceContract.Presenter {

    @Inject lateinit var preferencesManager: PreferencesManager

    init {
        component.inject(this)
    }

    override fun start() {
        findPreferencesAndBindThemToValues()
    }

    private fun findPreferencesAndBindThemToValues() {
        bindPreferenceSummaryToValue(view.getPreferenceForLanguage())
        bindPreferenceSummaryToValue(view.getPreferenceForDefaultFingers())
        bindPreferenceSummaryToValue(view.getPreferenceForSyncUponLaunchToggle())
        bindPreferenceSummaryToValue(view.getPreferenceForBackgroundSyncToggle())
        bindPreferenceSummaryToValue(view.getAppVersionPreference())
        bindPreferenceSummaryToValue(view.getScannerVersionPreference())
    }


    private fun bindPreferenceSummaryToValue(preference: Preference) {
        preference.onPreferenceChangeListener = sBindPreferenceSummaryToValueListener

        when (preference) {
            is ListPreference -> sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                    .getDefaultSharedPreferences(preference.context)
                    .getString(preference.key, ""))
            is MultiSelectListPreference -> sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                    .getDefaultSharedPreferences(preference.context)
                    .getStringSet(preference.key, null))
            is SwitchPreference -> sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                    .getDefaultSharedPreferences(preference.context)
                    .getBoolean(preference.key, true))
            else -> sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                    .getDefaultSharedPreferences(preference.context)
                    .getString(preference.key, ""))
        }
    }

    private val sBindPreferenceSummaryToValueListener =
        Preference.OnPreferenceChangeListener { preference, value ->
        when (preference) {
            is ListPreference -> {
                if(preference.key == view.getKeyForLanguagePreference()) {
                    handleLanguagePreferenceChanged(preference, value.toString())
                }
            }
            is MultiSelectListPreference -> {
                if(preference.key == view.getKeyForDefaultFingersPreference()) {
                    handleDefaultFingersChanged(preference, value as HashSet<String>)
                }
            }
            is SwitchPreference -> {
                if (preference.key == view.getKeyForSyncUponLaunchPreference()) {

                }
                else if(preference.key == view.getKeyForBackgroundSyncPreference()) {

                }
            }
            is Preference -> {
                if(preference.key == view.getKeyForAppVersionPreference()) {
                    setAppVersionInPreference(preference)
                }
                else if(preference.key == view.getKeyForScannerVersionPreference()) {
                    setScannerVersionInPreference(preference)
                }
            }
        }
        true
    }

    private fun handleLanguagePreferenceChanged(listPreference: ListPreference, stringValue: String) {
        val index = listPreference.findIndexOfValue(stringValue)
        preferencesManager.language = listPreference.value

        listPreference.summary = if (index >= 0) {
            listPreference.entries[index]
        }
        else {
            null
        }
    }

    private fun handleDefaultFingersChanged(preference: MultiSelectListPreference,
                                            fingersHash: HashSet<String>) {
        if(selectionContainsDefaultFingers(fingersHash)){
            preferencesManager.fingerStatus = getMapFromFingersHash(fingersHash)
            preferencesManager.fingerStatusPersist = true
        }
        else{
            view.showToastForInvalidSelectionOfFingers()
            fingersHash.clear()
            fingersHash.addAll(preference.values)
        }
    }

    private fun selectionContainsDefaultFingers(fingersHash: HashSet<String>): Boolean =
        fingersHash.contains("LEFT_THUMB") && fingersHash.contains("LEFT_INDEX_FINGER")

    private fun setAppVersionInPreference(preference: Preference) {
        preference.summary = preferencesManager.appVersionName
    }

    private fun setScannerVersionInPreference(preference: Preference) {
        preference.summary = preferencesManager.scannerId
    }

    private fun getMapFromFingersHash(fingersHash: HashSet<String>): Map<FingerIdentifier, Boolean> {
        val map = mutableMapOf<FingerIdentifier, Boolean>()
        fingersHash.forEach {
            val fingerIdentifier = FingerIdentifier.valueOf(it)
            map[fingerIdentifier] = true
        }
        return map
    }
}
