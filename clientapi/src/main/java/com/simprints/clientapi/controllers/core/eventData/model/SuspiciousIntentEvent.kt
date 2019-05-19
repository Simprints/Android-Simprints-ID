package com.simprints.clientapi.controllers.core.eventData.model

import androidx.annotation.Keep
import com.simprints.id.data.analytics.eventdata.models.domain.events.SuspiciousIntentEvent as CoreSuspiciousIntentEvent

@Keep
class SuspiciousIntentEvent(starTime: Long,
                            val unexpectedExtras: Map<String, Any?>) : Event(EventType.SUSPICIOUS_INTENT, starTime)

fun SuspiciousIntentEvent.fromDomainToCore() = CoreSuspiciousIntentEvent(starTime, unexpectedExtras)
