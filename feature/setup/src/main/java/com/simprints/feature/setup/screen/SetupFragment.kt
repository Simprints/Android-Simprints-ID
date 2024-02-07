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
import com.simprints.feature.setup.R
import com.simprints.feature.setup.SetupResult
import com.simprints.infra.uibase.navigation.finishWithResult
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
internal class SetupFragment : Fragment(R.layout.fragment_setup) {

    private val viewModel: SetupViewModel by viewModels()

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
        finishWithResult() // Notifications permission is best-effort
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.requestLocationPermission.observe(viewLifecycleOwner) {
            if (requireActivity().hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
                viewModel.collectLocation()
                viewModel.requestNotificationsPermission()
            } else {
                launchLocationPermissionRequest.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }

        viewModel.requestNotificationPermission.observe(viewLifecycleOwner) {
            if (requireActivity().hasPermission(Manifest.permission.POST_NOTIFICATIONS)) {
                finishWithResult()
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                launchNotificationPermissionRequest.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                finishWithResult()
            }
        }

        viewModel.start()
    }

    private fun finishWithResult() {
        // Always finish with true result, even if permissions are not granted
        findNavController().finishWithResult(this, SetupResult(permissionGranted = true))
    }
}
