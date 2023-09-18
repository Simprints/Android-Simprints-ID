package com.simprints.face.controllers.core.events.model

import androidx.annotation.Keep

@Keep
enum class EventType {
    FACE_ONBOARDING_COMPLETE,
    FACE_FALLBACK_CAPTURE,
    FACE_CAPTURE,
    FACE_CAPTURE_BIOMETRICS,
    FACE_CAPTURE_CONFIRMATION,
}
