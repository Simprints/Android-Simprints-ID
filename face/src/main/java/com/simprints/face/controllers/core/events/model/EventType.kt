package com.simprints.face.controllers.core.events.model

import androidx.annotation.Keep

@Keep
enum class EventType {
    FACE_ONBOARDING_COMPLETE,
    FACE_FALLBACK_CAPTURE,
    FACE_CAPTURE,
    FACE_CAPTURE_CONFIRMATION,
    FACE_CAPTURE_RETRY,
    ALERT_SCREEN,
    REFUSAL_RESPONSE,
    ONE_TO_ONE_MATCH,
    ONE_TO_MANY_MATCH,
    REFUSAL,
}
