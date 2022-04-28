package com.simprints.eventsystem.event.domain.models.face

import androidx.annotation.Keep
import com.simprints.eventsystem.event.domain.models.Event
import com.simprints.eventsystem.event.domain.models.EventLabels
import com.simprints.eventsystem.event.domain.models.EventPayload
import com.simprints.eventsystem.event.domain.models.EventType
import com.simprints.eventsystem.event.domain.models.face.FaceCaptureEventV3.Companion.FACE_CAPTURE_ID

@Keep
data class FaceCaptureBiometricsEvent(
    override val id: String = FACE_CAPTURE_ID,
    override var labels: EventLabels,
    override val payload: FaceCaptureBiometricsPayload,
    override val type: EventType
) : Event() {

    constructor(
        startTime: Long,
        qualityThreshold: Float,
        result: FaceCaptureBiometricsPayload.Result,
        face: FaceCaptureBiometricsPayload.Face?,
        labels: EventLabels = EventLabels(),
        id: String = FACE_CAPTURE_ID,
    ) : this(
        id,
        labels,
        FaceCaptureBiometricsPayload(id, startTime, EVENT_VERSION, qualityThreshold, result, face),
        EventType.FACE_CAPTURE_BIOMETRICS
    )

    @Keep
    data class FaceCaptureBiometricsPayload(
        val id: String,
        override val createdAt: Long,
        override val eventVersion: Int,
        val qualityThreshold: Float,
        val result: Result,
        val face: Face?,
        override var endedAt: Long = 0,
        override val type: EventType = EventType.FACE_CAPTURE_BIOMETRICS
    ) : EventPayload() {

        @Keep
        data class Face(
            val template: String,
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
        const val EVENT_VERSION = 0
    }
}
