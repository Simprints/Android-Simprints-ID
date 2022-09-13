package com.simprints.id.activities.requestLogin

import android.os.Bundle
import com.simprints.core.sharedpreferences.PreferencesManager
import com.simprints.core.tools.activity.BaseSplitActivity
import com.simprints.core.tools.viewbinding.viewBinding
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.databinding.ActivityFrontBinding
import com.simprints.id.tools.extensions.deviceId
import com.simprints.id.tools.extensions.packageVersionName
import javax.inject.Inject
import com.simprints.infraresources.R as IDR

open class RequestLoginActivity : BaseSplitActivity() {

    @Inject lateinit var preferencesManager: PreferencesManager

    lateinit var app: Application
    private val binding by viewBinding(ActivityFrontBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as Application).component.inject(this)
        app = application as Application
        title = getString(IDR.string.requestLogin_title)

        setContentView(binding.root)
        setTextInLayout()

        initSimprintsIdVersionTextView(packageVersionName)
    }

    private fun setTextInLayout() {
        binding.libSimprintsVersionTextView.text = getString(IDR.string.libsimprints_label)
        binding.simprintsIdVersionTextView.text = getString(R.string.simprints_label)
        binding.requestLogin.text = getString(IDR.string.requestLogin_message)
        binding.tvDeviceId.text = getString(IDR.string.device_id, app.deviceId)
    }

    private fun initSimprintsIdVersionTextView(simprintsIdVersion: String) {
        val simprintsIdVersionString = String.format(getString(IDR.string.front_simprintsId_version), simprintsIdVersion)
        binding.simprintsIdVersionTextView.text = simprintsIdVersionString
    }
}
