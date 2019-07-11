package com.simprints.id.domain.alert

import com.simprints.id.data.analytics.eventdata.models.domain.events.AlertScreenEvent

enum class AlertType {
    DIFFERENT_PROJECT_ID_SIGNED_IN,
    DIFFERENT_USER_ID_SIGNED_IN,
    SAFETYNET_DOWN,
    SAFETYNET_ERROR,
    UNEXPECTED_ERROR
}

fun AlertType.fromAlertToAlertTypeEvent() = when (this) {
    AlertType.DIFFERENT_PROJECT_ID_SIGNED_IN -> AlertScreenEvent.AlertScreenEventType.DIFFERENT_PROJECT_ID
    AlertType.DIFFERENT_USER_ID_SIGNED_IN -> AlertScreenEvent.AlertScreenEventType.DIFFERENT_USER_ID
    AlertType.UNEXPECTED_ERROR -> AlertScreenEvent.AlertScreenEventType.UNEXPECTED_ERROR
    AlertType.SAFETYNET_DOWN -> AlertScreenEvent.AlertScreenEventType.SAFETYNET_DOWN
    AlertType.SAFETYNET_ERROR -> AlertScreenEvent.AlertScreenEventType.SAFETYNET_ERROR
}
