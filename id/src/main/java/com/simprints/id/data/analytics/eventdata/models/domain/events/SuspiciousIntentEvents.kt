package com.simprints.id.data.analytics.eventdata.models.domain.events

class SuspiciousIntentEvent(val unexpectedExtras: String) : Event(EventType.SUSPICIOUS_INTENT)
