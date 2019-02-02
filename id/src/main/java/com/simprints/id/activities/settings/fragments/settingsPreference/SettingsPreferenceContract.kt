package com.simprints.id.activities.settings.fragments.settingsPreference

import android.preference.Preference
import com.simprints.id.activities.BasePresenter
import com.simprints.id.activities.BaseView


interface SettingsPreferenceContract {

    interface  View: BaseView<Presenter> {

        fun getLanguageCodeAndNamePairs(): Map<String, String>

        fun getPreferenceForLanguage(): Preference

        fun getPreferenceForSelectModules(): Preference

        fun getPreferenceForDefaultFingers(): Preference

        fun getSyncAndSearchConfigurationPreference(): Preference

        fun getAppVersionPreference(): Preference

        fun getScannerVersionPreference(): Preference

        fun getDeviceIdPreference(): Preference

        fun getLogoutPreference(): Preference

        fun getKeyForLanguagePreference(): String

        fun getKeyForSelectModulesPreference(): String

        fun getKeyForDefaultFingersPreference(): String

        fun getKeyForSyncAndSearchConfigurationPreference(): String

        fun getKeyForAppVersionPreference(): String

        fun getKeyForScannerVersionPreference(): String

        fun getKeyForLogoutPreference(): String

        fun getKeyForDeviceIdPreference(): String

        fun setSelectModulePreferenceEnabled(enabled: Boolean)

        fun showToastForNoModulesSelected()

        fun showToastForTooManyModulesSelected(maxModules: Int)

        fun showToastForInvalidSelectionOfFingers()

        fun showConfirmationDialogForLogout()

        fun finishSettings()
    }

    interface Presenter : BasePresenter {
        fun logout()
    }
}
