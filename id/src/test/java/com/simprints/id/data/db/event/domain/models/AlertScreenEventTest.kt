package com.simprints.id.data.db.event.domain.models

import com.google.common.truth.Truth.assertThat
import com.simprints.id.data.db.event.domain.models.AlertScreenEvent.AlertScreenPayload
import com.simprints.id.data.db.event.domain.models.AlertScreenEvent.AlertScreenPayload.AlertScreenEventType.BLUETOOTH_NOT_ENABLED
import com.simprints.id.data.db.event.domain.models.EventLabel.SessionIdLabel
import com.simprints.id.data.db.event.domain.models.EventType.ALERT_SCREEN
import com.simprints.id.data.db.event.domain.models.subject.EnrolmentRecordMoveEvent
import com.simprints.id.orchestrator.SOME_GUID1
import org.junit.Test

class AlertScreenEventTest {

    @Test
    fun create_AlertScreenEvent() {

        val event = AlertScreenEvent(CREATED_AT, BLUETOOTH_NOT_ENABLED, SOME_GUID1)
        assertThat(event.id).isNotNull()
        assertThat(event.labels).containsExactly(
            SessionIdLabel(SOME_GUID1)
        )
        assertThat(event.type).isEqualTo(ALERT_SCREEN)
        with(event.payload as AlertScreenPayload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(endedAt).isEqualTo(DEFAULT_ENDED_AT)
            assertThat(eventVersion).isEqualTo(EnrolmentRecordMoveEvent.EVENT_VERSION)
            assertThat(type).isEqualTo(ALERT_SCREEN)
            assertThat(alertType).isEqualTo(BLUETOOTH_NOT_ENABLED)
        }
    }
}
