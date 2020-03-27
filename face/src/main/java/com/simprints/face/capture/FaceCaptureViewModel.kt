package com.simprints.face.capture

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.otaliastudios.cameraview.frame.Frame
import com.simprints.core.Path
import com.simprints.core.data.analytics.AnalyticsManager
import com.simprints.core.data.analytics.AnalyticsManager.Events.FACE_FLOW_TIME
import com.simprints.core.data.analytics.AnalyticsManager.Events.RETRY
import com.simprints.core.data.analytics.AnalyticsManager.Events.RETRY_FAIL
import com.simprints.core.data.analytics.AnalyticsManager.Events.SUCCESS
import com.simprints.core.data.images.ImageStoreManager
import com.simprints.core.data.preferences.CameraPreferences
import com.simprints.core.extensions.set
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.send
import com.simprints.core.metadata.BeneficiaryMetadata
import com.simprints.face.data.moduleapi.face.requests.FaceCaptureRequest
import com.simprints.face.data.moduleapi.face.requests.FaceRequest
import com.simprints.face.data.moduleapi.face.responses.FaceCaptureResponse
import com.simprints.face.data.moduleapi.face.responses.entities.FaceCaptureResult
import com.simprints.face.data.moduleapi.face.responses.entities.toFaceSample
import com.simprints.face.models.FaceDetection
import com.simprints.uicomponents.models.CameraOptions
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.launch

class FaceCaptureViewModel(
    private val beneficiaryMetadata: BeneficiaryMetadata?,
    private val projectId: String,
    private val sessionId: String,
    private val cameraPreferences: CameraPreferences,
    private val imageStoreManager: ImageStoreManager,
    private val analyticsManager: AnalyticsManager
) : ViewModel() {
    val captures = MutableLiveData<List<FaceDetection>>()
    val processFrames: MutableLiveData<LiveDataEventWithContent<Boolean>> = MutableLiveData()

    val frameChannel = Channel<Frame>(CONFLATED)

    val startCamera: MutableLiveData<LiveDataEventWithContent<CameraOptions>> = MutableLiveData()

    val onboardingExperience: MutableLiveData<LiveDataEventWithContent<OnboardingExperience>> =
        MutableLiveData()

    val flowFinished: MutableLiveData<LiveDataEventWithContent<FaceCaptureResponse>> =
        MutableLiveData()

    private var retriesUsed: Int = 0
    val canRetry: Boolean
        get() = retriesUsed++ < cameraPreferences.retries
    private val qualityThreshold = cameraPreferences.qualityThreshold
    var samplesToCapture = 10

    init {
        startCamera.set(
            LiveDataEventWithContent(
                CameraOptions(
                    cameraPreferences.getFacingFront(),
                    cameraPreferences.useFlash
                )
            )
        )
        onboardingExperience.set(
            if (cameraPreferences.useStaticOnboarding)
                LiveDataEventWithContent(OnboardingExperience.STATIC)
            else
                LiveDataEventWithContent(OnboardingExperience.TIMED)
        )
        viewModelScope.launch {
            startNewAnalyticsSession()
        }
    }

    fun setupCapture(faceRequest: FaceRequest) {
        when (faceRequest) {
            is FaceCaptureRequest -> {
                samplesToCapture = faceRequest.nFaceSamplesToCapture
            }
        }
    }

    fun startFaceDetection() {
        processFrames.set(LiveDataEventWithContent(true))
    }

    fun stopFaceDetection() {
        processFrames.set(LiveDataEventWithContent(false))
    }

    fun handlePreviewFrame(frame: Frame) = frameChannel.offer(frame)

    fun flowFinished() {
        analyticsManager.faceFlowFinished(SUCCESS)
        analyticsManager.endSession()

        val results = captures.value?.mapIndexed { index, detection ->
            FaceCaptureResult(index, detection.toFaceCapture(sessionId).toFaceSample())
        } ?: listOf()

        flowFinished.send(FaceCaptureResponse(results))
    }

    fun captureFinished(captures: List<FaceDetection>) {
        stopFaceDetection()
        this.captures.set(captures)
        saveCaptures()
    }

    fun willRetry() {
        analyticsManager.faceFlowFinished(RETRY)
    }

    fun retryFailed() {
        analyticsManager.faceFlowFinished(RETRY_FAIL)
    }

    private fun startNewAnalyticsSession() {
        analyticsManager.startSession(FACE_FLOW_TIME)
    }

    private fun saveCaptures() {
        captures.value?.forEachIndexed { index, capture ->
            saveImage(capture, Path.pathForFaceImage(projectId, sessionId, index))
        }
    }

    private fun saveImage(capture: FaceDetection, path: Path) {
        imageStoreManager.storeImageBitmap(capture.frame.toBitmap(), path)
    }

    enum class OnboardingExperience { TIMED, STATIC }
}
