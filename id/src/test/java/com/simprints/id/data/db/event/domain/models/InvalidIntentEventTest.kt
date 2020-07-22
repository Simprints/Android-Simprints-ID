package com.simprints.id.data.db.event.domain.models

import com.google.common.truth.Truth.assertThat
import com.simprints.id.data.db.event.domain.models.EventLabel.SessionIdLabel
import com.simprints.id.data.db.event.domain.models.EventType.INVALID_INTENT
import com.simprints.id.data.db.event.domain.models.InvalidIntentEvent.Companion.EVENT_VERSION
import com.simprints.id.data.db.event.domain.models.InvalidIntentEvent.InvalidIntentPayload
import com.simprints.id.orchestrator.SOME_GUID1
import org.junit.Test

class InvalidIntentEventTest {

    @Test
    fun create_InvalidIntentEvent() {
        val extras = mapOf("extra_key" to "value")
        val event = InvalidIntentEvent(CREATED_AT, "REGISTER", extras, SOME_GUID1)

        assertThat(event.id).isNotNull()
        assertThat(event.labels).containsExactly(
            SessionIdLabel(SOME_GUID1)
        )
        assertThat(event.type).isEqualTo(INVALID_INTENT)
        with(event.payload as InvalidIntentPayload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(endedAt).isEqualTo(DEFAULT_ENDED_AT)
            assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            assertThat(type).isEqualTo(INVALID_INTENT)
            assertThat(extras).isEqualTo(extras)
        }
    }
}
