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

        fun getPreferenceForAbout(): Preference

        fun getKeyForLanguagePreference(): String

        fun getKeyForSelectModulesPreference(): String

        fun getKeyForDefaultFingersPreference(): String

        fun getKeyForAboutPreference(): String

        fun setSelectModulePreferenceEnabled(enabled: Boolean)

        fun showToastForNoModulesSelected()

        fun showToastForTooManyModulesSelected(maxModules: Int)

        fun showToastForInvalidSelectionOfFingers()

        fun openSettingAboutActivity()
    }

    interface Presenter : BasePresenter
}
