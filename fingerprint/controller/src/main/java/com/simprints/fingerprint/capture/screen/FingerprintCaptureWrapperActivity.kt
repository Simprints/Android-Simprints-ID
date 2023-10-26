package com.simprints.fingerprint.capture.screen

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.Keep
import androidx.navigation.findNavController
import com.simprints.core.tools.activity.BaseActivity
import com.simprints.fingerprint.R
import com.simprints.fingerprint.capture.FingerprintCaptureContract
import com.simprints.fingerprint.databinding.ActivityFingerprintCaptureWrapperBinding
import com.simprints.infra.uibase.navigation.handleResult
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@Keep
@AndroidEntryPoint
class FingerprintCaptureWrapperActivity : BaseActivity() {

    private val binding by viewBinding(ActivityFingerprintCaptureWrapperBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.captureHost.handleResult<Parcelable>(this, R.id.fingerprintCaptureFragment) { result ->
            setResult(RESULT_OK, Intent().also { it.putExtra(FingerprintCaptureContract.RESULT, result) })
            finish()
        }
    }

    override fun onStart() {
        super.onStart()

        val args = intent.extras?.getBundle(FINGERPRINT_CAPTURE_ARGS_EXTRA)
        if (args == null) {
            finish()
            return
        }
        findNavController(R.id.captureHost).setGraph(R.navigation.graph_fingerprint_capture, args)
    }

    companion object {

        const val FINGERPRINT_CAPTURE_ARGS_EXTRA = "fingerprint_capture_args"
    }
}
