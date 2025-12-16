package com.simprints.feature.orchestrator.usecases

import com.google.common.truth.Truth.*
import com.simprints.core.domain.common.Modality
import com.simprints.core.domain.capture.BiometricReferenceCapture
import com.simprints.core.domain.capture.BiometricTemplateCapture
import com.simprints.core.domain.common.TemplateIdentifier
import com.simprints.feature.enrollast.EnrolLastBiometricResult
import com.simprints.feature.enrollast.EnrolLastBiometricStepResult
import com.simprints.infra.config.store.models.FaceConfiguration
import com.simprints.infra.config.store.models.FingerprintConfiguration
import com.simprints.infra.matching.MatchResult
import org.junit.Before
import org.junit.Test

internal class MapStepsForLastBiometricEnrolUseCaseTest {
    private lateinit var useCase: MapStepsForLastBiometricEnrolUseCase

    @Before
    fun setUp() {
        useCase = MapStepsForLastBiometricEnrolUseCase()
    }

    @Test
    fun `maps EnrolLastBiometricRequest correctly`() {
        val result = useCase(
            listOf(
                EnrolLastBiometricResult("subjectId", null),
            ),
        )

        assertThat(result.first()).isEqualTo(EnrolLastBiometricStepResult.EnrolLastBiometricsResult("subjectId"))
    }

    @Test
    fun `maps FaceMatchResult correctly`() {
        val result = useCase(
            listOf(
                MatchResult(emptyList(), FaceConfiguration.BioSdk.RANK_ONE),
            ),
        )

        assertThat(result.first()).isEqualTo(EnrolLastBiometricStepResult.MatchResult(emptyList(), FaceConfiguration.BioSdk.RANK_ONE))
    }

    @Test
    fun `maps face BiometricReferenceCapture correctly`() {
        val result = useCase(
            listOf(
                BiometricReferenceCapture(
                    "referenceId",
                    modality = Modality.FACE,
                    format = "format",
                    templates = listOf(
                        BiometricTemplateCapture(
                            captureEventId = "captureId",
                            template = byteArrayOf(),
                        ),
                    ),
                ),
            ),
        )

        assertThat(result.first()).isEqualTo(
            EnrolLastBiometricStepResult.CaptureResult(
                result = BiometricReferenceCapture(
                    referenceId = "referenceId",
                    modality = Modality.FACE,
                    format = "format",
                    templates = listOf(
                        BiometricTemplateCapture(
                            captureEventId = "captureId",
                            template = byteArrayOf(),
                        ),
                    ),
                ),
            ),
        )
    }

    @Test
    fun `maps FingerprintMatchResult correctly`() {
        val result = useCase(
            listOf(
                MatchResult(emptyList(), FingerprintConfiguration.BioSdk.NEC),
            ),
        )

        assertThat(
            result.first(),
        ).isEqualTo(EnrolLastBiometricStepResult.MatchResult(emptyList(), FingerprintConfiguration.BioSdk.NEC))
    }

    @Test
    fun `maps fingerprint CaptureIdentity correctly`() {
        val result = useCase(
            listOf(
                BiometricReferenceCapture(
                    "referenceId",
                    modality = Modality.FINGERPRINT,
                    format = "format",
                    templates = listOf(
                        BiometricTemplateCapture(
                            captureEventId = "captureId",
                            template = byteArrayOf(),
                            identifier = TemplateIdentifier.RIGHT_THUMB,
                        ),
                    ),
                ),
            ),
        )

        assertThat(result.first()).isEqualTo(
            EnrolLastBiometricStepResult.CaptureResult(
                result = BiometricReferenceCapture(
                    "referenceId",
                    modality = Modality.FINGERPRINT,
                    format = "format",
                    templates = listOf(
                        BiometricTemplateCapture(
                            captureEventId = "captureId",
                            template = byteArrayOf(),
                            identifier = TemplateIdentifier.RIGHT_THUMB,
                        ),
                    ),
                ),
            ),
        )
    }
}
