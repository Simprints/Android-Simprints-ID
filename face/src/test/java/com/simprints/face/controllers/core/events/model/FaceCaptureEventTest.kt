package com.simprints.face.controllers.core.events.model

import com.google.common.truth.Truth.assertThat
import com.simprints.face.controllers.core.events.model.FaceCaptureEvent.Result.Companion.fromFaceDetectionStatus
import com.simprints.face.models.FaceDetection
import org.junit.Test
import com.simprints.eventsystem.event.domain.models.face.FaceCaptureEvent as CoreFaceCaptureEvent
import com.simprints.eventsystem.event.domain.models.face.FaceCaptureEvent.FaceCapturePayload.Result as CoreFaceCaptureEventResult

class FaceCaptureEventTest {

    @Test
    fun `fromDomainToCore maps correctly`() {
        val domain = FaceCaptureEvent(
            startTime = 0,
            endTime = 0,
            attemptNb = 0,
            qualityThreshold = 0.0f,
            result = FaceCaptureEvent.Result.VALID,
            isFallback = false,
            eventFace = null,
            payloadId = "someId"
        )

        val core = domain.fromDomainToCore()

        assertThat(domain.eventFace).isEqualTo(core.payload.face)
        assertThat(core.payload.createdAt).isEqualTo(0)
        assertThat(core.payload.qualityThreshold).isEqualTo(0.0f)
        assertThat(core.payload.result).isEqualTo(CoreFaceCaptureEvent.FaceCapturePayload.Result.VALID)
    }

    @Test
    fun `result mapping is done correctly`() {
        listOf(
            FaceCaptureEvent.Result.VALID,
            FaceCaptureEvent.Result.INVALID,
            FaceCaptureEvent.Result.OFF_YAW,
            FaceCaptureEvent.Result.OFF_ROLL,
            FaceCaptureEvent.Result.TOO_CLOSE,
            FaceCaptureEvent.Result.TOO_FAR
        ).zip(
            listOf(
                CoreFaceCaptureEventResult.VALID,
                CoreFaceCaptureEventResult.INVALID,
                CoreFaceCaptureEventResult.OFF_YAW,
                CoreFaceCaptureEventResult.OFF_ROLL,
                CoreFaceCaptureEventResult.TOO_CLOSE,
                CoreFaceCaptureEventResult.TOO_FAR
            )
        ).forEach {
            assertThat(it.first.fromDomainToCore()).isEqualTo(it.second)
        }
    }

    @Test
    fun `fromFaceDetectionStatus maps correctly`() {
        listOf(
            FaceDetection.Status.VALID,
            FaceDetection.Status.VALID_CAPTURING,
            FaceDetection.Status.NOFACE,
            FaceDetection.Status.OFFYAW,
            FaceDetection.Status.OFFROLL,
            FaceDetection.Status.TOOCLOSE,
            FaceDetection.Status.TOOFAR,
        ).zip(
            listOf(
                FaceCaptureEvent.Result.VALID,
                FaceCaptureEvent.Result.VALID,
                FaceCaptureEvent.Result.INVALID,
                FaceCaptureEvent.Result.OFF_YAW,
                FaceCaptureEvent.Result.OFF_ROLL,
                FaceCaptureEvent.Result.TOO_CLOSE,
                FaceCaptureEvent.Result.TOO_FAR,
            )
        ).forEach {
            assertThat(fromFaceDetectionStatus(it.first)).isEqualTo(it.second)
        }
    }
}
