package com.simprints.feature.orchestrator.usecases

import com.google.common.truth.Truth.*
import com.simprints.core.domain.common.Modality
import com.simprints.core.domain.sample.CaptureSample
import com.simprints.core.domain.sample.SampleIdentifier
import com.simprints.face.capture.FaceCaptureResult
import com.simprints.feature.enrollast.EnrolLastBiometricResult
import com.simprints.feature.enrollast.EnrolLastBiometricStepResult
import com.simprints.feature.enrollast.FaceTemplateCaptureResult
import com.simprints.feature.enrollast.FingerTemplateCaptureResult
import com.simprints.fingerprint.capture.FingerprintCaptureResult
import com.simprints.infra.config.store.models.FaceConfiguration
import com.simprints.infra.config.store.models.FingerprintConfiguration
import com.simprints.infra.events.sampledata.SampleDefaults.GUID1
import com.simprints.infra.matching.FaceMatchResult
import com.simprints.infra.matching.FingerprintMatchResult
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
                FaceMatchResult(emptyList(), FaceConfiguration.BioSdk.RANK_ONE),
            ),
        )

        assertThat(result.first()).isEqualTo(EnrolLastBiometricStepResult.FaceMatchResult(emptyList(), FaceConfiguration.BioSdk.RANK_ONE))
    }

    @Test
    fun `maps FaceCaptureResult correctly`() {
        val result = useCase(
            listOf(
                FaceCaptureResult(
                    "referenceId",
                    results = listOf(
                        CaptureSample(
                            captureEventId = GUID1,
                            modality = Modality.FACE,
                            format = "format",
                            template = byteArrayOf(),
                        ),
                    ),
                ),
            ),
        )

        assertThat(result.first()).isEqualTo(
            EnrolLastBiometricStepResult.FaceCaptureResult(
                referenceId = "referenceId",
                results = listOf(
                    element = FaceTemplateCaptureResult(
                        template = byteArrayOf(),
                        format = "format",
                    ),
                ),
            ),
        )
    }

    @Test
    fun `maps FingerprintMatchResult correctly`() {
        val result = useCase(
            listOf(
                FingerprintMatchResult(emptyList(), FingerprintConfiguration.BioSdk.NEC),
            ),
        )

        assertThat(
            result.first(),
        ).isEqualTo(EnrolLastBiometricStepResult.FingerprintMatchResult(emptyList(), FingerprintConfiguration.BioSdk.NEC))
    }

    @Test
    fun `maps FingerprintCaptureResult correctly`() {
        val result = useCase(
            listOf(
                FingerprintCaptureResult(
                    "referenceId",
                    results = listOf(
                        CaptureSample(
                            captureEventId = GUID1,
                            modality = Modality.FINGERPRINT,
                            format = "format",
                            template = byteArrayOf(),
                            identifier = SampleIdentifier.RIGHT_THUMB,
                        ),
                    ),
                ),
            ),
        )

        assertThat(result.first()).isEqualTo(
            EnrolLastBiometricStepResult.FingerprintCaptureResult(
                referenceId = "referenceId",
                results = listOf(
                    FingerTemplateCaptureResult(
                        template = byteArrayOf(),
                        format = "format",
                        finger = SampleIdentifier.RIGHT_THUMB,
                    ),
                ),
            ),
        )
    }
}
