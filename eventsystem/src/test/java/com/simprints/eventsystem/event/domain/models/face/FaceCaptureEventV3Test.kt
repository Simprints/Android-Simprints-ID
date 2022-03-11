package com.simprints.eventsystem.event.domain.models.face

import androidx.annotation.Keep
import com.google.common.truth.Truth.assertThat
import com.simprints.eventsystem.event.domain.models.EventLabels
import com.simprints.eventsystem.event.domain.models.EventType
import com.simprints.eventsystem.sampledata.SampleDefaults
import org.junit.Test

@Keep
class FaceCaptureEventV3Test {
    @Test
    fun create_FaceCaptureEvent() {
        val labels = EventLabels(sessionId = SampleDefaults.GUID1)
        val faceArg = FaceCaptureEventV3.FaceCapturePayloadV3.Face(0F, 1F, 2F, FaceTemplateFormat.RANK_ONE_1_23)
        val event = FaceCaptureEventV3(
            SampleDefaults.CREATED_AT,
            SampleDefaults.ENDED_AT, 0, 1F,
            FaceCaptureEventV3.FaceCapturePayloadV3.Result.VALID, true, faceArg, labels
        )
        assertThat(event.id).isNotNull()
        assertThat(event.labels).isEqualTo(labels)
        assertThat(event.type).isEqualTo(EventType.FACE_CAPTURE_V3)
        with(event.payload) {
            assertThat(createdAt).isEqualTo(SampleDefaults.CREATED_AT)
            assertThat(endedAt).isEqualTo(SampleDefaults.ENDED_AT)
            assertThat(eventVersion).isEqualTo(FaceCaptureEventV3.EVENT_VERSION)
            assertThat(type).isEqualTo(EventType.FACE_CAPTURE_V3)
            assertThat(attemptNb).isEqualTo(0)
            assertThat(qualityThreshold).isEqualTo(1F)
            assertThat(result).isEqualTo(FaceCaptureEventV3.FaceCapturePayloadV3.Result.VALID)
            assertThat(isFallback).isEqualTo(true)
            assertThat(face).isEqualTo(faceArg)
        }
    }
}

