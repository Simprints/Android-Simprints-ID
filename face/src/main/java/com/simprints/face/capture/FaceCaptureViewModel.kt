package com.simprints.face.capture

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.livedata.LiveDataEvent
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.send
import com.simprints.face.capture.FaceCaptureActivity.BackButtonContext
import com.simprints.face.capture.FaceCaptureActivity.BackButtonContext.CAPTURE
import com.simprints.face.capture.FaceCaptureActivity.BackButtonContext.RETRY
import com.simprints.face.controllers.core.crashreport.FaceCrashReportManager
import com.simprints.face.controllers.core.crashreport.FaceCrashReportTag.FACE_CAPTURE
import com.simprints.face.controllers.core.crashreport.FaceCrashReportTrigger.UI
import com.simprints.face.controllers.core.events.model.RefusalAnswer
import com.simprints.face.controllers.core.image.FaceImageManager
import com.simprints.face.data.moduleapi.face.requests.FaceCaptureRequest
import com.simprints.face.data.moduleapi.face.responses.FaceCaptureResponse
import com.simprints.face.data.moduleapi.face.responses.FaceExitFormResponse
import com.simprints.face.data.moduleapi.face.responses.entities.FaceCaptureResult
import com.simprints.face.models.FaceDetection
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class FaceCaptureViewModel(
    private val maxRetries: Int,
    private val shouldSaveFaceImages: Boolean,
    private val faceImageManager: FaceImageManager,
    private val crashReportManager: FaceCrashReportManager
) : ViewModel() {
    var faceDetections = listOf<FaceDetection>()

    val retryFlowEvent: MutableLiveData<LiveDataEvent> = MutableLiveData()
    val recaptureEvent: MutableLiveData<LiveDataEvent> = MutableLiveData()
    val exitFormEvent: MutableLiveData<LiveDataEvent> = MutableLiveData()
    val unexpectedErrorEvent: MutableLiveData<LiveDataEvent> = MutableLiveData()

    val finishFlowEvent: MutableLiveData<LiveDataEventWithContent<FaceCaptureResponse>> =
        MutableLiveData()
    val finishFlowWithExitFormEvent: MutableLiveData<LiveDataEventWithContent<FaceExitFormResponse>> =
        MutableLiveData()

    var attemptNumber: Int = 0

    val canRetry: Boolean
        get() = attemptNumber++ < maxRetries

    var samplesToCapture = 1

    init {
        viewModelScope.launch { startNewAnalyticsSession() }
    }

    fun setupCapture(faceRequest: FaceCaptureRequest) {
        samplesToCapture = faceRequest.nFaceSamplesToCapture
    }

    fun flowFinished() {
        if (shouldSaveFaceImages) {
            saveFaceDetections()
        }

        val results = faceDetections.mapIndexed { index, detection ->
            FaceCaptureResult(index, detection.toFaceSample())
        }

        finishFlowEvent.send(FaceCaptureResponse(results))
    }

    fun captureFinished(newFaceDetections: List<FaceDetection>) {
        faceDetections = newFaceDetections
    }

    fun handleBackButton(backButtonContext: BackButtonContext) {
        when (backButtonContext) {
            CAPTURE -> startExitForm()
            RETRY -> handleRetry(true)
        }
    }

    fun handleRetry(isBackButton: Boolean) {
        if (canRetry) {
            if (isBackButton) startExitForm() else retryFlow()
        } else {
            finishFlowWithFailedRetries()
        }
    }

    fun recapture() {
        crashReportManager.logMessageForCrashReport(
            FACE_CAPTURE,
            UI,
            message = "Starting face recapture flow"
        )
        faceDetections = listOf()
        recaptureEvent.send()
    }

    private fun startExitForm() {
        exitFormEvent.send()
    }

    private fun retryFlow() {
        faceDetections = listOf()
        retryFlowEvent.send()
    }

    private fun finishFlowWithFailedRetries() {
        flowFinished()
    }

    private fun startNewAnalyticsSession() {
        crashReportManager.logMessageForCrashReport(
            FACE_CAPTURE,
            UI,
            message = "Starting face capture flow"
        )
    }

    private fun saveFaceDetections() {
        crashReportManager.logMessageForCrashReport(
            FACE_CAPTURE,
            UI,
            message = "Saving captures to disk"
        )

        faceDetections.forEach { saveImage(it, it.id) }
    }

    private fun saveImage(faceDetection: FaceDetection, captureEventId: String) {
        runBlocking {
            faceDetection.securedImageRef =
                faceImageManager.save(faceDetection.frame.toByteArray(), captureEventId)
        }
    }

    fun submitExitForm(reason: RefusalAnswer, exitFormText: String) {
        finishFlowWithExitFormEvent.send(FaceExitFormResponse(reason, exitFormText))
    }

    fun submitError(throwable: Throwable) {
        crashReportManager.logException(throwable)
        unexpectedErrorEvent.send()
    }

}
