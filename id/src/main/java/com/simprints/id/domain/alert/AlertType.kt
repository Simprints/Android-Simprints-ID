package com.simprints.id.domain.alert

import com.simprints.id.data.db.session.domain.models.events.AlertScreenEvent.*

enum class AlertType {
    GUID_NOT_FOUND_ONLINE,
    GUID_NOT_FOUND_OFFLINE,
    INVALID_ACTION_INTENT,
    DIFFERENT_PROJECT_ID_SIGNED_IN,
    DIFFERENT_USER_ID_SIGNED_IN,
    SAFETYNET_ERROR,
    UNEXPECTED_ERROR
}

fun AlertType.fromAlertToAlertTypeEvent() = when (this) {
    AlertType.DIFFERENT_PROJECT_ID_SIGNED_IN -> AlertScreenEventType.DIFFERENT_PROJECT_ID
    AlertType.DIFFERENT_USER_ID_SIGNED_IN -> AlertScreenEventType.DIFFERENT_USER_ID
    AlertType.UNEXPECTED_ERROR -> AlertScreenEventType.UNEXPECTED_ERROR
    AlertType.SAFETYNET_ERROR -> AlertScreenEventType.SAFETYNET_ERROR
    AlertType.GUID_NOT_FOUND_ONLINE -> AlertScreenEventType.GUID_NOT_FOUND_ONLINE
    AlertType.GUID_NOT_FOUND_OFFLINE -> AlertScreenEventType.GUID_NOT_FOUND_OFFLINE
    AlertType.INVALID_ACTION_INTENT -> AlertScreenEventType.INVALID_ACTION_INTENT
}
