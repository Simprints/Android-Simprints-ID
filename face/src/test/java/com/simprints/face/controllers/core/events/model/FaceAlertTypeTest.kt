package com.simprints.face.controllers.core.events.model

import com.google.common.truth.Truth
import com.google.common.truth.Truth.assertThat
import com.simprints.eventsystem.event.domain.models.AlertScreenEvent
import org.junit.Assert.*
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
}
