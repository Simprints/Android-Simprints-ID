package com.simprints.infra.events.event.domain.models

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.events.event.domain.models.EventType.INVALID_INTENT
import com.simprints.infra.events.event.domain.models.InvalidIntentEvent.Companion.EVENT_VERSION
import com.simprints.infra.events.sampledata.SampleDefaults.CREATED_AT
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Test

class InvalidIntentEventTest {
    @Test
    fun create_InvalidIntentEvent() {
        val extras = mapOf("extra_key" to JsonPrimitive("value"), "extra_key2" to JsonPrimitive(123))
        val event = InvalidIntentEvent(CREATED_AT, "REGISTER", extras)

        assertThat(event.id).isNotNull()
        assertThat(event.type).isEqualTo(INVALID_INTENT)
        with(event.payload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            assertThat(type).isEqualTo(INVALID_INTENT)
            assertThat(extras).isEqualTo(extras)
        }
    }
}
