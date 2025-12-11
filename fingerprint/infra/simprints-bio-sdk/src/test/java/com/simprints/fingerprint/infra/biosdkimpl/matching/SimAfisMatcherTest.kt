package com.simprints.fingerprint.infra.biosdkimpl.matching

import com.google.common.truth.Truth.*
import com.simprints.core.domain.common.Modality
import com.simprints.core.domain.reference.BiometricReferenceCapture
import com.simprints.core.domain.reference.BiometricTemplate
import com.simprints.core.domain.reference.BiometricTemplateCapture
import com.simprints.core.domain.reference.TemplateIdentifier
import com.simprints.core.domain.sample.Identity
import com.simprints.core.domain.sample.Sample
import com.simprints.fingerprint.infra.scanner.v2.tools.primitives.byteArrayOf
import com.simprints.fingerprint.infra.simafiswrapper.JNILibAfisInterface
import io.mockk.*
import io.mockk.impl.annotations.MockK
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

        val probes = BiometricReferenceCapture(
            referenceId = "referenceId",
            format = SIMAFIS_MATCHER_SUPPORTED_TEMPLATE_FORMAT,
            modality = Modality.FINGERPRINT,
            templates = listOf(
                BiometricTemplateCapture(
                    captureEventId = "referenceId",
                    template = BiometricTemplate(
                        identifier = TemplateIdentifier.RIGHT_THUMB,
                        template = IsoFingerprintTemplateGenerator.generate(1),
                    ),
                ),
            ),
        )
        val candidate = Identity(
            "candidate",
            listOf(
                Sample(
                    referenceId = "referenceId",
                    template = BiometricTemplate(
                        template = IsoFingerprintTemplateGenerator.generate(1),
                        identifier = TemplateIdentifier.RIGHT_THUMB,
                    ),
                    format = SIMAFIS_MATCHER_SUPPORTED_TEMPLATE_FORMAT,
                    modality = Modality.FINGERPRINT,
                ),
            ),
        )
        // When
        val result = simAfisMatcher.match(probes, listOf(candidate), false).last()
        // Then
        verify { jniLibAfis.identify(any(), any(), any()) }
        assertThat(result.confidence).isEqualTo(1)
    }

    @Test
    fun `test matching probe with other template format ignore candidate`() = runTest {
        every { jniLibAfis.identify(any(), any(), 1) } returns floatArrayOf(1F)
        val probes = BiometricReferenceCapture(
            referenceId = "referenceId",
            format = "NEC_1",
            modality = Modality.FINGERPRINT,
            templates = listOf(
                BiometricTemplateCapture(
                    captureEventId = "referenceId",
                    template = BiometricTemplate(
                        identifier = TemplateIdentifier.RIGHT_3RD_FINGER,
                        template = IsoFingerprintTemplateGenerator.generate(1),
                    ),
                ),
            ),
        )
        val candidate = Identity(
            "candidate",
            listOf(
                Sample(
                    referenceId = "referenceId",
                    template = BiometricTemplate(
                        template = IsoFingerprintTemplateGenerator.generate(1),
                        identifier = TemplateIdentifier.RIGHT_3RD_FINGER,
                    ),
                    format = "NEC_1",
                    modality = Modality.FINGERPRINT,
                ),
            ),
        )
        // When
        val result = simAfisMatcher.match(probes, listOf(candidate), false)
        // Then
        assertThat(result).isEmpty()
    }

    @Test
    fun `test cross finger match`() = runTest {
        val template1 = byteArrayOf(1)
        val template2 = byteArrayOf(2)
        val template3 = byteArrayOf(3)

        val probe = BiometricReferenceCapture(
            referenceId = "referenceId",
            format = SIMAFIS_MATCHER_SUPPORTED_TEMPLATE_FORMAT,
            modality = Modality.FINGERPRINT,
            templates = listOf(
                BiometricTemplateCapture(
                    captureEventId = "referenceId",
                    template = BiometricTemplate(
                        identifier = TemplateIdentifier.RIGHT_THUMB,
                        template = template1,
                    ),
                ),
                BiometricTemplateCapture(
                    captureEventId = "referenceId",
                    template = BiometricTemplate(
                        identifier = TemplateIdentifier.LEFT_THUMB,
                        template = template2,
                    ),
                ),
            ),
        )

        val candidate1 = Identity(
            subjectId = "candidate1",
            samples = listOf(
                Sample(
                    referenceId = "referenceId",
                    template = BiometricTemplate(
                        identifier = TemplateIdentifier.LEFT_4TH_FINGER,
                        template = template2,
                    ),
                    format = SIMAFIS_MATCHER_SUPPORTED_TEMPLATE_FORMAT,
                    modality = Modality.FINGERPRINT,
                ),
                Sample(
                    referenceId = "referenceId",
                    template = BiometricTemplate(
                        identifier = TemplateIdentifier.LEFT_5TH_FINGER,
                        template = template1,
                    ),
                    format = SIMAFIS_MATCHER_SUPPORTED_TEMPLATE_FORMAT,
                    modality = Modality.FINGERPRINT,
                ),
            ),
        )
        val candidate2 = Identity(
            subjectId = "candidate2",
            samples = listOf(
                Sample(
                    referenceId = "referenceId",
                    template = BiometricTemplate(
                        identifier = TemplateIdentifier.RIGHT_3RD_FINGER,
                        template = template3,
                    ),
                    format = SIMAFIS_MATCHER_SUPPORTED_TEMPLATE_FORMAT,
                    modality = Modality.FINGERPRINT,
                ),
                Sample(
                    referenceId = "referenceId",
                    template = BiometricTemplate(
                        identifier = TemplateIdentifier.RIGHT_5TH_FINGER,
                        template = template1,
                    ),
                    format = SIMAFIS_MATCHER_SUPPORTED_TEMPLATE_FORMAT,
                    modality = Modality.FINGERPRINT,
                ),
            ),
        )

        every { jniLibAfis.verify(any(), any()) } answers {
            if (firstArg<ByteBuffer>().get(0) == secondArg<ByteBuffer>().get(0)) 1F else 0F
        }
        // When
        val matchingResult = simAfisMatcher.match(
            probe,
            listOf(candidate1, candidate2),
            true,
        )
        val maxScore = matchingResult.maxOf { it.confidence }
        val minScore = matchingResult.minOf { it.confidence }
        // Then
        verify(exactly = 8) { jniLibAfis.verify(any(), any()) }
        assertThat(maxScore).isEqualTo(1)
        assertThat(minScore).isEqualTo(0.5f)
    }

    @Test
    fun `test crossFingerMatching zero fingers success`() {
        // Given
        every { jniLibAfis.verify(any(), any()) } returns 1F
        val probes = BiometricReferenceCapture(
            referenceId = "referenceId",
            format = SIMAFIS_MATCHER_SUPPORTED_TEMPLATE_FORMAT,
            modality = Modality.FINGERPRINT,
            templates = emptyList(),
        )
        val candidate = Identity(
            "candidate",
            listOf(
                Sample(
                    referenceId = "referenceId",
                    template = BiometricTemplate(
                        identifier = TemplateIdentifier.LEFT_THUMB,
                        template = IsoFingerprintTemplateGenerator.generate(1),
                    ),
                    format = SIMAFIS_MATCHER_SUPPORTED_TEMPLATE_FORMAT,
                    modality = Modality.FINGERPRINT,
                ),
                Sample(
                    referenceId = "referenceId",
                    template = BiometricTemplate(
                        identifier = TemplateIdentifier.LEFT_3RD_FINGER,
                        template = IsoFingerprintTemplateGenerator.generate(1),
                    ),
                    format = SIMAFIS_MATCHER_SUPPORTED_TEMPLATE_FORMAT,
                    modality = Modality.FINGERPRINT,
                ),
            ),
        )

        // When
        val result = simAfisMatcher.match(probes, listOf(candidate), true)
        // Then
        verify(exactly = 0) { jniLibAfis.verify(any(), any()) }
        assertThat(result[0].confidence).isEqualTo(0)
    }

    companion object {
        const val SIMAFIS_MATCHER_SUPPORTED_TEMPLATE_FORMAT = "ISO_19794_2"
    }
}
