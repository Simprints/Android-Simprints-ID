package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.FaceCaptureAttemptEvent.FaceCaptureAttemptPayload
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray

/**
 * Wire model for `FaceCaptureAttempt v1`.
 *
 * Mirrors the JSON schema agreed with Cloud: nullable fields are omitted from the output
 * (SimJson has explicitNulls = false), and each status run is encoded as the tuple
 * `[status, frameCount, durationMs]`.
 */
@Keep
@Serializable
internal data class ApiFaceCaptureAttemptPayload(
    override val startTime: ApiTimestamp,
    val endTime: ApiTimestamp?,
    val attemptNb: Int,
    val bioSdk: ApiBioSdk,
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
        bioSdk = domainPayload.bioSdk.fromDomainToApi(),
        targetFrameWidth = domainPayload.targetFrameWidth,
        targetFrameHeight = domainPayload.targetFrameHeight,
        totalFramesAnalysed = domainPayload.totalFramesAnalysed,
        validFrameCount = domainPayload.validFrameCount,
        timeToFirstValidMs = domainPayload.timeToFirstValidMs,
        rejectionStats = domainPayload.rejectionStats.fromDomainToApi(),
        qualityAllFrames = domainPayload.qualityAllFrames?.fromDomainToApi(),
        statusRuns = domainPayload.statusRuns.map { it.fromDomainToApi() },
        // Schema: "absent means false" - only emit the flag when it carries information.
        statusRunsTruncated = domainPayload.statusRunsTruncated.takeIf { it },
    )

    @Keep
    @Serializable
    enum class ApiBioSdk {
        RANK_ONE,
        SIM_FACE,
    }

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

    /** Encoded on the wire as `[status, frameCount, durationMs]`. */
    @Keep
    @Serializable(with = ApiStatusRunSerializer::class)
    data class ApiStatusRun(
        val status: String,
        val frameCount: Int,
        val durationMs: Long,
    )

    override fun getTokenizedFieldJsonPath(tokenKeyType: TokenKeyType): String? = null // this payload doesn't have tokenizable fields
}

internal object ApiStatusRunSerializer : KSerializer<ApiFaceCaptureAttemptPayload.ApiStatusRun> {
    override val descriptor: SerialDescriptor = JsonArray.serializer().descriptor

    override fun serialize(
        encoder: Encoder,
        value: ApiFaceCaptureAttemptPayload.ApiStatusRun,
    ) {
        val jsonEncoder = encoder as? JsonEncoder
            ?: error("ApiStatusRun can only be serialized to JSON")
        jsonEncoder.encodeJsonElement(
            buildJsonArray {
                add(JsonPrimitive(value.status))
                add(JsonPrimitive(value.frameCount))
                add(JsonPrimitive(value.durationMs))
            },
        )
    }

    override fun deserialize(decoder: Decoder): ApiFaceCaptureAttemptPayload.ApiStatusRun {
        error("Deserialization of ApiStatusRun is not supported")
    }
}

internal fun FaceCaptureAttemptPayload.BioSdk.fromDomainToApi() = when (this) {
    FaceCaptureAttemptPayload.BioSdk.RANK_ONE -> ApiFaceCaptureAttemptPayload.ApiBioSdk.RANK_ONE
    FaceCaptureAttemptPayload.BioSdk.SIM_FACE -> ApiFaceCaptureAttemptPayload.ApiBioSdk.SIM_FACE
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

/** Wire vocabulary for status runs, matching the `statusRun` enum in the schema. */
internal fun FaceCaptureAttemptPayload.RunStatus.fromDomainToApi(): String = when (this) {
    FaceCaptureAttemptPayload.RunStatus.VALID -> "valid"
    FaceCaptureAttemptPayload.RunStatus.INVALID -> "invalid"
    FaceCaptureAttemptPayload.RunStatus.BAD_QUALITY -> "badQuality"
    FaceCaptureAttemptPayload.RunStatus.OFF_YAW -> "offYaw"
    FaceCaptureAttemptPayload.RunStatus.OFF_ROLL -> "offRoll"
    FaceCaptureAttemptPayload.RunStatus.TOO_CLOSE -> "tooClose"
    FaceCaptureAttemptPayload.RunStatus.TOO_FAR -> "tooFar"
}
