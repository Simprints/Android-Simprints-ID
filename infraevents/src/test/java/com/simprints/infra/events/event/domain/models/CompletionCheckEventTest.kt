package com.simprints.infra.events.event.domain.models

import com.google.common.truth.Truth
import com.simprints.infra.events.event.domain.models.CompletionCheckEvent.Companion.EVENT_VERSION
import com.simprints.infra.events.event.domain.models.EventType.COMPLETION_CHECK
import com.simprints.infra.events.sampledata.CompletionCheckEventSample
import com.simprints.infra.events.sampledata.SampleDefaults.CREATED_AT
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_ENDED_AT
import com.simprints.infra.events.sampledata.SampleDefaults.GUID1
import org.junit.Test

class CompletionCheckEventTest {

    @Test
    fun create_CompletionCheckEvent() {
        val labels = EventLabels(sessionId = GUID1)
        val event = CompletionCheckEventSample.getEvent(labels)
        Truth.assertThat(event.id).isNotNull()
        Truth.assertThat(event.labels).isEqualTo(labels)
        Truth.assertThat(event.type).isEqualTo(COMPLETION_CHECK)
        with(event.payload) {
            Truth.assertThat(createdAt).isEqualTo(CREATED_AT)
            Truth.assertThat(endedAt).isEqualTo(DEFAULT_ENDED_AT)
            Truth.assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            Truth.assertThat(type).isEqualTo(COMPLETION_CHECK)
            Truth.assertThat(completed).isTrue()
        }
    }
}
