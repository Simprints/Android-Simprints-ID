package com.simprints.id.data.db.event.domain.models

import com.google.common.truth.Truth.assertThat

import com.simprints.id.data.db.event.domain.models.EventType.INTENT_PARSING
import com.simprints.id.data.db.event.domain.models.IntentParsingEvent.Companion.EVENT_VERSION
import com.simprints.id.data.db.event.domain.models.IntentParsingEvent.IntentParsingPayload
import com.simprints.id.data.db.event.domain.models.IntentParsingEvent.IntentParsingPayload.IntegrationInfo.COMMCARE
import com.simprints.id.commontesttools.DefaultTestConstants.GUID1
import com.simprints.id.commontesttools.events.CREATED_AT
import com.simprints.id.commontesttools.events.DEFAULT_ENDED_AT
import org.junit.Test

class IntentParsingEventTest {

    @Test
    fun create_IntentParsingEvent() {
        val labels = EventLabels(sessionId = GUID1)
        val event = IntentParsingEvent(CREATED_AT, COMMCARE, labels)

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
