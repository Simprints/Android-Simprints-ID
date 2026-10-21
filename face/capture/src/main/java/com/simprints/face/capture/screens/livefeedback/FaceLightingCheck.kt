package com.simprints.face.capture.screens.livefeedback

import android.graphics.Bitmap
import android.graphics.RectF
import com.simprints.infra.camera.lighting.ClassifyLightingConditionsUseCase
import com.simprints.infra.camera.lighting.LightingConditionsAssessment
import com.simprints.infra.camera.lighting.LightingConditionsAssessmentConfig
import com.simprints.infra.camera.lighting.LightingStats
import com.simprints.infra.camera.lighting.MeasureLightingStatsUseCase
import com.simprints.infra.config.store.models.ExperimentalProjectConfiguration
import com.simprints.infra.config.store.models.FaceLightingConditionsAssessmentMode
import javax.inject.Inject

/**
 * Lighting check for face capture. It only informs: it never blocks capture.
 *
 * Measures the face box, or the whole capture circle when no face is found, about every
 * [Settings.intervalMs] and reuses the last result in between, to keep the per-frame cost low.
 * Only a too-dark frame is reported: glare and too-bright checks give false alarms on faces,
 * and the face SDK copes with moderate darkness, failing only by not finding a very dark face.
 *
 * Not thread-safe apart from [configure]: confine the other calls to the analyser executor.
 */
internal class FaceLightingCheck @Inject constructor(
    private val measureLightingStats: MeasureLightingStatsUseCase,
    private val classifyLightingConditions: ClassifyLightingConditionsUseCase,
) {
    @Volatile
    private var settings: Settings? = null

    private var lastMeasuredMs: Long? = null

    /** Whether the last measured frame was too dark. */
    var isTooDark: Boolean = false
        private set

    val mode: FaceLightingConditionsAssessmentMode
        get() = settings?.mode ?: FaceLightingConditionsAssessmentMode.OFF

    fun configure(config: ExperimentalProjectConfiguration) {
        val mode = config.faceLightingConditionsAssessmentMode
        settings = if (mode == FaceLightingConditionsAssessmentMode.OFF) {
            null
        } else {
            Settings(
                mode = mode,
                intervalMs = config.faceLightingConditionsAssessmentIntervalMillis,
                hintDebouncer = LightingHintDebouncer(config.faceLightingConditionsAssessmentHintDebounceMillis),
                assessmentConfig = LightingConditionsAssessmentConfig(
                    isEnabled = true,
                    borderWidthPercent = BORDER_WIDTH_PERCENT,
                    lowContrastThresholdPercent = config.faceLightingConditionsAssessmentLowContrast,
                    lowMedianLuminanceThresholdPercent = config.faceLightingConditionsAssessmentLowBrightness,
                    highMedianLuminanceThresholdPercent = TOO_BRIGHT_DISABLED_PERCENT,
                    highGlareLuminanceThresholdPercent = TOO_BRIGHT_DISABLED_PERCENT,
                    glareDetectionGridMinDimension = 1,
                    isGlareDetectionEnabled = false,
                ),
            )
        }
    }

    /**
     * Measures [bitmap] if the check is on and the last measurement is older than the interval.
     * Must be called before [bitmap] is recycled.
     *
     * @param faceBox face bounding box relative to [bitmap], or null when no face was found
     * @return the new measurement, or null when the check is off or not due
     */
    fun measureIfDue(
        bitmap: Bitmap,
        faceBox: RectF?,
        nowMs: Long,
    ): Measurement? {
        val settings = settings ?: return null
        val lastMs = lastMeasuredMs
        if (lastMs != null && nowMs - lastMs < settings.intervalMs) return null

        val stats = measureLightingStats(
            bitmap = bitmap,
            region = faceBox,
            borderWidthPercent = if (faceBox == null) BORDER_WIDTH_PERCENT else 0,
        ) ?: return null
        lastMeasuredMs = nowMs
        isTooDark = classifyLightingConditions(stats, settings.assessmentConfig) == LightingConditionsAssessment.TOO_DIM
        return Measurement(stats = stats, isTooDark = isTooDark)
    }

    /**
     * Whether the too-dark hint should be visible: in hint mode only, when no face is found on a too-dark frame,
     * debounced so it does not flicker.
     */
    fun updateHint(
        isNoFace: Boolean,
        nowMs: Long,
    ): Boolean {
        val settings = settings ?: return false
        if (settings.mode != FaceLightingConditionsAssessmentMode.HINT) return false
        return settings.hintDebouncer.update(condition = isNoFace && isTooDark, nowMs = nowMs)
    }

    /** Forgets the last measurement and hides the hint, e.g. when capture is paused. */
    fun reset() {
        settings?.hintDebouncer?.reset()
        lastMeasuredMs = null
        isTooDark = false
    }

    data class Measurement(
        val stats: LightingStats,
        val isTooDark: Boolean,
    )

    private class Settings(
        val mode: FaceLightingConditionsAssessmentMode,
        val intervalMs: Long,
        val hintDebouncer: LightingHintDebouncer,
        val assessmentConfig: LightingConditionsAssessmentConfig,
    )

    private companion object {
        // Ignore the circle's edge, where the frame may include background around the target
        private const val BORDER_WIDTH_PERCENT = 5

        // Median luminance is never above 100%, so the too-bright rule never fires
        private const val TOO_BRIGHT_DISABLED_PERCENT = 100
    }
}
