package com.simprints.feature.orchestrator.usecases

import com.google.common.truth.Truth.*
import com.simprints.core.domain.capture.BiometricReferenceCapture
import com.simprints.core.domain.capture.BiometricTemplateCapture
import com.simprints.core.domain.common.Modality
import com.simprints.core.domain.common.TemplateIdentifier
import com.simprints.core.domain.comparison.ComparisonResult
import com.simprints.feature.enrollast.EnrolLastBiometricResult
import com.simprints.feature.enrollast.EnrolLastBiometricStepResult
import com.simprints.feature.externalcredential.ExternalCredentialSearchResult
import com.simprints.feature.externalcredential.model.CredentialMatch
import com.simprints.infra.config.store.models.ModalitySdkType
import com.simprints.infra.matching.MatchResult
import io.mockk.every
import io.mockk.mockk
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
                MatchResult(emptyList(), ModalitySdkType.RANK_ONE),
            ),
        )

        assertThat(result.first()).isEqualTo(EnrolLastBiometricStepResult.MatchResult(emptyList(), ModalitySdkType.RANK_ONE))
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
                MatchResult(emptyList(), ModalitySdkType.NEC),
            ),
        )

        assertThat(
            result.first(),
        ).isEqualTo(EnrolLastBiometricStepResult.MatchResult(emptyList(), ModalitySdkType.NEC))
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

    @Test
    fun `maps ExternalCredentialSearchResult with matches correctly`() {
        val matchResult = ComparisonResult("subjectId", 0.95f)
        val match = mockk<CredentialMatch>
            {
                every { comparisonResult } returns matchResult
                every { bioSdk } returns ModalitySdkType.SIM_FACE
            }
        val result = useCase(
            listOf(
                mockk<ExternalCredentialSearchResult> {
                    every { matchResults } returns listOf(match)
                },
            ),
        )

        assertThat(result.first()).isEqualTo(
            EnrolLastBiometricStepResult.MatchResult(
                results = listOf(matchResult),
                sdk = ModalitySdkType.SIM_FACE,
            ),
        )
    }

    @Test
    fun `maps ExternalCredentialSearchResult with empty matches to null (filtered out)`() {
        val result = useCase(
            listOf(
                mockk<ExternalCredentialSearchResult> {
                    every { matchResults } returns emptyList()
                },
            ),
        )

        assertThat(result).isEmpty()
    }

    @Test
    fun `maps ExternalCredentialSearchResult with multiple matches correctly`() {
        val matchResult1 = ComparisonResult("subjectId1", 0.90f)
        val matchResult2 = ComparisonResult("subjectId2", 0.85f)
        val match1 = mockk<CredentialMatch> {
            every { comparisonResult } returns matchResult1
            every { bioSdk } returns ModalitySdkType.SIM_FACE
        }
        val match2 = mockk<CredentialMatch> {
            every { comparisonResult } returns matchResult2
            every { bioSdk } returns ModalitySdkType.SIM_FACE
        }
        val result = useCase(
            listOf(
                mockk<ExternalCredentialSearchResult> {
                    every { matchResults } returns listOf(match1, match2)
                },
            ),
        )

        assertThat(result.first()).isEqualTo(
            EnrolLastBiometricStepResult.MatchResult(
                results = listOf(matchResult1, matchResult2),
                sdk = ModalitySdkType.SIM_FACE,
            ),
        )
    }
}
