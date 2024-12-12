package com.simprints.infra.events.event.domain.models.fingerprint

import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.fingerprint.IFingerIdentifier
import com.simprints.infra.events.event.domain.models.EventType
import com.simprints.infra.events.sampledata.SampleDefaults
import org.junit.Test

class FingerprintCaptureBiometricsEventTest {
    @Test
    fun create_FingerprintCaptureBiometricsEvent() {
        val fingerArg =
            FingerprintCaptureBiometricsEvent.FingerprintCaptureBiometricsPayload.Fingerprint(
                IFingerIdentifier.LEFT_3RD_FINGER,
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
