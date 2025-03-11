package com.simprints.ear.capture.screen

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.livedata.LiveDataEvent
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.send
import com.simprints.core.tools.time.Timestamp
import com.simprints.ear.capture.EarCaptureResult
import com.simprints.ear.capture.models.EarDetection
import com.simprints.ear.capture.usecases.BitmapToByteArrayUseCase
import com.simprints.ear.capture.usecases.SaveEarImageUseCase
import com.simprints.ear.capture.usecases.SimpleCaptureEventReporter
import com.simprints.ear.infra.biosdkresolver.ResolveEarBioSdkUseCase
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.FACE_CAPTURE
import com.simprints.infra.logging.Simber
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@HiltViewModel
internal class EarCaptureViewModel @Inject constructor(
    private val configManager: ConfigManager,
    private val bitmapToByteArray: BitmapToByteArrayUseCase,
    private val resolveEarBioSdk: ResolveEarBioSdkUseCase,
    private val eventReporter: SimpleCaptureEventReporter,
    private val saveEarImage: SaveEarImageUseCase,
) : ViewModel() {
    // Updated in live feedback screen
    var attemptNumber: Int = 0
    var samplesToCapture = 1
    var initialised = false

    var shouldCheckCameraPermissions = AtomicBoolean(true)

    private var faceDetections = listOf<EarDetection>()

    val recaptureEvent: LiveData<LiveDataEvent>
        get() = _recaptureEvent
    private val _recaptureEvent = MutableLiveData<LiveDataEvent>()

    val exitFormEvent: LiveData<LiveDataEvent>
        get() = _exitFormEvent
    private val _exitFormEvent = MutableLiveData<LiveDataEvent>()

    val unexpectedErrorEvent: LiveData<LiveDataEvent>
        get() = _unexpectedErrorEvent
    private val _unexpectedErrorEvent = MutableLiveData<LiveDataEvent>()

    val finishFlowEvent: LiveData<LiveDataEventWithContent<EarCaptureResult>>
        get() = _finishFlowEvent
    private val _finishFlowEvent = MutableLiveData<LiveDataEventWithContent<EarCaptureResult>>()

    fun setupCapture(samplesToCapture: Int) {
        this.samplesToCapture = samplesToCapture
    }

    fun initialize(activity: Activity) {
        if (initialised) return
        viewModelScope.launch {
            initialised = resolveEarBioSdk().initializer.tryInitWithLicense(activity, "")
        }
    }

    fun getSampleDetection() = faceDetections.firstOrNull()

    fun flowFinished() {
        Simber.i("Finishing capture flow", tag = FACE_CAPTURE)
        viewModelScope.launch {
            val projectConfiguration = configManager.getProjectConfiguration()
            if (projectConfiguration.face?.imageSavingStrategy?.shouldSaveImage() == true) {
                saveFaceDetections()
            }

            val items = faceDetections.mapIndexed { index, detection ->
                EarCaptureResult.Item(
                    captureEventId = detection.id,
                    index = index,
                    sample = EarCaptureResult.Sample(
                        faceId = detection.id,
                        template = detection.ear?.template ?: ByteArray(0),
                        imageRef = detection.securedImageRef,
                        format = detection.ear?.format ?: "",
                    ),
                )
            }
            val referenceId = UUID.randomUUID().toString()
            eventReporter.addBiometricReferenceCreationEvents(referenceId, items.mapNotNull { it.captureEventId })

            _finishFlowEvent.send(EarCaptureResult(referenceId, items))
        }
    }

    fun captureFinished(newFaceDetections: List<EarDetection>) {
        faceDetections = newFaceDetections
    }

    fun handleBackButton() {
        _exitFormEvent.send()
    }

    fun recapture() {
        Simber.i("Starting face recapture flow", tag = FACE_CAPTURE)
        faceDetections = listOf()
        _recaptureEvent.send()
    }

    private fun saveFaceDetections() {
        Simber.i("Saving captures to disk", tag = FACE_CAPTURE)
        faceDetections.forEach { saveImage(it, it.id) }
    }

    private fun saveImage(
        faceDetection: EarDetection,
        captureEventId: String,
    ) {
        runBlocking {
            faceDetection.securedImageRef = saveEarImage(bitmapToByteArray(faceDetection.bitmap), captureEventId)
        }
    }

    fun submitError(throwable: Throwable) {
        Simber.e("Face capture failed", throwable, FACE_CAPTURE)
        _unexpectedErrorEvent.send()
    }

    fun addOnboardingComplete(startTime: Timestamp) {
        Simber.i("Face capture onboarding complete", tag = FACE_CAPTURE)
        eventReporter.addOnboardingCompleteEvent(startTime)
    }

    fun addCaptureConfirmationAction(
        startTime: Timestamp,
        isContinue: Boolean,
    ) {
        eventReporter.addCaptureConfirmationEvent(startTime, isContinue)
    }
}

