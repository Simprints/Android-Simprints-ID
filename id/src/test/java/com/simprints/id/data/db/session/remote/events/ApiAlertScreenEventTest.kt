package com.simprints.id.data.db.session.remote.events

import com.google.common.truth.Truth.assertThat
import com.simprints.id.data.db.session.domain.models.events.AlertScreenEvent
import com.simprints.id.data.db.session.remote.events.ApiAlertScreenEvent
import com.simprints.id.data.db.session.remote.events.ApiAlertScreenEvent.ApiAlertScreenEvent.Companion.fromDomainToApi
import org.junit.Test

class ApiAlertScreenEventTest {

    @Test
    fun differentProjectId_fromDomainToApi() {
        val domain = AlertScreenEvent.AlertScreenEventType.DIFFERENT_PROJECT_ID
        val api = ApiAlertScreenEvent.ApiAlertScreenEvent.DIFFERENT_PROJECT_ID

        assertThat(fromDomainToApi(domain)).isEqualTo(api)
    }

    @Test
    fun differentUserId_fromDomainToApi() {
        val domain = AlertScreenEvent.AlertScreenEventType.DIFFERENT_USER_ID
        val api = ApiAlertScreenEvent.ApiAlertScreenEvent.DIFFERENT_USER_ID

        assertThat(fromDomainToApi(domain)).isEqualTo(api)
    }

    @Test
    fun guidNotFoundOnline_fromDomainToApi() {
        val domain = AlertScreenEvent.AlertScreenEventType.GUID_NOT_FOUND_ONLINE
        val api = ApiAlertScreenEvent.ApiAlertScreenEvent.GUID_NOT_FOUND_ONLINE

        assertThat(fromDomainToApi(domain)).isEqualTo(api)
    }

    @Test
    fun guidNotFoundOffline_fromDomainToApi() {
        val domain = AlertScreenEvent.AlertScreenEventType.GUID_NOT_FOUND_OFFLINE
        val api = ApiAlertScreenEvent.ApiAlertScreenEvent.GUID_NOT_FOUND_OFFLINE

        assertThat(fromDomainToApi(domain)).isEqualTo(api)
    }

    @Test
    fun bluetoothNotSupported_fromDomainToApi() {
        val domain = AlertScreenEvent.AlertScreenEventType.BLUETOOTH_NOT_SUPPORTED
        val api = ApiAlertScreenEvent.ApiAlertScreenEvent.BLUETOOTH_NOT_SUPPORTED

        assertThat(fromDomainToApi(domain)).isEqualTo(api)
    }

    @Test
    fun lowBattery_fromDomainToApi() {
        val domain = AlertScreenEvent.AlertScreenEventType.LOW_BATTERY
        val api = ApiAlertScreenEvent.ApiAlertScreenEvent.LOW_BATTERY

        assertThat(fromDomainToApi(domain)).isEqualTo(api)
    }

    @Test
    fun unexpectedError_fromDomainToApi() {
        val domain = AlertScreenEvent.AlertScreenEventType.UNEXPECTED_ERROR
        val api = ApiAlertScreenEvent.ApiAlertScreenEvent.UNEXPECTED_ERROR

        assertThat(fromDomainToApi(domain)).isEqualTo(api)
    }

    @Test
    fun disconnected_fromDomainToApi() {
        val domain = AlertScreenEvent.AlertScreenEventType.DISCONNECTED
        val api = ApiAlertScreenEvent.ApiAlertScreenEvent.DISCONNECTED

        assertThat(fromDomainToApi(domain)).isEqualTo(api)
    }

    @Test
    fun multiplePairedScanners_fromDomainToApi() {
        val domain = AlertScreenEvent.AlertScreenEventType.MULTIPLE_PAIRED_SCANNERS
        val api = ApiAlertScreenEvent.ApiAlertScreenEvent.MULTIPLE_PAIRED_SCANNERS

        assertThat(fromDomainToApi(domain)).isEqualTo(api)
    }

