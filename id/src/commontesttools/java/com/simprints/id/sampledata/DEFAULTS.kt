package com.simprints.id.sampledata

import com.simprints.eventsystem.sampledata.SampleDefaults
import com.simprints.id.data.db.subject.domain.Subject

object DEFAULTS {

    val defaultSubject = Subject(
        SampleDefaults.GUID1,
        SampleDefaults.DEFAULT_PROJECT_ID,
        SampleDefaults.GUID2,
        SampleDefaults.DEFAULT_MODULE_ID
    )

}
