package com.simprints.feature.enrollast

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.domain.models.Finger
import com.simprints.infra.events.event.domain.models.fingerprint.FingerprintTemplateFormat

import org.junit.Test

class EnrolLastBiometricParamsTest {

    @Test
    fun testFingerTemplateCaptureResultEquals() {
        val result = FingerTemplateCaptureResult(
            finger = Finger.LEFT_THUMB,
            template = byteArrayOf(3, 4),
            templateQualityScore = 42,
            format = FingerprintTemplateFormat.ISO_19794_2
        )

        assertThat(result).isEqualTo(
            FingerTemplateCaptureResult(
                finger = Finger.LEFT_THUMB,
                template = byteArrayOf(3, 4),
                templateQualityScore = 42,
                format = FingerprintTemplateFormat.ISO_19794_2
            )
        )
        assertThat(result).isNotEqualTo(
            FingerTemplateCaptureResult(
                finger = Finger.RIGHT_3RD_FINGER,
                template = byteArrayOf(3, 4),
                templateQualityScore = 42,
                format = FingerprintTemplateFormat.ISO_19794_2
            )
        )
        assertThat(result).isNotEqualTo(
            FingerTemplateCaptureResult(
                finger = Finger.RIGHT_3RD_FINGER,
                template = byteArrayOf(3, 4, 5),
                templateQualityScore = 42,
                format = FingerprintTemplateFormat.ISO_19794_2
            )
        )
        assertThat(result).isNotEqualTo(
            FingerTemplateCaptureResult(
                finger = Finger.RIGHT_3RD_FINGER,
                template = byteArrayOf(3, 4),
                templateQualityScore = 41,
                format = FingerprintTemplateFormat.ISO_19794_2
            )
        )
        assertThat(result).isNotEqualTo(
            FingerTemplateCaptureResult(
                finger = Finger.RIGHT_3RD_FINGER,
                template = byteArrayOf(3, 4, 5, 6),
                templateQualityScore = 42,
                format = FingerprintTemplateFormat.NEC_1
            )
        )

    }

    @Test
    fun testFingerTemplateCaptureResultHashCode() {
        assertThat(
            FingerTemplateCaptureResult(
                finger = Finger.LEFT_THUMB,
                template = byteArrayOf(3, 4),
                templateQualityScore = 42,
                format = FingerprintTemplateFormat.ISO_19794_2
            ).hashCode()
        ).isEqualTo(
            FingerTemplateCaptureResult(
                finger = Finger.LEFT_THUMB,
                template = byteArrayOf(3, 4),
                templateQualityScore = 42,
                format = FingerprintTemplateFormat.ISO_19794_2
            ).hashCode()
        )
        assertThat(
            FingerTemplateCaptureResult(
                finger = Finger.LEFT_THUMB,
                template = byteArrayOf(3, 4),
                templateQualityScore = 42,
                format = FingerprintTemplateFormat.ISO_19794_2
            ).hashCode()
        ).isNotEqualTo(
            FingerTemplateCaptureResult(
                finger = Finger.RIGHT_3RD_FINGER,
                template = byteArrayOf(3, 4, 5, 6),
                templateQualityScore = 42,
                format = FingerprintTemplateFormat.ISO_19794_2
            ).hashCode()
        )
    }

    @Test
    fun testFaceTemplateCaptureResultEquals() {
        val result = FaceTemplateCaptureResult(
            template = byteArrayOf(3, 4),
            format = "format 1"
        )

        assertThat(result).isEqualTo(
            FaceTemplateCaptureResult(
                template = byteArrayOf(3, 4),
                format = "format 1"
            )
        )
        assertThat(result).isNotEqualTo(
            FaceTemplateCaptureResult(
                template = byteArrayOf(3, 4, 5),
                format = "format 1"
            )
        )
        assertThat(result).isNotEqualTo(
            FaceTemplateCaptureResult(
                template = byteArrayOf(3, 4),
                format = "format 2"
            )
        )
    }

    @Test
    fun testFaceTemplateCaptureResultHashCode() {
        assertThat(
            FaceTemplateCaptureResult(
                template = byteArrayOf(3, 4),
                format = "format 1"
            ).hashCode()
        ).isEqualTo(
            FaceTemplateCaptureResult(
                template = byteArrayOf(3, 4),
                format = "format 1"
            ).hashCode()
        )
        assertThat(
            FaceTemplateCaptureResult(
                template = byteArrayOf(3, 4),
                format = "format 1"
            ).hashCode()
        ).isNotEqualTo(
            FaceTemplateCaptureResult(
                template = byteArrayOf(3, 4),
                format = "format 2"
            ).hashCode()
        )
    }
}
