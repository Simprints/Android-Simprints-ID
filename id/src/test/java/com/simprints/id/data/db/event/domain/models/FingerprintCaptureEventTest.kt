package com.simprints.id.data.db.event.domain.models

import com.google.common.truth.Truth.assertThat
import com.simprints.id.data.db.event.domain.models.EventLabel.SessionIdLabel
import com.simprints.id.data.db.event.domain.models.EventType.FINGERPRINT_CAPTURE
import com.simprints.id.data.db.event.domain.models.FingerprintCaptureEvent.Companion.EVENT_VERSION
import com.simprints.id.data.db.event.domain.models.FingerprintCaptureEvent.FingerprintCapturePayload
import com.simprints.id.data.db.event.domain.models.FingerprintCaptureEvent.FingerprintCapturePayload.Fingerprint
import com.simprints.id.data.db.event.domain.models.FingerprintCaptureEvent.FingerprintCapturePayload.Result.BAD_QUALITY
import com.simprints.id.data.db.subject.domain.FingerIdentifier.LEFT_THUMB
import com.simprints.id.orchestrator.SOME_GUID1
import com.simprints.id.orchestrator.SOME_GUID2
import org.junit.Test

class FingerprintCaptureEventTest {

    @Test
    fun create_FingerprintCaptureEvent() {
        val fingerprint = Fingerprint(LEFT_THUMB, 8, "template")
        val event = FingerprintCaptureEvent(
            CREATED_AT,
            ENDED_AT,
            LEFT_THUMB,
            10,
            BAD_QUALITY,
            fingerprint,
            SOME_GUID1,
            SOME_GUID2)

        assertThat(event.id).isNotNull()
        assertThat(event.labels).containsExactly(
            SessionIdLabel(SOME_GUID2)
        )
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
