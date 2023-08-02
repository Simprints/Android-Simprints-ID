package com.simprints.infra.eventsync.event.remote.events

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.events.event.domain.models.AlertScreenEvent.AlertScreenPayload.AlertScreenEventType
import com.simprints.infra.eventsync.event.remote.models.ApiAlertScreenPayload.ApiAlertScreenEventType
import com.simprints.infra.eventsync.event.remote.models.fromDomainToApi
import com.simprints.infra.eventsync.event.remote.models.ApiAlertScreenPayload.ApiAlertScreenEventType.*
import org.junit.Test

class ApiAlertScreenEventTest {


    @Test
    fun `should map domain alert screen event to api correctly`() {
        mapOf(
            AlertScreenEventType.DIFFERENT_PROJECT_ID to DIFFERENT_PROJECT_ID,
            AlertScreenEventType.DIFFERENT_USER_ID to DIFFERENT_USER_ID,
            AlertScreenEventType.GUID_NOT_FOUND_ONLINE to GUID_NOT_FOUND_ONLINE,
            AlertScreenEventType.GUID_NOT_FOUND_OFFLINE to GUID_NOT_FOUND_OFFLINE,
            AlertScreenEventType.BLUETOOTH_NOT_SUPPORTED to BLUETOOTH_NOT_SUPPORTED,
            AlertScreenEventType.LOW_BATTERY to LOW_BATTERY,
            AlertScreenEventType.UNEXPECTED_ERROR to UNEXPECTED_ERROR,
            AlertScreenEventType.DISCONNECTED to DISCONNECTED,
            AlertScreenEventType.MULTIPLE_PAIRED_SCANNERS to MULTIPLE_PAIRED_SCANNERS,
            AlertScreenEventType.NOT_PAIRED to NOT_PAIRED,
            AlertScreenEventType.BLUETOOTH_NOT_ENABLED to BLUETOOTH_NOT_ENABLED,
            AlertScreenEventType.NFC_NOT_ENABLED to NFC_NOT_ENABLED,
            AlertScreenEventType.NFC_PAIR to NFC_PAIR,
            AlertScreenEventType.SERIAL_ENTRY_PAIR to SERIAL_ENTRY_PAIR,
            AlertScreenEventType.OTA to OTA,
            AlertScreenEventType.OTA_RECOVERY to OTA_RECOVERY,
            AlertScreenEventType.OTA_FAILED to OTA_FAILED,
            AlertScreenEventType.INVALID_INTENT_ACTION to INVALID_INTENT_ACTION,
            AlertScreenEventType.INVALID_METADATA to INVALID_METADATA,
            AlertScreenEventType.INVALID_MODULE_ID to INVALID_MODULE_ID,
            AlertScreenEventType.INVALID_PROJECT_ID to INVALID_PROJECT_ID,
            AlertScreenEventType.INVALID_SELECTED_ID to INVALID_SELECTED_ID,
            AlertScreenEventType.INVALID_SESSION_ID to INVALID_SESSION_ID,
            AlertScreenEventType.INVALID_USER_ID to INVALID_USER_ID,
            AlertScreenEventType.INVALID_VERIFY_ID to INVALID_VERIFY_ID,
            AlertScreenEventType.INTEGRITY_SERVICE_ERROR to INTEGRITY_SERVICE_ERROR,
            AlertScreenEventType.ENROLMENT_LAST_BIOMETRICS_FAILED to ENROLMENT_LAST_BIOMETRICS_FAILED,
            AlertScreenEventType.INVALID_STATE_FOR_INTENT_ACTION to INVALID_STATE_FOR_INTENT_ACTION,
            AlertScreenEventType.FACE_LICENSE_INVALID to FACE_LICENSE_INVALID,
            AlertScreenEventType.FACE_LICENSE_MISSING to FACE_LICENSE_MISSING,
            AlertScreenEventType.BACKEND_MAINTENANCE_ERROR to BACKEND_MAINTENANCE_ERROR,
            AlertScreenEventType.GOOGLE_PLAY_SERVICES_OUTDATED to GOOGLE_PLAY_SERVICES_OUTDATED,
            AlertScreenEventType.MISSING_GOOGLE_PLAY_SERVICES to MISSING_GOOGLE_PLAY_SERVICES,
            AlertScreenEventType.MISSING_OR_OUTDATED_GOOGLE_PLAY_STORE_APP to MISSING_OR_OUTDATED_GOOGLE_PLAY_STORE_APP,
            AlertScreenEventType.PROJECT_PAUSED to PROJECT_PAUSED,
            AlertScreenEventType.PROJECT_ENDING to PROJECT_ENDING,
            AlertScreenEventType.BLUETOOTH_NO_PERMISSION to BLUETOOTH_NO_PERMISSION
        ).forEach {
            assertThat(it.key.fromDomainToApi()).isEqualTo(it.value)
        }
    }
}
