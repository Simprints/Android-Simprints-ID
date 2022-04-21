package com.simprints.face.controllers.core.events.model

import com.simprints.eventsystem.event.domain.models.AlertScreenEvent.AlertScreenPayload.AlertScreenEventType
import com.simprints.face.error.ErrorType

enum class FaceAlertType {
    UNEXPECTED_ERROR,
    FACE_LICENSE_INVALID,
    BACKEND_MAINTENANCE_ERROR,
    FACE_LICENSE_MISSING;

    fun fromDomainToCore(): AlertScreenEventType = when (this) {
        FACE_LICENSE_INVALID -> AlertScreenEventType.FACE_LICENSE_INVALID
        FACE_LICENSE_MISSING -> AlertScreenEventType.FACE_LICENSE_MISSING
        UNEXPECTED_ERROR -> AlertScreenEventType.UNEXPECTED_ERROR
        BACKEND_MAINTENANCE_ERROR -> AlertScreenEventType.BACKEND_MAINTENANCE_ERROR
    }

    companion object {
        fun fromErrorType(errorType: ErrorType): FaceAlertType = when (errorType) {
            ErrorType.LICENSE_MISSING -> FACE_LICENSE_MISSING
            ErrorType.LICENSE_INVALID -> FACE_LICENSE_INVALID
            ErrorType.CONFIGURATION_ERROR -> FACE_LICENSE_MISSING
            ErrorType.UNEXPECTED_ERROR -> UNEXPECTED_ERROR
            ErrorType.BACKEND_MAINTENANCE_ERROR -> BACKEND_MAINTENANCE_ERROR
        }
    }
}
