package com.simprints.eventsystem.event.domain.models

import com.google.common.truth.Truth.assertThat
import com.simprints.eventsystem.event.domain.models.AlertScreenEvent
import com.simprints.eventsystem.event.domain.models.AlertScreenEvent.AlertScreenPayload.AlertScreenEventType.BLUETOOTH_NOT_ENABLED
import com.simprints.eventsystem.event.domain.models.AlertScreenEvent.Companion.EVENT_VERSION
import com.simprints.eventsystem.event.domain.models.EventLabels
import com.simprints.eventsystem.event.domain.models.EventType.ALERT_SCREEN
import com.simprints.eventsystem.sampledata.SampleDefaults.CREATED_AT
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_ENDED_AT
import com.simprints.eventsystem.sampledata.SampleDefaults.GUID1
import org.junit.Test

class AlertScreenEventTest {

    @Test
    fun create_AlertScreenEvent() {
        val labels = EventLabels(sessionId = GUID1)
        val event = AlertScreenEvent(CREATED_AT, BLUETOOTH_NOT_ENABLED, labels)
        assertThat(event.id).isNotNull()
        assertThat(event.labels).isEqualTo(labels)
        assertThat(event.type).isEqualTo(ALERT_SCREEN)
        with(event.payload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(endedAt).isEqualTo(DEFAULT_ENDED_AT)
            assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            assertThat(type).isEqualTo(ALERT_SCREEN)
            assertThat(alertType).isEqualTo(BLUETOOTH_NOT_ENABLED)
        }
    }
}
