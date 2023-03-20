package com.simprints.face.error

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.events.event.domain.models.AlertScreenEvent.AlertScreenPayload.AlertScreenEventType
import org.junit.Test

class ErrorTypeTest {

    @Test
    fun errorTypeToConfig_licenseInvalid_eventTypeLicenseInvalid() {
        val config = ErrorType.LICENSE_INVALID.toAlertConfiguration()
        assertThat(config.eventType).isEqualTo(AlertScreenEventType.FACE_LICENSE_INVALID)
    }

    @Test
    fun errorTypeToConfig_licenseMissing_eventTypeLicenseMissing() {
        val config = ErrorType.LICENSE_MISSING.toAlertConfiguration()
        assertThat(config.eventType).isEqualTo(AlertScreenEventType.FACE_LICENSE_MISSING)
    }

    @Test
    fun errorTypeToConfig_configError_eventConfigError() {
        val config = ErrorType.CONFIGURATION_ERROR.toAlertConfiguration()
        assertThat(config.eventType).isEqualTo(AlertScreenEventType.FACE_LICENSE_MISSING)
    }

    @Test
    fun errorTypeToConfig_backendError_eventBackendError() {
        val config = ErrorType.BACKEND_MAINTENANCE_ERROR.toAlertConfiguration()
        assertThat(config.eventType).isEqualTo(AlertScreenEventType.BACKEND_MAINTENANCE_ERROR)
    }

    @Test
    fun errorTypeToConfig_unexpectedError_eventTypeUnexpectedError() {
        val config = ErrorType.UNEXPECTED_ERROR.toAlertConfiguration()
        assertThat(config.eventType).isEqualTo(AlertScreenEventType.UNEXPECTED_ERROR)
    }
}
