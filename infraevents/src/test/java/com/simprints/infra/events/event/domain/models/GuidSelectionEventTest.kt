package com.simprints.infra.events.event.domain.models

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.events.event.domain.models.EventType.GUID_SELECTION
import com.simprints.infra.events.event.domain.models.GuidSelectionEvent.Companion.EVENT_VERSION
import com.simprints.infra.events.event.domain.models.GuidSelectionEvent.GuidSelectionPayload
import com.simprints.infra.events.sampledata.SampleDefaults.CREATED_AT
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_ENDED_AT
import com.simprints.infra.events.sampledata.SampleDefaults.GUID1
import org.junit.Test

class GuidSelectionEventTest {

    @Test
    fun create_GuidSelectionEvent() {
        val labels = EventLabels(sessionId = GUID1)
        val event = GuidSelectionEvent(CREATED_AT, GUID1, labels)

        assertThat(event.id).isNotNull()
        assertThat(event.labels).isEqualTo(labels)
        assertThat(event.type).isEqualTo(GUID_SELECTION)
        with(event.payload as GuidSelectionPayload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(endedAt).isEqualTo(DEFAULT_ENDED_AT)
            assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            assertThat(type).isEqualTo(GUID_SELECTION)
        }
    }
}
