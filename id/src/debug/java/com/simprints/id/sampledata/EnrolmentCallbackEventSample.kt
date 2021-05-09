package com.simprints.id.sampledata

import com.simprints.id.data.db.event.domain.models.EventLabels
import com.simprints.id.data.db.event.domain.models.callback.EnrolmentCallbackEvent
import com.simprints.id.sampledata.SampleDefaults.CREATED_AT
import com.simprints.id.sampledata.SampleDefaults.GUID1

object EnrolmentCallbackEventSample : SampleEvent() {
    override fun getEvent(
        sessionId: String,
        subjectId: String,
        isClosed: Boolean
    ): EnrolmentCallbackEvent {
        val labels = EventLabels(sessionId = sessionId)
        return EnrolmentCallbackEvent(CREATED_AT, GUID1, labels)
    }
}
