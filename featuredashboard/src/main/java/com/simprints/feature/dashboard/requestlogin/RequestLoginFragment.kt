package com.simprints.feature.dashboard.requestlogin

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.simprints.core.DeviceID
import com.simprints.core.PackageVersionName
import com.simprints.core.tools.viewbinding.viewBinding
import com.simprints.feature.dashboard.R
import com.simprints.feature.dashboard.databinding.FragmentRequestLoginBinding
import com.simprints.infra.login.LoginManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.simprints.infra.resources.R as IDR

@AndroidEntryPoint
internal class RequestLoginFragment : Fragment(R.layout.fragment_request_login) {

    private val binding by viewBinding(FragmentRequestLoginBinding::bind)

    @PackageVersionName
    @Inject
    lateinit var packageVersionName: String

    @DeviceID
    @Inject
    lateinit var deviceId: String

    @Inject
    lateinit var loginManager: LoginManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvDeviceId.text = getString(IDR.string.device_id, deviceId)
        binding.simprintsIdVersionTextView.text =
            String.format(getString(IDR.string.front_simprintsId_version), packageVersionName)
    }

    override fun onResume() {
        super.onResume()
        if (loginManager.signedInProjectId.isNotEmpty() && loginManager.signedInUserId.isNotEmpty())
            findNavController().navigate(R.id.action_requestLoginFragment_to_mainFragment)
    }
}
