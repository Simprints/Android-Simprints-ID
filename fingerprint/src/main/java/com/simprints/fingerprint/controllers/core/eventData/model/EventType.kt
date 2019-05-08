package com.simprints.fingerprint.controllers.core.eventData.model

import androidx.annotation.Keep

@Keep
enum class EventType {
    REFUSAL_RESPONSE,
    CANDIDATE_READ,
    CONSENT,
    FINGERPRINT_CAPTURE,
    ONE_TO_ONE_MATCH,
    ONE_TO_MANY_MATCH,
    CONNECTIVITY_SNAPSHOT,
    REFUSAL,
    SCANNER_CONNECTION
}
