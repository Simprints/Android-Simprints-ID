package com.simprints.id.data.db.event.domain.models

import com.google.common.truth.Truth.assertThat
import com.simprints.id.commontesttools.DefaultTestConstants.GUID1
import com.simprints.id.commontesttools.DefaultTestConstants.GUID2
import com.simprints.id.commontesttools.events.CREATED_AT
import com.simprints.id.commontesttools.events.DEFAULT_ENDED_AT
import com.simprints.id.data.db.event.domain.models.EventType.Companion.ENROLMENT_V1_KEY
import org.junit.Test
import com.simprints.id.data.db.event.domain.models.EventType.ENROLMENT_V1

class EnrolmentEventV1Test {

    @Test
    fun create_EnrolmentEvent() {
        val labels = EventLabels(sessionId = GUID1)
        val event = EnrolmentEventV1(CREATED_AT, GUID2, labels)
        assertThat(event.id).isNotNull()
        assertThat(event.labels).isEqualTo(labels)
        assertThat(event.type).isEqualTo(ENROLMENT_V1)
        with(event.payload as EnrolmentEventV1.EnrolmentPayload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(endedAt).isEqualTo(DEFAULT_ENDED_AT)
            assertThat(eventVersion).isEqualTo(EnrolmentEventV1.EVENT_VERSION)
            assertThat(type).isEqualTo(ENROLMENT_V1)
            assertThat(personId).isEqualTo(GUID2)
        }
    }
}

