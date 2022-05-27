package com.simprints.face.controllers.core.events.model

import androidx.annotation.Keep
import com.simprints.core.tools.utils.EncodingUtilsImpl
import com.simprints.eventsystem.event.domain.models.face.FaceTemplateFormat
import com.simprints.face.detection.Face
import com.simprints.face.models.FaceDetection
import com.simprints.eventsystem.event.domain.models.face.FaceCaptureBiometricsEvent as CoreFaceCaptureBiometricsEventFace

@Keep
class FaceCaptureBiometricsEvent(
    startTime: Long,
    endTime: Long = 0,
    val result: Result,
    private val qualityThreshold: Float,
    private val eventFace: EventFace?
) : Event(EventType.FACE_CAPTURE_BIOMETRICS, startTime, endTime) {

    fun fromDomainToCore() = CoreFaceCaptureBiometricsEventFace(
        startTime = startTime,
        qualityThreshold = qualityThreshold,
        result = result.fromDomainToCore(),
        face = eventFace?.fromDomainToCore()
    )

    @Keep
    class EventFace(
        val template: String,
        val format: FaceTemplateFormat
    ) {
        fun fromDomainToCore() =
            CoreFaceCaptureBiometricsEventFace.FaceCaptureBiometricsPayload.Face(template, format)

        companion object {
            fun fromFaceDetectionFace(face: Face?): EventFace? =
                face?.let {
                    EventFace(
                        EncodingUtilsImpl.byteArrayToBase64(it.template),
                        it.format.fromDomainToCore()
                    )
                }
        }
    }

    enum class Result {
        VALID,
        INVALID,
        OFF_YAW,
        OFF_ROLL,
        TOO_CLOSE,
        TOO_FAR;

        fun fromDomainToCore(): CoreFaceCaptureBiometricsEventFace.FaceCaptureBiometricsPayload.Result =
            when (this) {
                VALID -> CoreFaceCaptureBiometricsEventFace.FaceCaptureBiometricsPayload.Result.VALID
                INVALID -> CoreFaceCaptureBiometricsEventFace.FaceCaptureBiometricsPayload.Result.INVALID
                OFF_YAW -> CoreFaceCaptureBiometricsEventFace.FaceCaptureBiometricsPayload.Result.OFF_YAW
                OFF_ROLL -> CoreFaceCaptureBiometricsEventFace.FaceCaptureBiometricsPayload.Result.OFF_ROLL
                TOO_CLOSE -> CoreFaceCaptureBiometricsEventFace.FaceCaptureBiometricsPayload.Result.TOO_CLOSE
                TOO_FAR -> CoreFaceCaptureBiometricsEventFace.FaceCaptureBiometricsPayload.Result.TOO_FAR
            }

        companion object {
            fun fromFaceDetectionStatus(status: FaceDetection.Status): Result =
                when (status) {
                    FaceDetection.Status.VALID -> VALID
                    FaceDetection.Status.VALID_CAPTURING -> VALID
                    FaceDetection.Status.NOFACE -> INVALID
                    FaceDetection.Status.OFFYAW -> OFF_YAW
                    FaceDetection.Status.OFFROLL -> OFF_ROLL
                    FaceDetection.Status.TOOCLOSE -> TOO_CLOSE
                    FaceDetection.Status.TOOFAR -> TOO_FAR
                }
        }
    }
}
