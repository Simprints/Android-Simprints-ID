package com.simprints.id.sampledata

import com.simprints.id.data.db.event.domain.models.EventLabels
import com.simprints.id.data.db.event.domain.models.callout.EnrolmentCalloutEvent
import com.simprints.id.sampledata.DefaultTestConstants.CREATED_AT
import com.simprints.id.sampledata.DefaultTestConstants.DEFAULT_METADATA
import com.simprints.id.sampledata.DefaultTestConstants.DEFAULT_MODULE_ID
import com.simprints.id.sampledata.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.sampledata.DefaultTestConstants.DEFAULT_USER_ID

object EnrolmentCalloutEventSample : SampleEvent() {
    override fun getEvent(
        sessionId: String,
        subjectId: String,
        isClosed: Boolean
    ): EnrolmentCalloutEvent {
        val labels = EventLabels(sessionId = sessionId)
        return EnrolmentCalloutEvent(
            CREATED_AT,
            DEFAULT_PROJECT_ID,
            DEFAULT_USER_ID,
            DEFAULT_MODULE_ID,
            DEFAULT_METADATA,
            labels
        )
    }
}
