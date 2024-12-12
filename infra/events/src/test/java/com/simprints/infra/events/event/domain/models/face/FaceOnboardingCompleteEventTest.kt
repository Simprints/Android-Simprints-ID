package com.simprints.infra.events.event.domain.models.face

import androidx.annotation.Keep
import com.google.common.truth.Truth.*
import com.simprints.infra.events.event.domain.models.EventType.FACE_ONBOARDING_COMPLETE
import com.simprints.infra.events.event.domain.models.face.FaceOnboardingCompleteEvent.Companion.EVENT_VERSION
import com.simprints.infra.events.sampledata.SampleDefaults.CREATED_AT
import com.simprints.infra.events.sampledata.SampleDefaults.ENDED_AT
import org.junit.Test

@Keep
class FaceOnboardingCompleteEventTest {
    @Test
    fun create_FaceOnboardingCompleteEvent() {
        val event = FaceOnboardingCompleteEvent(CREATED_AT, ENDED_AT)

        assertThat(event.id).isNotNull()
        assertThat(event.type).isEqualTo(FACE_ONBOARDING_COMPLETE)
        with(event.payload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(endedAt).isEqualTo(ENDED_AT)
            assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            assertThat(type).isEqualTo(FACE_ONBOARDING_COMPLETE)
        }
    }
}
