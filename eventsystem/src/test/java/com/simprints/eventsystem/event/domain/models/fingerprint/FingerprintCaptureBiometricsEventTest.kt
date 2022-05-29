package com.simprints.eventsystem.event.domain.models.fingerprint

import com.google.common.truth.Truth.assertThat
import com.simprints.eventsystem.event.domain.models.EventLabels
import com.simprints.eventsystem.event.domain.models.EventType
import com.simprints.eventsystem.sampledata.SampleDefaults
import com.simprints.moduleapi.fingerprint.IFingerIdentifier
import org.junit.Test

class FingerprintCaptureBiometricsEventTest {

    @Test
    fun create_FaceCaptureBiometricsEvent() {
        val labels = EventLabels(sessionId = SampleDefaults.GUID1)
        val fingerArg =
            FingerprintCaptureBiometricsEvent.FingerprintCaptureBiometricsPayload.Fingerprint(IFingerIdentifier.LEFT_3RD_FINGER, "template")
        val event = FingerprintCaptureBiometricsEvent(
            createdAt = SampleDefaults.CREATED_AT,
            result = FingerprintCaptureBiometricsEvent.FingerprintCaptureBiometricsPayload.Result.GOOD_SCAN,
            fingerprint = fingerArg,
            id = "someId",
            labels = labels
        )

        assertThat(event.id).isEqualTo("someId")
        assertThat(event.labels).isEqualTo(labels)
        assertThat(event.type).isEqualTo(EventType.FINGERPRINT_CAPTURE_BIOMETRICS)

        with(event.payload) {
            assertThat(id).isNotNull()
            assertThat(type).isEqualTo(EventType.FINGERPRINT_CAPTURE_BIOMETRICS)
            assertThat(result).isEqualTo(FingerprintCaptureBiometricsEvent.FingerprintCaptureBiometricsPayload.Result.GOOD_SCAN)
            assertThat(fingerprint).isEqualTo(fingerArg)
            assertThat(eventVersion).isEqualTo(0)
        }
    }
}
