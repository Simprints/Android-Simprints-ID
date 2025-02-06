package com.simprints.core.domain.common

import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.face.FaceSample
import com.simprints.core.domain.fingerprint.FingerprintSample
import com.simprints.core.domain.fingerprint.IFingerIdentifier
import org.junit.Test
import java.util.UUID
import kotlin.collections.listOf

class ReferenceIdsTest {
    @Test
    fun `face reference ID from arrays calculated from correctly`() {
        val templates = listOf(
            byteArrayOf(2),
            byteArrayOf(1),
            byteArrayOf(3),
        )
        val expected = UUID.nameUUIDFromBytes(byteArrayOf(1, 2, 3)).toString()

        assertThat(templates.faceReferenceId()).isEqualTo(expected)
    }

    @Test
    fun `face reference ID from samples is calculated correctly`() {
        val samples = listOf(
            FaceSample(
                template = byteArrayOf(2),
                format = "",
            ),
            FaceSample(
                template = byteArrayOf(3),
                format = "",
            ),
            FaceSample(
                template = byteArrayOf(1),
                format = "",
            ),
        )
        val expected = UUID.nameUUIDFromBytes(byteArrayOf(1, 2, 3)).toString()

        assertThat(samples.faceReferenceIdFromSamples()).isEqualTo(expected)
    }

    @Test
    fun `face reference ID returns null for empty list`() {
        assertThat(listOf<ByteArray>().faceReferenceId()).isNull()
    }

    @Test
    fun `fingerprint reference ID from arrays calculated from correctly`() {
        val samples = listOf(
            3 to byteArrayOf(31, 32),
            2 to byteArrayOf(21, 22),
            1 to byteArrayOf(1, 2),
        )
        val expected = UUID.nameUUIDFromBytes(byteArrayOf(1, 2, 21, 22, 31, 32)).toString()

        assertThat(samples.fingerprintReferenceId()).isEqualTo(expected)
    }

    @Test
    fun `fingerprint reference ID from samples calculated from correctly`() {
        val samples = listOf(
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
        val expected = UUID.nameUUIDFromBytes(byteArrayOf(1, 2, 21, 22, 31, 32)).toString()

        assertThat(samples.fingerprintReferenceIdFromSamples()).isEqualTo(expected)
    }

    @Test
    fun `fingerprint reference ID returns null for empty list`() {
        assertThat(listOf<Pair<Int, ByteArray>>().fingerprintReferenceId()).isNull()
    }
}
