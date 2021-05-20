package com.simprints.eventsystem.event.domain.models.face

import androidx.annotation.Keep
import com.google.common.truth.Truth.assertThat
import com.simprints.eventsystem.event.domain.models.EventLabels
import com.simprints.eventsystem.event.domain.models.EventType.FACE_CAPTURE
import com.simprints.eventsystem.event.domain.models.face.FaceCaptureEvent.Companion.EVENT_VERSION
import com.simprints.eventsystem.event.domain.models.face.FaceCaptureEvent.FaceCapturePayload.Face
import com.simprints.eventsystem.event.domain.models.face.FaceCaptureEvent.FaceCapturePayload.Result.VALID
import com.simprints.eventsystem.sampledata.SampleDefaults.CREATED_AT
import com.simprints.eventsystem.sampledata.SampleDefaults.ENDED_AT
import com.simprints.eventsystem.sampledata.SampleDefaults.GUID1
import org.junit.Test

@Keep
class FaceCaptureEventTest {
    @Test
    fun create_FaceCaptureEvent() {
        val labels = EventLabels(sessionId = GUID1)
        val faceArg = Face(0F, 1F, 2F, "", FaceTemplateFormat.RANK_ONE_1_23)
        val event = FaceCaptureEvent(CREATED_AT, ENDED_AT, 0, 1F, VALID, true, faceArg, labels)
        assertThat(event.id).isNotNull()
        assertThat(event.labels).isEqualTo(labels)
        assertThat(event.type).isEqualTo(FACE_CAPTURE)
        with(event.payload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(endedAt).isEqualTo(ENDED_AT)
            assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            assertThat(type).isEqualTo(FACE_CAPTURE)
            assertThat(attemptNb).isEqualTo(0)
            assertThat(qualityThreshold).isEqualTo(1F)
            assertThat(result).isEqualTo(VALID)
            assertThat(isFallback).isEqualTo(true)
            assertThat(face).isEqualTo(faceArg)
        }
    }
}
