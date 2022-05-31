package com.simprints.fingerprint.controllers.core.eventData.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import com.simprints.eventsystem.event.domain.models.fingerprint.FingerprintCaptureBiometricsEvent as FingerprintCaptureBiometricsEventCore

class FingerprintCaptureBiometricsEventTest {

    @Test
    fun `from domain to core maps correctly`() {
        val domainEvent = FingerprintCaptureBiometricsEvent(
            createdAt = 0,
            endedAt = 0,
            result = FingerprintCaptureBiometricsEvent.Result.GOOD_SCAN,
            fingerprint = null,
            payloadId = "someId",
            qualityThreshold = 1
            )

        val coreEvent = domainEvent.fromDomainToCore()

        assertThat(domainEvent.fingerprint).isEqualTo(coreEvent.payload.fingerprint)
        assertThat(domainEvent.startTime).isEqualTo(coreEvent.payload.createdAt)
        assertThat(domainEvent.endTime).isEqualTo(coreEvent.payload.endedAt)
        assertThat(coreEvent.payload.result).isEqualTo(FingerprintCaptureBiometricsEventCore.FingerprintCaptureBiometricsPayload.Result.GOOD_SCAN)
    }

    @Test
    fun `domain result maps to core properly`() {
        // This test counts on the fact that the enum values have been arranged the same so keep this in mind if ever edited
        FingerprintCaptureBiometricsEvent.Result.values().zip(
            FingerprintCaptureBiometricsEventCore.FingerprintCaptureBiometricsPayload.Result.values()
        ).forEach {
            assertThat(it.first.fromDomainToCore()).isEqualTo(it.second)
        }
    }
}
