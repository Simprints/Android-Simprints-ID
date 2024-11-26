package com.simprints.face.capture.screens.livefeedback

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.DispatcherBG
import com.simprints.core.tools.extentions.area
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.face.capture.models.FaceDetection
import com.simprints.face.capture.models.FaceTarget
import com.simprints.face.capture.models.SymmetricTarget
import com.simprints.face.capture.usecases.SimpleCaptureEventReporter
import com.simprints.face.infra.basebiosdk.detection.Face
import com.simprints.face.infra.basebiosdk.detection.FaceDetector
import com.simprints.face.infra.basebiosdk.matching.FaceMatcher
import com.simprints.face.infra.biosdkresolver.ResolveFaceBioSdkUseCase
import com.simprints.infra.config.sync.ConfigManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
internal class LiveFeedbackFragmentViewModel @Inject constructor(
    private val resolveFaceBioSdk: ResolveFaceBioSdkUseCase,
    private val configManager: ConfigManager,
    private val eventReporter: SimpleCaptureEventReporter,
    private val timeHelper: TimeHelper,
    @DispatcherBG private val bgDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private var attemptNumber: Int = 1
    private var samplesToCapture: Int = 1
    private var qualityThreshold: Float = 0f

    private val faceTarget = FaceTarget(
        SymmetricTarget(VALID_YAW_DELTA),
        SymmetricTarget(VALID_ROLL_DELTA),
        0.20f..0.5f
    )
    private var captureStart: Timestamp? = null

    val userCaptures = mutableListOf<FaceDetection>()
    var sortedQualifyingCaptures = listOf<FaceDetection>()
    val currentDetection = MutableLiveData<FaceDetection>()
    val capturingState = MutableLiveData(CapturingState.PREPARING)
    private lateinit var faceDetector: FaceDetector
    private lateinit var faceMatcher: FaceMatcher

    /**
     * Processes the image
     *
     * @param croppedBitmap is the camera frame
     */
    fun process(croppedBitmap: Bitmap) {
        if (capturingState.value == CapturingState.PREPARING) {
            capturingState.postValue(CapturingState.READY_TO_CAPTURE)
        }

        val captureStartTime = timeHelper.now()
        val potentialFace = faceDetector.analyze(croppedBitmap)

        val faceDetection = getFaceDetectionFromPotentialFace(croppedBitmap, potentialFace)
        faceDetection.detectionStartTime = captureStartTime
        faceDetection.detectionEndTime = timeHelper.now()

        currentDetection.postValue(faceDetection)

        if (capturingState.value == CapturingState.CAPTURING) {
            if (faceDetection.status == FaceDetection.Status.VALID_CAPTURING) {
                captureStart = captureStart ?: timeHelper.now()

                userCaptures += faceDetection
                if (userCaptures.size >= samplesToCapture && minimumCaptureTimeHasElapsed()) {
                    finishCapture(attemptNumber)
                }
            }
        }
    }

    fun initCapture(
        samplesToCapture: Int,
        attemptNumber: Int,
    ) {
        this.samplesToCapture = samplesToCapture
        this.attemptNumber = attemptNumber
        viewModelScope.launch {
            val faceBioSdk = resolveFaceBioSdk()
            faceDetector = faceBioSdk.detector
            faceMatcher = faceBioSdk.matcher

            val config = configManager.getProjectConfiguration()
            qualityThreshold = config.face?.qualityThreshold ?: 0f
        }
    }

    fun startCapture() {
        capturingState.value = CapturingState.CAPTURING
    }

    private fun minimumCaptureTimeHasElapsed(): Boolean {
        captureStart?.let { captureStart ->
            val captureTime = timeHelper.now().ms - captureStart.ms
            return captureTime >= 5000
        }
        return false
    }

    /**
     * If any of the user captures are good, use them. If not, use the fallback capture.
     */
    private fun finishCapture(attemptNumber: Int) {
        captureStart = null
        viewModelScope.launch {
            capturingState.value = CapturingState.PROCESSING

            delay(100)

            sortedQualifyingCaptures = runBlocking(bgDispatcher) {
                var sortedCaptures = userCaptures
                    .filter { it.hasValidStatus() }
                    .sortedByDescending { it.face?.quality }

                val bestCapture = sortedCaptures.first()

                sortedCaptures
                    .filter { faceMatcher.getComparisonScore(bestCapture.face?.template!!, it.face?.template!!) > 90f}
                    .take(samplesToCapture)
            }

            sendAllCaptureEvents(attemptNumber)

            capturingState.postValue(CapturingState.FINISHED)
        }
    }

    fun cancelCapture() {
        capturingState.postValue(CapturingState.READY_TO_CAPTURE)
        userCaptures.clear()
        captureStart = null
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
     * Since events are saved in a blocking way in [SimpleCaptureEventReporter.addCaptureEvents],
     * to speed things up this method creates multiple async jobs and run them all in parallel.
     */
    private fun sendAllCaptureEvents(attemptNumber: Int) = runBlocking {
        userCaptures.map { async { sendCaptureEvent(it, attemptNumber) } }
            .awaitAll()
    }

    private suspend fun sendCaptureEvent(faceDetection: FaceDetection?, attemptNumber: Int) {
        if (faceDetection == null) return
        eventReporter.addCaptureEvents(faceDetection, attemptNumber, qualityThreshold)
    }

    enum class CapturingState { PREPARING, READY_TO_CAPTURE, CAPTURING, PROCESSING, FINISHED }

    companion object {

        private const val VALID_ROLL_DELTA = 15f
        private const val VALID_YAW_DELTA = 30f
    }
}
