package com.simprints.feature.dashboard.settings.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.simprints.core.DeviceID
import com.simprints.core.PackageVersionName
import com.simprints.core.livedata.LiveDataEventObserver
import com.simprints.core.livedata.LiveDataEventWithContentObserver
import com.simprints.feature.dashboard.R
import com.simprints.feature.dashboard.databinding.FragmentSettingsAboutBinding
import com.simprints.feature.dashboard.settings.password.SettingsPasswordDialogFragment
import com.simprints.infra.config.store.models.GeneralConfiguration.Modality.FINGERPRINT
import com.simprints.infra.uibase.navigation.navigateSafely
import com.simprints.infra.uibase.system.Clipboard
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
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

    private val confirmationDialogForLogout: AlertDialog by lazy {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(IDR.string.dashboard_logout_confirmation_title))
            .setMessage(getString(IDR.string.dashboard_logout_confirmation_message))
            .setPositiveButton(
                getString(IDR.string.dashboard_logout_confirmation_log_out_button),
            ) { _, _ -> viewModel.processLogoutRequest() }
            .setNegativeButton(getString(IDR.string.dashboard_logout_confirmation_cancel_button), null)
            .create()
    }

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
        binding.settingsAboutToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        initLayout()

        viewModel.syncAndSearchConfig.observe(viewLifecycleOwner) {
            getSyncAndSearchConfigurationPreference()?.summary =
                "${it.sync.lowerCaseCapitalized()} Sync - ${it.search.lowerCaseCapitalized()} Search"
        }
        viewModel.modalities.observe(viewLifecycleOwner) {
            getScannerVersionPreference()?.isVisible = it.contains(FINGERPRINT)
        }
        viewModel.recentUserActivity.observe(viewLifecycleOwner) {
            getScannerVersionPreference()?.summary = it.lastScannerVersion
        }
        viewModel.logoutDestinationEvent.observe(
            viewLifecycleOwner,
            LiveDataEventWithContentObserver {
                val destination = when (it) {
                    LogoutDestination.LogoutDataSyncScreen -> AboutFragmentDirections.actionAboutFragmentToLogoutNavigation()
                    LogoutDestination.LoginScreen -> AboutFragmentDirections.actionAboutFragmentToRequestLoginFragment()
                }
                findNavController().navigateSafely(this, destination)
            },
        )
        viewModel.openTroubleshooting.observe(
            viewLifecycleOwner,
            LiveDataEventObserver {
                showPasswordIfRequired(ACTION_TROUBLESHOOTING) { openTroubleshooting() }
            },
        )

        SettingsPasswordDialogFragment.registerForResult(
            fragmentManager = childFragmentManager,
            lifecycleOwner = this,
            onSuccess = { action ->
                viewModel.unlockSettings()
                when (action) {
                    ACTION_LOGOUT -> viewModel.processLogoutRequest()
                    ACTION_TROUBLESHOOTING -> openTroubleshooting()
                }
            },
        )
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
            viewModel.troubleshootingClick()
            true
        }

        getLogoutPreference()?.setOnPreferenceClickListener {
            activity?.runOnUiThread {
                val password = viewModel.settingsLocked.value?.getNullablePassword()
                if (password != null) {
                    showPasswordIfRequired(ACTION_LOGOUT) { viewModel.processLogoutRequest() }
                } else {
                    confirmationDialogForLogout.show()
                }
            }
            true
        }
    }

    private fun openTroubleshooting() {
        findNavController().navigateSafely(this, AboutFragmentDirections.actionAboutFragmentToTroubleshooting())
    }

    private fun getAppVersionPreference(): Preference? = findPreference(getString(R.string.preference_app_version_key))

    private fun getDeviceIdPreference(): Preference? = findPreference(getString(R.string.preference_device_id_key))

    private fun getScannerVersionPreference(): Preference? = findPreference(getString(R.string.preference_scanner_version_key))

    private fun getSyncAndSearchConfigurationPreference(): Preference? = findPreference(getString(R.string.preference_sync_and_search_key))

    private fun getLogoutPreference(): Preference? = findPreference(getString(R.string.preference_logout_key))

    private fun String.lowerCaseCapitalized() =
        lowercase(Locale.getDefault()).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

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

    companion object {
        private const val ACTION_LOGOUT = "logout"
        private const val ACTION_TROUBLESHOOTING = "troubleshooting"
    }
}
