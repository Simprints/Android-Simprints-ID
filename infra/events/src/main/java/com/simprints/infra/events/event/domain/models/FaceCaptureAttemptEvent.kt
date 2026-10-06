package com.simprints.infra.events.event.domain.models

import androidx.annotation.Keep
import com.simprints.core.tools.time.Timestamp
import com.simprints.core.tools.utils.randomUUID
import com.simprints.infra.events.event.domain.models.EventType.Companion.FACE_CAPTURE_ATTEMPT_KEY
import com.simprints.infra.events.event.domain.models.EventType.FACE_CAPTURE_ATTEMPT
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** One per auto-capture attempt, including abandoned ones. Not emitted in manual capture mode. */
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
        bioSdk: String,
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
        val attemptNb: Int,
        val bioSdk: String,
        /** Cropped frame passed to the detector, not the camera resolution. */
        val targetFrameWidth: Int? = null,
        val targetFrameHeight: Int? = null,
        val totalFramesAnalysed: Int,
        val validFrameCount: Int,
        val timeToFirstValidMs: Long? = null,
        val rejectionStats: RejectionStats,
        val qualityAllFrames: ValueStats? = null,
        val statusRuns: List<StatusRun>,
        val statusRunsTruncated: Boolean = false,
        override val type: EventType = FACE_CAPTURE_ATTEMPT,
    ) : EventPayload() {
        override fun toSafeString(): String =
            "attempt nr: $attemptNb, sdk: $bioSdk, frames: $totalFramesAnalysed, valid: $validFrameCount, " +
                "first valid ms: $timeToFirstValidMs, runs: ${statusRuns.size}, truncated: $statusRunsTruncated"

        /** Null entries mean the reason did not occur. */
        @Keep
        @Serializable
        data class RejectionStats(
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
            val count: Int,
            val firstSeenMs: Long,
            val longestRunMs: Long,
            /** Area ratio, signed degrees or quality score depending on the reason. */
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
