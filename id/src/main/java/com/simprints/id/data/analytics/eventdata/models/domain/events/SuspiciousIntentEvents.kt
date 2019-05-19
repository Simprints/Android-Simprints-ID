package com.simprints.id.data.analytics.eventdata.models.domain.events

class SuspiciousIntentEvent(starTime: Long,
                            val unexpectedExtras: Map<String, Any?>) : Event(EventType.SUSPICIOUS_INTENT, starTime)
