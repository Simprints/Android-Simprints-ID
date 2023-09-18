package com.simprints.face.orchestrator

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.navigation.findNavController
import com.simprints.core.livedata.LiveDataEventObserver
import com.simprints.core.livedata.LiveDataEventWithContentObserver
import com.simprints.core.tools.activity.BaseActivity
import com.simprints.face.R
import com.simprints.face.capture.FaceCaptureActivity
import com.simprints.face.databinding.ActivityOrchestratorBinding
import com.simprints.face.error.ErrorType
import com.simprints.face.exceptions.InvalidFaceRequestException
import com.simprints.feature.alert.AlertContract
import com.simprints.feature.alert.AlertResult
import com.simprints.infra.uibase.navigation.handleResult
import com.simprints.infra.uibase.viewbinding.viewBinding
import com.simprints.moduleapi.face.requests.IFaceRequest
import com.simprints.moduleapi.face.responses.IFaceResponse
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FaceOrchestratorActivity : BaseActivity() {

    private val binding by viewBinding(ActivityOrchestratorBinding::inflate)

    private val viewModel: FaceOrchestratorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_orchestrator)

        val iFaceRequest: IFaceRequest = this.intent.extras?.getParcelable(IFaceRequest.BUNDLE_KEY)
            ?: throw InvalidFaceRequestException("No IFaceCaptureRequest found for FaceOrchestratorActivity")

        binding.orchestratorHostFragment.handleResult<AlertResult>(this, AlertContract.ALERT_DESTINATION_ID) { result ->
            if (result.isBackButtonPress()) {
                viewModel.finishWithError()
            }
        }

        observeViewModel()

        if (savedInstanceState == null) viewModel.start(iFaceRequest)
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
        viewModel.errorEvent.observe(this, LiveDataEventObserver {
            findNavController(R.id.orchestrator_host_fragment).navigate(
                R.id.action_global_errorFragment,
                ErrorType.toAlertConfiguration(),
            )
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CAPTURE_REQUEST -> viewModel.captureFinished(data?.getParcelableExtra(IFaceResponse.BUNDLE_KEY))
            }
        } else {
            viewModel.unexpectedErrorHappened()
        }
    }

    companion object {
        const val CAPTURE_REQUEST = 100
    }
}
