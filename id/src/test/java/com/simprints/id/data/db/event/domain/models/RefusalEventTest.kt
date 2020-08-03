package com.simprints.id.data.db.event.domain.models

import com.google.common.truth.Truth.assertThat
import com.simprints.id.commontesttools.DefaultTestConstants.GUID1
import com.simprints.id.commontesttools.events.CREATED_AT
import com.simprints.id.commontesttools.events.ENDED_AT
import com.simprints.id.data.db.event.domain.models.EventType.REFUSAL
import com.simprints.id.data.db.event.domain.models.RefusalEvent.Companion.EVENT_VERSION
import com.simprints.id.data.db.event.domain.models.RefusalEvent.RefusalPayload.Answer.OTHER
import org.junit.Test

class RefusalEventTest {

    @Test
    fun create_RefusalEvent() {
        val labels = EventLabels(sessionId = GUID1)
        val otherTextArg = "other_text"
        val event = RefusalEvent(CREATED_AT, ENDED_AT, OTHER, otherTextArg, labels)

        assertThat(event.id).isNotNull()
        assertThat(event.labels).isEqualTo(labels)
        assertThat(event.type).isEqualTo(REFUSAL)
        with(event.payload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(endedAt).isEqualTo(ENDED_AT)
            assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            assertThat(type).isEqualTo(REFUSAL)
            assertThat(reason).isEqualTo(OTHER)
            assertThat(otherText).isEqualTo(otherText)
        }
    }
}
