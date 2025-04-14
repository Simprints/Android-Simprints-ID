package com.simprints.document.capture.screens.livefeedback

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.tools.extentions.area
import com.simprints.core.tools.time.TimeHelper
import com.simprints.document.capture.models.DocumentDetection
import com.simprints.document.capture.models.DocumentTarget
import com.simprints.document.capture.models.SymmetricTarget
import com.simprints.document.infra.basedocumentsdk.detection.Document
import com.simprints.document.infra.basedocumentsdk.detection.DocumentDetector
import com.simprints.document.infra.documentsdkresolver.ResolveDocumentSdkUseCase
import com.simprints.infra.config.store.models.experimental
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.DOCUMENT_CAPTURE
import com.simprints.infra.logging.Simber
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@HiltViewModel
internal class LiveFeedbackFragmentViewModel @Inject constructor(
    private val resolveDocumentSdk: ResolveDocumentSdkUseCase,
    private val configManager: ConfigManager,
    private val timeHelper: TimeHelper,
) : ViewModel() {
    private var attemptNumber: Int = 1
    private var samplesToCapture: Int = 1
    private var qualityThreshold: Float = 0f
    private var singleQualityFallbackCaptureRequired: Boolean = false

    private val documentTarget = DocumentTarget(
        SymmetricTarget(VALID_YAW_DELTA),
        SymmetricTarget(VALID_ROLL_DELTA),
        0.20f..0.5f,
    )
    private val fallbackCaptureEventStartTime = timeHelper.now()
    private var shouldSendFallbackCaptureEvent: AtomicBoolean = AtomicBoolean(true)
    private var fallbackCapture: DocumentDetection? = null

    val userCaptures = mutableListOf<DocumentDetection>()
    var sortedQualifyingCaptures = listOf<DocumentDetection>()
    val currentDetection = MutableLiveData<DocumentDetection>()
    val capturingState = MutableLiveData(CapturingState.NOT_STARTED)
    private lateinit var documentDetector: DocumentDetector

    /**
     * Processes the image
     *
     * @param croppedBitmap is the camera frame
     */
    fun process(croppedBitmap: Bitmap) {
        val captureStartTime = timeHelper.now()
        val potentialDocument = documentDetector.analyze(croppedBitmap)

        val documentDetection = getDocumentDetectionFromPotentialDocument(croppedBitmap, potentialDocument)
        documentDetection.detectionStartTime = captureStartTime
        documentDetection.detectionEndTime = timeHelper.now()

        currentDetection.postValue(documentDetection)

        when (capturingState.value) {
            CapturingState.NOT_STARTED -> updateFallbackCaptureIfValid(documentDetection)
            CapturingState.CAPTURING -> {
                userCaptures += documentDetection
                if (userCaptures.size == samplesToCapture) {
                    finishCapture(attemptNumber)
                }
            }

            else -> { // no-op
            }
        }
    }

    fun initCapture(
        samplesToCapture: Int,
        attemptNumber: Int,
    ) {
        Simber.i("Initialise document detection", tag = DOCUMENT_CAPTURE)

        this.samplesToCapture = samplesToCapture
        this.attemptNumber = attemptNumber
        viewModelScope.launch {
            documentDetector = resolveDocumentSdk().detector

            val config = configManager.getProjectConfiguration()
            qualityThreshold = 0f // todo implement in config and add
            singleQualityFallbackCaptureRequired = config.experimental().singleQualityFallbackRequired
        }
    }

    fun startCapture() {
        capturingState.value = CapturingState.CAPTURING
    }

    /**
     * If any of the user captures are good, use them. If not, use the fallback capture.
     */
    private fun finishCapture(attemptNumber: Int) {
        Simber.i("Finish capture", tag = DOCUMENT_CAPTURE)
        viewModelScope.launch {
            sortedQualifyingCaptures = userCaptures
                .filter { it.hasValidStatus() }
                .sortedByDescending { it.document?.quality }
                .ifEmpty { listOfNotNull(fallbackCapture) }

            capturingState.postValue(CapturingState.FINISHED)
        }
    }

    private fun getDocumentDetectionFromPotentialDocument(
        bitmap: Bitmap,
        potentialDocument: Document?,
    ): DocumentDetection = if (potentialDocument == null) {
        bitmap.recycle()
        DocumentDetection(
            bitmap = bitmap,
            document = null,
            status = DocumentDetection.Status.NODOCUMENT,
            detectionStartTime = timeHelper.now(),
            detectionEndTime = timeHelper.now(),
        )
    } else {
        getDocumentDetection(bitmap, potentialDocument)
    }

    private fun getDocumentDetection(
        bitmap: Bitmap,
        potentialDocument: Document,
    ): DocumentDetection {
        val areaOccupied = potentialDocument.relativeBoundingBox.area()
        val status = when {
            areaOccupied < documentTarget.areaRange.start -> DocumentDetection.Status.TOOFAR
            areaOccupied > documentTarget.areaRange.endInclusive -> DocumentDetection.Status.TOOCLOSE
            potentialDocument.yaw !in documentTarget.yawTarget -> DocumentDetection.Status.OFFYAW
            potentialDocument.roll !in documentTarget.rollTarget -> DocumentDetection.Status.OFFROLL
            shouldCheckQuality() && potentialDocument.quality < qualityThreshold -> DocumentDetection.Status.BAD_QUALITY
            capturingState.value == CapturingState.CAPTURING -> DocumentDetection.Status.VALID_CAPTURING
            else -> DocumentDetection.Status.VALID
        }

        return DocumentDetection(
            bitmap = bitmap,
            document = potentialDocument,
            status = status,
            detectionStartTime = timeHelper.now(),
            detectionEndTime = timeHelper.now(),
        )
    }

    private fun shouldCheckQuality() = !singleQualityFallbackCaptureRequired || fallbackCapture == null

    /**
     * While the user has not started the capture flow, we save fallback images. If the capture doesn't
     * get any good images, at least one good image will be saved
     */
    private fun updateFallbackCaptureIfValid(documentDetection: DocumentDetection) {
        val fallbackQuality = fallbackCapture?.document?.quality ?: -1f // To ensure that detection is better with defaults
        val detectionQuality = documentDetection.document?.quality ?: 0f

        if (documentDetection.hasValidStatus() && detectionQuality >= fallbackQuality) {
            Simber.i("Fallback capture updated", tag = DOCUMENT_CAPTURE)
            fallbackCapture = documentDetection.apply { isFallback = true }
        }
    }

    enum class CapturingState { NOT_STARTED, CAPTURING, FINISHED }

    companion object {
        private const val VALID_ROLL_DELTA = 15f
        private const val VALID_YAW_DELTA = 30f
    }
}
