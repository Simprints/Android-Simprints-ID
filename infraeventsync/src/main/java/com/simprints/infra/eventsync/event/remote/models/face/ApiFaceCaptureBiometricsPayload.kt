package com.simprints.infra.eventsync.event.remote.models.face

import androidx.annotation.Keep
import com.simprints.infra.events.event.domain.models.face.FaceCaptureBiometricsEvent
import com.simprints.infra.eventsync.event.remote.models.ApiEventPayload
import com.simprints.infra.eventsync.event.remote.models.ApiEventPayloadType

@Keep
internal class ApiFaceCaptureBiometricsPayload(
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
        val format: String
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
