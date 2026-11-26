package com.simprints.feature.dashboard.settings.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.simprints.core.DeviceID
import com.simprints.core.PackageVersionName
import com.simprints.feature.dashboard.R
import com.simprints.feature.dashboard.databinding.FragmentSettingsAboutBinding
import com.simprints.feature.dashboard.view.StateDrivenDialog
import com.simprints.infra.uibase.navigation.navigateSafely
import com.simprints.infra.uibase.password.SettingsPasswordDialogFragment
import com.simprints.infra.uibase.system.Clipboard
import com.simprints.infra.uibase.view.applySystemBarInsets
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject
import com.simprints.infra.resources.R as IDR

@AndroidEntryPoint
internal class AboutFragment : PreferenceFragmentCompat() {
    @Inject
    @PackageVersionName
    lateinit var packageVersionName: String

    @Inject
    @DeviceID
    lateinit var deviceId: String

    private val viewModel by viewModels<AboutViewModel>()
    private val binding by viewBinding(FragmentSettingsAboutBinding::bind)

    private val logoutConfirmationDialog = StateDrivenDialog(
        onCancelledByUser = { viewModel.onAction(AboutUiAction.LogoutConfirmationDismissed) },
    )

    override fun onCreatePreferences(
        savedInstanceState: Bundle?,
        rootKey: String?,
    ) {
        addPreferencesFromResource(R.xml.preference_about)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val settingsView =
            inflater.inflate(R.layout.fragment_settings_about, container, false) as ViewGroup
        settingsView.addView(super.onCreateView(inflater, container, savedInstanceState))
        return settingsView
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        applySystemBarInsets(view)
        binding.settingsAboutToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        initLayout()
        observeState()

        SettingsPasswordDialogFragment.registerForResult(
            fragmentManager = childFragmentManager,
            lifecycleOwner = this,
            onSuccess = { action ->
                AboutGatedAction.fromKey(action)?.let { viewModel.onAction(AboutUiAction.PasswordUnlocked(it)) }
            },
        )
    }

    override fun onDestroyView() {
        logoutConfirmationDialog.release()
        super.onDestroyView()
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect(::render) }
                launch { viewModel.uiEffect.collect(::handleEffect) }
            }
        }
    }

    private fun render(state: AboutUiState) {
        state.syncAndSearchConfig?.let {
            getSyncAndSearchConfigurationPreference()?.summary =
                "${it.sync.lowerCaseCapitalized()} Sync - ${it.search.lowerCaseCapitalized()} Search"
        }
        state.isScannerVersionVisible?.let { isVisible ->
            getScannerVersionPreference()?.isVisible = isVisible
        }
        state.lastScannerVersion?.let { lastScannerVersion ->
            getScannerVersionPreference()?.summary = lastScannerVersion
        }
        logoutConfirmationDialog.render(state.isLogoutConfirmationVisible, ::createLogoutConfirmationDialog)
    }

    private fun createLogoutConfirmationDialog() = MaterialAlertDialogBuilder(requireContext())
        .setTitle(getString(IDR.string.dashboard_logout_confirmation_title))
        .setMessage(getString(IDR.string.dashboard_logout_confirmation_message))
        .setPositiveButton(
            getString(IDR.string.dashboard_logout_confirmation_log_out_button),
        ) { _, _ -> viewModel.onAction(AboutUiAction.LogoutConfirmed) }
        .setNegativeButton(getString(IDR.string.dashboard_logout_confirmation_cancel_button)) { dialog, _ -> dialog.cancel() }
        .create()

    private fun handleEffect(effect: AboutUiEffect) {
        when (effect) {
            is AboutUiEffect.ShowPasswordDialog -> SettingsPasswordDialogFragment
                .newInstance(
                    passwordToMatch = effect.password,
                    action = effect.action.key,
                ).show(childFragmentManager, SettingsPasswordDialogFragment.TAG)

            AboutUiEffect.NavigateToLogoutSync -> findNavController().navigateSafely(
                this,
                AboutFragmentDirections.actionAboutFragmentToLogoutNavigation(),
            )

            AboutUiEffect.NavigateToTroubleshooting -> findNavController().navigateSafely(
                this,
                AboutFragmentDirections.actionAboutFragmentToTroubleshooting(),
            )
        }
    }

    private fun initLayout() {
        getAppVersionPreference()?.summary = packageVersionName
        getDeviceIdPreference()?.let { preference ->
            preference.summary = deviceId
            preference.setOnPreferenceClickListener {
                Toast
                    .makeText(
                        requireContext(),
                        IDR.string.dashboard_preference_copied_to_clipboard,
                        Toast.LENGTH_SHORT,
                    ).show()
                Clipboard.copyToClipboard(requireContext(), deviceId)
                true
            }
        }
        getSyncAndSearchConfigurationPreference()?.setOnPreferenceClickListener {
            viewModel.onAction(AboutUiAction.TroubleshootingClicked)
            true
        }

        getLogoutPreference()?.setOnPreferenceClickListener {
            viewModel.onAction(AboutUiAction.LogoutClicked)
            true
        }
    }

    private fun getAppVersionPreference(): Preference? = findPreference(getString(R.string.preference_app_version_key))

    private fun getDeviceIdPreference(): Preference? = findPreference(getString(R.string.preference_device_id_key))

    private fun getScannerVersionPreference(): Preference? = findPreference(getString(R.string.preference_scanner_version_key))

    private fun getSyncAndSearchConfigurationPreference(): Preference? = findPreference(getString(R.string.preference_sync_and_search_key))

    private fun getLogoutPreference(): Preference? = findPreference(getString(R.string.preference_logout_key))

    private fun String.lowerCaseCapitalized() =
        lowercase(Locale.getDefault()).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
}
