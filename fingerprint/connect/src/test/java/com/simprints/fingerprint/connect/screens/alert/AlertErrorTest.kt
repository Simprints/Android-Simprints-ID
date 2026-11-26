package com.simprints.fingerprint.connect.screens.alert

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.events.event.domain.models.AlertScreenEvent.AlertScreenPayload.AlertScreenEventType
import org.junit.Test

class AlertErrorTest {
    @Test
    fun `bluetooth not enabled alert logs bluetooth not enabled event type`() {
        assertThat(AlertError.BLUETOOTH_NOT_ENABLED.eventType).isEqualTo(AlertScreenEventType.BLUETOOTH_NOT_ENABLED)
    }

    @Test
    fun `bluetooth not supported alert logs bluetooth not supported event type`() {
        assertThat(AlertError.BLUETOOTH_NOT_SUPPORTED.eventType).isEqualTo(AlertScreenEventType.BLUETOOTH_NOT_SUPPORTED)
    }
}
