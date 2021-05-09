package com.simprints.id.data.db.event.domain.models

import com.google.common.truth.Truth
import com.simprints.id.commontesttools.DefaultTestConstants.GUID1
import com.simprints.id.commontesttools.events.CREATED_AT
import com.simprints.id.commontesttools.events.DEFAULT_ENDED_AT
import com.simprints.id.data.db.event.domain.models.CompletionCheckEvent.Companion.EVENT_VERSION
import com.simprints.id.data.db.event.domain.models.EventType.COMPLETION_CHECK
import com.simprints.id.sampledata.CompletionCheckEventSample
import org.junit.Test

class CompletionCheckEventTest {

    @Test
    fun create_CompletionCheckEvent() {
        val labels = EventLabels(sessionId = GUID1)
        val event = CompletionCheckEventSample.getEvent()
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
