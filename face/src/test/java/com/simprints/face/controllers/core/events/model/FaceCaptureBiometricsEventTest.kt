package com.simprints.face.controllers.core.events.model

import com.google.common.truth.Truth.assertThat
import com.simprints.face.controllers.core.events.model.FaceCaptureBiometricsEvent.Result.Companion.fromFaceDetectionStatus
import com.simprints.face.models.FaceDetection
import org.junit.Test
import com.simprints.eventsystem.event.domain.models.face.FaceCaptureBiometricsEvent as CoreFaceCaptureBiometricsEventFace

class FaceCaptureBiometricsEventTest {

    @Test
    fun `fromDomainToCore maps correctly`() {
        val domain = FaceCaptureBiometricsEvent(
            startTime = 0,
            endTime = 0,
            result = FaceCaptureBiometricsEvent.Result.VALID,
            eventFace = null,
            payloadId = "someId"
        )

        val core = domain.fromDomainToCore()

        with(core) {
            assertThat(payload.face).isEqualTo(domain.eventFace)
            assertThat(payload.result).isEqualTo(CoreFaceCaptureBiometricsEventFace.FaceCaptureBiometricsPayload.Result.VALID)
            assertThat(payload.createdAt).isEqualTo(domain.startTime)
        }
    }

    @Test
    fun `result fromDomainToCore maps correctly`() {
        listOf(
            FaceCaptureBiometricsEvent.Result.VALID,
            FaceCaptureBiometricsEvent.Result.INVALID,
            FaceCaptureBiometricsEvent.Result.OFF_YAW,
            FaceCaptureBiometricsEvent.Result.OFF_ROLL,
            FaceCaptureBiometricsEvent.Result.TOO_CLOSE,
            FaceCaptureBiometricsEvent.Result.TOO_FAR,
        ).zip(
            listOf(
                CoreFaceCaptureBiometricsEventFace.FaceCaptureBiometricsPayload.Result.VALID,
                CoreFaceCaptureBiometricsEventFace.FaceCaptureBiometricsPayload.Result.INVALID,
                CoreFaceCaptureBiometricsEventFace.FaceCaptureBiometricsPayload.Result.OFF_YAW,
                CoreFaceCaptureBiometricsEventFace.FaceCaptureBiometricsPayload.Result.OFF_ROLL,
                CoreFaceCaptureBiometricsEventFace.FaceCaptureBiometricsPayload.Result.TOO_CLOSE,
                CoreFaceCaptureBiometricsEventFace.FaceCaptureBiometricsPayload.Result.TOO_FAR
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
                FaceCaptureBiometricsEvent.Result.VALID,
                FaceCaptureBiometricsEvent.Result.VALID,
                FaceCaptureBiometricsEvent.Result.INVALID,
                FaceCaptureBiometricsEvent.Result.OFF_YAW,
                FaceCaptureBiometricsEvent.Result.OFF_ROLL,
                FaceCaptureBiometricsEvent.Result.TOO_CLOSE,
                FaceCaptureBiometricsEvent.Result.TOO_FAR
            )
        ).forEach {
            assertThat(fromFaceDetectionStatus(it.first)).isEqualTo(it.second)
        }
    }
}
