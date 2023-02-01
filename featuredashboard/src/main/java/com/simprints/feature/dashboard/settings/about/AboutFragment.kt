package com.simprints.feature.dashboard.settings.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.simprints.core.DeviceID
import com.simprints.core.PackageVersionName
import com.simprints.core.tools.viewbinding.viewBinding
import com.simprints.feature.dashboard.R
import com.simprints.feature.dashboard.databinding.FragmentSettingsAboutBinding
import com.simprints.infra.config.domain.models.GeneralConfiguration.Modality.FINGERPRINT
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
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
        AlertDialog.Builder(requireContext())
            .setTitle(getString(IDR.string.confirmation_logout_title))
            .setMessage(getString(IDR.string.confirmation_logout_message))
            .setPositiveButton(
                getString(IDR.string.logout)
            ) { _, _ ->
                viewModel.logout()
                findNavController().navigate(R.id.action_aboutFragment_to_requestLoginFragment)
            }
            .setNegativeButton(
                getString(IDR.string.confirmation_logout_cancel), null
            ).create()
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preference_about)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val settingsView =
            inflater.inflate(R.layout.fragment_settings_about, container, false) as ViewGroup
        settingsView.addView(super.onCreateView(inflater, container, savedInstanceState))
        return settingsView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
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
    }

    private fun initLayout() {
        getAppVersionPreference()?.summary = packageVersionName
        getDeviceIdPreference()?.summary = deviceId
        getLogoutPreference()?.setOnPreferenceClickListener {
            activity?.runOnUiThread { confirmationDialogForLogout.show() }
            true
        }
    }


    private fun getAppVersionPreference(): Preference? =
        findPreference(getString(R.string.preference_app_version_key))

    private fun getDeviceIdPreference(): Preference? =
        findPreference(getString(R.string.preference_device_id_key))

    private fun getScannerVersionPreference(): Preference? =
        findPreference(getString(R.string.preference_scanner_version_key))

    private fun getSyncAndSearchConfigurationPreference(): Preference? =
        findPreference(getString(R.string.preference_sync_and_search_key))

    private fun getLogoutPreference(): Preference? =
        findPreference(getString(R.string.preference_logout_key))

    private fun String.lowerCaseCapitalized() =
        lowercase(Locale.getDefault()).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
}
