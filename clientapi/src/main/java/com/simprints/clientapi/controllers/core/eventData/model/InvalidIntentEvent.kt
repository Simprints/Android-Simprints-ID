package com.simprints.clientapi.controllers.core.eventData.model

import androidx.annotation.Keep
import com.simprints.id.data.analytics.eventdata.models.domain.events.InvalidIntentEvent as CoreInvalidIntentEvent

@Keep
class InvalidIntentEvent(val action: String, val extras: Map<String, Any>?) : Event(EventType.INVALID_INTENT)

fun InvalidIntentEvent.fromDomainToCore() = CoreInvalidIntentEvent(action, hashMapOf("extras" to extras))
