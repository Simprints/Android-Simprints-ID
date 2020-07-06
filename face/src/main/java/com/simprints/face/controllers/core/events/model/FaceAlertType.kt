package com.simprints.face.controllers.core.events.model

import com.simprints.face.error.ErrorType
import com.simprints.id.data.db.session.domain.models.events.AlertScreenEvent.AlertScreenEventType

enum class FaceAlertType {
    FACE_INVALID_LICENSE,
    FACE_MISSING_LICENSE;

    fun fromDomainToCore(): AlertScreenEventType = when (this) {
        FACE_INVALID_LICENSE -> AlertScreenEventType.FACE_INVALID_LICENSE
        FACE_MISSING_LICENSE -> AlertScreenEventType.FACE_MISSING_LICENSE
    }

    companion object {
        fun fromErrorType(errorType: ErrorType): FaceAlertType = when (errorType) {
            ErrorType.LicenseMissing -> FACE_MISSING_LICENSE
            ErrorType.LicenseInvalid -> FACE_INVALID_LICENSE
        }
    }
}
