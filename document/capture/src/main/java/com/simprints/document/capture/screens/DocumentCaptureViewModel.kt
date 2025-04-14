package com.simprints.document.capture.screens

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.DeviceID
import com.simprints.core.livedata.LiveDataEvent
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.send
import com.simprints.document.capture.DocumentCaptureResult
import com.simprints.document.capture.models.DocumentDetection
import com.simprints.document.capture.usecases.BitmapToByteArrayUseCase
import com.simprints.document.capture.usecases.SaveDocumentImageUseCase
import com.simprints.document.infra.documentsdkresolver.ResolveDocumentSdkUseCase
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.DOCUMENT_CAPTURE
import com.simprints.infra.logging.Simber
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@HiltViewModel
internal class DocumentCaptureViewModel @Inject constructor(
    private val resolveDocumentSdk: ResolveDocumentSdkUseCase,
) : ViewModel() {
    // Updated in live feedback screen
    var attemptNumber: Int = 0
    var samplesToCapture = 1
    var initialised = false

    var shouldCheckCameraPermissions = AtomicBoolean(true)

    private var documentDetections = listOf<DocumentDetection>()

    val recaptureEvent: LiveData<LiveDataEvent>
        get() = _recaptureEvent
    private val _recaptureEvent = MutableLiveData<LiveDataEvent>()

    val exitFormEvent: LiveData<LiveDataEvent>
        get() = _exitFormEvent
    private val _exitFormEvent = MutableLiveData<LiveDataEvent>()

    val unexpectedErrorEvent: LiveData<LiveDataEvent>
        get() = _unexpectedErrorEvent
    private val _unexpectedErrorEvent = MutableLiveData<LiveDataEvent>()

    val finishFlowEvent: LiveData<LiveDataEventWithContent<DocumentCaptureResult>>
        get() = _finishFlowEvent
    private val _finishFlowEvent = MutableLiveData<LiveDataEventWithContent<DocumentCaptureResult>>()

    fun initDocumentSdk(activity: Activity) = viewModelScope.launch {
        if (initialised) {
            Simber.i("Document SDK already initialised", tag = DOCUMENT_CAPTURE)
            return@launch
        }

        Simber.i("Starting document capture flow", tag = DOCUMENT_CAPTURE)

        initialize(activity)
    }

    private suspend fun initialize(activity: Activity) {
        resolveDocumentSdk().initializer.init(activity)
    }

    fun getSampleDetection() = documentDetections.firstOrNull()

    fun flowFinished() {
        Simber.i("Finishing capture flow", tag = DOCUMENT_CAPTURE)
        viewModelScope.launch {
            val items = documentDetections.mapIndexed { index, detection ->
                DocumentCaptureResult.Item(
                    captureEventId = detection.id,
                    index = index,
                    sample = DocumentCaptureResult.Sample(
                        documentId = detection.id,
                        template = detection.document?.template ?: ByteArray(0),
                        imageRef = detection.securedImageRef,
                        format = detection.document?.format ?: "",
                    ),
                )
            }
            val referenceId = UUID.randomUUID().toString()

            _finishFlowEvent.send(DocumentCaptureResult(referenceId, items))
        }
    }

    fun captureFinished(newDocumentDetections: List<DocumentDetection>) {
        documentDetections = newDocumentDetections
    }

    fun handleBackButton() {
        _exitFormEvent.send()
    }

    fun recapture() {
        Simber.i("Starting document recapture flow", tag = DOCUMENT_CAPTURE)
        documentDetections = listOf()
        _recaptureEvent.send()
    }
}
