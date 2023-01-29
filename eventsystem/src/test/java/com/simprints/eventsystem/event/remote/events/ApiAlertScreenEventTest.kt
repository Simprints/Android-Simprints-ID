package com.simprints.eventsystem.event.remote.events

import com.google.common.truth.Truth.assertThat
import com.simprints.eventsystem.event.domain.models.AlertScreenEvent.AlertScreenPayload.AlertScreenEventType
import com.simprints.eventsystem.event.remote.models.ApiAlertScreenPayload.ApiAlertScreenEventType
import com.simprints.eventsystem.event.remote.models.fromDomainToApi
import org.junit.Test

class ApiAlertScreenEventTest {

    @Test
    fun differentProjectId_fromDomainToApi() {
        val domain = AlertScreenEventType.DIFFERENT_PROJECT_ID
        val api = ApiAlertScreenEventType.DIFFERENT_PROJECT_ID

        assertThat(domain.fromDomainToApi()).isEqualTo(api)
    }

    @Test
    fun differentUserId_fromDomainToApi() {
        val domain = AlertScreenEventType.DIFFERENT_USER_ID
        val api = ApiAlertScreenEventType.DIFFERENT_USER_ID

        assertThat(domain.fromDomainToApi()).isEqualTo(api)
    }

    @Test
    fun guidNotFoundOnline_fromDomainToApi() {
        val domain = AlertScreenEventType.GUID_NOT_FOUND_ONLINE
        val api = ApiAlertScreenEventType.GUID_NOT_FOUND_ONLINE

        assertThat(domain.fromDomainToApi()).isEqualTo(api)
    }

    @Test
    fun guidNotFoundOffline_fromDomainToApi() {
        val domain = AlertScreenEventType.GUID_NOT_FOUND_OFFLINE
        val api = ApiAlertScreenEventType.GUID_NOT_FOUND_OFFLINE

        assertThat(domain.fromDomainToApi()).isEqualTo(api)
    }

    @Test
    fun bluetoothNotSupported_fromDomainToApi() {
        val domain = AlertScreenEventType.BLUETOOTH_NOT_SUPPORTED
        val api = ApiAlertScreenEventType.BLUETOOTH_NOT_SUPPORTED

        assertThat(domain.fromDomainToApi()).isEqualTo(api)
    }

    @Test
    fun lowBattery_fromDomainToApi() {
        val domain = AlertScreenEventType.LOW_BATTERY
        val api = ApiAlertScreenEventType.LOW_BATTERY

        assertThat(domain.fromDomainToApi()).isEqualTo(api)
    }

    @Test
    fun unexpectedError_fromDomainToApi() {
        val domain = AlertScreenEventType.UNEXPECTED_ERROR
        val api = ApiAlertScreenEventType.UNEXPECTED_ERROR

        assertThat(domain.fromDomainToApi()).isEqualTo(api)
    }

    @Test
    fun disconnected_fromDomainToApi() {
        val domain = AlertScreenEventType.DISCONNECTED
        val api = ApiAlertScreenEventType.DISCONNECTED

        assertThat(domain.fromDomainToApi()).isEqualTo(api)
    }

    @Test
    fun multiplePairedScanners_fromDomainToApi() {
        val domain = AlertScreenEventType.MULTIPLE_PAIRED_SCANNERS
        val api = ApiAlertScreenEventType.MULTIPLE_PAIRED_SCANNERS

        assertThat(domain.fromDomainToApi()).isEqualTo(api)
    }

    @Test
    fun notPaired_fromDomainToApi() {
        val domain = AlertScreenEventType.NOT_PAIRED
        val api = ApiAlertScreenEventType.NOT_PAIRED

        assertThat(domain.fromDomainToApi()).isEqualTo(api)
    }

    @Test
    fun bluetoothNotEnabled_fromDomainToApi() {
        val domain = AlertScreenEventType.BLUETOOTH_NOT_ENABLED
        val api = ApiAlertScreenEventType.BLUETOOTH_NOT_ENABLED

        assertThat(domain.fromDomainToApi()).isEqualTo(api)
    }

