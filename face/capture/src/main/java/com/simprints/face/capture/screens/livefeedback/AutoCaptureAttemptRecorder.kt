package com.simprints.face.capture.screens.livefeedback

import androidx.lifecycle.SavedStateHandle
import com.simprints.core.tools.extensions.area
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.face.capture.models.FaceDetection
import com.simprints.face.capture.usecases.SimpleCaptureEventReporter
import com.simprints.infra.config.store.models.FaceLightingConditionsAssessmentMode
import com.simprints.infra.config.store.models.ModalitySdkType
import com.simprints.infra.events.event.domain.models.FaceCaptureAttemptEvent.FaceCaptureAttemptPayload.LightingCheckMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Owns the auto-capture attempt in progress: starts it, feeds it frames, persists it for process
 * death and reports it as a [com.simprints.infra.events.event.domain.models.FaceCaptureAttemptEvent].
 */
internal class AutoCaptureAttemptRecorder(
    private val savedStateHandle: SavedStateHandle,
    private val eventReporter: SimpleCaptureEventReporter,
    private val timeHelper: TimeHelper,
    private val scope: CoroutineScope,
) {
    private var bioSdk: String? = null
    private var lightingMode: LightingCheckMode? = null

    @Volatile
    var activeAttempt: AutoCaptureAttemptStats? = null
        private set

    init {
        reportAttemptPersistedBeforeProcessDeath()
    }

    fun setBioSdk(sdk: ModalitySdkType) {
        bioSdk = sdk.name
    }

    fun setLightingMode(mode: FaceLightingConditionsAssessmentMode) {
        lightingMode = when (mode) {
            FaceLightingConditionsAssessmentMode.OFF -> null
            FaceLightingConditionsAssessmentMode.LOG -> LightingCheckMode.LOG
            FaceLightingConditionsAssessmentMode.HINT -> LightingCheckMode.HINT
        }
    }

    fun start(attemptNb: Int) {
        // Report any unfinished previous attempt as abandoned
        finish()
        activeAttempt = bioSdk
            ?.let {
                AutoCaptureAttemptStats(
                    attemptNb = attemptNb,
                    bioSdk = it,
                    startTime = timeHelper.now(),
                    lightingMode = lightingMode,
                )
            }
            .also { savedStateHandle[KEY_ATTEMPT_STATS] = it?.snapshot() }
    }

    fun record(
        attempt: AutoCaptureAttemptStats,
        faceDetection: FaceDetection,
        frameTime: Timestamp,
        frameWidth: Int,
        frameHeight: Int,
        lighting: FaceLightingCheck.Measurement? = null,
        isLightingHintShown: Boolean = false,
    ) {
        attempt.recordFrame(
            detection = faceDetection,
            areaOccupied = faceDetection.face?.relativeBoundingBox?.area(),
            frameTime = frameTime,
            frameWidth = frameWidth,
            frameHeight = frameHeight,
            lighting = lighting,
            isLightingHintShown = isLightingHintShown,
        )
        attempt.snapshotIfDue()?.let { snapshot ->
            scope.launch {
                // The attempt may have been finished while this write was queued
                if (activeAttempt === attempt) savedStateHandle[KEY_ATTEMPT_STATS] = snapshot
            }
        }
    }

    // Removes the saved-state key synchronously: onCleared runs after viewModelScope is cancelled
    fun finish() {
        val attempt = activeAttempt ?: return
        activeAttempt = null
        eventReporter.addCaptureAttemptEvent(attempt.toEvent(timeHelper.now()))
        savedStateHandle.remove<String>(KEY_ATTEMPT_STATS)
    }

    private fun reportAttemptPersistedBeforeProcessDeath() {
        val snapshot = savedStateHandle.get<String>(KEY_ATTEMPT_STATS) ?: return
        savedStateHandle.remove<String>(KEY_ATTEMPT_STATS)
        val restored = AutoCaptureAttemptStats.restore(snapshot) ?: return
        eventReporter.addRecoveredCaptureAttemptEvent(restored.toEvent(endTime = restored.lastFrameTime))
    }

    companion object {
        internal const val KEY_ATTEMPT_STATS = "face_auto_capture_attempt_stats"
    }
}
