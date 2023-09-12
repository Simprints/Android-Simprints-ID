package com.simprints.infra.events.event.domain.models.face

import androidx.annotation.Keep
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.utils.randomUUID
import com.simprints.infra.config.domain.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.Event
import com.simprints.infra.events.event.domain.models.EventLabels
import com.simprints.infra.events.event.domain.models.EventPayload
import com.simprints.infra.events.event.domain.models.EventType
import com.simprints.infra.events.event.domain.models.EventType.FACE_CAPTURE
import com.simprints.infra.events.event.domain.models.face.FaceCaptureEvent.FaceCapturePayload.Face

@Keep
data class FaceCaptureEvent(
    override val id: String = randomUUID(),
    override var labels: EventLabels,
    override val payload: FaceCapturePayload,
    override val type: EventType
) : Event() {

    constructor(
        startTime: Long,
        endTime: Long,
        attemptNb: Int,
        qualityThreshold: Float,
        result: FaceCapturePayload.Result,
        isFallback: Boolean,
        face: Face?,
        labels: EventLabels = EventLabels(),
        id: String = randomUUID(),
        payloadId: String = randomUUID()
    ) : this(
        id,
        labels,
        FaceCapturePayload(
            createdAt = startTime,
            endedAt = endTime,
            eventVersion = EVENT_VERSION,
            attemptNb = attemptNb,
            qualityThreshold = qualityThreshold,
            result = result,
            isFallback = isFallback,
            face = face,
            id = payloadId
        ),
        FACE_CAPTURE
    )

    override fun getTokenizedFields(): Map<TokenKeyType, TokenizableString> = emptyMap()

    override fun setTokenizedFields(map: Map<TokenKeyType, TokenizableString>) = this // No tokenized fields

    @Keep
    data class FaceCapturePayload(
        val id: String,
        override val createdAt: Long,
        override var endedAt: Long,
        override val eventVersion: Int,
        val attemptNb: Int,
        val qualityThreshold: Float,
        val result: Result,
        val isFallback: Boolean,
        val face: Face?,
        override val type: EventType = FACE_CAPTURE
    ) : EventPayload() {

        @Keep
        data class Face(
            val yaw: Float,
            var roll: Float,
            val quality: Float,
            val format: String
        )

        enum class Result {
            VALID,
            INVALID,
            OFF_YAW,
            OFF_ROLL,
            TOO_CLOSE,
            TOO_FAR
        }
    }

    companion object {
        const val EVENT_VERSION = 3
    }
}
