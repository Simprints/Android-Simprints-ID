package com.simprints.infra.events.event.domain.models

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.events.event.domain.models.EventType.ENROLMENT_V1
import com.simprints.infra.events.sampledata.SampleDefaults.CREATED_AT
import com.simprints.infra.events.sampledata.SampleDefaults.GUID2
import org.junit.Test

class EnrolmentEventV1Test {
    @Test
    fun create_EnrolmentEvent() {
        val event = EnrolmentEventV1(CREATED_AT, GUID2)

        assertThat(event.id).isNotNull()
        assertThat(event.type).isEqualTo(ENROLMENT_V1)
        with(event.payload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(eventVersion).isEqualTo(EnrolmentEventV1.EVENT_VERSION)
            assertThat(type).isEqualTo(ENROLMENT_V1)
            assertThat(personId).isEqualTo(GUID2)
        }
    }
}
