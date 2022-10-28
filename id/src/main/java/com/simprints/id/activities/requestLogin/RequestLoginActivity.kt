package com.simprints.id.activities.requestLogin

import android.os.Bundle
import com.simprints.core.DeviceID
import com.simprints.core.PackageVersionName
import com.simprints.core.tools.activity.BaseSplitActivity
import com.simprints.core.tools.viewbinding.viewBinding
import com.simprints.id.databinding.ActivityFrontBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.simprints.infra.resources.R as IDR

@AndroidEntryPoint
class RequestLoginActivity : BaseSplitActivity() {

    @PackageVersionName
    @Inject
    lateinit var packageVersionName: String

    @DeviceID
    @Inject
    lateinit var deviceId: String

    private val binding by viewBinding(ActivityFrontBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = getString(IDR.string.requestLogin_title)

        setContentView(binding.root)
        setTextInLayout()

        initSimprintsIdVersionTextView(packageVersionName)
    }

    private fun setTextInLayout() {
        binding.libSimprintsVersionTextView.text = getString(IDR.string.libsimprints_label)
        binding.simprintsIdVersionTextView.text = getString(IDR.string.simprints_label)
        binding.requestLogin.text = getString(IDR.string.requestLogin_message)
        binding.tvDeviceId.text = getString(IDR.string.device_id, deviceId)
    }

    private fun initSimprintsIdVersionTextView(simprintsIdVersion: String) {
        val simprintsIdVersionString =
            String.format(getString(IDR.string.front_simprintsId_version), simprintsIdVersion)
        binding.simprintsIdVersionTextView.text = simprintsIdVersionString
    }
}
