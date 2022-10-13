package com.simprints.face.capture

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.DispatcherIO
import com.simprints.core.livedata.LiveDataEvent
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.send
import com.simprints.face.capture.FaceCaptureActivity.BackButtonContext
import com.simprints.face.capture.FaceCaptureActivity.BackButtonContext.CAPTURE
import com.simprints.face.controllers.core.image.FaceImageManager
import com.simprints.face.data.moduleapi.face.requests.FaceCaptureRequest
import com.simprints.face.data.moduleapi.face.responses.FaceCaptureResponse
import com.simprints.face.data.moduleapi.face.responses.entities.FaceCaptureResult
import com.simprints.face.models.FaceDetection
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.FaceConfiguration
import com.simprints.infra.logging.LoggingConstants.CrashReportTag
import com.simprints.infra.logging.Simber
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class FaceCaptureViewModel @Inject constructor(
    private val configManager: ConfigManager,
    private val faceImageManager: FaceImageManager,
    @DispatcherIO private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    var faceDetections = listOf<FaceDetection>()

    val recaptureEvent: MutableLiveData<LiveDataEvent> = MutableLiveData()
    val exitFormEvent: MutableLiveData<LiveDataEvent> = MutableLiveData()
    val unexpectedErrorEvent: MutableLiveData<LiveDataEvent> = MutableLiveData()

    val finishFlowEvent: MutableLiveData<LiveDataEventWithContent<FaceCaptureResponse>> =
        MutableLiveData()

    var attemptNumber: Int = 0

    var samplesToCapture = 1

    init {
        viewModelScope.launch { startNewAnalyticsSession() }
    }

    fun setupCapture(faceRequest: FaceCaptureRequest) {
        samplesToCapture = faceRequest.nFaceSamplesToCapture
    }

    fun flowFinished() {
        viewModelScope.launch(dispatcher) {
            val projectConfiguration = configManager.getProjectConfiguration()
            if (projectConfiguration.face?.imageSavingStrategy == FaceConfiguration.ImageSavingStrategy.ONLY_GOOD_SCAN) {
                saveFaceDetections()
            }

            val results = faceDetections.mapIndexed { index, detection ->
                FaceCaptureResult(index, detection.toFaceSample())
            }

            finishFlowEvent.send(FaceCaptureResponse(results))
        }
    }

    fun captureFinished(newFaceDetections: List<FaceDetection>) {
        faceDetections = newFaceDetections
    }

    fun handleBackButton(backButtonContext: BackButtonContext) {
        when (backButtonContext) {
            CAPTURE -> startExitForm()
        }
    }

    fun recapture() {
        Simber.tag(CrashReportTag.FACE_CAPTURE.name).i("Starting face recapture flow")
        faceDetections = listOf()
        recaptureEvent.send()
    }

    private fun startExitForm() {
        exitFormEvent.send()
    }

    private fun startNewAnalyticsSession() {
        Simber.tag(CrashReportTag.FACE_CAPTURE.name).i("Starting face capture flow")
    }

    private fun saveFaceDetections() {
        Simber.tag(CrashReportTag.FACE_CAPTURE.name).i("Saving captures to disk")
        faceDetections.forEach { saveImage(it, it.id) }
    }

    private fun saveImage(faceDetection: FaceDetection, captureEventId: String) {
        runBlocking {
            faceDetection.securedImageRef =
                faceImageManager.save(faceDetection.frame.toByteArray(), captureEventId)
        }
    }

    fun submitError(throwable: Throwable) {
        Simber.e(throwable)
        unexpectedErrorEvent.send()
    }

}
