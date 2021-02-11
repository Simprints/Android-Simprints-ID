package com.simprints.id.activities.settings.fragments.settingsPreference

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.simprints.core.tools.extentions.getStringArray
import com.simprints.core.tools.extentions.removeAnimationsToNextActivity
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.settings.SettingsActivity
import com.simprints.id.tools.extensions.runOnUiThreadIfStillRunning
import javax.inject.Inject

class SettingsPreferenceFragment : PreferenceFragmentCompat() {

    @Inject
    lateinit var settingsPreferenceViewModelFactory: SettingsPreferenceViewModelFactory

    private val settingsPreferenceViewModel by viewModels<SettingsPreferenceViewModel> { settingsPreferenceViewModelFactory }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_general)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        val component = (requireActivity().application as Application).component
        component.inject(this)

        setTextInLayout()

        initTextInLayout()
        settingsPreferenceViewModel.configureAvailableLanguageEntriesFromProjectLanguages(getPreferenceForLanguage(), getLanguageCodeAndNamePairs())
        loadPreferenceValuesAndBindThemToChangeListeners()
        settingsPreferenceViewModel.enableSettingsBasedOnModalities(getPreferenceForDefaultFingers())
    }

    private fun loadPreferenceValuesAndBindThemToChangeListeners() {
        loadValueAndBindChangeListener(getPreferenceForLanguage())
        loadValueAndBindChangeListener(getPreferenceForDefaultFingers())
        loadValueAndBindChangeListener(getPreferenceForAbout())
        loadValueAndBindChangeListener(getPreferenceForSyncInformation())
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

    private fun loadValueAndBindChangeListener(preference: Preference?) {
        when (preference?.key) {
            getKeyForLanguagePreference() -> {
                settingsPreferenceViewModel.loadLanguagePreference(preference as ListPreference)
                preference.setChangeListener { value: String ->
                    settingsPreferenceViewModel.handleLanguagePreferenceChanged(preference, value)
                    clearActivityStackAndRelaunchApp()
                }
            }
            getKeyForDefaultFingersPreference() -> {
                preference.setOnPreferenceClickListener {
                    openFingerSelectionActivity()
                    true
                }
            }
            getKeyForSyncInfoPreference() -> {
                preference.setOnPreferenceClickListener {
                    openSyncInfoActivity()
                    true
                }
            }
            getKeyForAboutPreference() -> {
                preference.setOnPreferenceClickListener {
                    openSettingAboutActivity()
                    true
                }
            }
        }
    }

    private fun getLanguageCodeAndNamePairs(): Map<String, String> {
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

    private fun getPreferenceForLanguage(): Preference? =
        findPreference(getKeyForLanguagePreference())

    private fun getPreferenceForDefaultFingers(): Preference? =
        findPreference(getKeyForDefaultFingersPreference())

    private fun getKeyForLanguagePreference(): String =
        getString(R.string.preference_select_language_key)

    private fun getKeyForDefaultFingersPreference(): String =
        getString(R.string.preference_select_fingers_key)

    private fun getPreferenceForAbout(): Preference? =
        findPreference(getKeyForAboutPreference())

    private fun getPreferenceForSyncInformation(): Preference? =
        findPreference(getKeyForSyncInfoPreference())

    private fun getKeyForAboutPreference(): String =
        getString(R.string.preference_app_details_key)

    private fun getKeyForSyncInfoPreference(): String =
        getString(R.string.preference_sync_info_key)

    fun setSelectModulePreferenceEnabled(enabled: Boolean) {
    }

    fun showToastForInvalidSelectionOfFingers() {
        Toast.makeText(activity, getString(R.string.settings_invalid_selection), Toast.LENGTH_LONG).show()
    }

    private fun openFingerSelectionActivity() {
        activity?.runOnUiThreadIfStillRunning {
            (activity as SettingsActivity).openFingerSelectionActivity()
        }
    }

    private fun openSettingAboutActivity() {
        activity?.runOnUiThreadIfStillRunning {
            (activity as SettingsActivity).openSettingAboutActivity()
        }
    }

    private fun openSyncInfoActivity() {
        activity?.runOnUiThreadIfStillRunning {
            (activity as SettingsActivity).openSyncInformationActivity()
        }
    }

    private fun clearActivityStackAndRelaunchApp() {
        activity?.runOnUiThreadIfStillRunning {
            activity?.finishAffinity()
            (activity as SettingsActivity).openCheckLoginFromMainLauncherActivity()
            activity?.removeAnimationsToNextActivity()
        }
    }

    private inline fun <reified V : Any> Preference.setChangeListener(crossinline listener: (V) -> Unit) {
        onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, value ->
            listener(value as V)
            true
        }
    }
}
