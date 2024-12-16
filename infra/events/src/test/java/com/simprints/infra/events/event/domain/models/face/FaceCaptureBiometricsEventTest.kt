package com.simprints.infra.events.event.domain.models.face

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.events.event.domain.models.EventType
import com.simprints.infra.events.sampledata.FACE_TEMPLATE_FORMAT
import com.simprints.infra.events.sampledata.SampleDefaults
import org.junit.Test

class FaceCaptureBiometricsEventTest {
    @Test
    fun create_FaceCaptureBiometricsEvent() {
        val faceArg = FaceCaptureBiometricsEvent.FaceCaptureBiometricsPayload.Face(
            0.0f,
            roll = 1.0f,
            template = "template",
            quality = 1.0f,
            format = FACE_TEMPLATE_FORMAT,
        )
        val event = FaceCaptureBiometricsEvent(
            startTime = SampleDefaults.CREATED_AT,
            face = faceArg,
            id = "someId",
        )

        assertThat(event.id).isEqualTo("someId")
        assertThat(event.type).isEqualTo(EventType.FACE_CAPTURE_BIOMETRICS)
        with(event.payload) {
            assertThat(id).isNotNull()
            assertThat(type).isEqualTo(EventType.FACE_CAPTURE_BIOMETRICS)
            assertThat(face).isEqualTo(faceArg)
            assertThat(eventVersion).isEqualTo(FaceCaptureBiometricsEvent.EVENT_VERSION)
        }
    }
}
