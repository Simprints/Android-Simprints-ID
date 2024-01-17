package com.simprints.fingerprint.infra.necsdkimpl.matching

import com.google.common.truth.Truth
import com.simprints.fingerprint.infra.basebiosdk.matching.domain.FingerIdentifier
import com.simprints.fingerprint.infra.basebiosdk.matching.domain.Fingerprint
import com.simprints.fingerprint.infra.basebiosdk.matching.domain.FingerprintIdentity
import com.simprints.necwrapper.nec.NEC
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class FingerprintMatcherImplTest {

    @MockK
    private lateinit var nec: NEC

    private lateinit var matcher: FingerprintMatcherImpl

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        matcher = FingerprintMatcherImpl(nec)
    }

    @Test
    fun `test match FingerprintIdentities  with the same fingerprint IDs`() = runTest {
        // Given
        every { nec.match(any(), any(), any()) } returns 3
        val probe =
            generatePersonIdentity(FingerIdentifier.LEFT_THUMB, FingerIdentifier.RIGHT_THUMB)
        val candidates =
            listOf(
                generatePersonIdentity(FingerIdentifier.LEFT_THUMB, FingerIdentifier.RIGHT_THUMB),
                generatePersonIdentity(FingerIdentifier.LEFT_THUMB, FingerIdentifier.RIGHT_THUMB),
                generatePersonIdentity(FingerIdentifier.LEFT_THUMB, FingerIdentifier.RIGHT_THUMB)
            )
        // When
        val result = matcher.match(probe, candidates, NecMatchingSettings(false))
        // Then
        Truth.assertThat(result.size).isEqualTo(3)
        Truth.assertThat(result[0].score).isEqualTo(3)
    }

    @Test
    fun `test match FingerprintIdentities  with different fingerprint IDs`() = runTest {
        // Given
        every { nec.match(any(), any(), any()) } returns 3
        val probe =
            generatePersonIdentity(
                FingerIdentifier.LEFT_INDEX_FINGER,
                FingerIdentifier.RIGHT_INDEX_FINGER
            )
        val candidate =
            generatePersonIdentity(FingerIdentifier.LEFT_THUMB, FingerIdentifier.RIGHT_THUMB)
        // When
        val result = matcher.match(probe, listOf(candidate), NecMatchingSettings(false))
        // Then
        Truth.assertThat(result.size).isEqualTo(1)
        Truth.assertThat(result[0].score).isEqualTo(0)
    }

    @Test
    fun `test match FingerprintIdentities  with different fingerprint IDs and crossFingerComparison`() =
        runTest {
            // Given
            every { nec.match(any(), any(), any()) } returns 3
            val probe =
                generatePersonIdentity(
                    FingerIdentifier.LEFT_INDEX_FINGER,
                    FingerIdentifier.RIGHT_INDEX_FINGER
                )
            val candidate =
                generatePersonIdentity(FingerIdentifier.LEFT_THUMB, FingerIdentifier.RIGHT_THUMB)
            // When
            val result = matcher.match(probe, listOf(candidate), NecMatchingSettings(true))
            // Then
            Truth.assertThat(result.size).isEqualTo(1)
            Truth.assertThat(result[0].score).isEqualTo(3)
        }

    @Test
    fun `test match FingerprintIdentities  with only one equal fingerprint IDs`() = runTest {
        // Given
        every { nec.match(any(), any(), any()) } returns 3
        val probe =
            generatePersonIdentity(
                FingerIdentifier.LEFT_INDEX_FINGER,
                FingerIdentifier.RIGHT_INDEX_FINGER
            )
        val candidate =
            generatePersonIdentity(FingerIdentifier.LEFT_INDEX_FINGER, FingerIdentifier.RIGHT_THUMB)
        // When
        val result = matcher.match(probe, listOf(candidate), NecMatchingSettings(false))
        // Then
        Truth.assertThat(result.size).isEqualTo(1)
        Truth.assertThat(result[0].score).isEqualTo(3)
    }

    @Test(expected = NEC.AttemptedToRunBeforeInitializedException::class)
    fun `test match FingerprintIdentifies before initialize NEC`() = runTest {
        // Given
        every {
            nec.match(any(), any(), any())
        } throws NEC.AttemptedToRunBeforeInitializedException()
        val probe =
            generatePersonIdentity(
                FingerIdentifier.LEFT_INDEX_FINGER,
                FingerIdentifier.RIGHT_INDEX_FINGER
            )
        val candidate =
            generatePersonIdentity(FingerIdentifier.LEFT_INDEX_FINGER, FingerIdentifier.RIGHT_THUMB)
        // When
       matcher.match(probe, listOf(candidate), NecMatchingSettings(false))

    }

    private fun generatePersonIdentity(vararg fingers: FingerIdentifier) = FingerprintIdentity(
        "ID",
        fingers.map {
            Fingerprint(it, ByteArray(0), "NEC_1")
        }
    )

}

