package com.simprints.face.configuration.screen

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.simprints.core.livedata.LiveDataEventWithContentObserver
import com.simprints.core.tools.utils.TimeUtils
import com.simprints.face.configuration.FaceConfigurationResult
import com.simprints.face.configuration.R
import com.simprints.face.configuration.databinding.FragmentConfigurationBinding
import com.simprints.face.configuration.data.ErrorType
import com.simprints.face.configuration.data.FaceConfigurationState
import com.simprints.feature.alert.AlertContract
import com.simprints.feature.alert.AlertResult
import com.simprints.infra.facebiosdk.initialization.FaceBioSdkInitializer
import com.simprints.infra.logging.LoggingConstants.CrashReportTag
import com.simprints.infra.logging.Simber
import com.simprints.infra.uibase.navigation.finishWithResult
import com.simprints.infra.uibase.navigation.handleResult
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.simprints.infra.resources.R as IDR

@AndroidEntryPoint
internal class FaceConfigurationFragment : Fragment(R.layout.fragment_configuration) {

    private val viewModel: FaceConfigurationViewModel by viewModels()
    private val binding by viewBinding(FragmentConfigurationBinding::bind)

    private val args: FaceConfigurationFragmentArgs by navArgs()

    @Inject
    lateinit var faceBioSdkInitializer: FaceBioSdkInitializer


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        findNavController().handleResult<AlertResult>(
            viewLifecycleOwner,
            R.id.faceConfigurationFragment,
            AlertContract.DESTINATION,
        ) { result ->
            findNavController().finishWithResult(
                this,
                FaceConfigurationResult(false, ErrorType.reasonFromPayload(result.payload))
            )
        }

        observeViewModel()
        viewModel.retrieveLicense(args.projectId, args.deviceId)
    }

    private fun observeViewModel() {
        viewModel.configurationState.observe(viewLifecycleOwner, LiveDataEventWithContentObserver {
            when (it) {
                FaceConfigurationState.Started -> renderStarted()
                FaceConfigurationState.Downloading -> renderDownloading()
                is FaceConfigurationState.FinishedWithSuccess -> renderFinishedWithSuccess(it.license)
                is FaceConfigurationState.FinishedWithError -> renderFinishedWithError(it.errorCode)
                is FaceConfigurationState.FinishedWithBackendMaintenanceError -> renderFinishedWithBackendMaintenanceError(
                    it.estimatedOutage
                )
            }
        })
    }

    private fun renderStarted() {
        binding.configurationTxt.setText(IDR.string.face_configuration_started)
    }

    private fun renderDownloading() {
        binding.configurationTxt.setText(IDR.string.face_configuration_downloading)
    }

    private fun renderFinishedWithSuccess(license: String) {
        if (faceBioSdkInitializer.tryInitWithLicense(requireActivity(), license)) {
            findNavController().finishWithResult(this, FaceConfigurationResult(true))
        } else {
            viewModel.deleteInvalidLicense()
            Simber.tag(CrashReportTag.FACE_LICENSE.name).i("License is invalid")
            findNavController().navigate(
                R.id.action_global_errorFragment,
                ErrorType.LICENSE_INVALID.toAlertArgs()
            )
        }
    }

    private fun renderFinishedWithError(errorCode: String) {
        val errorTitle = "${getString(IDR.string.error_configuration_error_title)} ($errorCode)"
        Simber.tag(CrashReportTag.FACE_LICENSE.name).i("Error with configuration download. Error = $errorTitle")
        findNavController().navigate(
            R.id.action_global_errorFragment,
            ErrorType.CONFIGURATION_ERROR.apply { this.customTitle = errorTitle }.toAlertArgs()
        )
    }

    private fun renderFinishedWithBackendMaintenanceError(estimatedOutage: Long?) {
        val errorMessage = if (estimatedOutage != null && estimatedOutage != 0L) {
            getString(
                IDR.string.error_backend_maintenance_with_time_message,
                TimeUtils.getFormattedEstimatedOutage(estimatedOutage)
            )
        } else {
            getString(IDR.string.error_backend_maintenance_message)
        }

        Simber.tag(CrashReportTag.FACE_LICENSE.name).i("Error with configuration download. The backend is under maintenance")
        findNavController().navigate(
            R.id.action_global_errorFragment,
            ErrorType.BACKEND_MAINTENANCE_ERROR.apply { this.customMessage = errorMessage }.toAlertArgs()
        )
    }
}
