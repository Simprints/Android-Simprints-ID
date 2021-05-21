package com.simprints.eventsystem.event.domain.models

import com.google.common.truth.Truth.assertThat
import com.simprints.eventsystem.event.domain.models.EventType.SUSPICIOUS_INTENT
import com.simprints.eventsystem.event.domain.models.SuspiciousIntentEvent.Companion.EVENT_VERSION
import com.simprints.eventsystem.sampledata.SampleDefaults.GUID1
import com.simprints.eventsystem.sampledata.SampleDefaults.CREATED_AT
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_ENDED_AT
import org.junit.Test

class SuspiciousIntentEventTest {

    @Test
    fun create_SuspiciousIntentEvent() {
        val labels = EventLabels(sessionId = GUID1)
        val extrasArg = mapOf("extra_key" to "value")
        val event = SuspiciousIntentEvent(CREATED_AT, extrasArg, labels)

        assertThat(event.id).isNotNull()
        assertThat(event.labels).isEqualTo(labels)
        assertThat(event.type).isEqualTo(SUSPICIOUS_INTENT)
        with(event.payload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(endedAt).isEqualTo(DEFAULT_ENDED_AT)
            assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            assertThat(type).isEqualTo(SUSPICIOUS_INTENT)
            assertThat(unexpectedExtras).isEqualTo(extrasArg)
        }
    }
}

