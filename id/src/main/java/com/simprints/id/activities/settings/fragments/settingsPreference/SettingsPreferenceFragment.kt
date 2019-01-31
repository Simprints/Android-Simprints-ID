package com.simprints.id.activities.settings.fragments.settingsPreference

import android.content.Intent
import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceFragment
import android.view.MenuItem
import android.widget.Toast
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.settings.SettingsActivity
import com.simprints.id.di.AppComponent


class SettingsPreferenceFragment : PreferenceFragment(), SettingsPreferenceContract.View {

    override lateinit var viewPresenter: SettingsPreferenceContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.pref_general)
        setHasOptionsMenu(true)

        val component = (activity.application as Application).component as AppComponent
        viewPresenter = SettingsPreferencePresenter(this, component)
        viewPresenter.start()
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
        val languageCodes = resources.getStringArray(R.array.language_values)
        val languageNames = resources.getStringArray(R.array.language_array)
        return languageCodes.zip(languageNames).toMap()
    }

    override fun getPreferenceForLanguage(): Preference =
        findPreference(getKeyForLanguagePreference())

    override fun getPreferenceForSelectModules(): Preference =
        findPreference(getKeyForSelectModulesPreference())

    override fun getPreferenceForDefaultFingers(): Preference =
        findPreference(getKeyForDefaultFingersPreference())

    override fun getSyncAndSearchConfigurationPreference(): Preference =
        findPreference(getKeyForSyncAndSearchConfigurationPreference())

    override fun getAppVersionPreference(): Preference =
        findPreference(getKeyForAppVersionPreference())

    override fun getScannerVersionPreference(): Preference =
        findPreference(getKeyForScannerVersionPreference())

    override fun getKeyForLanguagePreference(): String =
        getString(R.string.select_language_preference)

    override fun getKeyForSelectModulesPreference(): String =
        getString(R.string.select_modules_preference)

    override fun getKeyForDefaultFingersPreference(): String =
        getString(R.string.select_fingers_preference)

    override fun getKeyForSyncAndSearchConfigurationPreference(): String =
        getString(R.string.sync_and_search_configuration_preference)

    override fun getKeyForAppVersionPreference(): String =
        getString(R.string.app_version_preference)

    override fun getKeyForScannerVersionPreference(): String =
        getString(R.string.scanner_version_preference)

    override fun setSelectModulePreferenceEnabled(enabled: Boolean) {
        getPreferenceForSelectModules().isEnabled = enabled
    }

    override fun showToastForNoModulesSelected() {
        Toast.makeText(activity, getString(R.string.settings_no_modules_toast), Toast.LENGTH_LONG).show()
    }

    override fun showToastForTooManyModulesSelected(maxModules: Int) {
        Toast.makeText(activity, getString(R.string.settings_too_many_modules_toast, maxModules), Toast.LENGTH_LONG).show()
    }

    override fun showToastForInvalidSelectionOfFingers() {
        Toast.makeText(activity, getString(R.string.settings_invalid_selection), Toast.LENGTH_LONG).show()
    }
}
