package com.simprints.id.data.db.event.domain.models

import com.google.common.truth.Truth.assertThat
import com.simprints.id.data.db.event.domain.models.AlertScreenEvent.AlertScreenPayload
import com.simprints.id.data.db.event.domain.models.AlertScreenEvent.AlertScreenPayload.AlertScreenEventType.BLUETOOTH_NOT_ENABLED
import com.simprints.id.data.db.event.domain.models.EventLabel.SessionIdLabel
import com.simprints.id.data.db.event.domain.models.EventType.ENROLMENT_RECORD_MOVE
import com.simprints.id.data.db.event.domain.models.subject.EnrolmentRecordMoveEvent
import com.simprints.id.orchestrator.SOME_GUID1
import org.junit.Test

class AlertScreenEventTest {

    @Test
    fun create_AlertScreenEvent() {

        val event = AlertScreenEvent(1, BLUETOOTH_NOT_ENABLED, SOME_GUID1)
        assertThat(event.id).isNotNull()
        assertThat(event.labels).containsExactly(
            SessionIdLabel(SOME_GUID1)
        )
        assertThat(event.type).isEqualTo(ENROLMENT_RECORD_MOVE)
        with(event.payload as AlertScreenPayload) {
            assertThat(createdAt).isEqualTo(1)
            assertThat(endedAt).isEqualTo(0)
            assertThat(eventVersion).isEqualTo(EnrolmentRecordMoveEvent.EVENT_VERSION)
            assertThat(type).isEqualTo(ENROLMENT_RECORD_MOVE)
            assertThat(alertType).isEqualTo(BLUETOOTH_NOT_ENABLED)
        }
    }
}
