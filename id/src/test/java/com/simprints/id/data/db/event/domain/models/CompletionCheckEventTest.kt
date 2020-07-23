package com.simprints.id.data.db.event.domain.models

import com.google.common.truth.Truth
import com.simprints.id.data.db.event.domain.models.CompletionCheckEvent.Companion.EVENT_VERSION
import com.simprints.id.data.db.event.domain.models.CompletionCheckEvent.CompletionCheckPayload

import com.simprints.id.data.db.event.domain.models.EventType.COMPLETION_CHECK
import com.simprints.id.orchestrator.SOME_GUID1
import org.junit.Test

class CompletionCheckEventTest {

    @Test
    fun create_CompletionCheckEvent() {
        val labels = EventLabels(sessionId = SOME_GUID1)
        val event = CompletionCheckEvent(CREATED_AT, true, labels)
        Truth.assertThat(event.id).isNotNull()
        Truth.assertThat(event.labels).isEqualTo(labels)
        Truth.assertThat(event.type).isEqualTo(COMPLETION_CHECK)
        with(event.payload as CompletionCheckPayload) {
            Truth.assertThat(createdAt).isEqualTo(CREATED_AT)
            Truth.assertThat(endedAt).isEqualTo(DEFAULT_ENDED_AT)
            Truth.assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            Truth.assertThat(type).isEqualTo(COMPLETION_CHECK)
            Truth.assertThat(completed).isTrue()
        }
    }
}
