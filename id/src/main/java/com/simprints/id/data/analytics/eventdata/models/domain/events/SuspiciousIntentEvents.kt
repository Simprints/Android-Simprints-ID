package com.simprints.id.data.analytics.eventdata.models.domain.events

class SuspiciousIntentEvent(override val starTime: Long,
                            val unexpectedExtras: Map<String, Any?>) : Event(EventType.SUSPICIOUS_INTENT)
