package com.simprints.face.configuration

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.simprints.core.livedata.LiveDataEventWithContentObserver
import com.simprints.face.R
import com.simprints.face.databinding.FragmentConfigurationBinding
import com.simprints.face.initializers.SdkInitializer
import com.simprints.face.orchestrator.FaceOrchestratorViewModel
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel
import org.koin.android.viewmodel.ext.android.viewModel

class ConfigurationFragment : Fragment() {
    private val mainVm: FaceOrchestratorViewModel by sharedViewModel()
    private val viewModel: ConfigurationViewModel by viewModel()
    private var binding: FragmentConfigurationBinding? = null

    private val args: ConfigurationFragmentArgs by navArgs()
    private val sdkInitializer: SdkInitializer by inject()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentConfigurationBinding.inflate(inflater, container, false)
        return binding?.root
    }

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
                ConfigurationState.FinishedWithError -> renderFinishedWithError()
            }
        })
    }

    private fun renderStarted() {
        binding?.configurationTxt?.setText(R.string.face_configuration_started)
    }

    private fun renderDownloading() {
        binding?.configurationTxt?.setText(R.string.face_configuration_downloading)
    }

    private fun renderFinishedWithSuccess(license: String) {
        if (sdkInitializer.tryInitWithLicense(requireActivity(), license)) {
            mainVm.configurationFinished(true)
        } else {
            viewModel.deleteInvalidLicense()
            mainVm.invalidLicense()
        }
    }

    private fun renderFinishedWithError() {
        mainVm.configurationFinished(false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
