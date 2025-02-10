package com.simprints.feature.dashboard.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.simprints.core.livedata.LiveDataEventObserver
import com.simprints.core.livedata.LiveDataEventWithContentObserver
import com.simprints.feature.dashboard.DashboardActivity
import com.simprints.feature.dashboard.R
import com.simprints.feature.dashboard.databinding.FragmentSettingsBinding
import com.simprints.feature.dashboard.settings.password.SettingsPasswordDialogFragment
import com.simprints.infra.config.store.models.GeneralConfiguration
import com.simprints.infra.config.store.models.GeneralConfiguration.Modality.FINGERPRINT
import com.simprints.infra.uibase.navigation.navigateSafely
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import com.simprints.infra.resources.R as IDR

@AndroidEntryPoint
internal class SettingsFragment : PreferenceFragmentCompat() {
    private val viewModel by viewModels<SettingsViewModel>()
    private val binding by viewBinding(FragmentSettingsBinding::bind)

    override fun onCreatePreferences(
        savedInstanceState: Bundle?,
        rootKey: String?,
    ) {
        addPreferencesFromResource(R.xml.preference_general)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val settingsView =
            inflater.inflate(R.layout.fragment_settings, container, false) as ViewGroup
        settingsView.addView(super.onCreateView(inflater, container, savedInstanceState))
        return settingsView
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        binding.settingsToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        viewModel.generalConfiguration.observe(viewLifecycleOwner) {
            enableFingerprintSettings(it.modalities)
        }
        viewModel.experimentalConfiguration.observe(viewLifecycleOwner) {
            showFaceAutoCaptureSetting(isVisible = it.faceAutoCaptureEnabled)
        }
        viewModel.languagePreference.observe(viewLifecycleOwner) {
            loadSelectedLanguage(it)
        }
        viewModel.configUpdated.observe(
            viewLifecycleOwner,
            LiveDataEventObserver {
                Toast
                    .makeText(
                        requireContext(),
                        IDR.string.dashboard_preference_update_config_finished,
                        Toast.LENGTH_SHORT,
                    ).show()
            },
        )
        viewModel.sinceConfigLastUpdated.observe(
            viewLifecycleOwner,
            LiveDataEventWithContentObserver<String> { lastUpdated ->
                getUpdateConfig()?.summary = getString(
                    IDR.string.dashboard_preference_summary_update_config_last_updated,
                    lastUpdated,
                )
            },
        )
        bindClickListeners()
    }

    private fun enableFingerprintSettings(modalities: List<GeneralConfiguration.Modality>) {
        getFingerSelectionPreference()?.isVisible = modalities.contains(FINGERPRINT)
    }

    private fun bindClickListeners() {
        SettingsPasswordDialogFragment.registerForResult(
            fragmentManager = childFragmentManager,
            lifecycleOwner = this,
            onSuccess = { action ->
                viewModel.unlockSettings()
                when (action) {
                    ACTION_LANGUAGE -> createLanguageSelectionDialog().show()
                }
            },
        )
        getLanguagePreference()?.setOnPreferenceClickListener {
            showPasswordIfRequired(ACTION_LANGUAGE) { createLanguageSelectionDialog().show() }
            true
        }

        getFingerSelectionPreference()?.setOnPreferenceClickListener {
            findNavController().navigateSafely(
                this@SettingsFragment,
                SettingsFragmentDirections.actionSettingsFragmentToFingerSelectionFragment(),
            )
            true
        }

        getSyncInfoPreference()?.setOnPreferenceClickListener {
            findNavController().navigateSafely(
                this@SettingsFragment,
                SettingsFragmentDirections.actionSettingsFragmentToSyncInfoFragment(),
            )
            true
        }

        getUpdateConfig()?.setOnPreferenceClickListener {
            updateConfiguration()
            true
        }

        getAboutPreference()?.setOnPreferenceClickListener {
            findNavController().navigateSafely(
                this@SettingsFragment,
                SettingsFragmentDirections.actionSettingsFragmentToAboutFragment(),
            )
            true
        }
    }

    private fun showPasswordIfRequired(
        action: String,
        cb: () -> Unit,
    ) {
        val password = viewModel.settingsLocked.value?.getNullablePassword()
        if (password != null) {
            SettingsPasswordDialogFragment
                .newInstance(
                    passwordToMatch = password,
                    action = action,
                ).show(childFragmentManager, SettingsPasswordDialogFragment.TAG)
        } else {
            cb()
        }
    }

    private fun createLanguageSelectionDialog(): AlertDialog {
        val languagesOptions = viewModel.generalConfiguration.value
            ?.languageOptions
            .orEmpty()
        val languagesCodeToName = computeAvailableLanguageCodeAndName(languagesOptions)
        val languageNames = languagesCodeToName.map { it.second }.toTypedArray()

        val selectedLanguage = viewModel.languagePreference.value
            ?.let { selectedCode -> languagesCodeToName.indexOfFirst { selectedCode == it.first } }
            ?: ArrayAdapter.NO_SELECTION

        return MaterialAlertDialogBuilder(requireContext())
            .setSingleChoiceItems(languageNames, selectedLanguage) { _, which -> handleLanguageChange(languagesCodeToName[which].first) }
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

    private fun updateConfiguration() {
        viewModel.scheduleConfigUpdate()
        getUpdateConfig()?.setSummary(IDR.string.dashboard_preference_update_config_scheduled)
    }

    private fun getLanguagePreference(): Preference? = findPreference(getString(R.string.preference_select_language_key))

    private fun getFingerSelectionPreference(): Preference? = findPreference(getString(R.string.preference_select_fingers_key))

    private fun getSyncInfoPreference(): Preference? = findPreference(getString(R.string.preference_sync_info_key))

    private fun getUpdateConfig(): Preference? = findPreference(getString(R.string.preference_update_config_key))

    private fun getAboutPreference(): Preference? = findPreference(getString(R.string.preference_app_details_key))

    private fun getFaceAutoCapturePreference(): Preference? = findPreference(getString(R.string.preference_enable_face_auto_capture))

    private fun showFaceAutoCaptureSetting(isVisible: Boolean) {
        getFaceAutoCapturePreference()?.isVisible = isVisible
    }

    companion object {
        private const val ACTION_LANGUAGE = "language"
    }
}
