package com.simprints.id.activities.settings.fragments.settingsPreference

import android.preference.Preference
import com.simprints.id.activities.BasePresenter
import com.simprints.id.activities.BaseView


interface SettingsPreferenceContract {

    interface  View: BaseView<Presenter> {
        fun getPreferenceForLanguage(): Preference

        fun getPreferenceForDefaultFingers(): Preference

        fun getPreferenceForSyncUponLaunchToggle(): Preference

        fun getPreferenceForBackgroundSyncToggle(): Preference

        fun getAppVersionPreference(): Preference

        fun getScannerVersionPreference(): Preference

        fun getKeyForLanguagePreference(): String

        fun getKeyForDefaultFingersPreference(): String

        fun getKeyForSyncUponLaunchPreference(): String

        fun getKeyForBackgroundSyncPreference(): String

        fun getKeyForAppVersionPreference(): String

        fun getKeyForScannerVersionPreference(): String

        fun showToastForInvalidSelectionOfFingers()
    }

    interface Presenter: BasePresenter {

    }
}
