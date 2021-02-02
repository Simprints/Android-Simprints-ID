package com.simprints.id.activities.settings.fragments.settingsPreference

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.simprints.core.tools.extentions.getStringArray
import com.simprints.core.tools.extentions.removeAnimationsToNextActivity
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.settings.SettingsActivity
import com.simprints.id.tools.extensions.runOnUiThreadIfStillRunning

class SettingsPreferenceFragment : PreferenceFragmentCompat(), SettingsPreferenceContract.View {

    override lateinit var viewPresenter: SettingsPreferenceContract.Presenter

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_general)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        val component = (requireActivity().application as Application).component
        component.inject(this)

        setTextInLayout()

        viewPresenter = SettingsPreferencePresenter(this, component)
        viewPresenter.start()

        initTextInLayout()
    }

    private fun setTextInLayout() {
        getPreferenceForGeneralCategory()?.title = getString(R.string.settings_general)
        getPreferenceForAppDetailsCategory()?.title = getString(R.string.settings_app_details)
    }

    private fun initTextInLayout() {
        getPreferenceForLanguage()?.title = getString(R.string.preference_select_language_title)
        getPreferenceForDefaultFingers()?.apply {
            title = getString(R.string.preference_select_fingers_title)
            summary = getString(R.string.preference_summary_settings_fingers)
        }

        getPreferenceForSyncInformation()?.apply {
            title = getString(R.string.preference_sync_information_title)
            summary = getString(R.string.preference_summary_sync_information)
        }

        getPreferenceForAbout()?.title = getString(R.string.preference_app_details_title)
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
        val languageCodes = requireActivity().getStringArray(R.array.language_values)
        val languageNames = requireActivity().getStringArray(R.array.language_array)
        return languageCodes.zip(languageNames).toMap()
    }

    private fun getPreferenceForGeneralCategory(): Preference? =
        findPreference(getKeyForGeneralPreferenceCategory())

    private fun getKeyForGeneralPreferenceCategory() =
        getString(R.string.preferences_general_key)

    private fun getPreferenceForAppDetailsCategory(): Preference? =
        findPreference(getKeyForAppDetailsPreferenceCategory())

    private fun getKeyForAppDetailsPreferenceCategory() =
        getString(R.string.preferences_app_details_key)

    override fun getPreferenceForLanguage(): Preference? =
        findPreference(getKeyForLanguagePreference())

    override fun getPreferenceForDefaultFingers(): Preference? =
        findPreference(getKeyForDefaultFingersPreference())

    override fun getKeyForLanguagePreference(): String =
        getString(R.string.preference_select_language_key)

    override fun getKeyForDefaultFingersPreference(): String =
        getString(R.string.preference_select_fingers_key)

    override fun getPreferenceForAbout(): Preference? =
        findPreference(getKeyForAboutPreference())

    override fun getPreferenceForSyncInformation(): Preference? =
        findPreference(getKeyForSyncInfoPreference())

    override fun getKeyForAboutPreference(): String =
        getString(R.string.preference_app_details_key)

    override fun getKeyForSyncInfoPreference(): String =
        getString(R.string.preference_sync_info_key)

    override fun setSelectModulePreferenceEnabled(enabled: Boolean) {
    }

    override fun showToastForInvalidSelectionOfFingers() {
        Toast.makeText(activity, getString(R.string.settings_invalid_selection), Toast.LENGTH_LONG).show()
    }

    override fun openFingerSelectionActivity() {
        activity?.runOnUiThreadIfStillRunning {
            (activity as SettingsActivity).openFingerSelectionActivity()
        }
    }

    override fun openSettingAboutActivity() {
        activity?.runOnUiThreadIfStillRunning {
            (activity as SettingsActivity).openSettingAboutActivity()
        }
    }

    override fun openSyncInfoActivity() {
        activity?.runOnUiThreadIfStillRunning {
            (activity as SettingsActivity).openSyncInformationActivity()
        }
    }

    override fun clearActivityStackAndRelaunchApp() {
        activity?.runOnUiThreadIfStillRunning {
            activity?.finishAffinity()
            (activity as SettingsActivity).openCheckLoginFromMainLauncherActivity()
            activity?.removeAnimationsToNextActivity()
        }
    }

    override fun enablePreference(preference: Preference?) {
        preference?.isEnabled = true
    }
}
