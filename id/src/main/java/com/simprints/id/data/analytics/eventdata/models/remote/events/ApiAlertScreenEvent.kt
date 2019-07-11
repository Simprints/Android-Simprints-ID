package com.simprints.id.data.analytics.eventdata.models.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.analytics.eventdata.models.domain.events.AlertScreenEvent
import com.simprints.id.data.analytics.eventdata.models.remote.events.ApiAlertScreenEvent.ApiAlertScreenEvent.Companion.fromDomainToApi

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
        INVALID_VERIFY_ID,
        SAFETYNET_DOWN,
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
                    AlertScreenEvent.AlertScreenEventType.UNKNOWN_BLUETOOTH_ISSUE -> UNKNOWN_BLUETOOTH_ISSUE
                    AlertScreenEvent.AlertScreenEventType.DISCONNECTED -> DISCONNECTED
                    AlertScreenEvent.AlertScreenEventType.MULTIPLE_PAIRED_SCANNERS -> MULTIPLE_PAIRED_SCANNERS
                    AlertScreenEvent.AlertScreenEventType.NOT_PAIRED -> NOT_PAIRED
                    AlertScreenEvent.AlertScreenEventType.BLUETOOTH_NOT_ENABLED -> BLUETOOTH_NOT_ENABLED
                    AlertScreenEvent.AlertScreenEventType.INVALID_INTENT_ACTION -> INVALID_INTENT_ACTION
                    AlertScreenEvent.AlertScreenEventType.INVALID_METADATA -> INVALID_METADATA
                    AlertScreenEvent.AlertScreenEventType.INVALID_MODULE_ID -> INVALID_MODULE_ID
                    AlertScreenEvent.AlertScreenEventType.INVALID_PROJECT_ID -> INVALID_PROJECT_ID
                    AlertScreenEvent.AlertScreenEventType.INVALID_SELECTED_ID -> INVALID_SELECTED_ID
                    AlertScreenEvent.AlertScreenEventType.INVALID_SESSION_ID -> INVALID_SESSION_ID
                    AlertScreenEvent.AlertScreenEventType.INVALID_USER_ID -> INVALID_USER_ID
                    AlertScreenEvent.AlertScreenEventType.INVALID_VERIFY_ID -> INVALID_VERIFY_ID
                    AlertScreenEvent.AlertScreenEventType.SAFETYNET_DOWN -> SAFETYNET_DOWN
                    AlertScreenEvent.AlertScreenEventType.SAFETYNET_ERROR -> SAFETYNET_ERROR
                }
        }
    }
}
