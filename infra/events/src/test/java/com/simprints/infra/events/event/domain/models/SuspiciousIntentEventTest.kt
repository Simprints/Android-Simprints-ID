package com.simprints.infra.events.event.domain.models

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.events.event.domain.models.EventType.SUSPICIOUS_INTENT
import com.simprints.infra.events.event.domain.models.SuspiciousIntentEvent.Companion.EVENT_VERSION
import com.simprints.infra.events.sampledata.SampleDefaults.CREATED_AT
import org.junit.Test

class SuspiciousIntentEventTest {
    @Test
    fun create_SuspiciousIntentEvent() {
        val extrasArg = mapOf("extra_key" to "value")
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
}
