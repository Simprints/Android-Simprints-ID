package com.simprints.infra.events.event.domain.models.face

import androidx.annotation.Keep
import com.google.common.truth.Truth.*
import com.simprints.infra.events.event.domain.models.EventType
import com.simprints.infra.events.event.domain.models.FaceCaptureEvent
import com.simprints.infra.events.sampledata.FACE_TEMPLATE_FORMAT
import com.simprints.infra.events.sampledata.SampleDefaults
import org.junit.Test

@Keep
class FaceCaptureEventTest {
    @Test
    fun create_FaceCaptureEvent() {
        val faceArg = FaceCaptureEvent.FaceCapturePayload.Face(
            yaw = 0F,
            roll = 1F,
            quality = 2F,
            format = FACE_TEMPLATE_FORMAT,
            spoofScore = 0.5f,
            spoofSkipReason = FaceCaptureEvent.FaceCapturePayload.SpoofSkipReason.IMAGE_TOO_SMALL,
        )
        val event = FaceCaptureEvent(
            startTime = SampleDefaults.CREATED_AT,
            endTime = SampleDefaults.ENDED_AT,
            attemptNb = 0,
            qualityThreshold = 1F,
            result = FaceCaptureEvent.FaceCapturePayload.Result.VALID,
            isAutoCapture = false,
            isFallback = true,
            face = faceArg,
        )

        assertThat(event.id).isNotNull()
        assertThat(event.type).isEqualTo(EventType.FACE_CAPTURE)
        with(event.payload) {
            assertThat(createdAt).isEqualTo(SampleDefaults.CREATED_AT)
            assertThat(endedAt).isEqualTo(SampleDefaults.ENDED_AT)
            assertThat(eventVersion).isEqualTo(FaceCaptureEvent.EVENT_VERSION)
            assertThat(type).isEqualTo(EventType.FACE_CAPTURE)
            assertThat(attemptNb).isEqualTo(0)
            assertThat(qualityThreshold).isEqualTo(1F)
            assertThat(result).isEqualTo(FaceCaptureEvent.FaceCapturePayload.Result.VALID)
            assertThat(isFallback).isEqualTo(true)
            assertThat(face).isEqualTo(faceArg)
        }
    }
}
