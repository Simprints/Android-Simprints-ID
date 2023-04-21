package com.simprints.face.configuration

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.simprints.core.livedata.LiveDataEventWithContentObserver
import com.simprints.core.tools.utils.TimeUtils
import com.simprints.core.tools.viewbinding.viewBinding
import com.simprints.face.R
import com.simprints.face.databinding.FragmentConfigurationBinding
import com.simprints.face.initializers.SdkInitializer
import com.simprints.face.orchestrator.FaceOrchestratorViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ConfigurationFragment : Fragment(R.layout.fragment_configuration) {
    private val mainVm: FaceOrchestratorViewModel by activityViewModels()
    private val viewModel: ConfigurationViewModel by viewModels()
    private val binding by viewBinding(FragmentConfigurationBinding::bind)

    private val args: ConfigurationFragmentArgs by navArgs()

    @Inject
    lateinit var sdkInitializer: SdkInitializer


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        viewModel.retrieveLicense(args.projectId, args.deviceId)
    }

    private fun observeViewModel() {
        viewModel.configurationState.observe(viewLifecycleOwner, LiveDataEventWithContentObserver {
            when (it) {
                ConfigurationState.Started -> renderStarted()
                ConfigurationState.Downloading -> renderDownloading()
                is ConfigurationState.FinishedWithSuccess -> renderFinishedWithSuccess(it.license)
                is ConfigurationState.FinishedWithError -> renderFinishedWithError(it.errorCode)
                is ConfigurationState.FinishedWithBackendMaintenanceError -> renderFinishedWithBackendMaintenanceError(
                    it.estimatedOutage
                )
            }
        })
    }

    private fun renderStarted() {
        binding.configurationTxt.setText(R.string.face_configuration_started)
    }

    private fun renderDownloading() {
        binding.configurationTxt.setText(R.string.face_configuration_downloading)
    }

    private fun renderFinishedWithSuccess(license: String) {
        if (sdkInitializer.tryInitWithLicense(requireActivity(), license)) {
            mainVm.configurationFinished(true)
        } else {
            viewModel.deleteInvalidLicense()
            mainVm.invalidLicense()
        }
    }

    private fun renderFinishedWithError(errorCode: String) {
        val errorTitle = "${getString(R.string.error_configuration_error_title)} ($errorCode)"
        mainVm.configurationFinished(false, errorTitle)
    }

    private fun renderFinishedWithBackendMaintenanceError(estimatedOutage: Long?) {
        val errorMessage =
            if (estimatedOutage != null && estimatedOutage != 0L) getString(
                R.string.error_backend_maintenance_with_time_message,
                TimeUtils.getFormattedEstimatedOutage(estimatedOutage)
            ) else getString(R.string.error_backend_maintenance_message)
        mainVm.configurationFinished(false, errorMessage = errorMessage)
    }
}
