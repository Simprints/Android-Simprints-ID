package com.simprints.infra.events.event.domain.models

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.events.event.domain.models.EventType.INTENT_PARSING
import com.simprints.infra.events.event.domain.models.IntentParsingEvent.Companion.EVENT_VERSION
import com.simprints.infra.events.event.domain.models.IntentParsingEvent.IntentParsingPayload.IntegrationInfo.COMMCARE
import com.simprints.infra.events.sampledata.SampleDefaults.CREATED_AT
import org.junit.Test

class IntentParsingEventTest {
    @Test
    fun create_IntentParsingEvent() {
        val event = IntentParsingEvent(CREATED_AT, COMMCARE)

        assertThat(event.id).isNotNull()
        assertThat(event.type).isEqualTo(INTENT_PARSING)
        with(event.payload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            assertThat(type).isEqualTo(INTENT_PARSING)
            assertThat(integration).isEqualTo(COMMCARE)
        }
    }
}
