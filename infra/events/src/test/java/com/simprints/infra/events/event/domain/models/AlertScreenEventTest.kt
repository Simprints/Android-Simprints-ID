package com.simprints.infra.events.event.domain.models

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.events.event.domain.models.AlertScreenEvent.AlertScreenPayload.AlertScreenEventType.BLUETOOTH_NOT_ENABLED
import com.simprints.infra.events.event.domain.models.AlertScreenEvent.Companion.EVENT_VERSION
import com.simprints.infra.events.event.domain.models.EventType.ALERT_SCREEN
import com.simprints.infra.events.sampledata.SampleDefaults.CREATED_AT
import org.junit.Test

class AlertScreenEventTest {
    @Test
    fun create_AlertScreenEvent() {
        val event = AlertScreenEvent(CREATED_AT, BLUETOOTH_NOT_ENABLED)

        assertThat(event.id).isNotNull()
        assertThat(event.type).isEqualTo(ALERT_SCREEN)
        with(event.payload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(endedAt).isNull()
            assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            assertThat(type).isEqualTo(ALERT_SCREEN)
            assertThat(alertType).isEqualTo(BLUETOOTH_NOT_ENABLED)
        }
    }
}
