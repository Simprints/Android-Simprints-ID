package com.simprints.id.data.db.session.remote.events

import com.google.common.truth.Truth.assertThat
import com.simprints.id.data.db.session.domain.models.events.AlertScreenEvent
import com.simprints.id.data.db.session.remote.events.ApiAlertScreenEvent.ApiAlertScreenEventType.Companion.fromDomainToApi
import org.junit.Test

class ApiAlertScreenEventTypeTest {

    @Test
    fun differentProjectId_fromDomainToApi() {
        val domain = AlertScreenEvent.AlertScreenEventType.DIFFERENT_PROJECT_ID
        val api = ApiAlertScreenEvent.ApiAlertScreenEventType.DIFFERENT_PROJECT_ID

        assertThat(fromDomainToApi(domain)).isEqualTo(api)
    }

    @Test
    fun differentUserId_fromDomainToApi() {
        val domain = AlertScreenEvent.AlertScreenEventType.DIFFERENT_USER_ID
        val api = ApiAlertScreenEvent.ApiAlertScreenEventType.DIFFERENT_USER_ID

        assertThat(fromDomainToApi(domain)).isEqualTo(api)
    }

    @Test
    fun guidNotFoundOnline_fromDomainToApi() {
        val domain = AlertScreenEvent.AlertScreenEventType.GUID_NOT_FOUND_ONLINE
        val api = ApiAlertScreenEvent.ApiAlertScreenEventType.GUID_NOT_FOUND_ONLINE

        assertThat(fromDomainToApi(domain)).isEqualTo(api)
    }

    @Test
    fun guidNotFoundOffline_fromDomainToApi() {
        val domain = AlertScreenEvent.AlertScreenEventType.GUID_NOT_FOUND_OFFLINE
        val api = ApiAlertScreenEvent.ApiAlertScreenEventType.GUID_NOT_FOUND_OFFLINE

        assertThat(fromDomainToApi(domain)).isEqualTo(api)
    }

    @Test
    fun bluetoothNotSupported_fromDomainToApi() {
        val domain = AlertScreenEvent.AlertScreenEventType.BLUETOOTH_NOT_SUPPORTED
        val api = ApiAlertScreenEvent.ApiAlertScreenEventType.BLUETOOTH_NOT_SUPPORTED

        assertThat(fromDomainToApi(domain)).isEqualTo(api)
    }

    @Test
    fun lowBattery_fromDomainToApi() {
        val domain = AlertScreenEvent.AlertScreenEventType.LOW_BATTERY
        val api = ApiAlertScreenEvent.ApiAlertScreenEventType.LOW_BATTERY

        assertThat(fromDomainToApi(domain)).isEqualTo(api)
    }

    @Test
    fun unexpectedError_fromDomainToApi() {
        val domain = AlertScreenEvent.AlertScreenEventType.UNEXPECTED_ERROR
        val api = ApiAlertScreenEvent.ApiAlertScreenEventType.UNEXPECTED_ERROR

        assertThat(fromDomainToApi(domain)).isEqualTo(api)
    }

    @Test
    fun disconnected_fromDomainToApi() {
        val domain = AlertScreenEvent.AlertScreenEventType.DISCONNECTED
        val api = ApiAlertScreenEvent.ApiAlertScreenEventType.DISCONNECTED

        assertThat(fromDomainToApi(domain)).isEqualTo(api)
    }

    @Test
    @Suppress("deprecation")
    fun multiplePairedScanners_fromDomainToApi() {
        val domain = AlertScreenEvent.AlertScreenEventType.MULTIPLE_PAIRED_SCANNERS
        val api = ApiAlertScreenEvent.ApiAlertScreenEventType.MULTIPLE_PAIRED_SCANNERS

        assertThat(fromDomainToApi(domain)).isEqualTo(api)
    }

    @Test
    @Suppress("deprecation")
    fun notPaired_fromDomainToApi() {
        val domain = AlertScreenEvent.AlertScreenEventType.NOT_PAIRED
        val api = ApiAlertScreenEvent.ApiAlertScreenEventType.NOT_PAIRED

        assertThat(fromDomainToApi(domain)).isEqualTo(api)
    }

