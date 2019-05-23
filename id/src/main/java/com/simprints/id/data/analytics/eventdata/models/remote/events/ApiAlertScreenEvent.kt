package com.simprints.id.data.analytics.eventdata.models.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.analytics.eventdata.models.domain.events.AlertScreenEvent
import com.simprints.id.data.analytics.eventdata.models.remote.events.ApiAlertScreenEvent.ApiAlertScreenEvent.Companion.fromDomainToApi

@Keep
class ApiAlertScreenEvent(val relativeStartTime: Long,
                          val alertType: ApiAlertScreenEvent) : ApiEvent(ApiEventType.ALERT_SCREEN) {

    constructor(alertScreenEventDomain: AlertScreenEvent) :
        this(alertScreenEventDomain.relativeStartTime ?: 0, fromDomainToApi(alertScreenEventDomain.alertType))

    enum class ApiAlertScreenEvent {
        INVALID_API_KEY,
        INVALID_PROJECT_ID,
        INVALID_INTENT_ACTION,
        INVALID_METADATA,
        INVALID_USER_ID,
        INVALID_MODULE_ID,
        INVALID_UPDATE_GUID,
        INVALID_VERIFY_GUID,
        INVALID_RESULT_FORMAT,
        INVALID_CALLING_PACKAGE,
        MISSING_PROJECT_ID_OR_API_KEY,
        DIFFERENT_PROJECT_ID,
        DIFFERENT_USER_ID,
        MISSING_USER_ID,
        MISSING_MODULE_ID,
        MISSING_UPDATE_GUID,
        MISSING_VERIFY_GUID,
        UNEXPECTED_PARAMETER,
        UNVERIFIED_API_KEY,
        GUID_NOT_FOUND_ONLINE,
        GUID_NOT_FOUND_OFFLINE,
        BLUETOOTH_NOT_SUPPORTED,
        BLUETOOTH_NOT_ENABLED,
        NOT_PAIRED,
        MULTIPLE_PAIRED_SCANNERS,
        DISCONNECTED,
        LOW_BATTERY,
        INVALID_SESSION_ID, //StopShip: to add on the server
        INVALID_SELECTED_ID, //StopShip: to add on the server
        UNKNOWN_BLUETOOTH_ISSUE, //StopShip: to add on the server
        UNEXPECTED_ERROR;

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
                    AlertScreenEvent.AlertScreenEventType.INVALID_VERIFY_ID -> INVALID_VERIFY_GUID
                }
        }
    }
}
