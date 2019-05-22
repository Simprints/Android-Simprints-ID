package com.simprints.id.data.analytics.eventdata.models.domain.events

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
        UNKNOWN_BLUETOOTH_ISSUE,
        UNEXPECTED_ERROR,
        DISCONNECTED,
        MULTIPLE_PAIRED_SCANNERS,
        NOT_PAIRED,
        BLUETOOTH_NOT_ENABLED,
        INVALID_INTENT_ACTION,
        INVALID_METADATA,
        INVALID_MODULE_ID,
        INVALID_PROJECT_ID,
        INVALID_SELECTED_ID,
        INVALID_SESSION_ID,
        INVALID_USER_ID,
        INVALID_VERIFIY_ID
    }
}
