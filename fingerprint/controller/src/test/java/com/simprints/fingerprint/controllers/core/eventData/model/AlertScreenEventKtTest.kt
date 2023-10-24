package com.simprints.fingerprint.controllers.core.eventData.model

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.activities.alert.AlertError
import org.junit.Test
import com.simprints.infra.events.event.domain.models.AlertScreenEvent.AlertScreenPayload.AlertScreenEventType as CoreAlertScreenEventType

class AlertScreenEventKtTest {

    @Test
    fun fromFingerprintAlertToAlertTypeEvent() {
        assertThat(AlertError.UNEXPECTED_ERROR.fromFingerprintAlertToAlertTypeEvent())
            .isEqualTo(CoreAlertScreenEventType.UNEXPECTED_ERROR)
    }
}
