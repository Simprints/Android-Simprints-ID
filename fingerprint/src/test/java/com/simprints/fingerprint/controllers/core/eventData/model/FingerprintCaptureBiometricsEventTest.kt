package com.simprints.fingerprint.controllers.core.eventData.model

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.data.domain.fingerprint.FingerIdentifier
import org.junit.Test

class FingerprintCaptureBiometricsEventTest {

    @Test
    fun `from domain to core maps correctly`() {
        val domainEvent = FingerprintCaptureBiometricsEvent(
            createdAt = 0,
            endedAt = 0,
            fingerprint = FingerprintCaptureBiometricsEvent.Fingerprint(
                finger = FingerIdentifier.LEFT_3RD_FINGER,
                quality = 0,
                template = "sometwm"
            ),
            payloadId = "someId"
        )

        val coreEvent = domainEvent.fromDomainToCore()

        assertThat(coreEvent.payload.fingerprint.quality).isEqualTo(0)
        assertThat(coreEvent.payload.fingerprint.template).isEqualTo("sometwm")
        assertThat(domainEvent.startTime).isEqualTo(coreEvent.payload.createdAt)
        assertThat(domainEvent.endTime).isEqualTo(coreEvent.payload.endedAt)
        assertThat(coreEvent.payload.id).isEqualTo("someId")
    }

}
