package com.simprints.face.controllers.core.events.model

import com.simprints.face.error.ErrorType
import com.simprints.id.data.db.session.domain.models.events.AlertScreenEvent.AlertScreenEventType
import com.simprints.id.data.db.session.domain.models.events.AlertScreenEvent.AlertScreenEventType.UNEXPECTED_ERROR

enum class FaceAlertType {
    FACE_INVALID_LICENSE,
    FACE_MISSING_LICENSE;

    // TODO: map to correct errors
    fun fromDomainToCore(): AlertScreenEventType = when (this) {
        FACE_INVALID_LICENSE -> UNEXPECTED_ERROR
        FACE_MISSING_LICENSE -> UNEXPECTED_ERROR
    }

    companion object {
        fun fromErrorType(errorType: ErrorType): FaceAlertType = when (errorType) {
            ErrorType.LicenseMissing -> FACE_MISSING_LICENSE
            ErrorType.LicenseInvalid -> FACE_INVALID_LICENSE
        }
    }
}
