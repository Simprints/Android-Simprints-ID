package com.simprints.id.data.db.event.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.CompletionCheckEvent
import com.simprints.id.data.db.event.domain.events.CompletionCheckEvent.CompletionCheckPayload

@Keep
class ApiCompletionCheckEvent(val relativeStartTime: Long,
                              val completed: Boolean) : ApiEvent(ApiEventType.COMPLETION_CHECK) {

    constructor(completionCheckEvent: CompletionCheckEvent) :
        this((completionCheckEvent.payload as CompletionCheckPayload).creationTime, completionCheckEvent.payload.completed)
}
