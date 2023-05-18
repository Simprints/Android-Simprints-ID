package com.simprints.face.capture.livefeedback

import android.graphics.Bitmap
import android.graphics.RectF
import android.util.Size
import androidx.camera.core.ImageProxy
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.tools.extentions.area
import com.simprints.face.capture.livefeedback.tools.FrameProcessor
import com.simprints.face.controllers.core.events.FaceSessionEventsManager
import com.simprints.face.controllers.core.events.model.FaceCaptureEvent
import com.simprints.face.controllers.core.events.model.FaceFallbackCaptureEvent
import com.simprints.face.controllers.core.timehelper.FaceTimeHelper
import com.simprints.face.detection.Face
import com.simprints.face.detection.FaceDetector
import com.simprints.face.models.FaceDetection
import com.simprints.face.models.FaceTarget
import com.simprints.face.models.FloatRange
import com.simprints.face.models.SymmetricTarget
import com.simprints.infra.config.ConfigManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@HiltViewModel
class LiveFeedbackFragmentViewModel @Inject constructor(
    private val faceDetector: FaceDetector,
    private val frameProcessor: FrameProcessor,
    private val configManager: ConfigManager,
    private val faceSessionEventsManager: FaceSessionEventsManager,
    private val faceTimeHelper: FaceTimeHelper,
) : ViewModel() {


    private var attemptNumber: Int = 1
    private var samplesToCapture: Int = 1

    private val faceTarget = FaceTarget(
        SymmetricTarget(VALID_YAW_DELTA),
        SymmetricTarget(VALID_ROLL_DELTA),
        FloatRange(0.25f, 0.5f)
    )
    private val fallbackCaptureEventStartTime = faceTimeHelper.now()
    private var shouldSendFallbackCaptureEvent: AtomicBoolean = AtomicBoolean(true)

    lateinit var fallbackCapture: FaceDetection
    val userCaptures = mutableListOf<FaceDetection>()
    var sortedQualifyingCaptures = listOf<FaceDetection>()
    val currentDetection = MutableLiveData<FaceDetection>()
    val capturingState = MutableLiveData(CapturingState.NOT_STARTED)

    /**
     * Processes the image
     *
     * @param image is the camera frame
     */
    fun process(image: ImageProxy) {
        val captureStartTime = faceTimeHelper.now()
        val croppedBitmap = frameProcessor.cropRotateFrame(image)
        val potentialFace = faceDetector.analyze(croppedBitmap)

        val faceDetection = getFaceDetectionFromPotentialFace(croppedBitmap, potentialFace)
        faceDetection.detectionStartTime = captureStartTime
        faceDetection.detectionEndTime = faceTimeHelper.now()

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
        previewSize: Size
    ) {
        this.samplesToCapture = samplesToCapture
        this.attemptNumber = attemptNumber
        frameProcessor.init(previewSize, cropRect)
    }

    fun startCapture() {
        capturingState.value = CapturingState.CAPTURING
    }

    /**
     * If any of the user captures are good, use them. If not, use the fallback capture.
     */
    private fun finishCapture(attemptNumber: Int) {
        viewModelScope.launch {
            val projectConfiguration = configManager.getProjectConfiguration()
            sortedQualifyingCaptures = userCaptures
                .filter { it.hasValidStatus() && it.isAboveQualityThreshold(projectConfiguration.face!!.qualityThreshold) }
                .sortedByDescending { it.face?.quality }
                .ifEmpty { listOf(fallbackCapture) }

            sendAllCaptureEvents(attemptNumber)

            capturingState.postValue(CapturingState.FINISHED)
        }
    }

    private fun getFaceDetectionFromPotentialFace(
        bitmap: Bitmap,
        potentialFace: Face?
    ): FaceDetection {
        return if (potentialFace == null) {
            bitmap.recycle()
            FaceDetection(bitmap, null, FaceDetection.Status.NOFACE)
        } else {
            getFaceDetection(bitmap, potentialFace)
        }
    }

    private fun getFaceDetection(bitmap: Bitmap, potentialFace: Face): FaceDetection {
        val areaOccupied = potentialFace.relativeBoundingBox.area()
        return when {
            areaOccupied < faceTarget.areaRange.start -> FaceDetection(
                bitmap,
                potentialFace,
                FaceDetection.Status.TOOFAR
            )

            areaOccupied > faceTarget.areaRange.endInclusive -> FaceDetection(
                bitmap, potentialFace,
                FaceDetection.Status.TOOCLOSE
            )

            potentialFace.yaw !in faceTarget.yawTarget -> FaceDetection(
                bitmap, potentialFace,
                FaceDetection.Status.OFFYAW
            )

            potentialFace.roll !in faceTarget.rollTarget -> FaceDetection(
                bitmap, potentialFace,
                FaceDetection.Status.OFFROLL
            )

            else -> FaceDetection(
                bitmap, potentialFace,
                if (capturingState.value == CapturingState.CAPTURING) FaceDetection.Status.VALID_CAPTURING else FaceDetection.Status.VALID
            )
        }
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
            faceSessionEventsManager.addEventInBackground(
                FaceFallbackCaptureEvent(
                    fallbackCaptureEventStartTime,
                    faceDetection.detectionEndTime
                )
            )
        }
    }

    private fun sendCaptureEvent(faceDetection: FaceDetection, attemptNumber: Int) {
        viewModelScope.launch {
            val projectConfiguration = configManager.getProjectConfiguration()
            // The payloads of these two events need to have the same ids
            val faceCaptureEvent =
                faceDetection.toFaceCaptureEvent(
                    attemptNumber,
                    projectConfiguration.face!!.qualityThreshold.toFloat(),
                )

            faceSessionEventsManager.addEvent(faceCaptureEvent)

            if (faceCaptureEvent.result == FaceCaptureEvent.Result.VALID)
                faceSessionEventsManager.addEvent(faceDetection.toFaceCaptureBiometricsEvent())

            faceDetection.id = faceCaptureEvent.id
        }
    }

    /**
     * Since all events are saved in a blocking way
     * [FaceSessionEventsManagerImpl.addEvent][com.simprints.face.controllers.core.events.FaceSessionEventsManagerImpl.addEvent],
     * to speed things up this method creates multiple async jobs and run them all in parallel.
     *
     * This is already running in a background Thread because of CameraView, don't worry about the runBlocking.
     */
    private fun sendAllCaptureEvents(attemptNumber: Int) = runBlocking {
        val allDeferredEvents = mutableListOf<Deferred<Unit>>()
        allDeferredEvents += userCaptures.map { async { sendCaptureEvent(it, attemptNumber) } }
        allDeferredEvents += async { sendCaptureEvent(fallbackCapture, attemptNumber) }
        allDeferredEvents.awaitAll()
    }

    enum class CapturingState { NOT_STARTED, CAPTURING, FINISHED }

    companion object {
        private const val VALID_ROLL_DELTA = 15f
        private const val VALID_YAW_DELTA = 30f
    }
}
