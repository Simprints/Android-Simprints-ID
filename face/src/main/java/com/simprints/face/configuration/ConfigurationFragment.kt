package com.simprints.face.configuration

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.simprints.core.livedata.LiveDataEventWithContentObserver
import com.simprints.core.tools.viewbinding.viewBinding
import com.simprints.face.R
import com.simprints.face.databinding.FragmentConfigurationBinding
import com.simprints.face.initializers.SdkInitializer
import com.simprints.face.orchestrator.FaceOrchestratorViewModel
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel
import org.koin.android.viewmodel.ext.android.viewModel

class ConfigurationFragment : Fragment(R.layout.fragment_configuration) {
    private val mainVm: FaceOrchestratorViewModel by sharedViewModel()
    private val viewModel: ConfigurationViewModel by viewModel()
    private val binding by viewBinding(FragmentConfigurationBinding::bind)

    private val args: ConfigurationFragmentArgs by navArgs()
    private val sdkInitializer: SdkInitializer by inject()


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
                is ConfigurationState.FinishedWithBackendMaintenanceError -> renderFinishedWithBackendMaitenanceError(it.estimatedOutage)
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
        mainVm.configurationFinished(false, errorCode)
    }

    private fun renderFinishedWithBackendMaitenanceError(estimatedOutage: Long?) {
        mainVm.configurationFinished(false, estimatedOutage = estimatedOutage)
    }
}
