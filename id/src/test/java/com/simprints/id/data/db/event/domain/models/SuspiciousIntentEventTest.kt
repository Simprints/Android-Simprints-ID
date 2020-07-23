package com.simprints.id.data.db.event.domain.models

import com.google.common.truth.Truth.assertThat

import com.simprints.id.data.db.event.domain.models.EventType.SUSPICIOUS_INTENT
import com.simprints.id.data.db.event.domain.models.SuspiciousIntentEvent.Companion.EVENT_VERSION
import com.simprints.id.data.db.event.domain.models.SuspiciousIntentEvent.SuspiciousIntentPayload
import com.simprints.id.orchestrator.SOME_GUID1
import org.junit.Test

class SuspiciousIntentEventTest {

    @Test
    fun create_SuspiciousIntentEvent() {
        val labels = EventLabels(sessionId = SOME_GUID1)
        val extrasArg = mapOf("extra_key" to "value")
        val event = SuspiciousIntentEvent(CREATED_AT, extrasArg, labels)

        assertThat(event.id).isNotNull()
        assertThat(event.labels).isEqualTo(labels)
        assertThat(event.type).isEqualTo(SUSPICIOUS_INTENT)
        with(event.payload as SuspiciousIntentPayload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(endedAt).isEqualTo(DEFAULT_ENDED_AT)
            assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            assertThat(type).isEqualTo(SUSPICIOUS_INTENT)
            assertThat(unexpectedExtras).isEqualTo(extrasArg)
        }
    }
}

