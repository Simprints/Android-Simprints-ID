package com.simprints.id.data.db.session.domain.events.callback

import androidx.annotation.Keep
import com.simprints.id.data.db.session.domain.events.Event
import com.simprints.id.data.db.session.domain.events.EventType

@Keep
class IdentificationCallbackEvent(starTime: Long,
                                  val sessionId: String,
                                  val scores: List<CallbackComparisonScore>) : Event(EventType.CALLBACK_IDENTIFICATION, starTime)
