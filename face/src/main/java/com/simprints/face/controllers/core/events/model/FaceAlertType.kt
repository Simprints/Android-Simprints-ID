package com.simprints.face.controllers.core.events.model

import com.simprints.face.error.ErrorType
import com.simprints.id.data.db.event.domain.models.AlertScreenEvent.AlertScreenPayload.AlertScreenEventType

enum class FaceAlertType {
    UNEXPECTED_ERROR,
    FACE_LICENSE_INVALID,
    FACE_LICENSE_MISSING;

    fun fromDomainToCore(): AlertScreenEventType = when (this) {
        FACE_LICENSE_INVALID -> AlertScreenEventType.FACE_LICENSE_INVALID
        FACE_LICENSE_MISSING -> AlertScreenEventType.FACE_LICENSE_MISSING
        UNEXPECTED_ERROR -> AlertScreenEventType.UNEXPECTED_ERROR
    }

    companion object {
        fun fromErrorType(errorType: ErrorType): FaceAlertType = when (errorType) {
            ErrorType.LICENSE_MISSING -> FACE_LICENSE_MISSING
            ErrorType.LICENSE_INVALID -> FACE_LICENSE_INVALID
            ErrorType.CONFIGURATION_ERROR -> FACE_LICENSE_MISSING
            ErrorType.UNEXPECTED_ERROR -> UNEXPECTED_ERROR
        }
    }
}
