package com.simprints.face.configuration

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.simprints.core.livedata.LiveDataEventObserver
import com.simprints.core.livedata.LiveDataEventWithContentObserver
import com.simprints.face.R
import com.simprints.face.models.RankOneInitializer
import com.simprints.face.orchestrator.FaceOrchestratorViewModel
import org.koin.android.viewmodel.ext.android.sharedViewModel
import org.koin.android.viewmodel.ext.android.viewModel

class ConfigurationFragment : Fragment(R.layout.configuration_fragment) {
    private val mainVm: FaceOrchestratorViewModel by sharedViewModel()
    private val viewModel: ConfigurationViewModel by viewModel()
    private val args: ConfigurationFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        // TODO create correct UI
        viewModel.retrieveLicense(args.projectId, args.deviceId)
    }

    private fun observeViewModel() {
        viewModel.licenseRetrieved.observe(viewLifecycleOwner, LiveDataEventWithContentObserver {
            mainVm.configurationFinished(RankOneInitializer.tryInitWithLicense(requireActivity(), it))
        })

        viewModel.configurationFailed.observe(viewLifecycleOwner, LiveDataEventObserver {
            mainVm.configurationFinished(false)
        })
    }
}
