package com.simprints.fingerprint.infra.biosdkimpl.matching

import com.google.common.truth.Truth
import com.simprints.fingerprint.infra.basebiosdk.matching.SimAfisMatcher
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
                    IsoFingerprintTemplateGenerator.generate(1),
                    "ISO_19794_2"
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
        mockkStatic("com.simprints.fingerprint.infra.biosdkimpl.matching.CrossMatchingKt")

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
                    FingerIdentifier.LEFT_THUMB, IsoFingerprintTemplateGenerator.generate(1), "ISO_19794_2"
                ),
                Fingerprint(
                    FingerIdentifier.RIGHT_THUMB, IsoFingerprintTemplateGenerator.generate(1), "ISO_19794_2"
                )
            )
        )
        //When
        val result = crossFingerMatching(probe, candidate, jniLibAfis).score
        //Then
        verify(exactly = 0) { jniLibAfis.verify(any(), any()) }
        Truth.assertThat(result).isEqualTo(0)
    }


}
