package com.simprints.feature.dashboard.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.simprints.core.tools.viewbinding.viewBinding
import com.simprints.feature.dashboard.DashboardActivity
import com.simprints.feature.dashboard.R
import com.simprints.feature.dashboard.databinding.FragmentSettingsBinding
import com.simprints.infra.config.domain.models.GeneralConfiguration
import com.simprints.infra.config.domain.models.GeneralConfiguration.Modality.FINGERPRINT
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
internal class SettingsFragment : PreferenceFragmentCompat() {

    private val viewModel by viewModels<SettingsViewModel>()
    private val binding by viewBinding(FragmentSettingsBinding::bind)

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preference_general)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val settingsView =
            inflater.inflate(R.layout.fragment_settings, container, false) as ViewGroup
        settingsView.addView(super.onCreateView(inflater, container, savedInstanceState))
        return settingsView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.settingsToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        viewModel.generalConfiguration.observe(viewLifecycleOwner) {
            enableFingerprintSettings(it.modalities)
            configureAvailableLanguageEntriesFromProjectLanguages(it.languageOptions)
        }
        viewModel.languagePreference.observe(viewLifecycleOwner) {
            loadSelectedLanguage(it)
        }
        bindClickListeners()
    }

    private fun bindClickListeners() {
        getLanguagePreference()?.setOnPreferenceChangeListener { _, newValue ->
            handleLanguageChange(newValue as String)
            clearActivityStackAndRelaunchApp()
            true
        }

        getFingerSelectionPreference()?.setOnPreferenceClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_fingerSelectionFragment)
            true
        }

        getSyncInfoPreference()?.setOnPreferenceClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_syncInfoFragment)
            true
        }

        getAboutPreference()?.setOnPreferenceClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_aboutFragment)
            true
        }
    }

    private fun enableFingerprintSettings(modalities: List<GeneralConfiguration.Modality>) {
        getFingerSelectionPreference()?.isVisible = modalities.contains(FINGERPRINT)
    }

    private fun configureAvailableLanguageEntriesFromProjectLanguages(availableLanguages: List<String>) {
        getLanguagePreference()?.apply {
            val languageCodeToName = resources.getStringArray(R.array.language_values)
                .zip(resources.getStringArray(R.array.language_array)).toMap()
            val (availableLanguageNames, availableLanguageCodes) = computeAvailableLanguageNamesAndCodes(
                availableLanguages,
                languageCodeToName
            )

            entries = availableLanguageNames.toTypedArray()
            entryValues = availableLanguageCodes.toTypedArray()
        }
    }

    private fun computeAvailableLanguageNamesAndCodes(
        availableLanguages: List<String>,
        languageCodeToName: Map<String, String>
    ): Pair<MutableList<String>, MutableList<String>> {

        val availableLanguageNames = mutableListOf<String>()
        val availableLanguageCodes = mutableListOf<String>()
        availableLanguages.forEach { code ->
            val name = languageCodeToName[code]
            if (name != null) {
                availableLanguageNames.add(name)
                availableLanguageCodes.add(code)
            }
        }

        if (availableLanguageNames.isEmpty()) {
            availableLanguageNames.addAll(languageCodeToName.values)
            availableLanguageCodes.addAll(languageCodeToName.keys)
        }
        return Pair(availableLanguageNames, availableLanguageCodes)
    }

    private fun loadSelectedLanguage(language: String) {
        getLanguagePreference()?.apply {
            value = language
            summary = findIndexOfValue(value).let { if (it == -1) null else entries[it] }
        }
    }

    private fun handleLanguageChange(newLanguage: String) {
        viewModel.updateLanguagePreference(newLanguage)
        loadSelectedLanguage(newLanguage)
    }

    private fun clearActivityStackAndRelaunchApp() {
        activity?.finishAffinity()
        activity?.startActivity(Intent(context, DashboardActivity::class.java))
        activity?.overridePendingTransition(0, 0)
    }

    private fun getLanguagePreference(): ListPreference? =
        findPreference(getString(R.string.preference_select_language_key))

    private fun getFingerSelectionPreference(): Preference? =
        findPreference(getString(R.string.preference_select_fingers_key))

    private fun getSyncInfoPreference(): Preference? =
        findPreference(getString(R.string.preference_sync_info_key))

    private fun getAboutPreference(): Preference? =
        findPreference(getString(R.string.preference_app_details_key))
}
