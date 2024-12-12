package com.simprints.infra.events.event.domain.models

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.events.event.domain.models.CompletionCheckEvent.Companion.EVENT_VERSION
import com.simprints.infra.events.event.domain.models.EventType.COMPLETION_CHECK
import com.simprints.infra.events.sampledata.SampleDefaults.CREATED_AT
import org.junit.Test

class CompletionCheckEventTest {
    @Test
    fun create_CompletionCheckEvent() {
        val event = CompletionCheckEvent(CREATED_AT, true)

        assertThat(event.id).isNotNull()
        assertThat(event.type).isEqualTo(COMPLETION_CHECK)
        with(event.payload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            assertThat(type).isEqualTo(COMPLETION_CHECK)
            assertThat(completed).isTrue()
        }
    }
}
