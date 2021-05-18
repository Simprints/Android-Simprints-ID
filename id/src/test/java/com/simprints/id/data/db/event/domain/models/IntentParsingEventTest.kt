package com.simprints.eventsystem.event.domain.models

import com.google.common.truth.Truth.assertThat
import com.simprints.eventsystem.event.domain.models.EventType.INTENT_PARSING
import com.simprints.eventsystem.event.domain.models.IntentParsingEvent.Companion.EVENT_VERSION
import com.simprints.eventsystem.event.domain.models.IntentParsingEvent.IntentParsingPayload
import com.simprints.eventsystem.event.domain.models.IntentParsingEvent.IntentParsingPayload.IntegrationInfo.COMMCARE
import com.simprints.id.sampledata.IntentParsingEventSample
import com.simprints.id.sampledata.SampleDefaults.CREATED_AT
import com.simprints.id.sampledata.SampleDefaults.DEFAULT_ENDED_AT
import com.simprints.id.sampledata.SampleDefaults.GUID1
import org.junit.Test

class IntentParsingEventTest {

    @Test
    fun create_IntentParsingEvent() {
        val labels = EventLabels(sessionId = GUID1)
        val event = IntentParsingEventSample.getEvent(labels)

        assertThat(event.id).isNotNull()
        assertThat(event.labels).isEqualTo(labels)
        assertThat(event.type).isEqualTo(INTENT_PARSING)
        with(event.payload as IntentParsingPayload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(endedAt).isEqualTo(DEFAULT_ENDED_AT)
            assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            assertThat(type).isEqualTo(INTENT_PARSING)
            assertThat(integration).isEqualTo(COMMCARE)
        }
    }
}
