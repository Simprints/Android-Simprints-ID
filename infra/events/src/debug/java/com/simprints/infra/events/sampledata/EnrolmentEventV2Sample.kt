package com.simprints.infra.events.sampledata

import com.simprints.core.domain.tokenization.asTokenizedRaw
import com.simprints.infra.events.event.domain.models.EnrolmentEventV2
import com.simprints.infra.events.event.domain.models.EventLabels
import com.simprints.infra.events.sampledata.SampleDefaults.CREATED_AT
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_MODULE_ID
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_USER_ID
import com.simprints.infra.events.sampledata.SampleDefaults.GUID1
import com.simprints.infra.events.sampledata.SampleDefaults.GUID2

object EnrolmentEventV2Sample : SampleEvent() {
    override fun getEvent(
        labels: EventLabels,
        isClosed: Boolean
    ): EnrolmentEventV2 {
        return EnrolmentEventV2(
            CREATED_AT,
            GUID1,
            DEFAULT_PROJECT_ID,
            DEFAULT_MODULE_ID.asTokenizedRaw(),
            DEFAULT_USER_ID.asTokenizedRaw(),
            GUID2,
            labels
        )
    }
}
