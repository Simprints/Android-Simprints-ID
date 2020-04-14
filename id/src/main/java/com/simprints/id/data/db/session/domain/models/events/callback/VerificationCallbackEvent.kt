package com.simprints.id.data.db.session.domain.models.events.callback

import androidx.annotation.Keep
import com.simprints.id.data.db.session.domain.models.events.Event
import com.simprints.id.data.db.session.domain.models.events.EventType

@Keep
class VerificationCallbackEvent(starTime: Long,
                                val score: CallbackComparisonScore) : Event(EventType.CALLBACK_VERIFICATION, starTime)
