package com.simprints.id.data.db.session.domain.models.events

import androidx.annotation.Keep

@Keep
class AlertScreenEvent(starTime: Long,
                       val alertType: AlertScreenEventType) : Event(EventType.ALERT_SCREEN, starTime) {

    enum class AlertScreenEventType {
        DIFFERENT_PROJECT_ID,
        DIFFERENT_USER_ID,
        GUID_NOT_FOUND_ONLINE,
        GUID_NOT_FOUND_OFFLINE,
        BLUETOOTH_NOT_SUPPORTED,
        LOW_BATTERY,
        UNEXPECTED_ERROR,
        DISCONNECTED,
        MULTIPLE_PAIRED_SCANNERS,
        NOT_PAIRED,
        BLUETOOTH_NOT_ENABLED,
        NFC_NOT_ENABLED,
        NFC_PAIR,
        SERIAL_ENTRY_PAIR,
        OTA,
        OTA_RECOVERY,
        OTA_FAILED,
        INVALID_INTENT_ACTION,
        INVALID_METADATA,
        INVALID_MODULE_ID,
        INVALID_PROJECT_ID,
        INVALID_SELECTED_ID,
        INVALID_SESSION_ID,
        INVALID_USER_ID,
        INVALID_VERIFY_ID,
        SAFETYNET_ERROR,
        ENROLMENT_LAST_BIOMETRICS_FAILED,
        INVALID_STATE_FOR_INTENT_ACTION
    }
}
