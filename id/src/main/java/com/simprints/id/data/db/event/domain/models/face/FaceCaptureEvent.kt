package com.simprints.id.data.db.event.domain.models.face

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.EventLabels

import com.simprints.id.data.db.event.domain.models.EventPayload
import com.simprints.id.data.db.event.domain.models.EventType
import com.simprints.id.data.db.event.domain.models.EventType.FACE_CAPTURE
import com.simprints.id.data.db.event.domain.models.face.FaceCaptureEvent.FaceCapturePayload.Face
import java.util.*

@Keep
data class FaceCaptureEvent(
    override val id: String = UUID.randomUUID().toString(),
    override var labels: EventLabels,
    override val payload: FaceCapturePayload,
    override val type: EventType
) : Event(id, labels, payload, type) {

    constructor(
        startTime: Long,
        endTime: Long,
        attemptNb: Int,
        qualityThreshold: Float,
        result: FaceCapturePayload.Result,
        isFallback: Boolean,
        face: Face?,
        labels: EventLabels = EventLabels()//StopShip: to change in PAS-993
    ) : this(
        UUID.randomUUID().toString(),
        labels,
        FaceCapturePayload(startTime, endTime, EVENT_VERSION, attemptNb, qualityThreshold, result, isFallback, face),
        FACE_CAPTURE)


    @Keep
    data class FaceCapturePayload(
        override val createdAt: Long,
        override var endedAt: Long,
        override val eventVersion: Int,
        val attemptNb: Int,
        val qualityThreshold: Float,
        val result: Result,
        val isFallback: Boolean,
        val face: Face?
    ) : EventPayload(FACE_CAPTURE, eventVersion, createdAt, endedAt) {

        @Keep
        data class Face(
            val yaw: Float,
            var roll: Float,
            val quality: Float,
            val template: String
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
        const val EVENT_VERSION = DEFAULT_EVENT_VERSION
    }
}
