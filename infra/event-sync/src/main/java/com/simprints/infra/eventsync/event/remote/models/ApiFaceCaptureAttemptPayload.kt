package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.FaceCaptureAttemptEvent.FaceCaptureAttemptPayload
import kotlinx.serialization.Serializable

@Keep
@Serializable
internal data class ApiFaceCaptureAttemptPayload(
    override val startTime: ApiTimestamp,
    val endTime: ApiTimestamp?,
    val attemptNb: Int,
    val bioSdk: String,
    val targetFrameWidth: Int?,
    val targetFrameHeight: Int?,
    val totalFramesAnalysed: Int,
    val validFrameCount: Int,
    val timeToFirstValidMs: Long?,
    val rejectionStats: ApiRejectionStats,
    val qualityAllFrames: ApiValueStats?,
    val statusRuns: List<ApiStatusRun>,
    val statusRunsTruncated: Boolean?,
) : ApiEventPayload() {
    constructor(domainPayload: FaceCaptureAttemptPayload) : this(
        startTime = domainPayload.createdAt.fromDomainToApi(),
        endTime = domainPayload.endedAt?.fromDomainToApi(),
        attemptNb = domainPayload.attemptNb,
        bioSdk = domainPayload.bioSdk,
        targetFrameWidth = domainPayload.targetFrameWidth,
        targetFrameHeight = domainPayload.targetFrameHeight,
        totalFramesAnalysed = domainPayload.totalFramesAnalysed,
        validFrameCount = domainPayload.validFrameCount,
        timeToFirstValidMs = domainPayload.timeToFirstValidMs,
        rejectionStats = domainPayload.rejectionStats.fromDomainToApi(),
        qualityAllFrames = domainPayload.qualityAllFrames?.fromDomainToApi(),
        statusRuns = domainPayload.statusRuns.map { it.fromDomainToApi() },
        // Absent means false
        statusRunsTruncated = domainPayload.statusRunsTruncated.takeIf { it },
    )

    @Keep
    @Serializable
    data class ApiRejectionStats(
        val invalid: ApiRejectionMetric? = null,
        val badQuality: ApiRejectionMetric? = null,
        val offYaw: ApiRejectionMetric? = null,
        val offRoll: ApiRejectionMetric? = null,
        val tooClose: ApiRejectionMetric? = null,
        val tooFar: ApiRejectionMetric? = null,
    )

    @Keep
    @Serializable
    data class ApiRejectionMetric(
        val count: Int,
        val firstSeenMs: Long,
        val longestRunMs: Long,
        val minValue: Float? = null,
        val maxValue: Float? = null,
        val medianValue: Float? = null,
    )

    @Keep
    @Serializable
    data class ApiValueStats(
        val minValue: Float,
        val maxValue: Float,
        val medianValue: Float,
    )

    @Keep
    @Serializable
    data class ApiStatusRun(
        val status: ApiRunStatus,
        val frameCount: Int,
        val durationMs: Long,
    )

    /** Same names as [ApiFaceCapturePayload.ApiResult], so the two events read alike. */
    @Keep
    enum class ApiRunStatus {
        VALID,
        INVALID,
        BAD_QUALITY,
        OFF_YAW,
        OFF_ROLL,
        TOO_CLOSE,
        TOO_FAR,
    }

    override fun getTokenizedFieldJsonPath(tokenKeyType: TokenKeyType): String? = null
}

internal fun FaceCaptureAttemptPayload.RejectionStats.fromDomainToApi() = ApiFaceCaptureAttemptPayload.ApiRejectionStats(
    invalid = invalid?.fromDomainToApi(),
    badQuality = badQuality?.fromDomainToApi(),
    offYaw = offYaw?.fromDomainToApi(),
    offRoll = offRoll?.fromDomainToApi(),
    tooClose = tooClose?.fromDomainToApi(),
    tooFar = tooFar?.fromDomainToApi(),
)

internal fun FaceCaptureAttemptPayload.RejectionMetric.fromDomainToApi() = ApiFaceCaptureAttemptPayload.ApiRejectionMetric(
    count = count,
    firstSeenMs = firstSeenMs,
    longestRunMs = longestRunMs,
    minValue = minValue,
    maxValue = maxValue,
    medianValue = medianValue,
)

internal fun FaceCaptureAttemptPayload.ValueStats.fromDomainToApi() = ApiFaceCaptureAttemptPayload.ApiValueStats(
    minValue = minValue,
    maxValue = maxValue,
    medianValue = medianValue,
)

internal fun FaceCaptureAttemptPayload.StatusRun.fromDomainToApi() = ApiFaceCaptureAttemptPayload.ApiStatusRun(
    status = status.fromDomainToApi(),
    frameCount = frameCount,
    durationMs = durationMs,
)

internal fun FaceCaptureAttemptPayload.RunStatus.fromDomainToApi() = when (this) {
    FaceCaptureAttemptPayload.RunStatus.VALID -> ApiFaceCaptureAttemptPayload.ApiRunStatus.VALID
    FaceCaptureAttemptPayload.RunStatus.INVALID -> ApiFaceCaptureAttemptPayload.ApiRunStatus.INVALID
    FaceCaptureAttemptPayload.RunStatus.BAD_QUALITY -> ApiFaceCaptureAttemptPayload.ApiRunStatus.BAD_QUALITY
    FaceCaptureAttemptPayload.RunStatus.OFF_YAW -> ApiFaceCaptureAttemptPayload.ApiRunStatus.OFF_YAW
    FaceCaptureAttemptPayload.RunStatus.OFF_ROLL -> ApiFaceCaptureAttemptPayload.ApiRunStatus.OFF_ROLL
    FaceCaptureAttemptPayload.RunStatus.TOO_CLOSE -> ApiFaceCaptureAttemptPayload.ApiRunStatus.TOO_CLOSE
    FaceCaptureAttemptPayload.RunStatus.TOO_FAR -> ApiFaceCaptureAttemptPayload.ApiRunStatus.TOO_FAR
}
