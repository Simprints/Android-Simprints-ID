package com.simprints.clientapi.controllers.core.eventData.model

import androidx.annotation.Keep

@Keep
class InvalidIntentEvent(val action: String, val extras: String) : Event(EventType.INVALID_INTENT)
