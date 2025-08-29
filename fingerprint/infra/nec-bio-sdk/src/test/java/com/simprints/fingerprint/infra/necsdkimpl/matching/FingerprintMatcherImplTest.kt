package com.simprints.fingerprint.infra.necsdkimpl.matching

import com.google.common.truth.Truth
import com.simprints.core.domain.modality.Modality
import com.simprints.core.domain.sample.CaptureIdentity
import com.simprints.core.domain.sample.CaptureSample
import com.simprints.core.domain.sample.Identity
import com.simprints.core.domain.sample.Sample
import com.simprints.core.domain.sample.SampleIdentifier
import com.simprints.fingerprint.infra.basebiosdk.exceptions.BioSdkException
import com.simprints.fingerprint.infra.necsdkimpl.acquisition.template.NEC_TEMPLATE_FORMAT
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
        val probe = generatePersonCaptureIdentity(SampleIdentifier.LEFT_THUMB, SampleIdentifier.RIGHT_THUMB)
        val candidates =
            listOf(
                generatePersonIdentity(SampleIdentifier.LEFT_THUMB, SampleIdentifier.RIGHT_THUMB),
                generatePersonIdentity(SampleIdentifier.LEFT_THUMB, SampleIdentifier.RIGHT_THUMB),
                generatePersonIdentity(SampleIdentifier.LEFT_THUMB, SampleIdentifier.RIGHT_THUMB),
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
        val probe = generatePersonCaptureIdentity(SampleIdentifier.LEFT_INDEX_FINGER, SampleIdentifier.RIGHT_INDEX_FINGER)
        val candidate =
            generatePersonIdentity(SampleIdentifier.LEFT_THUMB, SampleIdentifier.RIGHT_THUMB)
        // When
        val result = matcher.match(probe, listOf(candidate), NecMatchingSettings(false))
        // Then
        Truth.assertThat(result.size).isEqualTo(1)
        Truth.assertThat(result[0].score).isEqualTo(0)
    }

    @Test
    fun `test match FingerprintIdentities  with different fingerprint IDs and crossFingerComparison`() = runTest {
        // Given
        every { nec.match(any(), any(), any()) } returns 3
        val probe = generatePersonCaptureIdentity(SampleIdentifier.LEFT_INDEX_FINGER, SampleIdentifier.RIGHT_INDEX_FINGER)
        val candidate =
            generatePersonIdentity(SampleIdentifier.LEFT_THUMB, SampleIdentifier.RIGHT_THUMB)
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
        val probe = generatePersonCaptureIdentity(SampleIdentifier.LEFT_INDEX_FINGER, SampleIdentifier.RIGHT_INDEX_FINGER)
        val candidate =
            generatePersonIdentity(SampleIdentifier.LEFT_INDEX_FINGER, SampleIdentifier.RIGHT_THUMB)
        // When
        val result = matcher.match(probe, listOf(candidate), NecMatchingSettings(false))
        // Then
        Truth.assertThat(result.size).isEqualTo(1)
        Truth.assertThat(result[0].score).isEqualTo(3)
    }

    @Test(expected = BioSdkException.TemplateMatchingException::class)
    fun `test match FingerprintIdentifies before initialize NEC`() = runTest {
        // Given
        every {
            nec.match(any(), any(), any())
        } throws NEC.AttemptedToRunBeforeInitializedException()
        val probe = generatePersonCaptureIdentity(SampleIdentifier.LEFT_INDEX_FINGER, SampleIdentifier.RIGHT_INDEX_FINGER)
        val candidate =
            generatePersonIdentity(SampleIdentifier.LEFT_INDEX_FINGER, SampleIdentifier.RIGHT_THUMB)
        // When
        matcher.match(probe, listOf(candidate), NecMatchingSettings(false))
    }

    @Test
    fun `test match FingerprintIdentities probe  with different template format`() = runTest {
        // Given
        val probe =
            generatePersonCaptureIdentity(SampleIdentifier.LEFT_INDEX_FINGER, SampleIdentifier.RIGHT_INDEX_FINGER, format = "Unsupported")
        val candidate =
            generatePersonIdentity(SampleIdentifier.LEFT_THUMB, SampleIdentifier.RIGHT_THUMB)
        // When
        val result = matcher.match(probe, listOf(candidate), NecMatchingSettings(false))
        // Then
        Truth.assertThat(result.size).isEqualTo(0)
    }

    private fun generatePersonCaptureIdentity(
        vararg fingers: SampleIdentifier,
        format: String = NEC_TEMPLATE_FORMAT,
    ) = CaptureIdentity(
        modality = Modality.FINGERPRINT,
        samples = fingers.map {
            CaptureSample(
                identifier = it,
                template = ByteArray(0),
                templateQualityScore = 1,
                format = format,
                imageRef = null,
                modality = Modality.FINGERPRINT,
            )
        },
    )

    private fun generatePersonIdentity(
        vararg fingers: SampleIdentifier,
        format: String = NEC_TEMPLATE_FORMAT,
    ) = Identity(
        subjectId = "ID",
        modality = Modality.FINGERPRINT,
        samples = fingers.map {
            Sample(identifier = it, referenceId = "", template = ByteArray(0), format = format, modality = Modality.FINGERPRINT)
        },
    )
}
