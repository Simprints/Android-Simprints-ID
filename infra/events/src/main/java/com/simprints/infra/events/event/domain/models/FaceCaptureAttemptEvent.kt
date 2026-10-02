package com.simprints.infra.events.event.domain.models

import androidx.annotation.Keep
import com.simprints.core.tools.time.Timestamp
import com.simprints.core.tools.utils.randomUUID
import com.simprints.infra.events.event.domain.models.EventType.Companion.FACE_CAPTURE_ATTEMPT_KEY
import com.simprints.infra.events.event.domain.models.EventType.FACE_CAPTURE_ATTEMPT
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Per-attempt summary of face auto-capture.
 *
 * Emitted once per auto-capture attempt, including attempts that produce no capture and attempts
 * abandoned before the imaging window completed. Not emitted in manual capture mode, where every
 * retained frame already produces a [FaceCaptureEvent] carrying its own result.
 *
 * Carries counts, timings and frame dimensions only: no image data and no biometric templates.
 */
@Keep
@Serializable
@SerialName(FACE_CAPTURE_ATTEMPT_KEY)
data class FaceCaptureAttemptEvent(
    override val id: String = randomUUID(),
    override val payload: FaceCaptureAttemptPayload,
    override val type: EventType,
    override var scopeId: String? = null,
    override var projectId: String? = null,
) : Event() {
    constructor(
        startTime: Timestamp,
        endTime: Timestamp,
        attemptNb: Int,
        bioSdk: FaceCaptureAttemptPayload.BioSdk,
        targetFrameWidth: Int?,
        targetFrameHeight: Int?,
        totalFramesAnalysed: Int,
        validFrameCount: Int,
        timeToFirstValidMs: Long?,
        rejectionStats: FaceCaptureAttemptPayload.RejectionStats,
        qualityAllFrames: FaceCaptureAttemptPayload.ValueStats?,
        statusRuns: List<FaceCaptureAttemptPayload.StatusRun>,
        statusRunsTruncated: Boolean,
        id: String = randomUUID(),
    ) : this(
        id,
        FaceCaptureAttemptPayload(
            createdAt = startTime,
            endedAt = endTime,
            eventVersion = EVENT_VERSION,
            attemptNb = attemptNb,
            bioSdk = bioSdk,
            targetFrameWidth = targetFrameWidth,
            targetFrameHeight = targetFrameHeight,
            totalFramesAnalysed = totalFramesAnalysed,
            validFrameCount = validFrameCount,
            timeToFirstValidMs = timeToFirstValidMs,
            rejectionStats = rejectionStats,
            qualityAllFrames = qualityAllFrames,
            statusRuns = statusRuns,
            statusRunsTruncated = statusRunsTruncated,
        ),
        FACE_CAPTURE_ATTEMPT,
    )

    @Keep
    @Serializable
    data class FaceCaptureAttemptPayload(
        override val createdAt: Timestamp,
        override var endedAt: Timestamp?,
        override val eventVersion: Int,
        /** Capture attempt within the capture step, 0-based. */
        val attemptNb: Int,
        /** Which face SDK ran this attempt. Part of the attempt key together with attemptNb. */
        val bioSdk: BioSdk,
        /** Dimensions of the cropped target frame passed to the detector, not the camera or screen resolution. */
        val targetFrameWidth: Int? = null,
        val targetFrameHeight: Int? = null,
        /** How many frames the analyser evaluated during this attempt. */
        val totalFramesAnalysed: Int,
        /** Frames that passed every check. */
        val validFrameCount: Int,
        /** Milliseconds from attempt start to the first valid frame; null when no frame was ever valid. */
        val timeToFirstValidMs: Long? = null,
        val rejectionStats: RejectionStats,
        /** Quality across every analysed frame in which a face was detected, regardless of status. */
        val qualityAllFrames: ValueStats? = null,
        /** Run-length encoded status timeline, capped at [MAX_STATUS_RUNS] entries. */
        val statusRuns: List<StatusRun>,
        val statusRunsTruncated: Boolean = false,
        override val type: EventType = FACE_CAPTURE_ATTEMPT,
    ) : EventPayload() {
        override fun toSafeString(): String =
            "attempt nr: $attemptNb, sdk: $bioSdk, frames: $totalFramesAnalysed, valid: $validFrameCount, " +
                "first valid ms: $timeToFirstValidMs, runs: ${statusRuns.size}, truncated: $statusRunsTruncated"

        @Keep
        @Serializable
        enum class BioSdk {
            RANK_ONE,
            SIM_FACE,
        }

        /**
         * Per rejection reason: how often, how early and how long it blocked the attempt, and the value
         * of the check that failed. A null entry means that reason did not occur.
         *
         * A frame's reason is the first failing check in a fixed order (area, yaw, roll, quality), so
         * reasons mask one another and counts are not independent.
         */
        @Keep
        @Serializable
        data class RejectionStats(
            /** No face detected. Carries no value statistics. */
            val invalid: RejectionMetric? = null,
            val badQuality: RejectionMetric? = null,
            val offYaw: RejectionMetric? = null,
            val offRoll: RejectionMetric? = null,
            val tooClose: RejectionMetric? = null,
            val tooFar: RejectionMetric? = null,
        )

        @Keep
        @Serializable
        data class RejectionMetric(
            /** Frames analysed with this reason during the attempt. */
            val count: Int,
            /** Milliseconds from attempt start to the first frame with this reason. */
            val firstSeenMs: Long,
            /** Longest uninterrupted stretch, in ms, in which every analysed frame carried this reason. */
            val longestRunMs: Long,
            /**
             * Statistics of the check that failed: area ratio for tooClose/tooFar, signed degrees for
             * offYaw/offRoll, quality score for badQuality. Null for invalid.
             */
            val minValue: Float? = null,
            val maxValue: Float? = null,
            val medianValue: Float? = null,
        )

        @Keep
        @Serializable
        data class ValueStats(
            val minValue: Float,
            val maxValue: Float,
            val medianValue: Float,
        )

        /** One uninterrupted stretch of frames sharing a status. */
        @Keep
        @Serializable
        data class StatusRun(
            val status: RunStatus,
            val frameCount: Int,
            val durationMs: Long,
        )

        @Keep
        @Serializable
        enum class RunStatus {
            VALID,
            INVALID,
            BAD_QUALITY,
            OFF_YAW,
            OFF_ROLL,
            TOO_CLOSE,
            TOO_FAR,
        }
    }

    companion object {
        const val EVENT_VERSION = 1
        const val MAX_STATUS_RUNS = 300
    }
}
