package com.simprints.fingerprintmatcher.algorithms.simafis

import com.google.common.truth.Truth
import com.simprints.fingerprintmatcher.algorithms.simafis.models.SimAfisFingerIdentifier
import com.simprints.fingerprintmatcher.algorithms.simafis.models.SimAfisFingerprint
import com.simprints.fingerprintmatcher.domain.FingerIdentifier
import com.simprints.fingerprintmatcher.domain.Fingerprint
import com.simprints.fingerprintmatcher.domain.FingerprintIdentity
import com.simprints.fingerprintmatcher.domain.TemplateFormat
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
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
    fun `test same finger match`() = runBlocking {
        every { jniLibAfis.identify(any(), any(), 1) } returns floatArrayOf(1F)
        val candidate = FingerprintIdentity(
            "candidate",
            listOf(

                Fingerprint(
                    FingerIdentifier.RIGHT_THUMB,
                    mockTemplate(),
                    TemplateFormat.ISO_19794_2_2011
                )
            )
        )
        //When
        val result = simAfisMatcher.match(candidate, listOf(candidate), false).last()
        //Then
        verify { jniLibAfis.identify(any(), any(), any()) }
        Truth.assertThat(result.score).isEqualTo(1)
    }

    @Test
    fun `test cross finger match`() = runBlocking {
        mockkStatic("com.simprints.fingerprintmatcher.algorithms.simafis.CrossMatchingKt")

        val probe = mockk<FingerprintIdentity>()
        val candidate1 = mockk<FingerprintIdentity>()
        val candidate2 = mockk<FingerprintIdentity>()
        val template1 = mockk<ByteBuffer>()
        val template2 = mockk<ByteBuffer>()
        val template3 = mockk<ByteBuffer>()
        every { probe.fingerprintsTemplates } returns listOf(template1, template2)

        every { candidate1.fingerprintsTemplates } returns listOf(template2, template1)
        every { candidate2.fingerprintsTemplates } returns listOf(template3, template1)

        every { candidate1.id } returns "candidate1"
        every { candidate2.id } returns "candidate2"

        every { jniLibAfis.verify(template1, template1) } returns 1F
        every { jniLibAfis.verify(template2, template2) } returns 1F
        every { jniLibAfis.verify(template1, template2) } returns 0F
        every { jniLibAfis.verify(template2, template1) } returns 0F
        every { jniLibAfis.verify(template1, template3) } returns 0F
        every { jniLibAfis.verify(template2, template3) } returns 0F

        //When
        val matchingResult = simAfisMatcher.match(
            probe, listOf(candidate1, candidate2),
            true
        )
        val maxScore = matchingResult.maxOf { it.score }
        val minScore = matchingResult.minOf { it.score }
        //Then
        verify(exactly = 8) { jniLibAfis.verify(any(), any()) }
        Truth.assertThat(maxScore).isEqualTo(1)
        Truth.assertThat(minScore).isEqualTo(0.5f)

    }

    @Test
    fun `test crossFingerMatching zero fingers success`() {
        //Given
        every { jniLibAfis.verify(any(), any()) } returns 1F
        val probe = FingerprintIdentity("probe", listOf())
        val candidate = FingerprintIdentity(
            "candidate",
            listOf(
                Fingerprint(
                    FingerIdentifier.LEFT_THUMB, byteArrayOf(), TemplateFormat.ISO_19794_2_2011
                ),
                Fingerprint(
                    FingerIdentifier.RIGHT_THUMB, byteArrayOf(), TemplateFormat.ISO_19794_2_2011
                )
            )
        )
        //When
        val result = crossFingerMatching(probe, candidate, jniLibAfis).score
        //Then
        verify(exactly = 0) { jniLibAfis.verify(any(), any()) }
        Truth.assertThat(result).isEqualTo(0)
    }

    private fun mockTemplate(): ByteArray =
        SimAfisFingerprint.generateRandomFingerprint(SimAfisFingerIdentifier.LEFT_THUMB).templateBytes


}
