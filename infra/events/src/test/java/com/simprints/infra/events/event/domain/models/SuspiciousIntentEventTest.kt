package com.simprints.infra.events.event.domain.models

import com.google.common.truth.Truth.*
import com.simprints.core.tools.json.JsonHelper
import com.simprints.infra.events.event.domain.models.EventType.SUSPICIOUS_INTENT
import com.simprints.infra.events.event.domain.models.SuspiciousIntentEvent.Companion.EVENT_VERSION
import com.simprints.infra.events.sampledata.SampleDefaults.CREATED_AT
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Test

class SuspiciousIntentEventTest {
    @Test
    fun create_SuspiciousIntentEvent() {
        val extrasArg = mapOf("extra_key" to JsonPrimitive("value"), "extra_key2" to JsonPrimitive(123))
        val event = SuspiciousIntentEvent(CREATED_AT, extrasArg)

        assertThat(event.id).isNotNull()
        assertThat(event.type).isEqualTo(SUSPICIOUS_INTENT)
        with(event.payload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            assertThat(type).isEqualTo(SUSPICIOUS_INTENT)
            assertThat(unexpectedExtras).isEqualTo(extrasArg)
        }
    }

    @Test
    fun `parse json with permissive extras`() {
        val json = """
            {
                "id": "123",
                "type": "SUSPICIOUS_INTENT",
                "payload": {
                    "createdAt": {"ms": 1234},
                    "eventVersion": 2,
                    "unexpectedExtras": {
                        "extra_key": "value",
                        "extra_key2": 123,
                        "extra_key3": true                    
                    }
              }
            }
            """
        val event = JsonHelper.json.decodeFromString<SuspiciousIntentEvent>(json)
        assertThat(event.payload.unexpectedExtras).isEqualTo(
            mapOf(
                "extra_key" to JsonPrimitive("value"),
                "extra_key2" to JsonPrimitive(123),
                "extra_key3" to JsonPrimitive(true),
            ),
        )
    }
}
