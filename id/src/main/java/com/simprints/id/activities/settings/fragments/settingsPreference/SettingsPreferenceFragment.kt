package com.simprints.id.activities.settings.fragments.settingsPreference

import android.content.Intent
import android.os.Bundle
import android.preference.MultiSelectListPreference
import android.preference.Preference
import android.preference.PreferenceFragment
import android.view.MenuItem
import android.widget.Toast
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.settings.SettingsActivity
import com.simprints.id.tools.AndroidResourcesHelper
import com.simprints.id.tools.extensions.runOnUiThreadIfStillRunning
import javax.inject.Inject

// TODO: replace with PreferenceFragmentCompat
class SettingsPreferenceFragment : PreferenceFragment(), SettingsPreferenceContract.View {

    override lateinit var viewPresenter: SettingsPreferenceContract.Presenter
    @Inject lateinit var androidResourcesHelper: AndroidResourcesHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        val component = (activity.application as Application).component
        component.inject(this)

        addPreferencesFromResource(R.xml.pref_general)
        setTextInLayout()

        viewPresenter = SettingsPreferencePresenter(this, component)
        viewPresenter.start()

        initTextInLayout()
    }

    private fun setTextInLayout() {
        with(androidResourcesHelper) {
            getPreferenceForGeneralCategory().title = androidResourcesHelper.getString(R.string.settings_general)
            getPreferenceForAppDetailsCategory().title = androidResourcesHelper.getString(R.string.settings_app_details)

            val defaultFingersPreference = (getPreferenceForDefaultFingers() as MultiSelectListPreference)
                defaultFingersPreference.entries = arrayOf<CharSequence>(
                getString(R.string.l_1_finger_name),
                getString(R.string.l_2_finger_name),
                getString(R.string.r_1_finger_name),
                getString(R.string.r_2_finger_name),
                getString(R.string.l_3_finger_name),
                getString(R.string.r_3_finger_name),
                getString(R.string.l_4_finger_name),
                getString(R.string.r_4_finger_name),
                getString(R.string.l_5_finger_name),
                getString(R.string.r_5_finger_name)
            )
            defaultFingersPreference.positiveButtonText = androidResourcesHelper.getString(R.string.ok)
            defaultFingersPreference.negativeButtonText = androidResourcesHelper.getString(R.string.cancel_button)
        }
    }

    private fun initTextInLayout() {
        with(androidResourcesHelper) {
            getPreferenceForLanguage().title = getString(R.string.preference_select_language_title)
            getPreferenceForDefaultFingers().apply {
                title = getString(R.string.preference_select_fingers_title)
                summary = getString(R.string.preference_summary_settings_fingers)
            }

            getPreferenceForSelectModules().apply {
                title = getString(R.string.preference_select_modules_title)
                summary = getString(R.string.preference_summary_modules)
            }

            getPreferenceForSyncInformation().apply {
                title = androidResourcesHelper.getString(R.string.preference_sync_information_title)
            }

            getPreferenceForAbout().title = getString(R.string.preference_app_details_title)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            startActivity(Intent(activity, SettingsActivity::class.java))
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun getLanguageCodeAndNamePairs(): Map<String, String> {
        val languageCodes = androidResourcesHelper.getStringArray(R.array.language_values)
        val languageNames = androidResourcesHelper.getStringArray(R.array.language_array)
        return languageCodes.zip(languageNames).toMap()
    }

    private fun getPreferenceForGeneralCategory() =
        findPreference(getKeyForGeneralPreferenceCategory())

    private fun getKeyForGeneralPreferenceCategory() =
        androidResourcesHelper.getString(R.string.preferences_general_key)

    private fun getPreferenceForAppDetailsCategory() =
        findPreference(getKeyForAppDetailsPreferenceCategory())

    private fun getKeyForAppDetailsPreferenceCategory() =
        androidResourcesHelper.getString(R.string.preferences_app_details_key)

    override fun getPreferenceForLanguage(): Preference =
        findPreference(getKeyForLanguagePreference())

    override fun getPreferenceForSelectModules(): Preference =
        findPreference(getKeyForSelectModulesPreference())

    override fun getPreferenceForDefaultFingers(): Preference =
        findPreference(getKeyForDefaultFingersPreference())

    override fun getKeyForLanguagePreference(): String =
        androidResourcesHelper.getString(R.string.preference_select_language_key)

    override fun getKeyForSelectModulesPreference(): String =
        androidResourcesHelper.getString(R.string.preference_select_modules_key)

    override fun getKeyForDefaultFingersPreference(): String =
        androidResourcesHelper.getString(R.string.preference_select_fingers_key)

    override fun getPreferenceForAbout(): Preference =
        findPreference(getKeyForAboutPreference())

    override fun getPreferenceForSyncInformation(): Preference =
        findPreference(getKeyForSyncInfoPreference())

    override fun getKeyForAboutPreference(): String =
        androidResourcesHelper.getString(R.string.preference_app_details_key)

    override fun getKeyForSyncInfoPreference(): String =
        androidResourcesHelper.getString(R.string.preference_sync_info_key)

    override fun setSelectModulePreferenceEnabled(enabled: Boolean) {
        getPreferenceForSelectModules().isEnabled = enabled
    }

    override fun showToastForNoModulesSelected() {
        Toast.makeText(activity, androidResourcesHelper.getString(R.string.settings_no_modules_toast), Toast.LENGTH_LONG).show()
    }

    override fun showToastForTooManyModulesSelected(maxModules: Int) {
        Toast.makeText(activity, androidResourcesHelper.getString(R.string.settings_too_many_modules_toast, arrayOf(maxModules)), Toast.LENGTH_LONG).show()
    }

    override fun showToastForInvalidSelectionOfFingers() {
        Toast.makeText(activity, androidResourcesHelper.getString(R.string.settings_invalid_selection), Toast.LENGTH_LONG).show()
    }

    override fun openSettingAboutActivity() {
        activity.runOnUiThreadIfStillRunning {
            (activity as SettingsActivity).openSettingAboutActivity()
        }
    }

    override fun openModuleSelectionActivity() {
        activity.runOnUiThreadIfStillRunning {
            (activity as SettingsActivity).openModuleSelectionActivity()
        }
    }

    override fun openSyncInfoActivity() {
        activity.runOnUiThreadIfStillRunning {
            (activity as SettingsActivity).openSyncInformationActivity()
        }
    }
}
