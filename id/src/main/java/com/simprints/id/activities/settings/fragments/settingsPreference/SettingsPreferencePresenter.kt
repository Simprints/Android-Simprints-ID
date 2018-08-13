package com.simprints.id.activities.settings.fragments.settingsPreference

import android.preference.ListPreference
import android.preference.MultiSelectListPreference
import android.preference.Preference
import android.preference.SwitchPreference
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
        loadPreferencesAndBindThemToValues()
    }

    private fun loadPreferencesAndBindThemToValues() {
        loadValueAndBindChangeListener(view.getPreferenceForLanguage())
        loadValueAndBindChangeListener(view.getPreferenceForDefaultFingers())
        loadValueAndBindChangeListener(view.getPreferenceForSyncUponLaunchToggle())
        loadValueAndBindChangeListener(view.getPreferenceForBackgroundSyncToggle())
        loadValueAndBindChangeListener(view.getAppVersionPreference())
        loadValueAndBindChangeListener(view.getScannerVersionPreference())
    }

    private fun loadValueAndBindChangeListener(preference: Preference) {
        when (preference) {
            is ListPreference -> {
                if (preference.key == view.getKeyForLanguagePreference()) {
                    loadLanguagePreference(preference)
                    preference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, value ->
                        handleLanguagePreferenceChanged(preference, value.toString())
                    }
                }
            }
            is MultiSelectListPreference -> {
                if (preference.key == view.getKeyForDefaultFingersPreference()) {
                    loadDefaultFingersPreference(preference)
                    preference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, value ->
                        @Suppress("UNCHECKED_CAST")
                        handleDefaultFingersChanged(preference, value as HashSet<String>)
                    }
                }
            }
            is SwitchPreference -> {
                if (preference.key == view.getKeyForSyncUponLaunchPreference()) {
                    loadSyncUponLaunchPreference(preference)
                    preference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, value -> handleSyncUponLaunchChanged(value as Boolean) }
                } else if (preference.key == view.getKeyForBackgroundSyncPreference()) {
                    loadBackgroundSyncPreference(preference)
                    preference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, value -> handleBackgroundSyncChanged(value as Boolean) }
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

    inline fun <reified T : Preference, V : Any>Preference.setChangeListener(crossinline listener: (T, V) -> Unit) {
        onPreferenceChangeListener = Preference.OnPreferenceChangeListener{ preference, value ->
            @Suppress("UNCHECKED_CAST")
            listener(preference as T, value as V)
            true}
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

    private fun handleSyncUponLaunchChanged(value: Boolean): Boolean {
        preferencesManager.syncOnCallout = value
        return true
    }

    private fun handleBackgroundSyncChanged(value: Boolean): Boolean {
        preferencesManager.scheduledBackgroundSync = value
        return true
    }

    private fun handleLanguagePreferenceChanged(listPreference: ListPreference, stringValue: String): Boolean {
        val index = listPreference.findIndexOfValue(stringValue)
        preferencesManager.language = stringValue

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

    private fun getMapFromFingersHash(fingersHash: HashSet<String>): Map<FingerIdentifier, Boolean> {
        val map = mutableMapOf<FingerIdentifier, Boolean>()
        fingersHash.forEach {
            val fingerIdentifier = FingerIdentifier.valueOf(it)
            map[fingerIdentifier] = true
        }
        return map
    }
}
