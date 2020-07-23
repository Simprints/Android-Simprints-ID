package com.simprints.id.data.db.event.domain.models.face

import androidx.annotation.Keep
import com.google.common.truth.Truth
import com.simprints.id.data.db.event.domain.models.CREATED_AT
import com.simprints.id.data.db.event.domain.models.ENDED_AT
import com.simprints.id.data.db.event.domain.models.EventLabel.SessionIdLabel
import com.simprints.id.data.db.event.domain.models.EventType.FACE_FALLBACK_CAPTURE
import com.simprints.id.data.db.event.domain.models.face.FaceFallbackCaptureEvent.Companion.EVENT_VERSION
import com.simprints.id.orchestrator.SOME_GUID1
import org.junit.Test

@Keep
class FaceFallbackCaptureEventTest {
    @Test
    fun create_FaceFallbackCaptureEvent() {

        val event = FaceFallbackCaptureEvent(CREATED_AT, ENDED_AT, SOME_GUID1)
        Truth.assertThat(event.id).isNotNull()
        Truth.assertThat(event.labels).containsExactly(SessionIdLabel(SOME_GUID1))
        Truth.assertThat(event.type).isEqualTo(FACE_FALLBACK_CAPTURE)
        with(event.payload) {
            Truth.assertThat(createdAt).isEqualTo(CREATED_AT)
            Truth.assertThat(endedAt).isEqualTo(ENDED_AT)
            Truth.assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            Truth.assertThat(type).isEqualTo(FACE_FALLBACK_CAPTURE)
        }
    }
}

