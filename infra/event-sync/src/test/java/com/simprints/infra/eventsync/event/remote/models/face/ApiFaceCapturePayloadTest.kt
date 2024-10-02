package com.simprints.infra.eventsync.event.remote.models.face

import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.utils.randomUUID
import com.simprints.infra.events.event.domain.models.face.FaceCaptureEvent
import com.simprints.infra.events.sampledata.FACE_TEMPLATE_FORMAT
import com.simprints.infra.eventsync.event.remote.models.ApiTimestamp
import org.junit.Test

class ApiFaceCapturePayloadTest {

    @Test
    fun `creating face capture payload returns correct values`() {
        val apiFace = ApiFaceCapturePayload.ApiFace(
            yaw = 1.0f,
            roll = 2.3f,
            quality = 12f,
            format = FACE_TEMPLATE_FORMAT
        )
        val payload = ApiFaceCapturePayload(
            id = randomUUID(),
            startTime = ApiTimestamp(1),
            endTime = ApiTimestamp(2),
            attemptNb = 2,
            qualityThreshold = 1.2f,
            result = ApiFaceCapturePayload.ApiResult.VALID,
            isFallback = false,
            face = apiFace
        )
        with(payload) {
            assertThat(id).isNotNull()
            assertThat(startTime).isEqualTo(ApiTimestamp(1),)
            assertThat(endTime).isEqualTo(ApiTimestamp(2),)
            assertThat(attemptNb).isEqualTo(2)
            assertThat(qualityThreshold).isEqualTo(1.2f)
            assertThat(result).isEqualTo(ApiFaceCapturePayload.ApiResult.VALID)
            assertThat(isFallback).isFalse()
            assertThat(face).isEqualTo(apiFace)
        }
    }

    @Test
    fun `should map VALID correctly`() {
        val result = FaceCaptureEvent.FaceCapturePayload.Result.VALID.fromDomainToApi()
        assertThat(result).isInstanceOf(ApiFaceCapturePayload.ApiResult.VALID::class.java)
    }

    @Test
    fun `should map INVALID correctly`() {
        val result = FaceCaptureEvent.FaceCapturePayload.Result.INVALID.fromDomainToApi()
        assertThat(result).isInstanceOf(ApiFaceCapturePayload.ApiResult.INVALID::class.java)
    }

    @Test
    fun `should map OFF YAW correctly`() {
        val result = FaceCaptureEvent.FaceCapturePayload.Result.OFF_YAW.fromDomainToApi()
        assertThat(result).isInstanceOf(ApiFaceCapturePayload.ApiResult.OFF_YAW::class.java)
    }

    @Test
    fun `should map OFF ROLL correctly`() {
        val result = FaceCaptureEvent.FaceCapturePayload.Result.OFF_ROLL.fromDomainToApi()
        assertThat(result).isInstanceOf(ApiFaceCapturePayload.ApiResult.OFF_ROLL::class.java)
    }

    @Test
    fun `should map TOO CLOSE correctly`() {
        val result = FaceCaptureEvent.FaceCapturePayload.Result.TOO_CLOSE.fromDomainToApi()
        assertThat(result).isInstanceOf(ApiFaceCapturePayload.ApiResult.TOO_CLOSE::class.java)
    }

    @Test
    fun `should map TOO FAR correctly`() {
        val result = FaceCaptureEvent.FaceCapturePayload.Result.TOO_FAR.fromDomainToApi()
        assertThat(result).isInstanceOf(ApiFaceCapturePayload.ApiResult.TOO_FAR::class.java)
    }

    @Test
    fun `should map BAD QUALITY correctly`() {
        val result = FaceCaptureEvent.FaceCapturePayload.Result.BAD_QUALITY.fromDomainToApi()
        assertThat(result).isInstanceOf(ApiFaceCapturePayload.ApiResult.BAD_QUALITY::class.java)
    }

    @Test
    fun `should map api face correctly`() {
        val face = FaceCaptureEvent.FaceCapturePayload.Face(
            yaw = 2.0f,
            roll = 1.0f,
            quality = 3.0f,
            format = FACE_TEMPLATE_FORMAT
        ).fromDomainToApi()
        assertThat(face).isInstanceOf(ApiFaceCapturePayload.ApiFace::class.java)
        assertThat(face.format).isEqualTo(FACE_TEMPLATE_FORMAT)
        assertThat(face.yaw).isEqualTo(2.0f)
        assertThat(face.roll).isEqualTo(1.0f)
        assertThat(face.quality).isEqualTo(3.0f)
    }
}
