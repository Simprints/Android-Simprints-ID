package com.simprints.feature.dashboard.requestlogin

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.simprints.core.DeviceID
import com.simprints.core.PackageVersionName
import com.simprints.core.tools.viewbinding.viewBinding
import com.simprints.feature.dashboard.R
import com.simprints.feature.dashboard.databinding.FragmentRequestLoginBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.simprints.infra.resources.R as IDR

@AndroidEntryPoint
class RequestLoginFragment : Fragment(R.layout.fragment_request_login) {

    private val binding by viewBinding(FragmentRequestLoginBinding::bind)

    @PackageVersionName
    @Inject
    lateinit var packageVersionName: String

    @DeviceID
    @Inject
    lateinit var deviceId: String

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.libSimprintsVersionTextView.text = getString(IDR.string.libsimprints_label)
        binding.requestLogin.text = getString(IDR.string.requestLogin_message)
        binding.tvDeviceId.text = getString(IDR.string.device_id, deviceId)
        binding.simprintsIdVersionTextView.text =
            String.format(getString(IDR.string.front_simprintsId_version), packageVersionName)
    }
}
