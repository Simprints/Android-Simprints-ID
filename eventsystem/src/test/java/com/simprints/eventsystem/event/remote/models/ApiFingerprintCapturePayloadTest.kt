package com.simprints.eventsystem.event.remote.models

import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.utils.randomUUID
import com.simprints.eventsystem.event.domain.models.fingerprint.FingerprintCaptureEvent
import com.simprints.eventsystem.event.domain.models.fingerprint.FingerprintTemplateFormat
import com.simprints.moduleapi.fingerprint.IFingerIdentifier
import org.junit.Test

class ApiFingerprintCapturePayloadTest {

    @Test
    fun `creating fingerprint capture object has correct values`() {
        val fingerprint = ApiFingerprintCapturePayload.ApiFingerprint(
            FingerprintCaptureEvent.FingerprintCapturePayload.Fingerprint(
                finger = IFingerIdentifier.LEFT_3RD_FINGER,
                quality = 23,
                format = FingerprintTemplateFormat.ISO_19794_2
            )
        )
        val payload = ApiFingerprintCapturePayload(
            id = randomUUID(),
            startTime = 1,
            version = 3,
            endTime = 1,
            qualityThreshold = 23,
            finger = IFingerIdentifier.LEFT_3RD_FINGER,
            result = ApiFingerprintCapturePayload.ApiResult.GOOD_SCAN,
            fingerprint = fingerprint,
        )

        with(payload) {
            assertThat(id).isNotNull()
            assertThat(startTime).isEqualTo(1)
            assertThat(version).isEqualTo(3)
            assertThat(endTime).isEqualTo(1)
            assertThat(qualityThreshold).isEqualTo(23)
            assertThat(finger).isEqualTo(IFingerIdentifier.LEFT_3RD_FINGER)
            assertThat(result).isEqualTo(ApiFingerprintCapturePayload.ApiResult.GOOD_SCAN)
            assertThat(fingerprint).isEqualTo(fingerprint)
        }
    }

    @Test
    fun `should map GOOD_SCAN correctly`() {
        val result =
            FingerprintCaptureEvent.FingerprintCapturePayload.Result.GOOD_SCAN.fromDomainToApi()
        assertThat(result).isInstanceOf(ApiFingerprintCapturePayload.ApiResult.GOOD_SCAN::class.java)
    }

    @Test
    fun `should map BAD_QUALITY correctly`() {
        val result =
            FingerprintCaptureEvent.FingerprintCapturePayload.Result.BAD_QUALITY.fromDomainToApi()
        assertThat(result).isInstanceOf(ApiFingerprintCapturePayload.ApiResult.BAD_QUALITY::class.java)
    }

    @Test
    fun `should map NO_FINGER_DETECTED correctly`() {
        val result =
            FingerprintCaptureEvent.FingerprintCapturePayload.Result.NO_FINGER_DETECTED.fromDomainToApi()
        assertThat(result).isInstanceOf(ApiFingerprintCapturePayload.ApiResult.NO_FINGER_DETECTED::class.java)
    }

    @Test
    fun `should map SKIPPED correctly`() {
        val result =
            FingerprintCaptureEvent.FingerprintCapturePayload.Result.SKIPPED.fromDomainToApi()
        assertThat(result).isInstanceOf(ApiFingerprintCapturePayload.ApiResult.SKIPPED::class.java)
    }

    @Test
    fun `should map FAILURE_TO_ACQUIRE correctly`() {
        val result =
            FingerprintCaptureEvent.FingerprintCapturePayload.Result.FAILURE_TO_ACQUIRE.fromDomainToApi()
        assertThat(result).isInstanceOf(ApiFingerprintCapturePayload.ApiResult.FAILURE_TO_ACQUIRE::class.java)
    }
}
