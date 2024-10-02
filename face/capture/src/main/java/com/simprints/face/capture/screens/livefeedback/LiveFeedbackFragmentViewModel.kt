package com.simprints.face.capture.screens.livefeedback

import android.graphics.Bitmap
import android.graphics.RectF
import android.util.Size
import androidx.camera.core.ImageProxy
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.tools.extentions.area
import com.simprints.core.tools.time.TimeHelper
import com.simprints.face.capture.models.FaceDetection
import com.simprints.face.capture.models.FaceTarget
import com.simprints.face.capture.models.ScreenOrientation
import com.simprints.face.capture.models.SymmetricTarget
import com.simprints.face.capture.usecases.SimpleCaptureEventReporter
import com.simprints.face.infra.basebiosdk.detection.Face
import com.simprints.face.infra.basebiosdk.detection.FaceDetector
import com.simprints.face.infra.biosdkresolver.ResolveFaceBioSdkUseCase
import com.simprints.infra.config.sync.ConfigManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@HiltViewModel
internal class LiveFeedbackFragmentViewModel @Inject constructor(
    private val frameProcessor: FrameProcessor,
    private val resolveFaceBioSdk: ResolveFaceBioSdkUseCase,
    private val configManager: ConfigManager,
    private val eventReporter: SimpleCaptureEventReporter,
    private val timeHelper: TimeHelper,
) : ViewModel() {

    private var attemptNumber: Int = 1
    private var samplesToCapture: Int = 1
    private var qualityThreshold: Int = 0

    private val faceTarget = FaceTarget(
        SymmetricTarget(VALID_YAW_DELTA),
        SymmetricTarget(VALID_ROLL_DELTA),
        0.25f..0.5f
    )
    private val fallbackCaptureEventStartTime = timeHelper.now()
    private var shouldSendFallbackCaptureEvent: AtomicBoolean = AtomicBoolean(true)
    private lateinit var fallbackCapture: FaceDetection

    val userCaptures = mutableListOf<FaceDetection>()
    var sortedQualifyingCaptures = listOf<FaceDetection>()
    val currentDetection = MutableLiveData<FaceDetection>()
    val capturingState = MutableLiveData(CapturingState.NOT_STARTED)
    private lateinit var faceDetector: FaceDetector

    /**
     * Processes the image
     *
     * @param image is the camera frame
     */
    fun process(image: ImageProxy, screenOrientation: ScreenOrientation) {
        val captureStartTime = timeHelper.now()
        val croppedBitmap = frameProcessor.cropRotateFrame(image, screenOrientation)
        if (croppedBitmap == null) {
            image.close()
            return
        }
        val potentialFace = faceDetector.analyze(croppedBitmap)

        val faceDetection = getFaceDetectionFromPotentialFace(croppedBitmap, potentialFace)
        faceDetection.detectionStartTime = captureStartTime
        faceDetection.detectionEndTime = timeHelper.now()

        currentDetection.postValue(faceDetection)

        when (capturingState.value) {
            CapturingState.NOT_STARTED -> updateFallbackCaptureIfValid(faceDetection)
            CapturingState.CAPTURING -> {
                userCaptures += faceDetection
                if (userCaptures.size == samplesToCapture) {
                    finishCapture(attemptNumber)
                }
            }

            else -> {//no-op
            }
        }
        image.close()
    }

    fun initFrameProcessor(
        samplesToCapture: Int,
        attemptNumber: Int,
        cropRect: RectF,
        previewSize: Size,
    ) {
        this.samplesToCapture = samplesToCapture
        this.attemptNumber = attemptNumber
        viewModelScope.launch {
            faceDetector = resolveFaceBioSdk().detector
            frameProcessor.init(previewSize, cropRect)

            qualityThreshold = configManager.getProjectConfiguration().face?.qualityThreshold ?: 0
        }
    }

    fun clearFrameProcessor() = frameProcessor.clear()

    fun startCapture() {
        capturingState.value = CapturingState.CAPTURING
    }

    /**
     * If any of the user captures are good, use them. If not, use the fallback capture.
     */
    private fun finishCapture(attemptNumber: Int) {
        viewModelScope.launch {
            sortedQualifyingCaptures = userCaptures
                .filter { it.hasValidStatus() }
                .sortedByDescending { it.face?.quality }
                .ifEmpty { listOf(fallbackCapture) }

            sendAllCaptureEvents(attemptNumber)

            capturingState.postValue(CapturingState.FINISHED)
        }
    }

    private fun getFaceDetectionFromPotentialFace(
        bitmap: Bitmap,
        potentialFace: Face?,
    ): FaceDetection {
        return if (potentialFace == null) {
            bitmap.recycle()
            FaceDetection(
                bitmap = bitmap,
                face = null,
                status = FaceDetection.Status.NOFACE,
                detectionStartTime = timeHelper.now(),
                detectionEndTime = timeHelper.now()
            )
        } else {
            getFaceDetection(bitmap, potentialFace)
        }
    }

    private fun getFaceDetection(bitmap: Bitmap, potentialFace: Face): FaceDetection {
        val areaOccupied = potentialFace.relativeBoundingBox.area()
        val status = when {
            areaOccupied < faceTarget.areaRange.start -> FaceDetection.Status.TOOFAR
            areaOccupied > faceTarget.areaRange.endInclusive -> FaceDetection.Status.TOOCLOSE
            potentialFace.yaw !in faceTarget.yawTarget -> FaceDetection.Status.OFFYAW
            potentialFace.roll !in faceTarget.rollTarget -> FaceDetection.Status.OFFROLL
            potentialFace.quality < qualityThreshold -> FaceDetection.Status.BAD_QUALITY
            capturingState.value == CapturingState.CAPTURING -> FaceDetection.Status.VALID_CAPTURING
            else -> FaceDetection.Status.VALID
        }

        return FaceDetection(
            bitmap = bitmap, face = potentialFace,
            status = status,
            detectionStartTime = timeHelper.now(),
            detectionEndTime = timeHelper.now(),
        )
    }

    /**
     * While the user has not started the capture flow, we save fallback images. If the capture doesn't
     * get any good images, at least one good image will be saved
     */
    private fun updateFallbackCaptureIfValid(faceDetection: FaceDetection) {
        if (faceDetection.hasValidStatus()) {
            fallbackCapture = faceDetection.apply { isFallback = true }
            createFirstFallbackCaptureEvent(faceDetection)
        }
    }

    /**
     * Send a fallback capture event only once
     */
    private fun createFirstFallbackCaptureEvent(faceDetection: FaceDetection) {
        if (shouldSendFallbackCaptureEvent.getAndSet(false)) {
            eventReporter.addFallbackCaptureEvent(
                fallbackCaptureEventStartTime,
                faceDetection.detectionEndTime
            )
        }
    }

    /**
     * Since events are saved in a blocking way in [SimpleCaptureEventReporter.addCaptureEvents],
     * to speed things up this method creates multiple async jobs and run them all in parallel.
     */
    private fun sendAllCaptureEvents(attemptNumber: Int) = runBlocking {
        userCaptures.map { async { sendCaptureEvent(it, attemptNumber) } }
            .plus(async { sendCaptureEvent(fallbackCapture, attemptNumber) })
            .awaitAll()
    }

    private suspend fun sendCaptureEvent(faceDetection: FaceDetection, attemptNumber: Int) {
        val qualityThreshold =
            configManager.getProjectConfiguration().face!!.qualityThreshold.toFloat()
        eventReporter.addCaptureEvents(faceDetection, attemptNumber, qualityThreshold)
    }

    enum class CapturingState { NOT_STARTED, CAPTURING, FINISHED }

    companion object {

        private const val VALID_ROLL_DELTA = 15f
        private const val VALID_YAW_DELTA = 30f
    }
}
