package com.simprints.id.data.db.event.domain.models.face

import androidx.annotation.Keep
import com.google.common.truth.Truth.assertThat
import com.simprints.id.data.db.event.domain.models.CREATED_AT
import com.simprints.id.data.db.event.domain.models.ENDED_AT
import com.simprints.id.data.db.event.domain.models.EventLabels

import com.simprints.id.data.db.event.domain.models.EventType.FACE_CAPTURE_RETRY
import com.simprints.id.data.db.event.domain.models.face.FaceCaptureRetryEvent.Companion.EVENT_VERSION
import com.simprints.id.orchestrator.SOME_GUID1
import org.junit.Test

@Keep
class FaceCaptureRetryEventTest {
    @Test
    fun create_FaceCaptureRetryEvent() {
        val labels = EventLabels(sessionId = SOME_GUID1)
        val event = FaceCaptureRetryEvent(CREATED_AT, ENDED_AT, labels)
        assertThat(event.id).isNotNull()
        assertThat(event.labels).isEqualTo(labels)
        assertThat(event.type).isEqualTo(FACE_CAPTURE_RETRY)
        with(event.payload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(endedAt).isEqualTo(ENDED_AT)
            assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            assertThat(type).isEqualTo(FACE_CAPTURE_RETRY)
        }
    }
}

