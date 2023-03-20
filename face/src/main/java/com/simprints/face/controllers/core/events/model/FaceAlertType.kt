package com.simprints.face.controllers.core.events.model

import com.simprints.infra.events.event.domain.models.AlertScreenEvent.AlertScreenPayload.AlertScreenEventType

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

}
