package com.simprints.infra.events.sampledata

import com.simprints.infra.events.event.domain.models.EventLabels
import com.simprints.infra.events.event.domain.models.PersonCreationEvent
import com.simprints.infra.events.sampledata.SampleDefaults.CREATED_AT
import com.simprints.infra.events.sampledata.SampleDefaults.GUID1
import com.simprints.infra.events.sampledata.SampleDefaults.GUID2

object PersonCreationEventSample : SampleEvent() {
    override fun getEvent(
        labels: EventLabels,
        isClosed: Boolean
    ): PersonCreationEvent {
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
