package com.simprints.infra.events.event.domain.models.face

import androidx.annotation.Keep
import com.google.common.truth.Truth.assertThat
import com.simprints.infra.events.event.domain.models.EventLabels
import com.simprints.infra.events.event.domain.models.EventType.FACE_CAPTURE_CONFIRMATION
import com.simprints.infra.events.event.domain.models.face.FaceCaptureConfirmationEvent.Companion.EVENT_VERSION
import com.simprints.infra.events.event.domain.models.face.FaceCaptureConfirmationEvent.FaceCaptureConfirmationPayload.Result.CONTINUE
import com.simprints.infra.events.sampledata.SampleDefaults.CREATED_AT
import com.simprints.infra.events.sampledata.SampleDefaults.ENDED_AT
import com.simprints.infra.events.sampledata.SampleDefaults.GUID1
import org.junit.Test

@Keep
class FaceCaptureConfirmationEventTest {
    @Test
    fun create_FaceCaptureConfirmationEvent() {
        val labels = EventLabels(sessionId = GUID1)
        val event = FaceCaptureConfirmationEvent(CREATED_AT, ENDED_AT, CONTINUE, labels)
        assertThat(event.id).isNotNull()
        assertThat(event.labels).isEqualTo(labels)
        assertThat(event.type).isEqualTo(FACE_CAPTURE_CONFIRMATION)
        with(event.payload) {
            assertThat(type).isEqualTo(FACE_CAPTURE_CONFIRMATION)
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(endedAt).isEqualTo(ENDED_AT)
            assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            assertThat(result).isEqualTo(CONTINUE)
        }
    }
}

