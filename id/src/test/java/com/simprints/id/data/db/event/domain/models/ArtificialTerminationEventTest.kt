package com.simprints.id.data.db.event.domain.models

import com.google.common.truth.Truth
import com.simprints.id.data.db.event.domain.models.ArtificialTerminationEvent.ArtificialTerminationPayload
import com.simprints.id.data.db.event.domain.models.ArtificialTerminationEvent.ArtificialTerminationPayload.Reason.NEW_SESSION
import com.simprints.id.data.db.event.domain.models.EventLabel.SessionIdLabel
import com.simprints.id.data.db.event.domain.models.EventType.ARTIFICIAL_TERMINATION
import com.simprints.id.data.db.event.domain.models.subject.EnrolmentRecordMoveEvent
import com.simprints.id.orchestrator.SOME_GUID1
import org.junit.Test

class ArtificialTerminationEventTest {

    @Test
    fun create_ArtificialTerminationEvent() {

        val event = ArtificialTerminationEvent(CREATED_AT, NEW_SESSION, SOME_GUID1)
        Truth.assertThat(event.id).isNotNull()
        Truth.assertThat(event.labels).containsExactly(
            SessionIdLabel(SOME_GUID1)
        )
        Truth.assertThat(event.type).isEqualTo(ARTIFICIAL_TERMINATION)
        with(event.payload as ArtificialTerminationPayload) {
            Truth.assertThat(createdAt).isEqualTo(CREATED_AT)
            Truth.assertThat(endedAt).isEqualTo(DEFAULT_ENDED_AT)
            Truth.assertThat(eventVersion).isEqualTo(EnrolmentRecordMoveEvent.EVENT_VERSION)
            Truth.assertThat(type).isEqualTo(ARTIFICIAL_TERMINATION)
            Truth.assertThat(reason).isEqualTo(NEW_SESSION)
        }
    }
}
