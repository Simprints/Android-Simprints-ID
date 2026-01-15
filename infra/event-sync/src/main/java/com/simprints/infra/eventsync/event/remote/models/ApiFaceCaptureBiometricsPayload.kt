package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.FaceCaptureBiometricsEvent
import kotlinx.serialization.Serializable

@Keep
@Serializable
internal class ApiFaceCaptureBiometricsPayload(
    override val startTime: ApiTimestamp,
    val id: String,
    val face: Face?,
) : ApiEventPayload() {
    @Keep
    @Serializable
    data class Face(
        val yaw: Float,
        val roll: Float,
        val template: String,
        val quality: Float,
        val format: String,
    ) {
        constructor(face: FaceCaptureBiometricsEvent.FaceCaptureBiometricsPayload.Face) : this(
            yaw = face.yaw,
            roll = face.roll,
            template = face.template,
            quality = face.quality,
            format = face.format,
        )
    }

    constructor(domainPayload: FaceCaptureBiometricsEvent.FaceCaptureBiometricsPayload) : this(
        domainPayload.createdAt.fromDomainToApi(),
        domainPayload.id,
        Face(domainPayload.face),
    )

    override fun getTokenizedFieldJsonPath(tokenKeyType: TokenKeyType): String? = null // this payload doesn't have tokenizable fields
}
