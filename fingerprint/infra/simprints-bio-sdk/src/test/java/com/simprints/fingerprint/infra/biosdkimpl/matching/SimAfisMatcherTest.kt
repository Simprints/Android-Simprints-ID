package com.simprints.fingerprint.infra.biosdkimpl.matching

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.infra.basebiosdk.matching.domain.FingerIdentifier
import com.simprints.fingerprint.infra.basebiosdk.matching.domain.Fingerprint
import com.simprints.fingerprint.infra.basebiosdk.matching.domain.FingerprintIdentity
import com.simprints.fingerprint.infra.simafiswrapper.JNILibAfisInterface
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.nio.ByteBuffer

class SimAfisMatcherTest {
    @MockK
    lateinit var jniLibAfis: JNILibAfisInterface

    private lateinit var simAfisMatcher: SimAfisMatcher

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { jniLibAfis.getNbCores() } returns 1
        simAfisMatcher = SimAfisMatcher(jniLibAfis)
    }

    @Test
    fun `test same finger match`() = runTest {
        every { jniLibAfis.identify(any(), any(), 1) } returns floatArrayOf(1F)
        val candidate = FingerprintIdentity(
            "candidate",
            listOf(
                Fingerprint(
                    FingerIdentifier.RIGHT_THUMB,
                    IsoFingerprintTemplateGenerator.generate(1),
                    SIMAFIS_MATCHER_SUPPORTED_TEMPLATE_FORMAT,
                ),
            ),
        )
        // When
        val result = simAfisMatcher.match(candidate, listOf(candidate), false).last()
        // Then
        verify { jniLibAfis.identify(any(), any(), any()) }
        assertThat(result.score).isEqualTo(1)
    }

    @Test
    fun `test matching probe with other template format ignore candidate`() = runTest {
        every { jniLibAfis.identify(any(), any(), 1) } returns floatArrayOf(1F)
        val candidate = FingerprintIdentity(
            "candidate",
            listOf(
                Fingerprint(
                    FingerIdentifier.RIGHT_3RD_FINGER,
                    IsoFingerprintTemplateGenerator.generate(1),
                    "NEC_1",
                ),
            ),
        )
        // When
        val result = simAfisMatcher.match(candidate, listOf(candidate), false)
        // Then
        assertThat(result).isEmpty()
    }

    @Test
    fun `test cross finger match`() = runTest {
        mockkStatic("com.simprints.fingerprint.infra.biosdkimpl.matching.SimAfisMatcherKt")
        val template1 = mockk<ByteBuffer>()
        val template2 = mockk<ByteBuffer>()
        val template3 = mockk<ByteBuffer>()

        val probe = mockk<FingerprintIdentity> {
            every { templateFormatNotSupportedBySimAfisMatcher() } returns false
            every { fingerprintsTemplates } returns listOf(template1, template2)
        }
        val candidate1 = mockk<FingerprintIdentity> {
            every { subjectId } returns "candidate1"
            every { templateFormatNotSupportedBySimAfisMatcher() } returns false
            every { fingerprintsTemplates } returns listOf(template2, template1)
        }
        val candidate2 = mockk<FingerprintIdentity> {
            every { subjectId } returns "candidate2"
            every { templateFormatNotSupportedBySimAfisMatcher() } returns false
            every { fingerprintsTemplates } returns listOf(template3, template1)
        }

        every { jniLibAfis.verify(any(), any()) } answers {
            if (firstArg<ByteBuffer>() === secondArg<ByteBuffer>()) 1F else 0F
        }
        // When
        val matchingResult = simAfisMatcher.match(
            probe,
            listOf(candidate1, candidate2),
            true,
        )
        val maxScore = matchingResult.maxOf { it.score }
        val minScore = matchingResult.minOf { it.score }
        // Then
        verify(exactly = 8) { jniLibAfis.verify(any(), any()) }
        assertThat(maxScore).isEqualTo(1)
        assertThat(minScore).isEqualTo(0.5f)
    }

    @Test
    fun `test crossFingerMatching zero fingers success`() {
        // Given
        every { jniLibAfis.verify(any(), any()) } returns 1F
        val probe = FingerprintIdentity("probe", listOf())
        val candidate = FingerprintIdentity(
            "candidate",
            listOf(
                Fingerprint(
                    FingerIdentifier.LEFT_THUMB,
                    IsoFingerprintTemplateGenerator.generate(1),
                    SIMAFIS_MATCHER_SUPPORTED_TEMPLATE_FORMAT,
                ),
                Fingerprint(
                    FingerIdentifier.LEFT_3RD_FINGER,
                    IsoFingerprintTemplateGenerator.generate(1),
                    SIMAFIS_MATCHER_SUPPORTED_TEMPLATE_FORMAT,
                ),
            ),
        )
        // When
        val result = simAfisMatcher.match(probe, listOf(candidate), true)
        // Then
        verify(exactly = 0) { jniLibAfis.verify(any(), any()) }
        assertThat(result[0].score).isEqualTo(0)
    }

    companion object {
        const val SIMAFIS_MATCHER_SUPPORTED_TEMPLATE_FORMAT = "ISO_19794_2"
    }
}
