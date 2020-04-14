package com.simprints.id.data.db.session.domain.models.events.callback

import androidx.annotation.Keep
import com.simprints.id.data.db.session.domain.models.events.Event
import com.simprints.id.data.db.session.domain.models.events.EventType

@Keep
class ConfirmationCallbackEvent(
    startTime: Long,
    val identificationOutcome: Boolean
) : Event(EventType.CALLBACK_CONFIRMATION, startTime)
