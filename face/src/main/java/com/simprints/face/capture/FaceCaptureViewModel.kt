package com.simprints.face.capture

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.otaliastudios.cameraview.frame.Frame
import com.simprints.core.livedata.LiveDataEvent
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.send
import com.simprints.face.controllers.core.image.FaceImageManager
import com.simprints.face.data.moduleapi.face.requests.FaceCaptureRequest
import com.simprints.face.data.moduleapi.face.requests.FaceRequest
import com.simprints.face.data.moduleapi.face.responses.FaceCaptureResponse
import com.simprints.face.data.moduleapi.face.responses.entities.FaceCaptureResult
import com.simprints.face.models.FaceDetection
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class FaceCaptureViewModel(private val maxRetries: Int, private val faceImageManager: FaceImageManager) : ViewModel() {
//    private val analyticsManager: AnalyticsManager

    val faceDetections = MutableLiveData<List<FaceDetection>>()
    val shouldProcessFrames: MutableLiveData<LiveDataEventWithContent<Boolean>> = MutableLiveData()

    val frameChannel = Channel<Frame>(CONFLATED)

    val startCamera: MutableLiveData<LiveDataEvent> = MutableLiveData()

    val flowFinished: MutableLiveData<LiveDataEventWithContent<FaceCaptureResponse>> =
        MutableLiveData()

    private var retriesUsed: Int = 0

    val canRetry: Boolean
        get() = retriesUsed++ < maxRetries

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
        shouldProcessFrames.send(true)
    }

    fun stopFaceDetection() {
        shouldProcessFrames.send(false)
    }

    fun handlePreviewFrame(frame: Frame) {
        frameChannel.offer(frame)
    }

    fun flowFinished() {
        saveFaceDetections()
        // TODO: add analytics for FlowFinished(SUCCESS) and EndSession

        val results = faceDetections.value?.mapIndexed { index, detection ->
            FaceCaptureResult(index, detection.toFaceSample())
        } ?: listOf()

        flowFinished.send(FaceCaptureResponse(results))
    }

    fun captureFinished(faceDetections: List<FaceDetection>) {
        stopFaceDetection()
        this.faceDetections.value = faceDetections
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

    private fun saveFaceDetections() {
        // TODO: send the correct captureEventId once we can get it
        faceDetections.value?.forEachIndexed { index, faceDetection -> saveImage(faceDetection, index.toString()) }
    }

    private fun saveImage(faceDetection: FaceDetection, captureEventId: String) {
        runBlocking {
            faceImageManager.save(faceDetection.frame.toByteArray(), captureEventId)
        }
    }

}
