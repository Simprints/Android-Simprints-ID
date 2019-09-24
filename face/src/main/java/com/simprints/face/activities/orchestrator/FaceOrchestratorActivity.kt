package com.simprints.face.activities.orchestrator

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.simprints.core.livedata.LiveDataEventObserver
import com.simprints.face.activities.FaceCaptureActivity
import com.simprints.face.di.KoinInjector
import com.simprints.face.exceptions.InvalidFaceRequestException
import com.simprints.moduleapi.face.requests.IFaceRequest
import com.simprints.moduleapi.face.responses.IFaceResponse
import com.simprints.core.livedata.LiveDataEvent1Observer
import org.koin.android.viewmodel.ext.android.viewModel

class FaceOrchestratorActivity : AppCompatActivity() {
    private val viewModel: FaceOrchestratorViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        KoinInjector.acquireFaceKoinModules()

        val iFaceRequest: IFaceRequest = this.intent.extras?.getParcelable(IFaceRequest.BUNDLE_KEY)
            ?: throw InvalidFaceRequestException("No IFaceCaptureRequest found for FaceOrchestratorActivity")

        observeViewModel()

        viewModel.start(iFaceRequest)
    }

    override fun onDestroy() {
        KoinInjector.releaseFaceKoinModules()
        super.onDestroy()
    }

    private fun observeViewModel() {
        viewModel.startCapture.observe(this, LiveDataEventObserver {
            startActivityForResult(Intent(this, FaceCaptureActivity::class.java), CAPTURE_REQUEST)
        })
        viewModel.captureFinished.observe(this, LiveDataEvent1Observer {
            val intent = Intent().apply { putExtra(IFaceResponse.BUNDLE_KEY, it) }
            setResult(Activity.RESULT_OK, intent)
            finish()
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CAPTURE_REQUEST) {
                viewModel.captureFinished()
            }
        }
    }

    companion object {
        const val CAPTURE_REQUEST = 100
    }
}
