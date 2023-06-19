package com.simprints.infra.events.event.domain.models.face

import androidx.annotation.Keep
import com.simprints.core.tools.utils.randomUUID
import com.simprints.infra.events.event.domain.models.Event
import com.simprints.infra.events.event.domain.models.EventLabels
import com.simprints.infra.events.event.domain.models.EventPayload
import com.simprints.infra.events.event.domain.models.EventType

@Keep
data class FaceCaptureBiometricsEvent(
    override val id: String = randomUUID(),
    override var labels: EventLabels,
    override val payload: FaceCaptureBiometricsPayload,
    override val type: EventType
) : Event() {

    constructor(
        startTime: Long,
        face: FaceCaptureBiometricsPayload.Face,
        labels: EventLabels = EventLabels(),
        id: String = randomUUID(),
        payloadId: String = randomUUID()
    ) : this(
        id,
        labels,
        FaceCaptureBiometricsPayload(
            createdAt = startTime,
            eventVersion = EVENT_VERSION,
            face = face,
            id = payloadId
        ),
        EventType.FACE_CAPTURE_BIOMETRICS
    )

    @Keep
    data class FaceCaptureBiometricsPayload(
        val id: String,
        override val createdAt: Long,
        override val eventVersion: Int,
        val face: Face,
        override var endedAt: Long = 0,
        override val type: EventType = EventType.FACE_CAPTURE_BIOMETRICS
    ) : EventPayload() {

        @Keep
        data class Face(
            val yaw: Float,
            var roll: Float,
            val template: String,
            val quality: Float,
            val format:String
        )
    }

    companion object {
        const val EVENT_VERSION = 0
    }
}
