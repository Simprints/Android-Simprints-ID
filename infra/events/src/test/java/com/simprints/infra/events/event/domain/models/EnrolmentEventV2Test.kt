package com.simprints.infra.events.event.domain.models

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.events.event.domain.models.EventType.ENROLMENT_V2
import com.simprints.infra.events.sampledata.SampleDefaults.CREATED_AT
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_MODULE_ID
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_USER_ID
import com.simprints.infra.events.sampledata.SampleDefaults.GUID1
import com.simprints.infra.events.sampledata.SampleDefaults.GUID2
import org.junit.Test

class EnrolmentEventV2Test {
    @Test
    fun create_EnrolmentEvent() {
        val event = EnrolmentEventV2(
            CREATED_AT,
            GUID1,
            DEFAULT_PROJECT_ID,
            DEFAULT_MODULE_ID,
            DEFAULT_USER_ID,
            GUID2,
        )

        assertThat(event.id).isNotNull()
        assertThat(event.type).isEqualTo(ENROLMENT_V2)
        with(event.payload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(eventVersion).isEqualTo(EnrolmentEventV2.EVENT_VERSION)
            assertThat(type).isEqualTo(ENROLMENT_V2)
            assertThat(projectId).isEqualTo(DEFAULT_PROJECT_ID)
            assertThat(moduleId).isEqualTo(DEFAULT_MODULE_ID)
            assertThat(attendantId).isEqualTo(DEFAULT_USER_ID)
            assertThat(personCreationEventId).isEqualTo(GUID2)
        }
    }
}
