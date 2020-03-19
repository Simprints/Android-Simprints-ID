package com.simprints.id.activities.settings.fragments.settingsAbout

import android.preference.Preference
import com.simprints.id.data.consent.longconsent.LongConsentRepository
import com.simprints.id.data.db.session.domain.SessionEventsManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.prefs.events.RecentEventsPreferencesManager
import com.simprints.id.di.AppComponent
import com.simprints.id.domain.GROUP
import com.simprints.id.secure.SignerManager
import com.simprints.id.services.scheduledSync.SyncManager
import javax.inject.Inject

class SettingsAboutPresenter(private val view: SettingsAboutContract.View,
                             component: AppComponent) :
    SettingsAboutContract.Presenter {

    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var signerManager: SignerManager
    @Inject lateinit var syncManager: SyncManager
    @Inject lateinit var sessionEventManager: SessionEventsManager
    @Inject lateinit var recentEventsManager: RecentEventsPreferencesManager
    @Inject lateinit var longConsentRepository: LongConsentRepository

    init {
        component.inject(this)
    }

    override fun start() {
        loadPreferenceValuesAndBindThemToChangeListeners()
    }

    private fun loadPreferenceValuesAndBindThemToChangeListeners() {
        loadValueAndBindChangeListener(view.getSyncAndSearchConfigurationPreference())
        loadValueAndBindChangeListener(view.getAppVersionPreference())
        loadValueAndBindChangeListener(view.getScannerVersionPreference())
        loadValueAndBindChangeListener(view.getDeviceIdPreference())
        loadValueAndBindChangeListener(view.getLogoutPreference())
    }

    internal fun loadValueAndBindChangeListener(preference: Preference) {
        when (preference.key) {
            view.getKeyForSyncAndSearchConfigurationPreference() -> {
                loadSyncAndSearchConfigurationPreference(preference)
            }
            view.getKeyForAppVersionPreference() -> {
                loadAppVersionInPreference(preference)
            }
            view.getKeyForScannerVersionPreference() -> {
                loadScannerVersionInPreference(preference)
            }
            view.getKeyForDeviceIdPreference() -> {
                loadDeviceIdInPreference(preference)
            }
            view.getKeyForLogoutPreference() -> {
                preference.setOnPreferenceClickListener {
                    handleLogoutPreferenceClicked()
                    true
                }
            }
        }
    }

    internal fun loadSyncAndSearchConfigurationPreference(preference: Preference) {
        preference.summary = "${preferencesManager.syncGroup.lowerCaseCapitalized()} Sync" +
            " - ${preferencesManager.matchGroup.lowerCaseCapitalized()} Search"
    }

    private fun GROUP.lowerCaseCapitalized() = toString().toLowerCase().capitalize()

    internal fun loadAppVersionInPreference(preference: Preference) {
        preference.summary = view.packageVersionName
    }

    internal fun loadScannerVersionInPreference(preference: Preference) {
        preference.summary = recentEventsManager.lastScannerVersion
    }

    internal fun loadDeviceIdInPreference(preference: Preference) {
        preference.summary = view.deviceId
    }

    private fun handleLogoutPreferenceClicked() {
        view.showConfirmationDialogForLogout()
    }

    override suspend fun logout() {
        signerManager.signOut()
        syncManager.cancelBackgroundSyncs()
        longConsentRepository.deleteLongConsents()
        sessionEventManager.signOut()

        view.finishSettings()
    }
}
