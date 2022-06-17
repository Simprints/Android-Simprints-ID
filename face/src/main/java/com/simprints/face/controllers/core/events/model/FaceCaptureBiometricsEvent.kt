package com.simprints.face.controllers.core.events.model

import androidx.annotation.Keep
import com.simprints.core.tools.utils.EncodingUtilsImpl
import com.simprints.eventsystem.event.domain.models.face.FaceTemplateFormat
import com.simprints.face.detection.Face
import com.simprints.eventsystem.event.domain.models.face.FaceCaptureBiometricsEvent as CoreFaceCaptureBiometricsEventFace

@Keep
class FaceCaptureBiometricsEvent(
    startTime: Long,
    endTime: Long = 0,
    val eventFace: EventFace,
    val payloadId: String
) : Event(EventType.FACE_CAPTURE_BIOMETRICS, startTime, endTime) {

    fun fromDomainToCore() = CoreFaceCaptureBiometricsEventFace(
        startTime = startTime,
        face = eventFace.fromDomainToCore(),
        payloadId = payloadId
    )

    @Keep
    class EventFace(
        val yaw: Float,
        var roll: Float,
        val template: String,
        val quality: Float,
        val format: FaceTemplateFormat
    ) {
        fun fromDomainToCore() =
            CoreFaceCaptureBiometricsEventFace.FaceCaptureBiometricsPayload.Face(
                yaw = yaw,
                roll = roll,
                template = template,
                quality = quality,
                format = format
            )

        companion object {
            fun fromFaceDetectionFace(face: Face?): EventFace? =
                face?.let {
                    EventFace(
                        yaw = it.yaw,
                        roll = it.roll,
                        template = EncodingUtilsImpl.byteArrayToBase64(it.template),
                        quality = it.quality,
                        format = it.format.fromDomainToCore()
                    )
                }
        }
    }
}
