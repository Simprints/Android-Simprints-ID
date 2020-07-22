package com.simprints.id.data.db.event.domain.models

import com.google.common.truth.Truth.assertThat
import com.simprints.id.data.db.event.domain.models.EventLabel.SessionIdLabel
import com.simprints.id.data.db.event.domain.models.EventType.PERSON_CREATION
import com.simprints.id.data.db.event.domain.models.PersonCreationEvent.Companion.EVENT_VERSION
import com.simprints.id.data.db.event.domain.models.PersonCreationEvent.PersonCreationPayload
import com.simprints.id.orchestrator.SOME_GUID1
import org.junit.Test

class PersonCreationEventTest {

    @Test
    fun create_PersonCreationEvent() {
        val fingerprintCaptureEventIds = listOf(SOME_GUID1)
        val event = PersonCreationEvent(CREATED_AT, fingerprintCaptureEventIds, SOME_GUID1)

        assertThat(event.id).isNotNull()
        assertThat(event.labels).containsExactly(
            SessionIdLabel(SOME_GUID1)
        )
        assertThat(event.type).isEqualTo(PERSON_CREATION)
        with(event.payload as PersonCreationPayload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(endedAt).isEqualTo(DEFAULT_ENDED_AT)
            assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            assertThat(type).isEqualTo(PERSON_CREATION)
            assertThat(fingerprintCaptureIds).isEqualTo(fingerprintCaptureEventIds)
        }
    }
}
