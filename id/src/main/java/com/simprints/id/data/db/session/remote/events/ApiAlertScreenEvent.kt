package com.simprints.id.data.db.session.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.session.domain.models.events.AlertScreenEvent
import com.simprints.id.data.db.session.remote.events.ApiAlertScreenEvent.ApiAlertScreenEvent.Companion.fromDomainToApi

@Keep
class ApiAlertScreenEvent(val relativeStartTime: Long,
                          val alertType: ApiAlertScreenEvent) : ApiEvent(ApiEventType.ALERT_SCREEN) {

    constructor(alertScreenEventDomain: AlertScreenEvent) :
        this(alertScreenEventDomain.relativeStartTime ?: 0, fromDomainToApi(alertScreenEventDomain.alertType))

    @Keep
    enum class ApiAlertScreenEvent {
        DIFFERENT_PROJECT_ID,
        DIFFERENT_USER_ID,
        GUID_NOT_FOUND_ONLINE,
        GUID_NOT_FOUND_OFFLINE,
        BLUETOOTH_NOT_SUPPORTED,
        LOW_BATTERY,
        @Deprecated("Fingerprint module doesn't triggers it anymore")
        UNKNOWN_BLUETOOTH_ISSUE,
        UNEXPECTED_ERROR,
        DISCONNECTED,
        @Deprecated("Fingerprint module doesn't triggers it anymore")
        MULTIPLE_PAIRED_SCANNERS,
        @Deprecated("Fingerprint module doesn't triggers it anymore")
        NOT_PAIRED,
        BLUETOOTH_NOT_ENABLED,
        NFC_NOT_ENABLED,
        NFC_PAIR,
        SERIAL_ENTRY_PAIR,
        OTA,
        OTA_RECOVERY,
        OTA_FAILED,
        @Deprecated("That can never been triggered, so to be removed soon")
        INVALID_INTENT_ACTION,
        ENROLMENT_LAST_BIOMETRICS_FAILED,
        INVALID_STATE_FOR_INTENT_ACTION,
        INVALID_METADATA,
        INVALID_MODULE_ID,
        INVALID_PROJECT_ID,
        INVALID_SELECTED_ID,
        INVALID_SESSION_ID,
        INVALID_USER_ID,
        INVALID_VERIFY_ID,
        SAFETYNET_ERROR;

        companion object {
            fun fromDomainToApi(type: AlertScreenEvent.AlertScreenEventType): ApiAlertScreenEvent =
                when (type) {
                    AlertScreenEvent.AlertScreenEventType.DIFFERENT_PROJECT_ID -> DIFFERENT_PROJECT_ID
                    AlertScreenEvent.AlertScreenEventType.DIFFERENT_USER_ID -> DIFFERENT_USER_ID
                    AlertScreenEvent.AlertScreenEventType.GUID_NOT_FOUND_ONLINE -> GUID_NOT_FOUND_ONLINE
                    AlertScreenEvent.AlertScreenEventType.GUID_NOT_FOUND_OFFLINE -> GUID_NOT_FOUND_OFFLINE
                    AlertScreenEvent.AlertScreenEventType.BLUETOOTH_NOT_SUPPORTED -> BLUETOOTH_NOT_SUPPORTED
                    AlertScreenEvent.AlertScreenEventType.LOW_BATTERY -> LOW_BATTERY
                    AlertScreenEvent.AlertScreenEventType.UNEXPECTED_ERROR -> UNEXPECTED_ERROR
                    AlertScreenEvent.AlertScreenEventType.DISCONNECTED -> DISCONNECTED
                    AlertScreenEvent.AlertScreenEventType.MULTIPLE_PAIRED_SCANNERS -> MULTIPLE_PAIRED_SCANNERS
                    AlertScreenEvent.AlertScreenEventType.NOT_PAIRED -> NOT_PAIRED
                    AlertScreenEvent.AlertScreenEventType.BLUETOOTH_NOT_ENABLED -> BLUETOOTH_NOT_ENABLED
                    AlertScreenEvent.AlertScreenEventType.NFC_NOT_ENABLED -> NFC_NOT_ENABLED
                    AlertScreenEvent.AlertScreenEventType.NFC_PAIR -> NFC_PAIR
                    AlertScreenEvent.AlertScreenEventType.SERIAL_ENTRY_PAIR -> SERIAL_ENTRY_PAIR
                    AlertScreenEvent.AlertScreenEventType.OTA -> OTA
                    AlertScreenEvent.AlertScreenEventType.OTA_RECOVERY -> OTA_RECOVERY
                    AlertScreenEvent.AlertScreenEventType.OTA_FAILED -> OTA_FAILED
                    AlertScreenEvent.AlertScreenEventType.INVALID_INTENT_ACTION -> INVALID_INTENT_ACTION
                    AlertScreenEvent.AlertScreenEventType.INVALID_METADATA -> INVALID_METADATA
                    AlertScreenEvent.AlertScreenEventType.INVALID_MODULE_ID -> INVALID_MODULE_ID
                    AlertScreenEvent.AlertScreenEventType.INVALID_PROJECT_ID -> INVALID_PROJECT_ID
                    AlertScreenEvent.AlertScreenEventType.INVALID_SELECTED_ID -> INVALID_SELECTED_ID
                    AlertScreenEvent.AlertScreenEventType.INVALID_SESSION_ID -> INVALID_SESSION_ID
                    AlertScreenEvent.AlertScreenEventType.INVALID_USER_ID -> INVALID_USER_ID
                    AlertScreenEvent.AlertScreenEventType.INVALID_VERIFY_ID -> INVALID_VERIFY_ID
                    AlertScreenEvent.AlertScreenEventType.SAFETYNET_ERROR -> SAFETYNET_ERROR
                    AlertScreenEvent.AlertScreenEventType.ENROLMENT_LAST_BIOMETRICS_FAILED -> ENROLMENT_LAST_BIOMETRICS_FAILED
                    AlertScreenEvent.AlertScreenEventType.INVALID_STATE_FOR_INTENT_ACTION -> INVALID_STATE_FOR_INTENT_ACTION
                }
        }
    }
}
