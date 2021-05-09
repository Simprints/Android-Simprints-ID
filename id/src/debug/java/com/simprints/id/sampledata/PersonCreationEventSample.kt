package com.simprints.id.sampledata

import com.simprints.id.data.db.event.domain.models.EventLabels
import com.simprints.id.data.db.event.domain.models.PersonCreationEvent
import com.simprints.id.sampledata.DefaultTestConstants.CREATED_AT
import com.simprints.id.sampledata.DefaultTestConstants.GUID1
import com.simprints.id.sampledata.DefaultTestConstants.GUID2

object PersonCreationEventSample : SampleEvent() {
    override fun getEvent(
        sessionId: String,
        subjectId: String,
        isClosed: Boolean
    ): PersonCreationEvent {
        val labels = EventLabels(sessionId = GUID1)
        val fingerprintCaptureEventIds = listOf(GUID1)
        val faceCaptureEventIds = listOf(GUID2)
        return PersonCreationEvent(
            CREATED_AT,
            fingerprintCaptureEventIds,
            GUID1,
            faceCaptureEventIds,
            GUID2,
            labels
        )
    }
}