    @Test
    fun bluetoothNotEnabled_fromDomainToApi() {
        val domain = AlertScreenEvent.AlertScreenEventType.BLUETOOTH_NOT_ENABLED
        val api = ApiAlertScreenEvent.ApiAlertScreenEventType.BLUETOOTH_NOT_ENABLED

        assertThat(fromDomainToApi(domain)).isEqualTo(api)
    }

    @Test
    @Suppress("deprecation")
    fun invalidIntentAction_fromDomainToApi() {
        val domain = AlertScreenEvent.AlertScreenEventType.INVALID_INTENT_ACTION
        val api = ApiAlertScreenEvent.ApiAlertScreenEventType.INVALID_INTENT_ACTION

        assertThat(fromDomainToApi(domain)).isEqualTo(api)
    }

    @Test
    fun invalidMetadata_fromDomainToApi() {
        val domain = AlertScreenEvent.AlertScreenEventType.INVALID_METADATA
        val api = ApiAlertScreenEvent.ApiAlertScreenEventType.INVALID_METADATA

        assertThat(fromDomainToApi(domain)).isEqualTo(api)
    }

    @Test
    fun invalidModuleId_fromDomainToApi() {
        val domain = AlertScreenEvent.AlertScreenEventType.INVALID_MODULE_ID
        val api = ApiAlertScreenEvent.ApiAlertScreenEventType.INVALID_MODULE_ID

        assertThat(fromDomainToApi(domain)).isEqualTo(api)
    }

    @Test
    fun invalidProjectId_fromDomainToApi() {
        val domain = AlertScreenEvent.AlertScreenEventType.INVALID_PROJECT_ID
        val api = ApiAlertScreenEvent.ApiAlertScreenEventType.INVALID_PROJECT_ID

        assertThat(fromDomainToApi(domain)).isEqualTo(api)
    }

    @Test
    fun invalidSelectedId_fromDomainToApi() {
        val domain = AlertScreenEvent.AlertScreenEventType.INVALID_SELECTED_ID
        val api = ApiAlertScreenEvent.ApiAlertScreenEventType.INVALID_SELECTED_ID

        assertThat(fromDomainToApi(domain)).isEqualTo(api)
    }

    @Test
    fun invalidSessionId_fromDomainToApi() {
        val domain = AlertScreenEvent.AlertScreenEventType.INVALID_SESSION_ID
        val api = ApiAlertScreenEvent.ApiAlertScreenEventType.INVALID_SESSION_ID

        assertThat(fromDomainToApi(domain)).isEqualTo(api)
    }

    @Test
    fun invalidUserId_fromDomainToApi() {
        val domain = AlertScreenEvent.AlertScreenEventType.INVALID_USER_ID
        val api = ApiAlertScreenEvent.ApiAlertScreenEventType.INVALID_USER_ID

        assertThat(fromDomainToApi(domain)).isEqualTo(api)
    }

    @Test
    fun invalidVerifyId_fromDomainToApi() {
        val domain = AlertScreenEvent.AlertScreenEventType.INVALID_VERIFY_ID
        val api = ApiAlertScreenEvent.ApiAlertScreenEventType.INVALID_VERIFY_ID

        assertThat(fromDomainToApi(domain)).isEqualTo(api)
    }

    @Test
    fun safetynetError_fromDomainToApi() {
        val domain = AlertScreenEvent.AlertScreenEventType.SAFETYNET_ERROR
        val api = ApiAlertScreenEvent.ApiAlertScreenEventType.SAFETYNET_ERROR

        assertThat(fromDomainToApi(domain)).isEqualTo(api)
    }

    @Test
    fun faceInvalidLicense_fromDomainToApi() {
        val domain = AlertScreenEvent.AlertScreenEventType.FACE_INVALID_LICENSE
        val api = ApiAlertScreenEvent.ApiAlertScreenEventType.FACE_INVALID_LICENSE

        assertThat(fromDomainToApi(domain)).isEqualTo(api)
    }

    @Test
    fun faceMissingLicense_fromDomainToApi() {
        val domain = AlertScreenEvent.AlertScreenEventType.FACE_MISSING_LICENSE
        val api = ApiAlertScreenEvent.ApiAlertScreenEventType.FACE_MISSING_LICENSE

        assertThat(fromDomainToApi(domain)).isEqualTo(api)
    }

}
