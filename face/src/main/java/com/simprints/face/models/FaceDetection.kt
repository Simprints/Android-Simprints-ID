package com.simprints.face.models

import com.simprints.face.controllers.core.events.model.FaceCaptureEvent
import com.simprints.face.data.moduleapi.face.responses.entities.FaceSample
import com.simprints.face.data.moduleapi.face.responses.entities.SecuredImageRef
import com.simprints.face.detection.Face
import com.simprints.id.data.db.event.domain.models.face.FaceTemplateFormat
import com.simprints.uicomponents.models.PreviewFrame
import java.util.*

data class FaceDetection(
    val frame: PreviewFrame,
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

    enum class TemplateFormat {
        RANK_ONE_1_23,
        MOCK;

        fun fromDomainToCore(): FaceTemplateFormat =
            when (this) {
                RANK_ONE_1_23 -> FaceTemplateFormat.RANK_ONE_1_23
                MOCK -> FaceTemplateFormat.MOCK
            }
    }

    fun toFaceSample(): FaceSample =
        FaceSample(id, face?.template ?: ByteArray(0), securedImageRef)

    fun toFaceCaptureEvent(attemptNumber: Int, qualityThreshold: Float): FaceCaptureEvent =
        FaceCaptureEvent(
            detectionStartTime,
            detectionEndTime,
            attemptNumber,
            qualityThreshold,
            FaceCaptureEvent.Result.fromFaceDetectionStatus(status),
            isFallback,
            FaceCaptureEvent.EventFace.fromFaceDetectionFace(face)
        )

    fun hasValidStatus(): Boolean = status == Status.VALID || status == Status.VALID_CAPTURING
    fun isAboveQualityThreshold(qualityThreshold: Float): Boolean =
        face?.let { it.quality > qualityThreshold } ?: false
}
