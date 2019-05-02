package com.simprints.id.data.analytics.eventdata.models.remote.events

import androidx.annotation.Keep

@Keep
enum class ApiEventType {
    CALLOUT,
    CALLBACK,
    ARTIFICIAL_TERMINATION,
    AUTHENTICATION,
    CONSENT,
    ENROLLMENT,
    AUTHORIZATION,
    FINGERPRINT_CAPTURE,
    ONE_TO_ONE_MATCH,
    ONE_TO_MANY_MATCH,
    PERSON_CREATION,
    ALERT_SCREEN,
    GUID_SELECTION,
    CONNECTIVITY_SNAPSHOT,
    REFUSAL,
    CANDIDATE_READ,
    SCANNER_CONNECTION,
    INVALID_INTENT
}
