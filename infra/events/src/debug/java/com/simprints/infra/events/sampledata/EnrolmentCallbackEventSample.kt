package com.simprints.infra.events.sampledata

import com.simprints.infra.events.event.domain.models.EventLabels
import com.simprints.infra.events.event.domain.models.callback.EnrolmentCallbackEvent
import com.simprints.infra.events.sampledata.SampleDefaults.CREATED_AT
import com.simprints.infra.events.sampledata.SampleDefaults.GUID1

object EnrolmentCallbackEventSample : SampleEvent() {
    override fun getEvent(
        labels: EventLabels,
        isClosed: Boolean
    ): EnrolmentCallbackEvent {
        return EnrolmentCallbackEvent(CREATED_AT, GUID1, labels)
    }
}
