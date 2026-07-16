package com.simprints.infra.eventsync.event.remote.models.face

import com.google.common.truth.Truth.*
import com.simprints.core.tools.utils.randomUUID
import com.simprints.infra.events.event.domain.models.FaceCaptureEvent
import com.simprints.infra.events.sampledata.FACE_TEMPLATE_FORMAT
import com.simprints.infra.eventsync.event.remote.models.ApiFaceCapturePayload
import com.simprints.infra.eventsync.event.remote.models.ApiTimestamp
import com.simprints.infra.eventsync.event.remote.models.fromDomainToApi
import org.junit.Test

class ApiFaceCapturePayloadTest {
    @Test
    fun `creating face capture payload returns correct values`() {
        val apiFace = ApiFaceCapturePayload.ApiFace(
            yaw = 1.0f,
            roll = 2.3f,
            quality = 12f,
            format = FACE_TEMPLATE_FORMAT,
            spoofScore = 0.5f,
        )
        val payload = ApiFaceCapturePayload(
            id = randomUUID(),
            startTime = ApiTimestamp(1),
            endTime = ApiTimestamp(2),
            attemptNb = 2,
            qualityThreshold = 1.2f,
            result = ApiFaceCapturePayload.ApiResult.VALID,
            isFallback = false,
            face = apiFace,
        )
        with(payload) {
            assertThat(id).isNotNull()
            assertThat(startTime).isEqualTo(ApiTimestamp(1))
            assertThat(endTime).isEqualTo(ApiTimestamp(2))
            assertThat(attemptNb).isEqualTo(2)
            assertThat(qualityThreshold).isEqualTo(1.2f)
            assertThat(result).isEqualTo(ApiFaceCapturePayload.ApiResult.VALID)
            assertThat(isFallback).isFalse()
            assertThat(face).isEqualTo(apiFace)
        }
    }

    @Test
    fun `should map all face capture results correctly`() {
        mapOf(
            FaceCaptureEvent.FaceCapturePayload.Result.VALID to ApiFaceCapturePayload.ApiResult.VALID,
            FaceCaptureEvent.FaceCapturePayload.Result.INVALID to ApiFaceCapturePayload.ApiResult.INVALID,
            FaceCaptureEvent.FaceCapturePayload.Result.OFF_YAW to ApiFaceCapturePayload.ApiResult.OFF_YAW,
            FaceCaptureEvent.FaceCapturePayload.Result.OFF_ROLL to ApiFaceCapturePayload.ApiResult.OFF_ROLL,
            FaceCaptureEvent.FaceCapturePayload.Result.TOO_CLOSE to ApiFaceCapturePayload.ApiResult.TOO_CLOSE,
            FaceCaptureEvent.FaceCapturePayload.Result.TOO_FAR to ApiFaceCapturePayload.ApiResult.TOO_FAR,
            FaceCaptureEvent.FaceCapturePayload.Result.BAD_QUALITY to ApiFaceCapturePayload.ApiResult.BAD_QUALITY,
        ).forEach { (mappedResult, expected) -> assertThat(mappedResult.fromDomainToApi()).isEqualTo(expected) }
    }

    @Test
    fun `should map all face spoof skip reasons correctly`() {
        mapOf(
            FaceCaptureEvent.FaceCapturePayload.SpoofSkipReason.IMAGE_TOO_SMALL to ApiFaceCapturePayload.ApiSpoofSkipReason.IMAGE_TOO_SMALL,
            FaceCaptureEvent.FaceCapturePayload.SpoofSkipReason.IOD_TOO_LARGE to ApiFaceCapturePayload.ApiSpoofSkipReason.IOD_TOO_LARGE,
            FaceCaptureEvent.FaceCapturePayload.SpoofSkipReason.IOD_TOO_SMALL to ApiFaceCapturePayload.ApiSpoofSkipReason.IOD_TOO_SMALL,
        ).forEach { (mappedResult, expected) -> assertThat(mappedResult.fromDomainToApi()).isEqualTo(expected) }
    }

    @Test
    fun `should map api face correctly`() {
        val face = FaceCaptureEvent.FaceCapturePayload
            .Face(
                yaw = 2.0f,
                roll = 1.0f,
                quality = 3.0f,
                format = FACE_TEMPLATE_FORMAT,
                spoofScore = 0.5f,
                spoofSkipReason = FaceCaptureEvent.FaceCapturePayload.SpoofSkipReason.IOD_TOO_SMALL,
            ).fromDomainToApi()
        assertThat(face).isInstanceOf(ApiFaceCapturePayload.ApiFace::class.java)
        assertThat(face.format).isEqualTo(FACE_TEMPLATE_FORMAT)
        assertThat(face.yaw).isEqualTo(2.0f)
        assertThat(face.roll).isEqualTo(1.0f)
        assertThat(face.quality).isEqualTo(3.0f)
        assertThat(face.spoofScore).isEqualTo(0.5f)
        assertThat(face.spoofSkipReason).isEqualTo(ApiFaceCapturePayload.ApiSpoofSkipReason.IOD_TOO_SMALL)
    }
}
