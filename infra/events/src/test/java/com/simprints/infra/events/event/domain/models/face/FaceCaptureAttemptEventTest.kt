package com.simprints.infra.events.event.domain.models.face

import androidx.annotation.Keep
import com.google.common.truth.Truth.*
import com.simprints.infra.events.event.domain.models.Event
import com.simprints.infra.events.event.domain.models.EventType.FACE_CAPTURE_ATTEMPT
import com.simprints.infra.events.event.domain.models.FaceCaptureAttemptEvent
import com.simprints.infra.events.event.domain.models.FaceCaptureAttemptEvent.Companion.EVENT_VERSION
import com.simprints.infra.events.event.domain.models.FaceCaptureAttemptEvent.FaceCaptureAttemptPayload.RunStatus
import com.simprints.infra.events.sampledata.SampleDefaults.CREATED_AT
import com.simprints.infra.events.sampledata.SampleDefaults.ENDED_AT
import com.simprints.infra.events.sampledata.createFaceCaptureAttemptEvent
import com.simprints.infra.serialization.SimJson
import org.junit.Test

@Keep
class FaceCaptureAttemptEventTest {
    @Test
    fun create_FaceCaptureAttemptEvent() {
        val event = createFaceCaptureAttemptEvent()

        assertThat(event.id).isNotNull()
        assertThat(event.type).isEqualTo(FACE_CAPTURE_ATTEMPT)
        with(event.payload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(endedAt).isEqualTo(ENDED_AT)
            assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            assertThat(type).isEqualTo(FACE_CAPTURE_ATTEMPT)
            assertThat(attemptNb).isEqualTo(0)
            assertThat(totalFramesAnalysed).isEqualTo(47)
            assertThat(validFrameCount).isEqualTo(15)
            assertThat(timeToFirstValidMs).isEqualTo(950L)
            assertThat(rejectionStats.tooFar?.count).isEqualTo(12)
            assertThat(rejectionStats.badQuality).isNull()
            assertThat(statusRuns.map { it.status }).containsExactly(RunStatus.TOO_FAR, RunStatus.OFF_YAW, RunStatus.VALID).inOrder()
            assertThat(statusRunsTruncated).isFalse()
        }
    }

    @Test
    fun `round-trips through JSON as a polymorphic Event`() {
        val original = createFaceCaptureAttemptEvent()

        val json = original.toJson()
        val decoded: Event = SimJson.decodeFromString(json)

        assertThat(decoded).isInstanceOf(FaceCaptureAttemptEvent::class.java)
        assertThat((decoded as FaceCaptureAttemptEvent).payload).isEqualTo(original.payload)
    }

    @Test
    fun `safe string carries no biometric data`() {
        val safe = createFaceCaptureAttemptEvent().payload.toSafeString()

        assertThat(safe).isEqualTo(
            "attempt nr: 0, sdk: RANK_ONE, frames: 47, valid: 15, first valid ms: 950, runs: 3, truncated: false",
        )
    }
}
