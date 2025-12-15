package com.simprints.infra.events.event.domain.models

import com.google.common.truth.Truth.*
import com.simprints.core.domain.sample.SampleIdentifier
import com.simprints.infra.events.sampledata.SampleDefaults
import org.junit.Test

class FingerprintCaptureBiometricsEventTest {
    @Test
    fun create_FingerprintCaptureBiometricsEvent() {
        val fingerArg =
            FingerprintCaptureBiometricsEvent.FingerprintCaptureBiometricsPayload.Fingerprint(
                SampleIdentifier.LEFT_3RD_FINGER,
                "template",
                1,
                "ISO_19794_2",
            )
        val event = FingerprintCaptureBiometricsEvent(
            createdAt = SampleDefaults.CREATED_AT,
            fingerprint = fingerArg,
            id = "someId",
        )

        assertThat(event.id).isEqualTo("someId")
        assertThat(event.type).isEqualTo(EventType.FINGERPRINT_CAPTURE_BIOMETRICS)
        with(event.payload) {
            assertThat(id).isNotNull()
            assertThat(type).isEqualTo(EventType.FINGERPRINT_CAPTURE_BIOMETRICS)
            assertThat(fingerprint).isEqualTo(fingerArg)
            assertThat(eventVersion).isEqualTo(FingerprintCaptureBiometricsEvent.EVENT_VERSION)
        }
    }
}
