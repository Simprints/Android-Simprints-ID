package com.simprints.eventsystem.event.domain.models.face

import androidx.annotation.Keep
import com.simprints.eventsystem.event.domain.models.Event
import com.simprints.eventsystem.event.domain.models.EventLabels
import com.simprints.eventsystem.event.domain.models.EventPayload
import com.simprints.eventsystem.event.domain.models.EventType
import com.simprints.eventsystem.event.domain.models.EventType.FACE_CAPTURE
import com.simprints.eventsystem.event.domain.models.EventType.FACE_CAPTURE_V3
import com.simprints.eventsystem.event.domain.models.face.FaceCaptureEventV3.FaceCapturePayloadV3.Face
import java.util.UUID

@Keep
data class FaceCaptureEventV3(
    override val id: String = UUID.randomUUID().toString(),
    override var labels: EventLabels,
    override val payload: FaceCapturePayloadV3,
    override val type: EventType
) : Event() {

    constructor(
        startTime: Long,
        endTime: Long,
        attemptNb: Int,
        qualityThreshold: Float,
        result: FaceCapturePayloadV3.Result,
        isFallback: Boolean,
        face: Face?,
        labels: EventLabels = EventLabels(),
        id: String = UUID.randomUUID().toString()
    ) : this(
        id,
        labels,
        FaceCapturePayloadV3(id, startTime, endTime, EVENT_VERSION, attemptNb, qualityThreshold, result, isFallback, face),
        FACE_CAPTURE_V3
    )


    @Keep
    data class FaceCapturePayloadV3(
        val id: String,
        override val createdAt: Long,
        override var endedAt: Long,
        override val eventVersion: Int,
        val attemptNb: Int,
        val qualityThreshold: Float,
        val result: Result,
        val isFallback: Boolean,
        val face: Face?,
        override val type: EventType = FACE_CAPTURE_V3
    ) : EventPayload() {

        @Keep
        data class Face(
            val yaw: Float,
            var roll: Float,
            val quality: Float,
            val format: FaceTemplateFormat = FaceTemplateFormat.RANK_ONE_1_23
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
