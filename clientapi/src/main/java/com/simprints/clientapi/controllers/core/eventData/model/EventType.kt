package com.simprints.clientapi.controllers.core.eventData.model

import androidx.annotation.Keep

@Keep
enum class EventType {
    SUSPICIOUS_INTENT,
    INVALID_INTENT,
    ALERT_SCREEN
}
