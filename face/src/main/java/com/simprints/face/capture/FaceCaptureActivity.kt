package com.simprints.face.capture

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.navigation.findNavController
import com.simprints.core.livedata.LiveDataEventObserver
import com.simprints.core.livedata.LiveDataEventWithContentObserver
import com.simprints.core.tools.whenNonNull
import com.simprints.core.tools.whenNull
import com.simprints.face.R
import com.simprints.face.base.FaceActivity
import com.simprints.face.data.moduleapi.face.requests.FaceCaptureRequest
import com.simprints.face.exceptions.InvalidFaceRequestException
import com.simprints.moduleapi.face.requests.IFaceRequest
import com.simprints.moduleapi.face.responses.IFaceResponse
import org.koin.android.viewmodel.ext.android.viewModel

class FaceCaptureActivity : FaceActivity() {
    private val vm: FaceCaptureViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_face_capture)
        bindViewModel()

        val faceRequest: FaceCaptureRequest =
            this.intent.extras?.getParcelable(IFaceRequest.BUNDLE_KEY)
                ?: throw InvalidFaceRequestException("No IFaceRequest found for FaceCaptureActivity")

        vm.setupCapture(faceRequest)
    }

    private fun bindViewModel() {
        vm.finishFlowEvent.observe(this, LiveDataEventWithContentObserver {
            val intent = Intent().apply { putExtra(IFaceResponse.BUNDLE_KEY, it) }
            setResult(Activity.RESULT_OK, intent)
            finish()
        })

        vm.recaptureEvent.observe(this, LiveDataEventObserver {
            findNavController(R.id.capture_host_fragment).navigate(R.id.action_confirmationFragment_to_liveFeedbackFragment)
        })

        vm.exitFormEvent.observe(this, LiveDataEventObserver {
            findNavController(R.id.capture_host_fragment).navigate(R.id.action_global_refusalFragment)
        })

        vm.finishFlowWithExitFormEvent.observe(this, LiveDataEventWithContentObserver {
            val intent = Intent().apply { putExtra(IFaceResponse.BUNDLE_KEY, it) }
            setResult(Activity.RESULT_OK, intent)
            finish()
        })

        vm.unexpectedErrorEvent.observe(this, LiveDataEventObserver {
            setResult(Activity.RESULT_CANCELED)
            finish()
        })
    }

    override fun onBackPressed() {
        BackButtonContext.fromFragmentId(
            findNavController(R.id.capture_host_fragment).currentDestination?.id
        )
            .whenNonNull { vm.handleBackButton(this) }
            .whenNull { super.onBackPressed() }
    }

    enum class BackButtonContext {
        CAPTURE;

        companion object {
            fun fromFragmentId(fragmentId: Int?): BackButtonContext? = when (fragmentId) {
                R.id.preparationFragment, R.id.liveFeedbackFragment -> CAPTURE
                else -> null
            }
        }
    }

    companion object {
        fun getStartingIntent(context: Context, faceCaptureRequest: FaceCaptureRequest): Intent =
            Intent(context, FaceCaptureActivity::class.java).apply {
                putExtra(IFaceRequest.BUNDLE_KEY, faceCaptureRequest)
            }
    }

}
