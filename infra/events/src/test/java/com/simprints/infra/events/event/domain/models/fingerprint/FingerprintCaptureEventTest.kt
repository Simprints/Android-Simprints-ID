package com.simprints.infra.events.event.domain.models.fingerprint

import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.fingerprint.IFingerIdentifier
import com.simprints.infra.events.event.domain.models.EventLabels
import com.simprints.infra.events.event.domain.models.EventType
import com.simprints.infra.events.sampledata.FingerprintCaptureEventSample
import com.simprints.infra.events.sampledata.SampleDefaults
import org.junit.Test

class FingerprintCaptureEventTest {

    @Test
    fun create_FingerprintCaptureEvent() {
        val labels = EventLabels(sessionId = SampleDefaults.GUID1)
        val fingerprint = FingerprintCaptureEvent.FingerprintCapturePayload.Fingerprint(
            IFingerIdentifier.LEFT_THUMB,
            8,
            "ISO_19794_2"
        )
        val event = FingerprintCaptureEventSample.getEvent(labels)

        assertThat(event.id).isNotNull()
        assertThat(event.labels).isEqualTo(labels)
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
