package com.simprints.core.domain.fingerprint

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class FingerprintSampleTest {
    @Test
    fun testUniqueId() {
        assertThat(listOf<FingerprintSample>().uniqueId()).isNull()
        assertThat(
            listOf(
                FingerprintSample(
                    fingerIdentifier = IFingerIdentifier.LEFT_3RD_FINGER,
                    template = byteArrayOf(1, 2),
                    templateQualityScore = 1,
                    format = "",
                ),
            ).uniqueId(),
        ).isNotNull()
    }

    @Test
    fun testConcatTemplates() {
        val fingerprintSample = listOf(
            FingerprintSample(
                fingerIdentifier = IFingerIdentifier.LEFT_3RD_FINGER,
                template = byteArrayOf(31, 32),
                templateQualityScore = 3,
                format = "",
            ),
            FingerprintSample(
                fingerIdentifier = IFingerIdentifier.LEFT_3RD_FINGER,
                template = byteArrayOf(1, 2),
                templateQualityScore = 1,
                format = "",
            ),
            FingerprintSample(
                fingerIdentifier = IFingerIdentifier.LEFT_3RD_FINGER,
                template = byteArrayOf(21, 22),
                templateQualityScore = 2,
                format = "",
            ),
        )
        assertThat(fingerprintSample.concatTemplates()).isEqualTo(byteArrayOf(1, 2, 21, 22, 31, 32))
    }
}
