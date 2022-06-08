package com.simprints.eventsystem.event.remote.models.face

import androidx.annotation.Keep
import com.simprints.eventsystem.event.domain.models.face.FaceCaptureBiometricsEvent
import com.simprints.eventsystem.event.domain.models.face.FaceTemplateFormat
import com.simprints.eventsystem.event.remote.models.ApiEventPayload
import com.simprints.eventsystem.event.remote.models.ApiEventPayloadType

@Keep
class ApiFaceCaptureBiometricsPayload(
    override val version: Int,
    override val startTime: Long,
    val id: String,
    val face: Face?
) : ApiEventPayload(ApiEventPayloadType.FaceCaptureBiometrics, version, startTime) {

    @Keep
    data class Face(
        val yaw: Float,
        val roll: Float,
        val template: String,
        val quality: Float,
        val format: FaceTemplateFormat
    ) {
        constructor(face: FaceCaptureBiometricsEvent.FaceCaptureBiometricsPayload.Face) : this(
            yaw = face.yaw,
            roll = face.roll,
            template = face.template,
            quality = face.quality,
            format = face.format
        )
    }

    constructor(domainPayload: FaceCaptureBiometricsEvent.FaceCaptureBiometricsPayload) : this(
        domainPayload.eventVersion,
        domainPayload.createdAt,
        domainPayload.id,
        Face(domainPayload.face)
    )
}
