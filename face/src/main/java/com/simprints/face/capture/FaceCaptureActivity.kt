package com.simprints.face.capture

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.otaliastudios.cameraview.frame.Frame
import com.simprints.core.livedata.LiveDataEventObserver
import com.simprints.core.livedata.LiveDataEventWithContentObserver
import com.simprints.face.R
import com.simprints.face.data.moduleapi.face.requests.FaceRequest
import com.simprints.face.exceptions.InvalidFaceRequestException
import com.simprints.moduleapi.face.requests.IFaceRequest
import com.simprints.moduleapi.face.responses.IFaceResponse
import kotlinx.android.synthetic.main.activity_face_capture.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import com.otaliastudios.cameraview.frame.FrameProcessor as CameraViewFrameProcessor

class FaceCaptureActivity : AppCompatActivity(), CameraViewFrameProcessor {

    private val vm: FaceCaptureViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_face_capture)
        bindViewModel()

        val faceRequest: FaceRequest = this.intent.extras?.getParcelable(IFaceRequest.BUNDLE_KEY)
            ?: throw InvalidFaceRequestException("No IFaceRequest found for FaceOrchestratorActivity")

        vm.setupCapture(faceRequest)
    }

    private fun bindViewModel() {
        vm.startCamera.observe(this, LiveDataEventObserver { startCamera() })

        vm.processFrames.observe(this, LiveDataEventWithContentObserver {
            if (it) {
                face_capture_camera.addFrameProcessor(this)
            } else {
                face_capture_camera.removeFrameProcessor(this)
            }
        })

        vm.flowFinished.observe(this, LiveDataEventWithContentObserver {
            val intent = Intent().apply { putExtra(IFaceResponse.BUNDLE_KEY, it) }
            setResult(Activity.RESULT_OK, intent)
            finish()
        })
    }

    private fun startCamera() {
        face_capture_camera.let {
            it.useDeviceOrientation = true
            it.setLifecycleOwner(this)
        }
    }

    /**
     * @process needs to block because frame is a singleton which cannot be released until it's
     * converted into a preview frame.
     * Also the frame sometimes throws IllegalStateException for null width and height
     */
    override fun process(frame: Frame) {
        try {
            vm.handlePreviewFrame(frame.freeze())
        } catch (ex: IllegalStateException) {
            Timber.e(ex)
        }
    }
}
