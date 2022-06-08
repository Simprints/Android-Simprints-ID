package com.simprints.eventsystem.event.domain.models.face

import com.google.common.truth.Truth.assertThat
import com.simprints.eventsystem.event.domain.models.EventLabels
import com.simprints.eventsystem.event.domain.models.EventType
import com.simprints.eventsystem.sampledata.SampleDefaults
import org.junit.Test

class FaceCaptureBiometricsEventTest {

    @Test
    fun create_FaceCaptureBiometricsEvent() {
        val labels = EventLabels(sessionId = SampleDefaults.GUID1)
        val faceArg = FaceCaptureBiometricsEvent.FaceCaptureBiometricsPayload.Face(
            0.0f,
            roll = 1.0f,
            template = "template",
            quality = 1.0f,
            format = FaceTemplateFormat.RANK_ONE_1_23
        )
        val event = FaceCaptureBiometricsEvent(
            startTime = SampleDefaults.CREATED_AT,
            face = faceArg,
            labels = labels,
            id = "someId"
        )

        assertThat(event.id).isEqualTo("someId")
        assertThat(event.labels).isEqualTo(labels)
        assertThat(event.type).isEqualTo(EventType.FACE_CAPTURE_BIOMETRICS)

        with(event.payload) {
            assertThat(id).isNotNull()
            assertThat(type).isEqualTo(EventType.FACE_CAPTURE_BIOMETRICS)
            assertThat(face).isEqualTo(faceArg)
            assertThat(eventVersion).isEqualTo(0)
        }
    }
}
