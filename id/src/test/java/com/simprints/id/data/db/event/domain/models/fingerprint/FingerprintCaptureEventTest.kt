package com.simprints.id.data.db.event.domain.models.fingerprint

import com.google.common.truth.Truth.assertThat
import com.simprints.id.commontesttools.DefaultTestConstants.GUID1
import com.simprints.id.commontesttools.events.CREATED_AT
import com.simprints.id.commontesttools.events.ENDED_AT
import com.simprints.id.data.db.event.domain.models.EventLabels
import com.simprints.id.data.db.event.domain.models.EventType.FINGERPRINT_CAPTURE
import com.simprints.id.data.db.event.domain.models.fingerprint.FingerprintCaptureEvent.Companion.EVENT_VERSION
import com.simprints.id.data.db.event.domain.models.fingerprint.FingerprintCaptureEvent.FingerprintCapturePayload
import com.simprints.id.data.db.event.domain.models.fingerprint.FingerprintCaptureEvent.FingerprintCapturePayload.Fingerprint
import com.simprints.id.data.db.subject.domain.FingerIdentifier.LEFT_THUMB
import com.simprints.id.sampledata.FingerprintCaptureEventSample
import org.junit.Test

class FingerprintCaptureEventTest {

    @Test
    fun create_FingerprintCaptureEvent() {
        val labels = EventLabels(sessionId = GUID1)
        val fingerprint = Fingerprint(LEFT_THUMB, 8, "template", FingerprintTemplateFormat.ISO_19794_2)
        val event = FingerprintCaptureEventSample.getEvent()

        assertThat(event.id).isNotNull()
        assertThat(event.labels).isEqualTo(labels)
        assertThat(event.type).isEqualTo(FINGERPRINT_CAPTURE)
        with(event.payload as FingerprintCapturePayload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(endedAt).isEqualTo(ENDED_AT)
            assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            assertThat(type).isEqualTo(FINGERPRINT_CAPTURE)
            assertThat(finger).isEqualTo(LEFT_THUMB)
            assertThat(qualityThreshold).isEqualTo(10)
            assertThat(fingerprint).isEqualTo(fingerprint)
        }
    }
}
