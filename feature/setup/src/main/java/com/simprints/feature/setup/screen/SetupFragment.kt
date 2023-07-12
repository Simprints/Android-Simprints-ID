package com.simprints.feature.setup.screen

import android.Manifest
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.simprints.core.tools.extentions.hasPermission
import com.simprints.feature.setup.R
import com.simprints.feature.setup.SetupResult
import com.simprints.infra.uibase.navigation.finishWithResult
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
internal class SetupFragment : Fragment(R.layout.fragment_setup) {

    private val viewModel: SetupViewModel by viewModels()

    private val launchLocationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            viewModel.collectLocation()
        }
        finishWithResult(granted)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.requestLocationPermission.observe(viewLifecycleOwner) {
            if (requireActivity().hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
                viewModel.collectLocation()
                finishWithResult(true)
            } else {
                launchLocationPermissionRequest.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }

        viewModel.finish.observe(viewLifecycleOwner, ::finishWithResult)

        viewModel.start()
    }

    private fun finishWithResult(hasPermission: Boolean) {
        findNavController().finishWithResult(this, SetupResult(hasPermission))
    }
}
