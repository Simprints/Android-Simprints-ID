package com.simprints.clientapi.controllers.core.eventData.model

import androidx.annotation.Keep

@Keep
class SuspiciousIntentEvent(val unexpectedExtras: String) : Event(EventType.SUSPICIOUS_INTENT)
