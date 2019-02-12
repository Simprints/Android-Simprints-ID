package com.simprints.id.activities.settings.fragments.settingsAbout

import android.preference.Preference
import com.simprints.id.activities.BasePresenter
import com.simprints.id.activities.BaseView


interface SettingsAboutContract {

    interface  View: BaseView<Presenter> {

        fun getAppVersionPreference(): Preference

        fun getScannerVersionPreference(): Preference

        fun getSyncAndSearchConfigurationPreference(): Preference

        fun getDeviceIdPreference(): Preference

        fun getLogoutPreference(): Preference

        fun getKeyForAppVersionPreference(): String

        fun getKeyForScannerVersionPreference(): String

        fun getKeyForSyncAndSearchConfigurationPreference(): String

        fun getKeyForDeviceIdPreference(): String

        fun getKeyForLogoutPreference(): String

        fun showConfirmationDialogForLogout()

        fun finishSettings()
    }

    interface Presenter : BasePresenter {
        fun logout()
    }
}
