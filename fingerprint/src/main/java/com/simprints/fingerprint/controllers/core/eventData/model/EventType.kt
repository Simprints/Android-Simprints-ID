package com.simprints.fingerprint.controllers.core.eventData.model

import androidx.annotation.Keep

@Keep
enum class EventType {
    REFUSAL_RESPONSE,
    FINGERPRINT_CAPTURE,
    ONE_TO_ONE_MATCH,
    ONE_TO_MANY_MATCH,
    REFUSAL,
    SCANNER_CONNECTION,
    ALERT_SCREEN,
    ALERT_SCREEN_WITH_SCANNER_ISSUE,
    VERO_2_INFO_SNAPSHOT,
    SCANNER_FIRMWARE_UPDATE
}
