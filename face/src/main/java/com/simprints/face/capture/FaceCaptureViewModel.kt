package com.simprints.face.capture

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.otaliastudios.cameraview.frame.Frame
import com.simprints.core.livedata.LiveDataEvent
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.send
import com.simprints.face.data.moduleapi.face.requests.FaceCaptureRequest
import com.simprints.face.data.moduleapi.face.requests.FaceRequest
import com.simprints.face.data.moduleapi.face.responses.FaceCaptureResponse
import com.simprints.face.data.moduleapi.face.responses.entities.FaceCaptureResult
import com.simprints.face.models.FaceDetection
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.launch
import java.util.*

class FaceCaptureViewModel : ViewModel() {
    // TODO: get correct information from SimprintsID managers
    private val projectId: String = UUID.randomUUID().toString()
    private val sessionId: String = UUID.randomUUID().toString()
//    private val imageStoreManager: ImageStoreManager
//    private val analyticsManager: AnalyticsManager

    val captures = MutableLiveData<List<FaceDetection>>()
    val processFrames: MutableLiveData<LiveDataEventWithContent<Boolean>> = MutableLiveData()

    val frameChannel = Channel<Frame>(CONFLATED)

    val startCamera: MutableLiveData<LiveDataEvent> = MutableLiveData()

    val flowFinished: MutableLiveData<LiveDataEventWithContent<FaceCaptureResponse>> =
        MutableLiveData()

    private var retriesUsed: Int = 0

    // TODO: get correct information from SimprintsID managers - cameraPreferences.retries
    val canRetry: Boolean
        get() = retriesUsed++ < 2

    // TODO: get correct information from SimprintsID managers - cameraPreferences.qualityThreshold
    private val qualityThreshold = -1
    var samplesToCapture = 1

    init {
        startCamera.send()
        viewModelScope.launch { startNewAnalyticsSession() }
    }

    fun setupCapture(faceRequest: FaceRequest) {
        when (faceRequest) {
            is FaceCaptureRequest -> {
                samplesToCapture = faceRequest.nFaceSamplesToCapture
            }
        }
    }

    fun startFaceDetection() {
        processFrames.send(true)
    }

    fun stopFaceDetection() {
        processFrames.send(false)
    }

    fun handlePreviewFrame(frame: Frame) = frameChannel.offer(frame)

    fun flowFinished() {
        saveCaptures()
        // TODO: add analytics for FlowFinished(SUCCESS) and EndSession

        val results = captures.value?.mapIndexed { index, detection ->
            FaceCaptureResult(index, detection.toFaceSample())
        } ?: listOf()

        flowFinished.send(FaceCaptureResponse(results))
    }

    fun captureFinished(captures: List<FaceDetection>) {
        stopFaceDetection()
        this.captures.value = captures
    }

    fun willRetry() {
        // TODO: add analytics for FlowFinished(RETRY)
    }

    fun retryFailed() {
        // TODO: add analytics for FlowFinished(RETRY_FAIL)
    }

    private fun startNewAnalyticsSession() {
        // TODO: add analytics for StartSession
    }

    private fun saveCaptures() {
        captures.value?.forEachIndexed { index, capture -> saveImage(capture) }
    }

    private fun saveImage(capture: FaceDetection) {
        // TODO: use image manager to store the images
    }

}
