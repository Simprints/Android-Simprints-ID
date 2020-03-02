package com.simprints.id.data.db.session.domain.models.events

import androidx.annotation.Keep

@Keep
class SuspiciousIntentEvent(starTime: Long,
                            val unexpectedExtras: Map<String, Any?>) : Event(EventType.SUSPICIOUS_INTENT, starTime)
