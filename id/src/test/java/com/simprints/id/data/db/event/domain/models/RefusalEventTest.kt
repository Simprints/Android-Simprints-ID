package com.simprints.id.data.db.event.domain.models

import com.google.common.truth.Truth.assertThat
import com.simprints.id.data.db.event.domain.models.EventLabel.SessionIdLabel
import com.simprints.id.data.db.event.domain.models.EventType.REFUSAL
import com.simprints.id.data.db.event.domain.models.RefusalEvent.Companion.EVENT_VERSION
import com.simprints.id.data.db.event.domain.models.RefusalEvent.RefusalPayload
import com.simprints.id.data.db.event.domain.models.RefusalEvent.RefusalPayload.Answer.OTHER
import com.simprints.id.orchestrator.SOME_GUID1
import org.junit.Test

class RefusalEventTest {

    @Test
    fun create_RefusalEvent() {
        val otherTextArg = "other_text"
        val event = RefusalEvent(CREATED_AT, ENDED_AT, OTHER, otherTextArg, SOME_GUID1)

        assertThat(event.id).isNotNull()
        assertThat(event.labels).containsExactly(
            SessionIdLabel(SOME_GUID1)
        )
        assertThat(event.type).isEqualTo(REFUSAL)
        with(event.payload as RefusalPayload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(endedAt).isEqualTo(ENDED_AT)
            assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            assertThat(type).isEqualTo(REFUSAL)
            assertThat(reason).isEqualTo(OTHER)
            assertThat(otherText).isEqualTo(otherText)
        }
    }
}
