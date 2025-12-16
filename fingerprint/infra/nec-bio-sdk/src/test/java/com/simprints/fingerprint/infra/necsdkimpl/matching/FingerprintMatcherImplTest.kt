package com.simprints.fingerprint.infra.necsdkimpl.matching

import com.google.common.truth.*
import com.simprints.core.domain.common.Modality
import com.simprints.core.domain.reference.BiometricReference
import com.simprints.core.domain.reference.BiometricReferenceCapture
import com.simprints.core.domain.reference.BiometricTemplate
import com.simprints.core.domain.reference.BiometricTemplateCapture
import com.simprints.core.domain.reference.TemplateIdentifier
import com.simprints.core.domain.sample.Identity
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
        val probe = generateProbe(TemplateIdentifier.LEFT_THUMB, TemplateIdentifier.RIGHT_THUMB)
        val candidates = listOf(
            generateIdentity(TemplateIdentifier.LEFT_THUMB, TemplateIdentifier.RIGHT_THUMB),
            generateIdentity(TemplateIdentifier.LEFT_THUMB, TemplateIdentifier.RIGHT_THUMB),
            generateIdentity(TemplateIdentifier.LEFT_THUMB, TemplateIdentifier.RIGHT_THUMB),
        )
        // When
        val result = matcher.match(probe, candidates, NecMatchingSettings(false))
        // Then
        Truth.assertThat(result.size).isEqualTo(3)
        Truth.assertThat(result[0].comparisonScore).isEqualTo(3)
    }

    @Test
    fun `test match FingerprintIdentities  with different fingerprint IDs`() = runTest {
        // Given
        every { nec.match(any(), any(), any()) } returns 3
        val probe = generateProbe(TemplateIdentifier.LEFT_INDEX_FINGER, TemplateIdentifier.RIGHT_INDEX_FINGER)
        val candidate = generateIdentity(TemplateIdentifier.LEFT_THUMB, TemplateIdentifier.RIGHT_THUMB)
        // When
        val result = matcher.match(probe, listOf(candidate), NecMatchingSettings(false))
        // Then
        Truth.assertThat(result.size).isEqualTo(1)
        Truth.assertThat(result[0].comparisonScore).isEqualTo(0)
    }

    @Test
    fun `test match FingerprintIdentities  with different fingerprint IDs and crossFingerComparison`() = runTest {
        // Given
        every { nec.match(any(), any(), any()) } returns 3
        val probe = generateProbe(TemplateIdentifier.LEFT_INDEX_FINGER, TemplateIdentifier.RIGHT_INDEX_FINGER)
        val candidate = generateIdentity(TemplateIdentifier.LEFT_THUMB, TemplateIdentifier.RIGHT_THUMB)
        // When
        val result = matcher.match(probe, listOf(candidate), NecMatchingSettings(true))
        // Then
        Truth.assertThat(result.size).isEqualTo(1)
        Truth.assertThat(result[0].comparisonScore).isEqualTo(3)
    }

    @Test
    fun `test match FingerprintIdentities  with only one equal fingerprint IDs`() = runTest {
        // Given
        every { nec.match(any(), any(), any()) } returns 3
        val probe = generateProbe(TemplateIdentifier.LEFT_INDEX_FINGER, TemplateIdentifier.RIGHT_INDEX_FINGER)
        val candidate = generateIdentity(TemplateIdentifier.LEFT_INDEX_FINGER, TemplateIdentifier.RIGHT_THUMB)
        // When
        val result = matcher.match(probe, listOf(candidate), NecMatchingSettings(false))
        // Then
        Truth.assertThat(result.size).isEqualTo(1)
        Truth.assertThat(result[0].comparisonScore).isEqualTo(3)
    }

    @Test(expected = BioSdkException.TemplateMatchingException::class)
    fun `test match FingerprintIdentifies before initialize NEC`() = runTest {
        // Given
        every {
            nec.match(any(), any(), any())
        } throws NEC.AttemptedToRunBeforeInitializedException()
        val probe = generateProbe(
            TemplateIdentifier.LEFT_INDEX_FINGER,
            TemplateIdentifier.RIGHT_INDEX_FINGER,
        )
        val candidate = generateIdentity(TemplateIdentifier.LEFT_INDEX_FINGER, TemplateIdentifier.RIGHT_THUMB)
        // When
        matcher.match(probe, listOf(candidate), NecMatchingSettings(false))
    }

    @Test
    fun `test match FingerprintIdentities probe  with different template format`() = runTest {
        // Given
        val probe =
            generateProbe(
                TemplateIdentifier.LEFT_INDEX_FINGER,
                TemplateIdentifier.RIGHT_INDEX_FINGER,
                format = "Unsupported",
            )
        val candidate =
            generateIdentity(TemplateIdentifier.LEFT_THUMB, TemplateIdentifier.RIGHT_THUMB)
        // When
        val result = matcher.match(probe, listOf(candidate), NecMatchingSettings(false))
        // Then
        Truth.assertThat(result.size).isEqualTo(0)
    }

    private fun generateProbe(
        vararg fingers: TemplateIdentifier,
        format: String = NEC_TEMPLATE_FORMAT,
    ) = BiometricReferenceCapture(
        referenceId = "referenceId",
        modality = Modality.FINGERPRINT,
        format = format,
        templates = fingers.map {
            BiometricTemplateCapture(
                captureEventId = it.name,
                template = ByteArray(0),
                identifier = it,
            )
        },
    )

    private fun generateIdentity(
        vararg fingers: TemplateIdentifier,
        format: String = NEC_TEMPLATE_FORMAT,
    ) = Identity(
        subjectId = "id",
        references = fingers.map {
            BiometricReference(
                referenceId = it.name,
                modality = Modality.FINGERPRINT,
                format = format,
                templates = listOf(
                    BiometricTemplate(
                        template = ByteArray(0),
                        identifier = it,
                    ),
                ),
            )
        },
    )
}
