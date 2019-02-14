package com.simprints.id.activities.settings.fragments.settingsAbout

import android.preference.Preference
import com.simprints.id.data.analytics.eventData.controllers.domain.SessionEventsManager
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.di.AppComponent
import com.simprints.id.domain.Constants
import com.simprints.id.services.scheduledSync.SyncSchedulerHelper
import javax.inject.Inject


class SettingsAboutPresenter(private val view: SettingsAboutContract.View,
                             component: AppComponent) :
    SettingsAboutContract.Presenter {

    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var dbManager: DbManager
    @Inject lateinit var syncSchedulerHelper: SyncSchedulerHelper
    @Inject lateinit var sessionEventManager: SessionEventsManager

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

    private fun Constants.GROUP.lowerCaseCapitalized() = toString().toLowerCase().capitalize()

    internal fun loadAppVersionInPreference(preference: Preference) {
        preference.summary = preferencesManager.appVersionName
    }

    internal fun loadScannerVersionInPreference(preference: Preference) {
        preference.summary = preferencesManager.hardwareVersionString
    }

    internal fun loadDeviceIdInPreference(preference: Preference) {
        preference.summary = preferencesManager.deviceId
    }

    private fun handleLogoutPreferenceClicked() {
        view.showConfirmationDialogForLogout()
    }

    override fun logout() {
        dbManager.signOut()
        syncSchedulerHelper.cancelDownSyncWorkers()
        sessionEventManager.signOut()

        view.finishSettings()
    }
}
