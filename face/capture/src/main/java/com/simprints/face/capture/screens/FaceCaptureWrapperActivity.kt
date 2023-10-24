package com.simprints.face.capture.screens

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.Keep
import androidx.navigation.findNavController
import com.simprints.core.tools.activity.BaseActivity
import com.simprints.face.capture.R
import com.simprints.face.capture.databinding.ActivityFaceCaptureWrapperBinding
import com.simprints.infra.uibase.navigation.handleResult
import com.simprints.infra.uibase.viewbinding.viewBinding
import com.simprints.face.capture.FaceCaptureContract
import dagger.hilt.android.AndroidEntryPoint

@Keep
@AndroidEntryPoint
class FaceCaptureWrapperActivity : BaseActivity() {

    private val binding by viewBinding(ActivityFaceCaptureWrapperBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.captureHost.handleResult<Parcelable>(this, R.id.faceCaptureControllerFragment) { result ->
            setResult(RESULT_OK, Intent().also { it.putExtra(FaceCaptureContract.RESULT, result) })
            finish()
        }
    }

    override fun onStart() {
        super.onStart()

        val args = intent.extras?.getBundle(FACE_CAPTURE_ARGS_EXTRA)
        if (args == null) {
            finish()
            return
        }
        findNavController(R.id.captureHost).setGraph(R.navigation.graph_face_capture, args)
    }

    companion object {

        const val FACE_CAPTURE_ARGS_EXTRA = "face_capture_args"
    }
}