    @Test
    fun notPaired_fromDomainToApi() {
        val domain = AlertScreenEvent.AlertScreenEventType.NOT_PAIRED
        val api = ApiAlertScreenEvent.ApiAlertScreenEvent.NOT_PAIRED

        assertThat(fromDomainToApi(domain)).isEqualTo(api)
    }

    @Test
    fun bluetoothNotEnabled_fromDomainToApi() {
        val domain = AlertScreenEvent.AlertScreenEventType.BLUETOOTH_NOT_ENABLED
        val api = ApiAlertScreenEvent.ApiAlertScreenEvent.BLUETOOTH_NOT_ENABLED

        assertThat(fromDomainToApi(domain)).isEqualTo(api)
    }

    @Test
    fun invalidIntentAction_fromDomainToApi() {
        val domain = AlertScreenEvent.AlertScreenEventType.INVALID_INTENT_ACTION
        val api = ApiAlertScreenEvent.ApiAlertScreenEvent.INVALID_INTENT_ACTION

        assertThat(fromDomainToApi(domain)).isEqualTo(api)
    }

    @Test
    fun invalidMetadata_fromDomainToApi() {
        val domain = AlertScreenEvent.AlertScreenEventType.INVALID_METADATA
        val api = ApiAlertScreenEvent.ApiAlertScreenEvent.INVALID_METADATA

        assertThat(fromDomainToApi(domain)).isEqualTo(api)
    }

    @Test
    fun invalidModuleId_fromDomainToApi() {
        val domain = AlertScreenEvent.AlertScreenEventType.INVALID_MODULE_ID
        val api = ApiAlertScreenEvent.ApiAlertScreenEvent.INVALID_MODULE_ID

        assertThat(fromDomainToApi(domain)).isEqualTo(api)
    }

    @Test
    fun invalidProjectId_fromDomainToApi() {
        val domain = AlertScreenEvent.AlertScreenEventType.INVALID_PROJECT_ID
        val api = ApiAlertScreenEvent.ApiAlertScreenEvent.INVALID_PROJECT_ID

        assertThat(fromDomainToApi(domain)).isEqualTo(api)
    }

    @Test
    fun invalidSelectedId_fromDomainToApi() {
        val domain = AlertScreenEvent.AlertScreenEventType.INVALID_SELECTED_ID
        val api = ApiAlertScreenEvent.ApiAlertScreenEvent.INVALID_SELECTED_ID

        assertThat(fromDomainToApi(domain)).isEqualTo(api)
    }

    @Test
    fun invalidSessionId_fromDomainToApi() {
        val domain = AlertScreenEvent.AlertScreenEventType.INVALID_SESSION_ID
        val api = ApiAlertScreenEvent.ApiAlertScreenEvent.INVALID_SESSION_ID

        assertThat(fromDomainToApi(domain)).isEqualTo(api)
    }

    @Test
    fun invalidUserId_fromDomainToApi() {
        val domain = AlertScreenEvent.AlertScreenEventType.INVALID_USER_ID
        val api = ApiAlertScreenEvent.ApiAlertScreenEvent.INVALID_USER_ID

        assertThat(fromDomainToApi(domain)).isEqualTo(api)
    }

    @Test
    fun invalidVerifyId_fromDomainToApi() {
        val domain = AlertScreenEvent.AlertScreenEventType.INVALID_VERIFY_ID
        val api = ApiAlertScreenEvent.ApiAlertScreenEvent.INVALID_VERIFY_ID

        assertThat(fromDomainToApi(domain)).isEqualTo(api)
    }

    @Test
    fun safetynetError_fromDomainToApi() {
        val domain = AlertScreenEvent.AlertScreenEventType.SAFETYNET_ERROR
        val api = ApiAlertScreenEvent.ApiAlertScreenEvent.SAFETYNET_ERROR

        assertThat(fromDomainToApi(domain)).isEqualTo(api)
    }

}
