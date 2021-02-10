package com.simprints.id.activities.settings.fragments.settingsAbout

import androidx.lifecycle.ViewModel
import androidx.preference.Preference
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.prefs.events.RecentEventsPreferencesManager
import com.simprints.id.domain.GROUP
import com.simprints.id.secure.SignerManager

class SettingsAboutViewModel(val preferencesManager: PreferencesManager,
                             val signerManager: SignerManager,
                             private val recentEventsManager: RecentEventsPreferencesManager) : ViewModel() {

    fun enableFingerprintSettings(scannerVersionPref: Preference?) {
        enablePreference(scannerVersionPref)
    }

    fun enableFaceSettings() {
        // No face-specific settings yet
    }

    internal fun loadSyncAndSearchConfigurationPreference(preference: Preference?) {
        preference?.summary = "${preferencesManager.syncGroup.lowerCaseCapitalized()} Sync" +
            " - ${preferencesManager.matchGroup.lowerCaseCapitalized()} Search"
    }

    private fun GROUP.lowerCaseCapitalized() = toString().toLowerCase().capitalize()

    internal fun loadAppVersionInPreference(preference: Preference, packageVersionName: String) {
        preference.summary = packageVersionName
    }

    internal fun loadScannerVersionInPreference(preference: Preference) {
        preference.summary = recentEventsManager.lastScannerVersion
    }

    internal fun loadDeviceIdInPreference(preference: Preference, deviceId: String) {
        preference.summary = deviceId
    }

    private fun enablePreference(preference: Preference?) {
        preference?.isEnabled = true
    }

    suspend fun logout() {
        signerManager.signOut()
    }
}
