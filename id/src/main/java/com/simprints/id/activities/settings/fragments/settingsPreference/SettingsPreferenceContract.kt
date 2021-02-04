package com.simprints.id.activities.settings.fragments.settingsPreference

import androidx.preference.Preference
import com.simprints.id.activities.BasePresenter
import com.simprints.id.activities.BaseView

interface SettingsPreferenceContract {

    interface  View: BaseView<Presenter> {

        fun getLanguageCodeAndNamePairs(): Map<String, String>

        fun getPreferenceForLanguage(): Preference?

        fun getPreferenceForDefaultFingers(): Preference?

        fun getPreferenceForAbout(): Preference?

        fun getPreferenceForSyncInformation(): Preference?

        fun getKeyForSyncInfoPreference(): String

        fun getKeyForLanguagePreference(): String

        fun getKeyForDefaultFingersPreference(): String

        fun getKeyForAboutPreference(): String

        fun setSelectModulePreferenceEnabled(enabled: Boolean)

        fun showToastForInvalidSelectionOfFingers()

        fun openFingerSelectionActivity()

        fun openSettingAboutActivity()

        fun openSyncInfoActivity()

        fun clearActivityStackAndRelaunchApp()

        fun enablePreference(preference: Preference?)
    }

    interface Presenter : BasePresenter
}
