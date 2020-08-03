package com.simprints.id.data.db.event.domain.models

import com.google.common.truth.Truth.assertThat
import com.simprints.id.commontesttools.DefaultTestConstants.GUID1
import com.simprints.id.commontesttools.events.CREATED_AT
import com.simprints.id.commontesttools.events.DEFAULT_ENDED_AT
import com.simprints.id.data.db.event.domain.models.AlertScreenEvent.AlertScreenPayload
import com.simprints.id.data.db.event.domain.models.AlertScreenEvent.AlertScreenPayload.AlertScreenEventType.BLUETOOTH_NOT_ENABLED
import com.simprints.id.data.db.event.domain.models.EventType.ALERT_SCREEN
import com.simprints.id.data.db.event.domain.models.subject.EnrolmentRecordMoveEvent
import org.junit.Test

class AlertScreenEventTest {

    @Test
    fun create_AlertScreenEvent() {
        val labels = EventLabels(sessionId = GUID1)
        val event = AlertScreenEvent(CREATED_AT, BLUETOOTH_NOT_ENABLED, labels)
        assertThat(event.id).isNotNull()
        assertThat(event.labels).isEqualTo(labels)
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
