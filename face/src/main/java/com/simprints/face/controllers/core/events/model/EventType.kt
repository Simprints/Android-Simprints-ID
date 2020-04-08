package com.simprints.face.controllers.core.events.model

import androidx.annotation.Keep

@Keep
enum class EventType {
    REFUSAL_RESPONSE,
    FACE_CAPTURE,
    ONE_TO_ONE_MATCH,
    ONE_TO_MANY_MATCH,
    REFUSAL,
    PERSON_CREATION,
}
