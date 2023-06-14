package com.simprints.feature.setup.screen

import android.Manifest
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.simprints.core.tools.extentions.hasPermission
import com.simprints.feature.setup.LocationStore
import com.simprints.feature.setup.R
import com.simprints.feature.setup.SetupResult
import com.simprints.infra.uibase.navigation.finishWithResult
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
internal class SetupFragment : Fragment(R.layout.fragment_setup) {

    @Inject
    lateinit var locationStore: LocationStore

    private val launchLocationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            locationStore.collectLocationInBackground()
        }
        finishWithResult(granted)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (requireActivity().hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            locationStore.collectLocationInBackground()
            finishWithResult(true)
        } else {
            launchLocationPermissionRequest.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun finishWithResult(hasPermission: Boolean) {
        findNavController().finishWithResult(this, SetupResult(hasPermission))
    }
}
