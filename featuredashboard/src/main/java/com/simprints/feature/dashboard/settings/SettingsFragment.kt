package com.simprints.feature.dashboard.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.simprints.core.tools.viewbinding.viewBinding
import com.simprints.feature.dashboard.DashboardActivity
import com.simprints.feature.dashboard.R
import com.simprints.feature.dashboard.databinding.FragmentSettingsBinding
import com.simprints.feature.dashboard.settings.password.SettingsPasswordDialogFragment
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
        }
        viewModel.languagePreference.observe(viewLifecycleOwner) {
            loadSelectedLanguage(it)
        }
        bindClickListeners()
    }

    private fun enableFingerprintSettings(modalities: List<GeneralConfiguration.Modality>) {
        getFingerSelectionPreference()?.isVisible = modalities.contains(FINGERPRINT)
    }

    private fun bindClickListeners() {
        getLanguagePreference()?.setOnPreferenceClickListener {
            val password = viewModel.settingsLocked.value?.getNullablePassword()
            if (password != null) {
                SettingsPasswordDialogFragment(
                    passwordToMatch = password,
                    onSuccess = {
                        viewModel.unlockSettings()
                        createLanguageSelectionDialog().show()
                    },
                ).show(childFragmentManager, SettingsPasswordDialogFragment.TAG)
            } else {
                createLanguageSelectionDialog().show()
            }
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

    private fun createLanguageSelectionDialog(): AlertDialog {
        val languagesOptions = viewModel.generalConfiguration.value?.languageOptions.orEmpty()
        val languagesCodeToName = computeAvailableLanguageCodeAndName(languagesOptions)
        val languageNames = languagesCodeToName.map { it.second }

        val selectedLanguage = viewModel.languagePreference.value
            ?.let { selectedCode -> languagesCodeToName.indexOfFirst { selectedCode == it.first } }
            ?: ArrayAdapter.NO_SELECTION

        return AlertDialog.Builder(requireContext())
            .setSingleChoiceItems(
                ArrayAdapter(requireContext(), android.R.layout.simple_list_item_single_choice, languageNames),
                selectedLanguage
            ) { _, which -> handleLanguageChange(languagesCodeToName[which].first) }
            .create()
    }

    private fun computeAvailableLanguageCodeAndName(availableLanguages: List<String>): List<Pair<String, String>> {
        val languageCodeToName = getLanguageMaps()
        return availableLanguages.mapNotNull { code -> languageCodeToName[code]?.let { code to it } }
    }

    private fun getLanguageMaps(): Map<String, String> = resources
        .getStringArray(R.array.language_values)
        .zip(resources.getStringArray(R.array.language_array))
        .toMap()

    private fun loadSelectedLanguage(languageCode: String) {
        val newLanguageName = getLanguageMaps()[languageCode]
        getLanguagePreference()?.summary = newLanguageName
    }

    private fun handleLanguageChange(newLanguageCode: String) {
        viewModel.updateLanguagePreference(newLanguageCode)
        loadSelectedLanguage(newLanguageCode)
        clearActivityStackAndRelaunchApp()
    }

    private fun clearActivityStackAndRelaunchApp() {
        activity?.finishAffinity()
        activity?.startActivity(Intent(context, DashboardActivity::class.java))
        activity?.overridePendingTransition(0, 0)
    }

    private fun getLanguagePreference(): Preference? =
        findPreference(getString(R.string.preference_select_language_key))

    private fun getFingerSelectionPreference(): Preference? =
        findPreference(getString(R.string.preference_select_fingers_key))

    private fun getSyncInfoPreference(): Preference? =
        findPreference(getString(R.string.preference_sync_info_key))

    private fun getAboutPreference(): Preference? =
        findPreference(getString(R.string.preference_app_details_key))
}
