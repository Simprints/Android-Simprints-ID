package com.simprints.id.data.db.session.domain.models.events.callback

import androidx.annotation.Keep
import com.simprints.id.data.db.session.domain.models.events.Event
import com.simprints.id.data.db.session.domain.models.events.EventType

@Keep
class IdentificationCallbackEvent(starTime: Long,
                                  val sessionId: String,
                                  val scores: List<CallbackComparisonScore>) : Event(EventType.CALLBACK_IDENTIFICATION, starTime)
