package com.simprints.id.sampledata

import com.simprints.id.data.db.event.domain.models.EnrolmentEventV2
import com.simprints.id.data.db.event.domain.models.EventLabels
import com.simprints.id.sampledata.DefaultTestConstants.CREATED_AT
import com.simprints.id.sampledata.DefaultTestConstants.DEFAULT_MODULE_ID
import com.simprints.id.sampledata.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.sampledata.DefaultTestConstants.DEFAULT_USER_ID
import com.simprints.id.sampledata.DefaultTestConstants.GUID1
import com.simprints.id.sampledata.DefaultTestConstants.GUID2

object EnrolmentEventV2Sample : SampleEvent() {
    override fun getEvent(
        sessionId: String,
        subjectId: String,
        isClosed: Boolean
    ): EnrolmentEventV2 {
        val labels = EventLabels(sessionId = sessionId)
        return EnrolmentEventV2(
            CREATED_AT,
            GUID1,
            DEFAULT_PROJECT_ID,
            DEFAULT_MODULE_ID,
            DEFAULT_USER_ID,
            GUID2,
            labels
        )
    }
}
