package com.simprints.infra.events.event.domain.models.fingerprint

import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.fingerprint.IFingerIdentifier
import com.simprints.infra.events.event.domain.models.EventType
import com.simprints.infra.events.sampledata.SampleDefaults
import org.junit.Test

class FingerprintCaptureEventTest {
    @Test
    fun create_FingerprintCaptureEvent() {
        val fingerprint = FingerprintCaptureEvent.FingerprintCapturePayload.Fingerprint(
            IFingerIdentifier.LEFT_THUMB,
            8,
            "ISO_19794_2",
        )
        val event = FingerprintCaptureEvent(
            SampleDefaults.CREATED_AT,
            SampleDefaults.ENDED_AT,
            IFingerIdentifier.LEFT_THUMB,
            10,
            FingerprintCaptureEvent.FingerprintCapturePayload.Result.BAD_QUALITY,
            fingerprint,
        )

        assertThat(event.id).isNotNull()
        assertThat(event.type).isEqualTo(EventType.FINGERPRINT_CAPTURE)
        with(event.payload) {
            assertThat(createdAt).isEqualTo(SampleDefaults.CREATED_AT)
            assertThat(endedAt).isEqualTo(SampleDefaults.ENDED_AT)
            assertThat(eventVersion).isEqualTo(FingerprintCaptureEvent.EVENT_VERSION)
            assertThat(type).isEqualTo(EventType.FINGERPRINT_CAPTURE)
            assertThat(finger).isEqualTo(IFingerIdentifier.LEFT_THUMB)
            assertThat(qualityThreshold).isEqualTo(10)
            assertThat(fingerprint).isEqualTo(fingerprint)
        }
    }
}
