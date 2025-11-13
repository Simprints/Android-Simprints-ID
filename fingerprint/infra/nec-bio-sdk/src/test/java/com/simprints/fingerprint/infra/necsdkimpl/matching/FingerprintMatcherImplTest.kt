package com.simprints.fingerprint.infra.necsdkimpl.matching

import com.google.common.truth.*
import com.simprints.core.domain.common.Modality
import com.simprints.core.domain.sample.CaptureSample
import com.simprints.core.domain.sample.Identity
import com.simprints.core.domain.sample.Sample
import com.simprints.core.domain.sample.SampleIdentifier
import com.simprints.fingerprint.infra.basebiosdk.exceptions.BioSdkException
import com.simprints.fingerprint.infra.necsdkimpl.acquisition.template.NEC_TEMPLATE_FORMAT
import com.simprints.necwrapper.nec.NEC
import io.mockk.*
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
        val probe = generateProbe(SampleIdentifier.LEFT_THUMB, SampleIdentifier.RIGHT_THUMB)
        val candidates = listOf(
            generateIdentity(SampleIdentifier.LEFT_THUMB, SampleIdentifier.RIGHT_THUMB),
            generateIdentity(SampleIdentifier.LEFT_THUMB, SampleIdentifier.RIGHT_THUMB),
            generateIdentity(SampleIdentifier.LEFT_THUMB, SampleIdentifier.RIGHT_THUMB),
        )
        // When
        val result = matcher.match(probe, candidates, NecMatchingSettings(false))
        // Then
        Truth.assertThat(result.size).isEqualTo(3)
        Truth.assertThat(result[0].confidence).isEqualTo(3)
    }

    @Test
    fun `test match FingerprintIdentities  with different fingerprint IDs`() = runTest {
        // Given
        every { nec.match(any(), any(), any()) } returns 3
        val probe = generateProbe(SampleIdentifier.LEFT_INDEX_FINGER, SampleIdentifier.RIGHT_INDEX_FINGER)
        val candidate = generateIdentity(SampleIdentifier.LEFT_THUMB, SampleIdentifier.RIGHT_THUMB)
        // When
        val result = matcher.match(probe, listOf(candidate), NecMatchingSettings(false))
        // Then
        Truth.assertThat(result.size).isEqualTo(1)
        Truth.assertThat(result[0].confidence).isEqualTo(0)
    }

    @Test
    fun `test match FingerprintIdentities  with different fingerprint IDs and crossFingerComparison`() = runTest {
        // Given
        every { nec.match(any(), any(), any()) } returns 3
        val probe = generateProbe(SampleIdentifier.LEFT_INDEX_FINGER, SampleIdentifier.RIGHT_INDEX_FINGER)
        val candidate = generateIdentity(SampleIdentifier.LEFT_THUMB, SampleIdentifier.RIGHT_THUMB)
        // When
        val result = matcher.match(probe, listOf(candidate), NecMatchingSettings(true))
        // Then
        Truth.assertThat(result.size).isEqualTo(1)
        Truth.assertThat(result[0].confidence).isEqualTo(3)
    }

    @Test
    fun `test match FingerprintIdentities  with only one equal fingerprint IDs`() = runTest {
        // Given
        every { nec.match(any(), any(), any()) } returns 3
        val probe = generateProbe(SampleIdentifier.LEFT_INDEX_FINGER, SampleIdentifier.RIGHT_INDEX_FINGER)
        val candidate = generateIdentity(SampleIdentifier.LEFT_INDEX_FINGER, SampleIdentifier.RIGHT_THUMB)
        // When
        val result = matcher.match(probe, listOf(candidate), NecMatchingSettings(false))
        // Then
        Truth.assertThat(result.size).isEqualTo(1)
        Truth.assertThat(result[0].confidence).isEqualTo(3)
    }

    @Test(expected = BioSdkException.TemplateMatchingException::class)
    fun `test match FingerprintIdentifies before initialize NEC`() = runTest {
        // Given
        every {
            nec.match(any(), any(), any())
        } throws NEC.AttemptedToRunBeforeInitializedException()
        val probe = generateProbe(
            SampleIdentifier.LEFT_INDEX_FINGER,
            SampleIdentifier.RIGHT_INDEX_FINGER,
        )
        val candidate = generateIdentity(SampleIdentifier.LEFT_INDEX_FINGER, SampleIdentifier.RIGHT_THUMB)
        // When
        matcher.match(probe, listOf(candidate), NecMatchingSettings(false))
    }

    @Test
    fun `test match FingerprintIdentities probe  with different template format`() = runTest {
        // Given
        val probe =
            generateProbe(
                SampleIdentifier.LEFT_INDEX_FINGER,
                SampleIdentifier.RIGHT_INDEX_FINGER,
                format = "Unsupported",
            )
        val candidate =
            generateIdentity(SampleIdentifier.LEFT_THUMB, SampleIdentifier.RIGHT_THUMB)
        // When
        val result = matcher.match(probe, listOf(candidate), NecMatchingSettings(false))
        // Then
        Truth.assertThat(result.size).isEqualTo(0)
    }

    private fun generateProbe(
        vararg fingers: SampleIdentifier,
        format: String = NEC_TEMPLATE_FORMAT,
    ) = fingers.map {
        CaptureSample(
            captureEventId = it.name,
            identifier = it,
            modality = Modality.FINGERPRINT,
            format = format,
            template = ByteArray(0),
        )
    }

    private fun generateIdentity(
        vararg fingers: SampleIdentifier,
        format: String = NEC_TEMPLATE_FORMAT,
    ) = Identity(
        subjectId = "id",
        samples = fingers.map {
            Sample(
                referenceId = it.name,
                identifier = it,
                modality = Modality.FINGERPRINT,
                format = format,
                template = ByteArray(0),
            )
        },
    )
}