    @Test
    fun invalidIntentAction_fromDomainToApi() {
        val domain = AlertScreenEventType.INVALID_INTENT_ACTION
        val api = ApiAlertScreenEventType.INVALID_INTENT_ACTION

        assertThat(domain.fromDomainToApi()).isEqualTo(api)
    }

    @Test
    fun invalidMetadata_fromDomainToApi() {
        val domain = AlertScreenEventType.INVALID_METADATA
        val api = ApiAlertScreenEventType.INVALID_METADATA

        assertThat(domain.fromDomainToApi()).isEqualTo(api)
    }

    @Test
    fun invalidModuleId_fromDomainToApi() {
        val domain = AlertScreenEventType.INVALID_MODULE_ID
        val api = ApiAlertScreenEventType.INVALID_MODULE_ID

        assertThat(domain.fromDomainToApi()).isEqualTo(api)
    }

    @Test
    fun invalidProjectId_fromDomainToApi() {
        val domain = AlertScreenEventType.INVALID_PROJECT_ID
        val api = ApiAlertScreenEventType.INVALID_PROJECT_ID

        assertThat(domain.fromDomainToApi()).isEqualTo(api)
    }

    @Test
    fun invalidSelectedId_fromDomainToApi() {
        val domain = AlertScreenEventType.INVALID_SELECTED_ID
        val api = ApiAlertScreenEventType.INVALID_SELECTED_ID

        assertThat(domain.fromDomainToApi()).isEqualTo(api)
    }

    @Test
    fun invalidSessionId_fromDomainToApi() {
        val domain = AlertScreenEventType.INVALID_SESSION_ID
        val api = ApiAlertScreenEventType.INVALID_SESSION_ID

        assertThat(domain.fromDomainToApi()).isEqualTo(api)
    }

    @Test
    fun invalidUserId_fromDomainToApi() {
        val domain = AlertScreenEventType.INVALID_USER_ID
        val api = ApiAlertScreenEventType.INVALID_USER_ID

        assertThat(domain.fromDomainToApi()).isEqualTo(api)
    }

    @Test
    fun invalidVerifyId_fromDomainToApi() {
        val domain = AlertScreenEventType.INVALID_VERIFY_ID
        val api = ApiAlertScreenEventType.INVALID_VERIFY_ID

        assertThat(domain.fromDomainToApi()).isEqualTo(api)
    }

    @Test
    fun integrityError_fromDomainToApi() {
        val domain = AlertScreenEventType.INTEGRITY_SERVICE_ERROR
        val api = ApiAlertScreenEventType.INTEGRITY_SERVICE_ERROR

        assertThat(domain.fromDomainToApi()).isEqualTo(api)
    }

    @Test
    fun backendMaintenanceError_fromDomainToApi() {
        val domain = AlertScreenEventType.BACKEND_MAINTENANCE_ERROR
        val api = ApiAlertScreenEventType.BACKEND_MAINTENANCE_ERROR

        assertThat(domain.fromDomainToApi()).isEqualTo(api)
    }

    @Test
    fun faceLicenseMissingError_fromDomainToApi() {
        val domain = AlertScreenEventType.FACE_LICENSE_MISSING
        val api = ApiAlertScreenEventType.FACE_LICENSE_MISSING

        assertThat(domain.fromDomainToApi()).isEqualTo(api)
    }

    @Test
    fun faceLicenseInvalidError_fromDomainToApi() {
        val domain = AlertScreenEventType.FACE_LICENSE_INVALID
        val api = ApiAlertScreenEventType.FACE_LICENSE_INVALID

        assertThat(domain.fromDomainToApi()).isEqualTo(api)
    }

    @Test
    fun invalidStateForIntentError_fromDomainToApi() {
        val domain = AlertScreenEventType.INVALID_STATE_FOR_INTENT_ACTION
        val api = ApiAlertScreenEventType.INVALID_STATE_FOR_INTENT_ACTION

        assertThat(domain.fromDomainToApi()).isEqualTo(api)
    }

    @Test
    fun enrollastBiometricsFailedError_fromDomainToApi() {
        val domain = AlertScreenEventType.ENROLMENT_LAST_BIOMETRICS_FAILED
        val api = ApiAlertScreenEventType.ENROLMENT_LAST_BIOMETRICS_FAILED

        assertThat(domain.fromDomainToApi()).isEqualTo(api)
    }
}
