package com.simprints.feature.orchestrator.usecases

import com.google.common.truth.Truth.*
import com.simprints.core.domain.modality.Modality
import com.simprints.core.domain.sample.CaptureSample
import com.simprints.core.domain.sample.SampleIdentifier
import com.simprints.face.capture.FaceCaptureResult
import com.simprints.feature.enrollast.EnrolLastBiometricResult
import com.simprints.feature.enrollast.EnrolLastBiometricStepResult
import com.simprints.fingerprint.capture.FingerprintCaptureResult
import com.simprints.infra.config.store.models.FaceConfiguration
import com.simprints.infra.config.store.models.FingerprintConfiguration
import com.simprints.infra.events.sampledata.SampleDefaults.GUID1
import com.simprints.matcher.MatchResult
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
                EnrolLastBiometricResult("subjectId"),
            ),
        )

        assertThat(result.first()).isEqualTo(EnrolLastBiometricStepResult.EnrolLastBiometricsResult("subjectId"))
    }

    @Test
    fun `maps FaceMatchResult correctly`() {
        val result = useCase(
            listOf(
                MatchResult(emptyList(), Modality.FACE, FaceConfiguration.BioSdk.RANK_ONE),
            ),
        )

        assertThat(result.first()).isEqualTo(
            EnrolLastBiometricStepResult.MatchResult(
                emptyList(),
                Modality.FACE,
                FaceConfiguration.BioSdk.RANK_ONE,
            ),
        )
    }

    @Test
    fun `maps FaceCaptureResult correctly`() {
        val result = useCase(
            listOf(
                FaceCaptureResult(
                    "referenceId",
                    results = listOf(
                        FaceCaptureResult.Item(captureEventId = null, index = 0, sample = null),
                        FaceCaptureResult.Item(
                            captureEventId = GUID1,
                            index = 0,
                            sample = CaptureSample(
                                format = "format",
                                template = byteArrayOf(),
                                templateQualityScore = 1,
                                imageRef = null,
                                modality = Modality.FACE,
                            ),
                        ),
                    ),
                ),
            ),
        )

        assertThat(result.first()).isEqualTo(
            EnrolLastBiometricStepResult.CaptureResult(
                referenceId = "referenceId",
                modality = Modality.FACE,
                results = listOf(
                    element = CaptureSample(
                        format = "format",
                        template = byteArrayOf(),
                        templateQualityScore = 1,
                        imageRef = null,
                        modality = Modality.FACE,
                    ),
                ),
            ),
        )
    }

    @Test
    fun `maps FingerprintMatchResult correctly`() {
        val result = useCase(
            listOf(
                MatchResult(emptyList(), Modality.FINGERPRINT, FingerprintConfiguration.BioSdk.NEC),
            ),
        )

        assertThat(
            result.first(),
        ).isEqualTo(EnrolLastBiometricStepResult.MatchResult(emptyList(), Modality.FINGERPRINT, FingerprintConfiguration.BioSdk.NEC))
    }

    @Test
    fun `maps FingerprintCaptureResult correctly`() {
        val result = useCase(
            listOf(
                FingerprintCaptureResult(
                    "referenceId",
                    results = listOf(
                        FingerprintCaptureResult.Item(null, SampleIdentifier.LEFT_THUMB, null),
                        FingerprintCaptureResult.Item(
                            identifier = SampleIdentifier.RIGHT_THUMB,
                            captureEventId = GUID1,
                            sample = CaptureSample(
                                identifier = SampleIdentifier.RIGHT_THUMB,
                                format = "format",
                                template = byteArrayOf(),
                                templateQualityScore = 0,
                                imageRef = null,
                                modality = Modality.FINGERPRINT,
                            ),
                        ),
                    ),
                ),
            ),
        )

        assertThat(result.first()).isEqualTo(
            EnrolLastBiometricStepResult.CaptureResult(
                referenceId = "referenceId",
                Modality.FINGERPRINT,
                results = listOf(
                    CaptureSample(
                        template = byteArrayOf(),
                        templateQualityScore = 0,
                        format = "format",
                        imageRef = null,
                        identifier = SampleIdentifier.RIGHT_THUMB,
                        modality = Modality.FINGERPRINT,
                    ),
                ),
            ),
        )
    }
}
