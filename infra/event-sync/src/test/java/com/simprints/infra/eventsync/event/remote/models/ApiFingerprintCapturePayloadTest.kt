package com.simprints.infra.eventsync.event.remote.models

import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.fingerprint.IFingerIdentifier
import com.simprints.core.tools.utils.randomUUID
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.fingerprint.FingerprintCaptureEvent
import io.mockk.mockk
import org.junit.Test

class ApiFingerprintCapturePayloadTest {
    @Test
    fun `creating fingerprint capture object has correct values`() {
        val fingerprint = ApiFingerprintCapturePayload.ApiFingerprint(
            FingerprintCaptureEvent.FingerprintCapturePayload.Fingerprint(
                finger = IFingerIdentifier.LEFT_3RD_FINGER,
                quality = 23,
                format = "ISO_19794_2",
            ),
        )
        val payload = ApiFingerprintCapturePayload(
            id = randomUUID(),
            startTime = ApiTimestamp(1),
            endTime = ApiTimestamp(1),
            qualityThreshold = 23,
            finger = IFingerIdentifier.LEFT_3RD_FINGER,
            result = ApiFingerprintCapturePayload.ApiResult.GOOD_SCAN,
            fingerprint = fingerprint,
        )

        with(payload) {
            assertThat(id).isNotNull()
            assertThat(startTime).isEqualTo(ApiTimestamp(1))
            assertThat(endTime).isEqualTo(ApiTimestamp(1))
            assertThat(qualityThreshold).isEqualTo(23)
            assertThat(finger).isEqualTo(IFingerIdentifier.LEFT_3RD_FINGER)
            assertThat(result).isEqualTo(ApiFingerprintCapturePayload.ApiResult.GOOD_SCAN)
            assertThat(fingerprint).isEqualTo(fingerprint)
        }
    }

    @Test
    fun `should map GOOD_SCAN correctly`() {
        val result =
            FingerprintCaptureEvent.FingerprintCapturePayload.Result.GOOD_SCAN
                .fromDomainToApi()
        assertThat(result).isInstanceOf(ApiFingerprintCapturePayload.ApiResult.GOOD_SCAN::class.java)
    }

    @Test
    fun `should map BAD_QUALITY correctly`() {
        val result =
            FingerprintCaptureEvent.FingerprintCapturePayload.Result.BAD_QUALITY
                .fromDomainToApi()
        assertThat(result).isInstanceOf(ApiFingerprintCapturePayload.ApiResult.BAD_QUALITY::class.java)
    }

    @Test
    fun `should map NO_FINGER_DETECTED correctly`() {
        val result =
            FingerprintCaptureEvent.FingerprintCapturePayload.Result.NO_FINGER_DETECTED
                .fromDomainToApi()
        assertThat(result).isInstanceOf(ApiFingerprintCapturePayload.ApiResult.NO_FINGER_DETECTED::class.java)
    }

    @Test
    fun `should map SKIPPED correctly`() {
        val result =
            FingerprintCaptureEvent.FingerprintCapturePayload.Result.SKIPPED
                .fromDomainToApi()
        assertThat(result).isInstanceOf(ApiFingerprintCapturePayload.ApiResult.SKIPPED::class.java)
    }

    @Test
    fun `should map FAILURE_TO_ACQUIRE correctly`() {
        val result =
            FingerprintCaptureEvent.FingerprintCapturePayload.Result.FAILURE_TO_ACQUIRE
                .fromDomainToApi()
        assertThat(result).isInstanceOf(ApiFingerprintCapturePayload.ApiResult.FAILURE_TO_ACQUIRE::class.java)
    }

    @Test
    fun `when getTokenizedFieldJsonPath is invoked, null is returned`() {
        val payload = ApiFingerprintCapturePayload(domainPayload = mockk(relaxed = true))
        TokenKeyType.values().forEach {
            assertThat(payload.getTokenizedFieldJsonPath(it)).isNull()
        }
    }
}
