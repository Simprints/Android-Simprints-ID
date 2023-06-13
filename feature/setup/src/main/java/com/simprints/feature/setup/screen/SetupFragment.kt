package com.simprints.feature.setup.screen

import android.Manifest
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.simprints.core.tools.extentions.hasPermission
import com.simprints.feature.setup.R
import com.simprints.feature.setup.SetupResult
import com.simprints.infra.uibase.navigation.finishWithResult
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
internal class SetupFragment : Fragment(R.layout.fragment_setup) {

    private val launchLocationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            // TODO start location collection
        }
        finishWithResult(granted)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // TODO see if other setup is required

        if (requireActivity().hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            finishWithResult(true)
        } else {
            launchLocationPermissionRequest.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun finishWithResult(hasPermission: Boolean) {
        findNavController().finishWithResult(this, SetupResult(hasPermission))
    }

}
