package com.simprints.infra.events.event.domain.models.face

import androidx.annotation.Keep
import com.google.common.truth.Truth.*
import com.simprints.infra.events.event.domain.models.EventType.FACE_FALLBACK_CAPTURE
import com.simprints.infra.events.event.domain.models.face.FaceFallbackCaptureEvent.Companion.EVENT_VERSION
import com.simprints.infra.events.sampledata.SampleDefaults.CREATED_AT
import com.simprints.infra.events.sampledata.SampleDefaults.ENDED_AT
import org.junit.Test

@Keep
class FaceFallbackCaptureEventTest {
    @Test
    fun create_FaceFallbackCaptureEvent() {
        val event = FaceFallbackCaptureEvent(CREATED_AT, ENDED_AT)

        assertThat(event.id).isNotNull()
        assertThat(event.type).isEqualTo(FACE_FALLBACK_CAPTURE)
        with(event.payload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(endedAt).isEqualTo(ENDED_AT)
            assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            assertThat(type).isEqualTo(FACE_FALLBACK_CAPTURE)
        }
    }
}
