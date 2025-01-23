package com.simprints.feature.setup.screen

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.simprints.core.tools.extentions.hasPermission
import com.simprints.core.tools.utils.TimeUtils
import com.simprints.feature.alert.AlertContract
import com.simprints.feature.alert.AlertResult
import com.simprints.feature.setup.R
import com.simprints.feature.setup.SetupResult
import com.simprints.feature.setup.data.ErrorType
import com.simprints.feature.setup.databinding.FragmentSetupBinding
import com.simprints.infra.license.models.LicenseState.Downloading
import com.simprints.infra.license.models.LicenseState.FinishedWithBackendMaintenanceError
import com.simprints.infra.license.models.LicenseState.FinishedWithError
import com.simprints.infra.license.models.LicenseState.FinishedWithSuccess
import com.simprints.infra.license.models.LicenseState.Started
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.LICENSE
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.ORCHESTRATION
import com.simprints.infra.logging.Simber
import com.simprints.infra.uibase.navigation.finishWithResult
import com.simprints.infra.uibase.navigation.handleResult
import com.simprints.infra.uibase.navigation.navigateSafely
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import com.simprints.infra.resources.R as IDR

@AndroidEntryPoint
internal class SetupFragment : Fragment(R.layout.fragment_setup) {
    private val viewModel: SetupViewModel by viewModels()
    private val binding by viewBinding(FragmentSetupBinding::bind)
    private val launchLocationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isLocationPermissionGranted ->
        if (isLocationPermissionGranted) {
            viewModel.collectLocation()
        }
        viewModel.requestNotificationsPermission()
    }

    private val launchNotificationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { _ ->
        // Do nothing
    }

    // The setup steps are:
    // 1. Request location permission
    // 2. Request notification permission
    // 3. Download required licenses
    // 4. Return overall setup result

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        Simber.i("SetupFragment started", tag = ORCHESTRATION)

        findNavController().handleResult<AlertResult>(
            viewLifecycleOwner,
            R.id.setupFragment,
            AlertContract.DESTINATION,
        ) { result -> findNavController().finishWithResult(this, result) }
        // Request location permission
        viewModel.requestLocationPermission.observe(viewLifecycleOwner) {
            if (requireActivity().hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
                viewModel.collectLocation()
                viewModel.requestNotificationsPermission()
            } else {
                launchLocationPermissionRequest.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
        // Request notification permission
        viewModel.requestNotificationPermission.observe(viewLifecycleOwner) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                launchNotificationPermissionRequest.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            viewModel.downloadRequiredLicenses()
        }
        // Download required licenses
        observeDownloadLicenseState()
        // Overall setup result
        observeOverallSetupResult()
        // Start the setup process
        viewModel.start()
    }

    private fun observeOverallSetupResult() = viewModel.overallSetupResult.observe(viewLifecycleOwner) {
        // if the overall setup result is success, finish the setup flow else an alert will be shown
        if (it) {
            findNavController().finishWithResult(this, SetupResult(isSuccess = true))
        }
    }

    private fun observeDownloadLicenseState() = viewModel.downloadLicenseState.observe(viewLifecycleOwner) { licenseState ->
        when (licenseState) {
            Started -> renderStarted()
            Downloading -> renderDownloading()
            is FinishedWithSuccess -> {
                // Do nothing
            }

            is FinishedWithError -> renderFinishedWithError(licenseState.errorCode)
            is FinishedWithBackendMaintenanceError -> renderFinishedWithBackendMaintenanceError(
                licenseState.estimatedOutage,
            )
        }
    }

    private fun renderStarted() {
        binding.configurationTxt.setText(IDR.string.configuration_started)
    }

    private fun renderDownloading() {
        binding.configurationTxt.setText(IDR.string.configuration_downloading)
    }

    private fun renderFinishedWithError(errorCode: String) {
        Simber.i("Error with licence download. Error code = $errorCode", tag = LICENSE)
        val errorTitle = getString(IDR.string.configuration_generic_error_title, errorCode)
        findNavController().navigateSafely(
            this,
            R.id.action_global_errorFragment,
            ErrorType.CONFIGURATION_ERROR.apply { this.customTitle = errorTitle }.toAlertArgs(),
        )
    }

    private fun renderFinishedWithBackendMaintenanceError(estimatedOutage: Long?) {
        Simber.i("Error with licence download. The backend is under maintenance", tag = LICENSE)
        val errorMessage = if (estimatedOutage != null && estimatedOutage != 0L) {
            getString(
                IDR.string.error_backend_maintenance_with_time_message,
                TimeUtils.getFormattedEstimatedOutage(estimatedOutage),
            )
        } else {
            getString(IDR.string.error_backend_maintenance_message)
        }
        findNavController().navigateSafely(
            this,
            R.id.action_global_errorFragment,
            ErrorType.BACKEND_MAINTENANCE_ERROR
                .apply { this.customMessage = errorMessage }
                .toAlertArgs(),
        )
    }
}
