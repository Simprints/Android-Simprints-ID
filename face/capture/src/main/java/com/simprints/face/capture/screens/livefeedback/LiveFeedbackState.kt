package com.simprints.face.capture.screens.livefeedback

import com.simprints.face.capture.models.FaceDetection

/**
 * Single, immutable snapshot of everything the live-feedback UI needs to render.
 */
internal data class LiveFeedbackState(
    val phase: Phase,
    val feedback: Feedback,
    val isAutoCapture: Boolean,
    val progress: Progress,
    val result: List<FaceDetection> = emptyList(),
) {
    /** Overall capture phase / state machine. */
    enum class Phase { NOT_STARTED, CAPTURING, VALIDATING, VALIDATION_FAILED, FINISHED }

    /**
     * UI-facing guidance derived from the latest [FaceDetection].
     * Decoupled from the biometric [FaceDetection.Status] so the fragment maps it to text/visuals via a simple lookup.
     */
    enum class Feedback { NONE, NO_FACE, LOOK_STRAIGHT, TOO_CLOSE, TOO_FAR, VALID, VALID_CAPTURING }

    companion object {
        fun initial(isAutoCapture: Boolean = false) = LiveFeedbackState(
            phase = Phase.NOT_STARTED,
            feedback = Feedback.NONE,
            isAutoCapture = isAutoCapture,
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
}
