package com.simprints.infra.events.event.domain.models.face

import androidx.annotation.Keep
import com.google.common.truth.Truth.assertThat
import com.simprints.infra.events.event.domain.models.EventType
import com.simprints.infra.events.sampledata.FACE_TEMPLATE_FORMAT
import com.simprints.infra.events.sampledata.SampleDefaults
import org.junit.Test

@Keep
class FaceCaptureEventTest {
    @Test
    fun create_FaceCaptureEvent() {
        val faceArg = FaceCaptureEvent.FaceCapturePayload.Face(0F, 1F, 2F, FACE_TEMPLATE_FORMAT)
        val event = FaceCaptureEvent(
            SampleDefaults.CREATED_AT,
            SampleDefaults.ENDED_AT,
            0,
            1F,
            FaceCaptureEvent.FaceCapturePayload.Result.VALID,
            false,
            true,
            faceArg,
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
