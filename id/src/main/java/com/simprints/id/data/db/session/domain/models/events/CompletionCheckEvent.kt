package com.simprints.id.data.db.session.domain.models.events

import androidx.annotation.Keep

@Keep
class CompletionCheckEvent(starTime: Long,
                           val completed: Boolean) : Event(EventType.COMPLETION_CHECK, starTime)
