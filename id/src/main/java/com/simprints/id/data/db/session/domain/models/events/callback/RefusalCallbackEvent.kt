package com.simprints.id.data.db.session.domain.models.events.callback

import androidx.annotation.Keep
import com.simprints.id.data.db.session.domain.models.events.Event
import com.simprints.id.data.db.session.domain.models.events.EventType

@Keep
class RefusalCallbackEvent(starTime: Long,
                           val reason: String,
                           val extra: String): Event(EventType.CALLBACK_REFUSAL, starTime)
