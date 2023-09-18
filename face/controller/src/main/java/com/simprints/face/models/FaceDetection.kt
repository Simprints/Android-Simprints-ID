package com.simprints.face.models

import android.graphics.Bitmap
import com.simprints.face.controllers.core.events.model.FaceCaptureBiometricsEvent
import com.simprints.face.controllers.core.events.model.FaceCaptureEvent
import com.simprints.face.data.moduleapi.face.responses.entities.FaceSample
import com.simprints.face.data.moduleapi.face.responses.entities.SecuredImageRef
import com.simprints.infra.facebiosdk.detection.Face
import java.util.UUID

data class FaceDetection(
    val bitmap: Bitmap,
    val face: Face?,
    val status: Status,
    var securedImageRef: SecuredImageRef? = null,
    var detectionStartTime: Long = System.currentTimeMillis(),
    var isFallback: Boolean = false,
    var id: String = UUID.randomUUID().toString(),
    var detectionEndTime: Long = System.currentTimeMillis()
) {
    enum class Status {
        VALID,
        VALID_CAPTURING,
        NOFACE,
        OFFYAW,
        OFFROLL,
        TOOCLOSE,
        TOOFAR
    }

    fun toFaceSample(): FaceSample =
        FaceSample(
            id,
            face?.template ?: ByteArray(0),
            securedImageRef,
            face?.format ?: ""
        )

    fun toFaceCaptureEvent(attemptNumber: Int, qualityThreshold: Float): FaceCaptureEvent =
        FaceCaptureEvent(
            detectionStartTime,
            detectionEndTime,
            attemptNumber,
            qualityThreshold,
            FaceCaptureEvent.Result.fromFaceDetectionStatus(status),
            isFallback,
            FaceCaptureEvent.EventFace.fromFaceDetectionFace(face),
            payloadId = id
        )

    fun toFaceCaptureBiometricsEvent(): FaceCaptureBiometricsEvent =
        FaceCaptureBiometricsEvent(
            startTime = detectionStartTime,
            eventFace = FaceCaptureBiometricsEvent.EventFace.fromFaceDetectionFace(face)!!,
            payloadId = id
        )

    fun hasValidStatus(): Boolean = status == Status.VALID || status == Status.VALID_CAPTURING
    fun isAboveQualityThreshold(qualityThreshold: Int): Boolean =
        face?.let { it.quality > qualityThreshold } ?: false
}