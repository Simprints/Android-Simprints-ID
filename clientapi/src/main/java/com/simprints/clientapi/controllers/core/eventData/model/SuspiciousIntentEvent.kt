package com.simprints.clientapi.controllers.core.eventData.model

import androidx.annotation.Keep
import com.simprints.id.data.analytics.eventdata.models.domain.events.SuspiciousIntentEvent as CoreSuspiciousIntentEvent

@Keep
class SuspiciousIntentEvent(val unexpectedExtras: String) : Event(EventType.SUSPICIOUS_INTENT)

fun SuspiciousIntentEvent.fromDomainToCore() = CoreSuspiciousIntentEvent(unexpectedExtras)
