package com.simprints.id.data.analytics.eventdata.models.domain.events

import androidx.annotation.Keep

@Keep
class CompletionCheckEvent(starTime: Long,
                           val completed: Boolean) : Event(EventType.COMPLETION_CHECK, starTime)
