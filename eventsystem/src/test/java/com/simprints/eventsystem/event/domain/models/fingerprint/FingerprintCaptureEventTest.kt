package com.simprints.eventsystem.event.domain.models.fingerprint

import com.google.common.truth.Truth.assertThat
import com.simprints.eventsystem.event.domain.models.EventLabels
import com.simprints.eventsystem.event.domain.models.EventType
import com.simprints.eventsystem.sampledata.FingerprintCaptureEventV3Sample
import com.simprints.eventsystem.sampledata.SampleDefaults
import com.simprints.moduleapi.fingerprint.IFingerIdentifier
import org.junit.Test

class FingerprintCaptureEventTest {

    @Test
    fun create_FingerprintCaptureEvent() {
        val labels = EventLabels(sessionId = SampleDefaults.GUID1)
        val fingerprint = FingerprintCaptureEvent.FingerprintCapturePayload.Fingerprint(
            IFingerIdentifier.LEFT_THUMB,
            8,
            FingerprintTemplateFormat.ISO_19794_2
        )
        val event = FingerprintCaptureEventV3Sample.getEvent(labels)

        assertThat(event.id).isNotNull()
        assertThat(event.labels).isEqualTo(labels)
        assertThat(event.type).isEqualTo(EventType.FINGERPRINT_CAPTURE_V3)
        with(event.payload) {
            assertThat(createdAt).isEqualTo(SampleDefaults.CREATED_AT)
            assertThat(endedAt).isEqualTo(SampleDefaults.ENDED_AT)
            assertThat(eventVersion).isEqualTo(FingerprintCaptureEvent.EVENT_VERSION)
            assertThat(type).isEqualTo(EventType.FINGERPRINT_CAPTURE_V3)
            assertThat(finger).isEqualTo(IFingerIdentifier.LEFT_THUMB)
            assertThat(qualityThreshold).isEqualTo(10)
            assertThat(fingerprint).isEqualTo(fingerprint)
        }
    }
}
