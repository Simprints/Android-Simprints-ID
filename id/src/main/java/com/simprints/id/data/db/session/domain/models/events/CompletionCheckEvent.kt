package com.simprints.id.data.db.session.domain.models.events

import androidx.annotation.Keep

@Keep
class CompletionCheckEvent(startTime: Long,
                           val completed: Boolean) : Event(EventType.COMPLETION_CHECK, startTime)
