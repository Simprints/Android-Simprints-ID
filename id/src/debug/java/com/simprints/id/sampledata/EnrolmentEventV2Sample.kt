package com.simprints.id.sampledata

import com.simprints.id.data.db.event.domain.models.EnrolmentEventV2
import com.simprints.id.data.db.event.domain.models.EventLabels
import com.simprints.id.sampledata.SampleDefaults.CREATED_AT
import com.simprints.id.sampledata.SampleDefaults.DEFAULT_MODULE_ID
import com.simprints.id.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.id.sampledata.SampleDefaults.DEFAULT_USER_ID
import com.simprints.id.sampledata.SampleDefaults.GUID1
import com.simprints.id.sampledata.SampleDefaults.GUID2

object EnrolmentEventV2Sample : SampleEvent() {
    override fun getEvent(
        labels: EventLabels,
        isClosed: Boolean
    ): EnrolmentEventV2 {
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
