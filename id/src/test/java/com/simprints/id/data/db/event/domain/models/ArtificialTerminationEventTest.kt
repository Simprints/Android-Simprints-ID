package com.simprints.id.data.db.event.domain.models

import com.google.common.truth.Truth
import com.simprints.id.data.db.event.domain.models.ArtificialTerminationEvent.ArtificialTerminationPayload
import com.simprints.id.data.db.event.domain.models.ArtificialTerminationEvent.ArtificialTerminationPayload.Reason.NEW_SESSION
import com.simprints.id.data.db.event.domain.models.EventLabel.SessionIdLabel
import com.simprints.id.data.db.event.domain.models.EventType.ENROLMENT_RECORD_MOVE
import com.simprints.id.data.db.event.domain.models.subject.EnrolmentRecordMoveEvent
import com.simprints.id.orchestrator.SOME_GUID1
import org.junit.Test

class ArtificialTerminationEventTest {

    @Test
    fun create_ArtificialTerminationEvent() {

        val event = ArtificialTerminationEvent(1, NEW_SESSION, SOME_GUID1)
        Truth.assertThat(event.id).isNotNull()
        Truth.assertThat(event.labels).containsExactly(
            SessionIdLabel(SOME_GUID1)
        )
        Truth.assertThat(event.type).isEqualTo(ENROLMENT_RECORD_MOVE)
        with(event.payload as ArtificialTerminationPayload) {
            Truth.assertThat(createdAt).isEqualTo(1)
            Truth.assertThat(endedAt).isEqualTo(0)
            Truth.assertThat(eventVersion).isEqualTo(EnrolmentRecordMoveEvent.EVENT_VERSION)
            Truth.assertThat(type).isEqualTo(ENROLMENT_RECORD_MOVE)
            Truth.assertThat(reason).isEqualTo(NEW_SESSION)
        }
    }
}
