package com.simprints.infra.events.event.domain.models

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.events.event.domain.models.EventType.PERSON_CREATION
import com.simprints.infra.events.event.domain.models.PersonCreationEvent.Companion.EVENT_VERSION
import com.simprints.infra.events.sampledata.SampleDefaults.CREATED_AT
import com.simprints.infra.events.sampledata.SampleDefaults.GUID1
import com.simprints.infra.events.sampledata.SampleDefaults.GUID2
import org.junit.Test

class PersonCreationEventTest {

    @Test
    fun create_PersonCreationEvent() {
        val fingerprintCaptureEventIds = listOf(GUID1)
        val faceCaptureEventIds = listOf(GUID2)
        val event = PersonCreationEvent(
            CREATED_AT,
            fingerprintCaptureEventIds,
            GUID1,
            faceCaptureEventIds,
            GUID2,
        )

        assertThat(event.id).isNotNull()
        assertThat(event.type).isEqualTo(PERSON_CREATION)
        with(event.payload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            assertThat(type).isEqualTo(PERSON_CREATION)
            assertThat(fingerprintCaptureIds).isEqualTo(fingerprintCaptureEventIds)
            assertThat(fingerprintReferenceId).isEqualTo(GUID1)
            assertThat(faceCaptureEventIds).isEqualTo(faceCaptureEventIds)
            assertThat(faceReferenceId).isEqualTo(GUID2)
        }
    }
}
