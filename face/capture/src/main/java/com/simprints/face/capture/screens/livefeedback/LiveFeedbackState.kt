package com.simprints.face.capture.screens.livefeedback

import android.graphics.RectF
import com.simprints.core.domain.permission.PermissionStatus
import com.simprints.face.capture.models.FaceDetection

/**
 * Single, immutable snapshot of everything the live-feedback UI needs to render.
 */
internal data class LiveFeedbackState(
    val phase: Phase,
    val feedback: Feedback,
    val isAutoCapture: Boolean,
    val isFaceTrackingEnabled: Boolean,
    // Tracking mode only: keeps the progress on the capture button instead of on the tracked face
    val isProgressAroundCaptureButton: Boolean,
    val permissionStatus: PermissionStatus,
    val progress: Progress,
    val targetBox: FaceTargetBox? = null, // Tracking mode only: the square drawn around the subject. Always null for the cutout.
    val result: List<FaceDetection> = emptyList(),
    val stateInitialised: Boolean = false, // Prevents camera init until the config values are available
) {
    /** Overall capture phase / state machine. */
    enum class Phase { NOT_STARTED, CAPTURING, VALIDATING, VALIDATION_FAILED, FINISHED }

    /**
     * UI-facing guidance derived from the latest [FaceDetection].
     * Decoupled from the biometric [FaceDetection.Status] so the fragment maps it to text/visuals via a simple lookup.
     */
    enum class Feedback { NONE, NO_FACE, LOOK_STRAIGHT, TOO_CLOSE, TOO_FAR, BAD_QUALITY, VALID, VALID_CAPTURING }

    companion object {
        fun initial(isAutoCapture: Boolean = false) = LiveFeedbackState(
            phase = Phase.NOT_STARTED,
            feedback = Feedback.NONE,
            isAutoCapture = isAutoCapture,
            isFaceTrackingEnabled = false,
            isProgressAroundCaptureButton = false,
            permissionStatus = PermissionStatus.Denied,
            progress = Progress.HIDDEN,
        )
    }
}

internal data class Progress(
    val value: Float,
    val tint: Tint,
    val visible: Boolean,
) {
    enum class Tint { DEFAULT, VALID, VALIDATION }

    companion object {
        val HIDDEN = Progress(value = 0f, tint = Tint.DEFAULT, visible = false)
    }
}

internal fun FaceDetection.Status.toFeedback(): LiveFeedbackState.Feedback = when (this) {
    FaceDetection.Status.VALID -> LiveFeedbackState.Feedback.VALID
    FaceDetection.Status.VALID_CAPTURING -> LiveFeedbackState.Feedback.VALID_CAPTURING
    FaceDetection.Status.NOFACE -> LiveFeedbackState.Feedback.NO_FACE
    FaceDetection.Status.OFFYAW -> LiveFeedbackState.Feedback.LOOK_STRAIGHT
    FaceDetection.Status.OFFROLL -> LiveFeedbackState.Feedback.LOOK_STRAIGHT
    FaceDetection.Status.TOOCLOSE -> LiveFeedbackState.Feedback.TOO_CLOSE
    FaceDetection.Status.TOOFAR -> LiveFeedbackState.Feedback.TOO_FAR
    FaceDetection.Status.BAD_QUALITY -> LiveFeedbackState.Feedback.BAD_QUALITY
}

/**
 * The square drawn around the tracked face.
 */
internal data class FaceTargetBox(
    val rect: RectF,
    val tint: Tint,
) {
    enum class Tint { INVALID, WARNING, VALID }
}

/**
 * Colour of the tracking square:
 * - red when the face cannot be used at all,
 * - yellow when it is usable but pose or quality is off,
 * - green when every parameter matches.
 */
internal fun FaceDetection.Status.toTargetTint(): FaceTargetBox.Tint = when (this) {
    FaceDetection.Status.VALID, FaceDetection.Status.VALID_CAPTURING -> FaceTargetBox.Tint.VALID
    FaceDetection.Status.OFFYAW, FaceDetection.Status.OFFROLL, FaceDetection.Status.BAD_QUALITY -> FaceTargetBox.Tint.WARNING
    FaceDetection.Status.TOOFAR, FaceDetection.Status.TOOCLOSE, FaceDetection.Status.NOFACE -> FaceTargetBox.Tint.INVALID
}
