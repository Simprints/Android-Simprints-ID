package com.simprints.id.activities.settings.fragments.settingsPreference

import android.content.SharedPreferences
import android.preference.*
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.prefs.settings.SettingsPreferencesManagerImpl
import com.simprints.id.di.AppComponent
import com.simprints.id.tools.serializers.Serializer
import com.simprints.libsimprints.FingerIdentifier
import javax.inject.Inject


class SettingsPreferencePresenter(private val view: SettingsPreferenceContract.View,
                                  component: AppComponent) :
    SettingsPreferenceContract.Presenter {

    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var fingerIdToBooleanSerializer: Serializer<Map<FingerIdentifier, Boolean>>

    init {
        component.inject(this)
    }

    override fun start() {
        findPreferencesAndBindThemToValues()
    }

    private fun findPreferencesAndBindThemToValues() {
        loadAndBindPreferenceSummaryToValue(view.getPreferenceForLanguage())
        loadAndBindPreferenceSummaryToValue(view.getPreferenceForDefaultFingers())
        loadAndBindPreferenceSummaryToValue(view.getPreferenceForSyncUponLaunchToggle())
        loadAndBindPreferenceSummaryToValue(view.getPreferenceForBackgroundSyncToggle())
        loadAndBindPreferenceSummaryToValue(view.getAppVersionPreference())
        loadAndBindPreferenceSummaryToValue(view.getScannerVersionPreference())
    }


    private fun loadAndBindPreferenceSummaryToValue(preference: Preference) {
        loadPreferenceValue(preference)
        bindPreferenceToSummaryValue(preference)
    }

    private fun loadPreferenceValue(preference: Preference) {
        when (preference) {
            is ListPreference -> {
                if (preference.key == view.getKeyForLanguagePreference()) {
                    loadLanguagePreference(preference)
                }
            }
            is MultiSelectListPreference -> {
                if (preference.key == view.getKeyForDefaultFingersPreference()) {
                    loadDefaultFingersPreference(preference)
                }
            }
            is SwitchPreference -> {
                if (preference.key == view.getKeyForSyncUponLaunchPreference()) {
                    loadSyncUponLaunchPreference(preference)
                } else if (preference.key == view.getKeyForBackgroundSyncPreference()) {
                    loadBackgroundSyncPreference(preference)
                }
            }
            else -> {
                if (preference.key == view.getKeyForAppVersionPreference()) {
                    loadAppVersionInPreference(preference)
                } else if (preference.key == view.getKeyForScannerVersionPreference()) {
                    loadScannerVersionInPreference(preference)
                }
            }
        }
    }

    private fun loadLanguagePreference(preference: ListPreference) {
        preference.value = preferencesManager.language
        val index = preference.findIndexOfValue(preference.value)
        preference.summary = if (index >= 0) {
            preference.entries[index]
        } else {
            null
        }
    }

    private fun loadDefaultFingersPreference(preference: MultiSelectListPreference) {
        preference.values = getHashSetFromFingersMap(preferencesManager.fingerStatus).toHashSet()
    }

    private fun loadSyncUponLaunchPreference(preference: SwitchPreference) {
        preference.isChecked = preferencesManager.syncOnCallout
    }

    private fun loadBackgroundSyncPreference(preference: SwitchPreference) {
        preference.isChecked = preferencesManager.scheduledBackgroundSync
    }

    private fun loadAppVersionInPreference(preference: Preference) {
        preference.summary = preferencesManager.appVersionName
    }

    private fun loadScannerVersionInPreference(preference: Preference) {
        preference.summary = preferencesManager.scannerId
    }

    private fun bindPreferenceToSummaryValue(preference: Preference) {
        preference.onPreferenceChangeListener = bindPreferenceSummaryToValueListener

        when (preference) {
            is ListPreference ->
                bindPreferenceSummaryToValueListener.onPreferenceChangedGetPreference(preference) { getString(preference.key, "") }
            is MultiSelectListPreference ->
                bindPreferenceSummaryToValueListener.onPreferenceChangedGetPreference(preference) { getStringSet(preference.key, null) }
            is SwitchPreference ->
                bindPreferenceSummaryToValueListener.onPreferenceChangedGetPreference(preference) { getBoolean(preference.key, true) }
            else ->
                bindPreferenceSummaryToValueListener.onPreferenceChangedGetPreference(preference) { getString(preference.key, "") }
        }
    }

    private fun Preference.OnPreferenceChangeListener.onPreferenceChangedGetPreference(preference: Preference, getPreference: SharedPreferences.() -> Any) =
        onPreferenceChange(preference,
            PreferenceManager
                .getDefaultSharedPreferences(preference.context)
                .getPreference())

    private val bindPreferenceSummaryToValueListener =
        Preference.OnPreferenceChangeListener { preference, value ->
            when (preference) {
                is ListPreference -> {
                    if (preference.key == view.getKeyForLanguagePreference()) {
                        handleLanguagePreferenceChanged(preference, value.toString())
                    }
                }
                is MultiSelectListPreference -> {
                    if (preference.key == view.getKeyForDefaultFingersPreference()) {
                        @Suppress("UNCHECKED_CAST")
                        handleDefaultFingersChanged(preference, value as HashSet<String>)
                    }
                }
                is SwitchPreference -> {
                    if (preference.key == view.getKeyForSyncUponLaunchPreference()) {
                        handleSyncUponLaunchChanged(value as Boolean)
                    } else if (preference.key == view.getKeyForBackgroundSyncPreference()) {
                        handleBackgroundSyncChanged(value as Boolean)
                    }
                }
            }
            true
        }

    private fun handleSyncUponLaunchChanged(value: Boolean) {
        preferencesManager.syncOnCallout = value
    }

    private fun handleBackgroundSyncChanged(value: Boolean) {
        preferencesManager.scheduledBackgroundSync = value
    }

    private fun handleLanguagePreferenceChanged(listPreference: ListPreference, stringValue: String) {
        val index = listPreference.findIndexOfValue(stringValue)
        preferencesManager.language = listPreference.value

        listPreference.summary = if (index >= 0) {
            listPreference.entries[index]
        } else {
            null
        }
    }

    private fun handleDefaultFingersChanged(preference: MultiSelectListPreference,
                                            fingersHash: HashSet<String>) {
        if (selectionContainsDefaultFingers(fingersHash)) {
            preferencesManager.fingerStatus = getMapFromFingersHash(fingersHash)
        } else {
            view.showToastForInvalidSelectionOfFingers()
            fingersHash.clear()
            fingersHash.addAll(preference.values)
        }
    }

    private fun selectionContainsDefaultFingers(fingersHash: HashSet<String>): Boolean =
        fingersHash.containsAll(getHashSetFromFingersMap(preferencesManager.getRemoteConfigFingerStatus()))

    private fun getHashSetFromFingersMap(fingersMap: Map<FingerIdentifier, Boolean>) =
        fingersMap.filter { it.value }.keys.map { it.toString() }.toHashSet()

    private fun getMapFromFingersHash(fingersHash: HashSet<String>): Map<FingerIdentifier, Boolean> {
        val map = mutableMapOf<FingerIdentifier, Boolean>()
        fingersHash.forEach {
            val fingerIdentifier = FingerIdentifier.valueOf(it)
            map[fingerIdentifier] = true
        }
        return map
    }
}
