package com.simprints.id.data.analytics.eventdata.models.domain.events.callback

import androidx.annotation.Keep
import com.simprints.id.data.analytics.eventdata.models.domain.events.Event
import com.simprints.id.data.analytics.eventdata.models.domain.events.EventType

@Keep
class VerificationCallbackEvent(val relativeStartTime: Long,
                                val score: CallbackComparisonScore) : Event(EventType.CALLBACK_VERIFICATION)
