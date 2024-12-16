package com.simprints.infra.events.event.domain.models

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.events.event.domain.models.EventType.REFUSAL
import com.simprints.infra.events.event.domain.models.RefusalEvent.Companion.EVENT_VERSION
import com.simprints.infra.events.event.domain.models.RefusalEvent.RefusalPayload.Answer.OTHER
import com.simprints.infra.events.sampledata.SampleDefaults.CREATED_AT
import com.simprints.infra.events.sampledata.SampleDefaults.ENDED_AT
import org.junit.Test

class RefusalEventTest {
    @Test
    fun create_RefusalEvent() {
        val otherTextArg = "other_text"
        val event = RefusalEvent(CREATED_AT, ENDED_AT, OTHER, otherTextArg)

        assertThat(event.id).isNotNull()
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
