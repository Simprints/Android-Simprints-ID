package com.simprints.face.orchestrator

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.navigation.findNavController
import com.simprints.core.livedata.LiveDataEventWithContentObserver
import com.simprints.face.OrchestratorGraphDirections
import com.simprints.face.R
import com.simprints.face.base.FaceActivity
import com.simprints.face.capture.FaceCaptureActivity
import com.simprints.face.di.KoinInjector
import com.simprints.face.exceptions.InvalidFaceRequestException
import com.simprints.face.match.FaceMatchActivity
import com.simprints.moduleapi.face.requests.IFaceRequest
import com.simprints.moduleapi.face.responses.IFaceResponse
import org.koin.android.viewmodel.ext.android.viewModel

class FaceOrchestratorActivity : FaceActivity() {
    private val viewModel: FaceOrchestratorViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_orchestrator)

        val iFaceRequest: IFaceRequest = this.intent.extras?.getParcelable(IFaceRequest.BUNDLE_KEY)
            ?: throw InvalidFaceRequestException("No IFaceCaptureRequest found for FaceOrchestratorActivity")

        observeViewModel()

        viewModel.start(iFaceRequest)
    }

    private fun observeViewModel() {
        viewModel.startCapture.observe(this, LiveDataEventWithContentObserver {
            startActivityForResult(FaceCaptureActivity.getStartingIntent(this, it), CAPTURE_REQUEST)
        })
        viewModel.flowFinished.observe(this, LiveDataEventWithContentObserver {
            val intent = Intent().putExtra(IFaceResponse.BUNDLE_KEY, it)
            setResult(Activity.RESULT_OK, intent)
            finish()
        })
        viewModel.startMatching.observe(this, LiveDataEventWithContentObserver {
            startActivityForResult(FaceMatchActivity.getStartingIntent(this, it), MATCH_REQUEST)
        })
        viewModel.errorEvent.observe(this, LiveDataEventWithContentObserver {
            findNavController(R.id.orchestrator_host_fragment)
                .navigate(OrchestratorGraphDirections.actionGlobalErrorFragment(it))
        })
        viewModel.startConfiguration.observe(this, LiveDataEventWithContentObserver {
            findNavController(R.id.orchestrator_host_fragment)
                .navigate(
                    BlankFragmentDirections.actionBlankFragmentToConfigurationFragment(
                        it.projectId,
                        it.deviceId
                    )
                )
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CAPTURE_REQUEST -> viewModel.captureFinished(data?.getParcelableExtra(IFaceResponse.BUNDLE_KEY))
                MATCH_REQUEST -> viewModel.matchFinished(data?.getParcelableExtra(IFaceResponse.BUNDLE_KEY))
            }
        } else {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    companion object {
        const val CAPTURE_REQUEST = 100
        const val MATCH_REQUEST = 101
    }
}
