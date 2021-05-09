package com.simprints.id.sampledata

import com.simprints.id.data.db.event.domain.models.CompletionCheckEvent
import com.simprints.id.data.db.event.domain.models.EventLabels
import com.simprints.id.sampledata.DefaultTestConstants.CREATED_AT

object CompletionCheckEventSample : SampleEvent() {
    override fun getEvent(
        sessionId: String,
        subjectId: String,
        isClosed: Boolean
    ): CompletionCheckEvent {
        val labels = EventLabels(sessionId = sessionId)
        return CompletionCheckEvent(CREATED_AT, true, labels)
    }
}
