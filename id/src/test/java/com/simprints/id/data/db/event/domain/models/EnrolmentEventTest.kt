package com.simprints.id.data.db.event.domain.models

import com.google.common.truth.Truth.assertThat
import com.simprints.id.data.db.event.domain.models.EnrolmentEvent.Companion.EVENT_VERSION
import com.simprints.id.data.db.event.domain.models.EnrolmentEvent.EnrolmentPayload

import com.simprints.id.data.db.event.domain.models.EventType.ENROLMENT
import com.simprints.id.orchestrator.SOME_GUID1
import com.simprints.id.orchestrator.SOME_GUID2
import org.junit.Test

class EnrolmentEventTest {

    @Test
    fun create_EnrolmentEvent() {
        val labels = EventLabels(sessionId = SOME_GUID1)
        val event = EnrolmentEvent(CREATED_AT, SOME_GUID2, labels)
        assertThat(event.id).isNotNull()
        assertThat(event.labels).isEqualTo(labels)
        assertThat(event.type).isEqualTo(ENROLMENT)
        with(event.payload as EnrolmentPayload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(endedAt).isEqualTo(DEFAULT_ENDED_AT)
            assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            assertThat(type).isEqualTo(ENROLMENT)
            assertThat(personId).isEqualTo(SOME_GUID2)
        }
    }
}

