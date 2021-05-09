package com.simprints.id.data.db.event.domain.models

import com.google.common.truth.Truth.assertThat
import com.simprints.id.sampledata.DefaultTestConstants.GUID1
import com.simprints.id.sampledata.DefaultTestConstants.GUID2
import com.simprints.id.sampledata.DefaultTestConstants.CREATED_AT
import com.simprints.id.sampledata.DefaultTestConstants.DEFAULT_ENDED_AT
import com.simprints.id.data.db.event.domain.models.EventType.PERSON_CREATION
import com.simprints.id.data.db.event.domain.models.PersonCreationEvent.Companion.EVENT_VERSION
import com.simprints.id.sampledata.PersonCreationEventSample
import org.junit.Test

class PersonCreationEventTest {

    @Test
    fun create_PersonCreationEvent() {
        val labels = EventLabels(sessionId = GUID1)
        val fingerprintCaptureEventIds = listOf(GUID1)
        val faceCaptureEventIds = listOf(GUID2)
        val event = PersonCreationEventSample.getEvent()

        assertThat(event.id).isNotNull()
        assertThat(event.labels).isEqualTo(labels)
        assertThat(event.type).isEqualTo(PERSON_CREATION)
        with(event.payload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(endedAt).isEqualTo(DEFAULT_ENDED_AT)
            assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            assertThat(type).isEqualTo(PERSON_CREATION)
            assertThat(fingerprintCaptureIds).isEqualTo(fingerprintCaptureEventIds)
            assertThat(fingerprintReferenceId).isEqualTo(GUID1)
            assertThat(faceCaptureEventIds).isEqualTo(faceCaptureEventIds)
            assertThat(faceReferenceId).isEqualTo(GUID2)
        }
    }
}
