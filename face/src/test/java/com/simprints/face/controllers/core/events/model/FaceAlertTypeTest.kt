package com.simprints.face.controllers.core.events.model

import com.google.common.truth.Truth.assertThat
import com.simprints.eventsystem.event.domain.models.AlertScreenEvent
import com.simprints.face.controllers.core.events.model.FaceAlertType.Companion.fromErrorType
import com.simprints.face.error.ErrorType
import org.junit.Test

class FaceAlertTypeTest {

    @Test
    fun fromDomainToCore_backendMaintenanceError_alertScreenEventType() {
        val coreError = AlertScreenEvent.AlertScreenPayload.AlertScreenEventType.BACKEND_MAINTENANCE_ERROR
        val domainError = FaceAlertType.BACKEND_MAINTENANCE_ERROR

        assertThat(domainError.fromDomainToCore()).isEqualTo(coreError)
    }

    @Test
    fun fromDomainToCore_faceLinceseMissingError_alertScreenEventType() {
        val coreError = AlertScreenEvent.AlertScreenPayload.AlertScreenEventType.FACE_LICENSE_MISSING
        val domainError = FaceAlertType.FACE_LICENSE_MISSING

        assertThat(domainError.fromDomainToCore()).isEqualTo(coreError)
    }

    @Test
    fun fromDomainToCore_unexpectedError_alertScreenEventType() {
        val coreError = AlertScreenEvent.AlertScreenPayload.AlertScreenEventType.UNEXPECTED_ERROR
        val domainError = FaceAlertType.UNEXPECTED_ERROR

        assertThat(domainError.fromDomainToCore()).isEqualTo(coreError)
    }

    @Test
    fun fromDomainToCore_faceLinceseInvalidError_alertScreenEventType() {
        val coreError = AlertScreenEvent.AlertScreenPayload.AlertScreenEventType.FACE_LICENSE_INVALID
        val domainError = FaceAlertType.FACE_LICENSE_INVALID

        assertThat(domainError.fromDomainToCore()).isEqualTo(coreError)
    }

    @Test
    fun fromErrorType_licenseInvalid_alertTypeLicenseInvalid() {
        val type = fromErrorType(ErrorType.LICENSE_INVALID)

        assertThat(type).isInstanceOf(FaceAlertType.FACE_LICENSE_INVALID::class.java)
    }

    @Test
    fun fromErrorType_licenseMissing_alertTypeLicenseMissing() {
        val type = fromErrorType(ErrorType.LICENSE_MISSING)

        assertThat(type).isInstanceOf(FaceAlertType.FACE_LICENSE_MISSING::class.java)
    }

    @Test
    fun fromErrorType_configError_alertTypeConfigError() {
        val type = fromErrorType(ErrorType.CONFIGURATION_ERROR)

        assertThat(type).isInstanceOf(FaceAlertType.FACE_LICENSE_MISSING::class.java)
    }

    @Test
    fun fromErrorType_unexpectedError_alertTypeUnexpectedError() {
        val type = fromErrorType(ErrorType.UNEXPECTED_ERROR)

        assertThat(type).isInstanceOf(FaceAlertType.UNEXPECTED_ERROR::class.java)
    }
}
