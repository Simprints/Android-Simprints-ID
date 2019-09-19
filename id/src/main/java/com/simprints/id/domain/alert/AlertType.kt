package com.simprints.id.domain.alert

import com.simprints.id.data.analytics.eventdata.models.domain.events.AlertScreenEvent

enum class AlertType {
    GUID_NOT_FOUND_ONLINE,
    GUID_NOT_FOUND_OFFLINE,
    DIFFERENT_PROJECT_ID_SIGNED_IN,
    DIFFERENT_USER_ID_SIGNED_IN,
    SAFETYNET_ERROR,
    UNEXPECTED_ERROR
}

fun AlertType.fromAlertToAlertTypeEvent() = when (this) {
    AlertType.DIFFERENT_PROJECT_ID_SIGNED_IN -> AlertScreenEvent.AlertScreenEventType.DIFFERENT_PROJECT_ID
    AlertType.DIFFERENT_USER_ID_SIGNED_IN -> AlertScreenEvent.AlertScreenEventType.DIFFERENT_USER_ID
    AlertType.UNEXPECTED_ERROR -> AlertScreenEvent.AlertScreenEventType.UNEXPECTED_ERROR
    AlertType.SAFETYNET_ERROR -> AlertScreenEvent.AlertScreenEventType.SAFETYNET_ERROR
    AlertType.GUID_NOT_FOUND_ONLINE -> AlertScreenEvent.AlertScreenEventType.GUID_NOT_FOUND_ONLINE
    AlertType.GUID_NOT_FOUND_OFFLINE -> AlertScreenEvent.AlertScreenEventType.GUID_NOT_FOUND_OFFLINE
}
