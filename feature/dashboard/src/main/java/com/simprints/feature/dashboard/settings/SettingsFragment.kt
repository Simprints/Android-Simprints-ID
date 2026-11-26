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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.simprints.feature.dashboard.DashboardActivity
import com.simprints.feature.dashboard.R
import com.simprints.feature.dashboard.databinding.FragmentSettingsBinding
import com.simprints.feature.dashboard.settings.SettingsUiState.UpdateConfigSummary
import com.simprints.feature.dashboard.view.StateDrivenDialog
import com.simprints.infra.uibase.navigation.navigateSafely
import com.simprints.infra.uibase.password.SettingsPasswordDialogFragment
import com.simprints.infra.uibase.view.applySystemBarInsets
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import com.simprints.infra.resources.R as IDR

@AndroidEntryPoint
internal class SettingsFragment : PreferenceFragmentCompat() {
    private val viewModel by viewModels<SettingsViewModel>()
    private val binding by viewBinding(FragmentSettingsBinding::bind)
    private val languageDialog = StateDrivenDialog(
        onCancelledByUser = { viewModel.onAction(SettingsUiAction.LanguageDialogDismissed) },
    )

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
        applySystemBarInsets(view)

        binding.settingsToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        bindClickListeners()
        observeState()
    }

    override fun onDestroyView() {
        languageDialog.release()
        super.onDestroyView()
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.onAction(SettingsUiAction.ScreenStarted)
                launch { viewModel.uiState.collect(::render) }
                launch { viewModel.uiEffect.collect(::handleEffect) }
            }
        }
    }

    private fun render(state: SettingsUiState) {
        state.isFingerprintSettingsVisible?.let { isVisible ->
            getFingerSelectionPreference()?.isVisible = isVisible
            getAudioAlertPreference()?.isVisible = isVisible
        }
        state.isFaceAutoCaptureVisible?.let { isVisible ->
            getFaceAutoCapturePreference()?.isVisible = isVisible
        }
        state.languageCode?.let(::loadSelectedLanguage)
        when (val summary = state.updateConfigSummary) {
            UpdateConfigSummary.Empty -> Unit

            UpdateConfigSummary.Scheduled -> getUpdateConfig()?.setSummary(IDR.string.dashboard_preference_update_config_scheduled)

            is UpdateConfigSummary.LastUpdated -> getUpdateConfig()?.summary = getString(
                IDR.string.dashboard_preference_summary_update_config_last_updated,
                summary.sinceLastUpdate,
            )
        }
        languageDialog.render(state.isLanguageDialogVisible) {
            createLanguageSelectionDialog(state.languageOptions, state.languageCode)
        }
    }

    private fun handleEffect(effect: SettingsUiEffect) {
        when (effect) {
            is SettingsUiEffect.ShowPasswordDialog ->
                SettingsPasswordDialogFragment
                    .newInstance(
                        passwordToMatch = effect.password,
                        action = effect.action.key,
                    ).show(childFragmentManager, SettingsPasswordDialogFragment.TAG)

            SettingsUiEffect.ConfigUpdated ->
                Toast
                    .makeText(
                        requireContext(),
                        IDR.string.dashboard_preference_update_config_finished,
                        Toast.LENGTH_SHORT,
                    ).show()

            SettingsUiEffect.RelaunchApp -> clearActivityStackAndRelaunchApp()
        }
    }

    private fun bindClickListeners() {
        SettingsPasswordDialogFragment.registerForResult(
            fragmentManager = childFragmentManager,
            lifecycleOwner = this,
            onSuccess = { action ->
                SettingsGatedAction.fromKey(action)?.let { viewModel.onAction(SettingsUiAction.PasswordUnlocked(it)) }
            },
        )
        getLanguagePreference()?.setOnPreferenceClickListener {
            viewModel.onAction(SettingsUiAction.LanguageClicked)
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
            viewModel.onAction(SettingsUiAction.UpdateConfigClicked)
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

    private fun createLanguageSelectionDialog(
        languageOptions: List<String>,
        selectedCode: String?,
    ): AlertDialog {
        val languagesCodeToName = computeAvailableLanguageCodeAndName(languageOptions)
        val languageNames = languagesCodeToName.map { it.second }.toTypedArray()

        val selectedLanguage = selectedCode
            ?.let { code -> languagesCodeToName.indexOfFirst { code == it.first } }
            ?: ArrayAdapter.NO_SELECTION

        return MaterialAlertDialogBuilder(requireContext())
            .setSingleChoiceItems(languageNames, selectedLanguage) { dialog, which ->
                // Dismissed straight away: a second pick during the save would otherwise relaunch the app twice.
                dialog.dismiss()
                handleLanguageChange(languagesCodeToName[which].first)
            }.create()
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

    // The summary is rendered from state and the relaunch comes as an effect, both after the save (KI-3 fix).
    private fun handleLanguageChange(newLanguageCode: String) {
        viewModel.onAction(SettingsUiAction.LanguageSelected(newLanguageCode))
    }

    private fun clearActivityStackAndRelaunchApp() {
        activity?.finishAffinity()
        activity?.startActivity(Intent(context, DashboardActivity::class.java))
        activity?.overridePendingTransition(0, 0)
    }

    private fun getLanguagePreference(): Preference? = findPreference(getString(R.string.preference_select_language_key))

    private fun getFingerSelectionPreference(): Preference? = findPreference(getString(R.string.preference_select_fingers_key))

    private fun getSyncInfoPreference(): Preference? = findPreference(getString(R.string.preference_sync_info_key))

    private fun getUpdateConfig(): Preference? = findPreference(getString(R.string.preference_update_config_key))

    private fun getAboutPreference(): Preference? = findPreference(getString(R.string.preference_app_details_key))

    private fun getAudioAlertPreference(): Preference? = findPreference(getString(R.string.preference_enable_audio_on_scan_complete_key))

    private fun getFaceAutoCapturePreference(): Preference? = findPreference(getString(R.string.preference_enable_face_auto_capture))
}
