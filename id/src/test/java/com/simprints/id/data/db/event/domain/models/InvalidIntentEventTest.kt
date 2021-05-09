package com.simprints.id.data.db.event.domain.models

import com.google.common.truth.Truth.assertThat

import com.simprints.id.data.db.event.domain.models.EventType.INVALID_INTENT
import com.simprints.id.data.db.event.domain.models.InvalidIntentEvent.Companion.EVENT_VERSION
import com.simprints.id.data.db.event.domain.models.InvalidIntentEvent.InvalidIntentPayload
import com.simprints.id.sampledata.DefaultTestConstants.GUID1
import com.simprints.id.sampledata.DefaultTestConstants.CREATED_AT
import com.simprints.id.sampledata.DefaultTestConstants.DEFAULT_ENDED_AT
import org.junit.Test

class InvalidIntentEventTest {

    @Test
    fun create_InvalidIntentEvent() {
        val labels = EventLabels(sessionId = GUID1)
        val extras = mapOf("extra_key" to "value")
        val event = InvalidIntentEvent(CREATED_AT, "REGISTER", extras, labels)

        assertThat(event.id).isNotNull()
        assertThat(event.labels).isEqualTo(labels)
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
