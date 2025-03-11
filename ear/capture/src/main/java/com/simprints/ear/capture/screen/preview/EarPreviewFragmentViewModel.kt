package com.simprints.ear.capture.screen.preview

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.tools.time.TimeHelper
import com.simprints.ear.capture.models.EarDetection
import com.simprints.ear.capture.usecases.SimpleCaptureEventReporter
import com.simprints.ear.infra.basebiosdk.detection.Ear
import com.simprints.ear.infra.basebiosdk.detection.EarDetector
import com.simprints.ear.infra.biosdkresolver.ResolveEarBioSdkUseCase
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.FACE_CAPTURE
import com.simprints.infra.logging.Simber
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@HiltViewModel
internal class EarPreviewFragmentViewModel @Inject constructor(
    private val resolveEarBioSdk: ResolveEarBioSdkUseCase,
    private val configManager: ConfigManager,
    private val eventReporter: SimpleCaptureEventReporter,
    private val timeHelper: TimeHelper,
) : ViewModel() {
    private var attemptNumber: Int = 1
    private var samplesToCapture: Int = 1
    private var qualityThreshold: Float = 0f

    private val fallbackCaptureEventStartTime = timeHelper.now()
    private var shouldSendFallbackCaptureEvent: AtomicBoolean = AtomicBoolean(true)
    private var fallbackCapture: EarDetection? = null

    val userCaptures = mutableListOf<EarDetection>()
    var sortedQualifyingCaptures = listOf<EarDetection>()
    val currentDetection = MutableLiveData<EarDetection>()
    val capturingState = MutableLiveData(CapturingState.NOT_STARTED)
    private lateinit var earDetector: EarDetector

    fun initCapture(
        samplesToCapture: Int,
        attemptNumber: Int,
    ) {
        Simber.i("Initialise ear detection", tag = FACE_CAPTURE)

        this.samplesToCapture = samplesToCapture
        this.attemptNumber = attemptNumber
        viewModelScope.launch {
            earDetector = resolveEarBioSdk().detector

            val config = configManager.getProjectConfiguration()
            qualityThreshold = config.ear?.qualityThreshold ?: 0f // TODO change to ear
        }
    }

    fun startCapture() {
        capturingState.value = CapturingState.CAPTURING
    }

    /**
     * Processes the image
     *
     * @param croppedBitmap is the camera frame
     */
    fun process(croppedBitmap: Bitmap) {
        val captureStartTime = timeHelper.now()
        val potentialEar = earDetector.analyze(croppedBitmap)

        val earDetection = getEarDetection(croppedBitmap, potentialEar)

        earDetection.detectionStartTime = captureStartTime
        earDetection.detectionEndTime = timeHelper.now()

        currentDetection.postValue(earDetection)

        when (capturingState.value) {
            CapturingState.NOT_STARTED -> updateFallbackCaptureIfValid(earDetection)
            CapturingState.CAPTURING -> {
                userCaptures += earDetection
                if (userCaptures.size == samplesToCapture) {
                    finishCapture(attemptNumber)
                }
            }

            else -> { // no-op
            }
        }
    }

    /**
     * If any of the user captures are good, use them. If not, use the fallback capture.
     */
    private fun finishCapture(attemptNumber: Int) {
        Simber.i("Finish capture", tag = FACE_CAPTURE)
        viewModelScope.launch {
            sortedQualifyingCaptures = userCaptures
                .filter { it.hasValidStatus() }
                .sortedByDescending { it.ear?.quality }
                .ifEmpty { listOfNotNull(fallbackCapture) }

            // TODO sendAllCaptureEvents(attemptNumber)

            capturingState.postValue(CapturingState.FINISHED)
        }
    }

    private fun getEarDetection(
        bitmap: Bitmap,
        potentialFace: Ear?,
    ): EarDetection {
        if (potentialFace == null) {
            bitmap.recycle()
            return EarDetection(
                bitmap = bitmap,
                ear = null,
                status = EarDetection.Status.NO_EAR,
                detectionStartTime = timeHelper.now(),
                detectionEndTime = timeHelper.now(),
            )
        }

        val status = when {
            capturingState.value == CapturingState.CAPTURING -> EarDetection.Status.VALID_CAPTURING
            else -> EarDetection.Status.VALID
        }

        return EarDetection(
            bitmap = bitmap,
            ear = potentialFace,
            status = status,
            detectionStartTime = timeHelper.now(),
            detectionEndTime = timeHelper.now(),
        )
    }

    /**
     * While the user has not started the capture flow, we save fallback images. If the capture doesn't
     * get any good images, at least one good image will be saved
     */
    private fun updateFallbackCaptureIfValid(earDetection: EarDetection) {
        val fallbackQuality = fallbackCapture?.ear?.quality ?: -1f // To ensure that detection is better with defaults
        val detectionQuality = earDetection.ear?.quality ?: 0f

        if (earDetection.hasValidStatus() && detectionQuality >= fallbackQuality) {
            Simber.i("Fallback capture updated", tag = FACE_CAPTURE)
            fallbackCapture = earDetection.apply { isFallback = true }
            // TODO createFirstFallbackCaptureEvent(earDetection)
        }
    }

    /**
     * Send a fallback capture event only once
     */
    private fun createFirstFallbackCaptureEvent(earDetection: EarDetection) {
        if (shouldSendFallbackCaptureEvent.getAndSet(false)) {
            eventReporter.addFallbackCaptureEvent(
                fallbackCaptureEventStartTime,
                earDetection.detectionEndTime,
            )
        }
    }

    /**
     * Since events are saved in a blocking way in [SimpleCaptureEventReporter.addCaptureEvents],
     * to speed things up this method creates multiple async jobs and run them all in parallel.
     */
    private fun sendAllCaptureEvents(attemptNumber: Int) = runBlocking {
        userCaptures
            .map { async { sendCaptureEvent(it, attemptNumber) } }
            .plus(async { sendCaptureEvent(fallbackCapture, attemptNumber) })
            .awaitAll()
    }

    private suspend fun sendCaptureEvent(
        earDetection: EarDetection?,
        attemptNumber: Int,
    ) {
        if (earDetection == null) return
        eventReporter.addCaptureEvents(earDetection, attemptNumber, qualityThreshold)
    }

    enum class CapturingState { NOT_STARTED, CAPTURING, FINISHED }
}
