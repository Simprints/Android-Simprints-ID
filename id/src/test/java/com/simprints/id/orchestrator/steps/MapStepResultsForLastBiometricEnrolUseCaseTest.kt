package com.simprints.id.orchestrator.steps

import com.google.common.truth.Truth.assertThat
import com.simprints.feature.enrollast.EnrolLastBiometricStepResult
import com.simprints.feature.enrollast.FaceTemplateCaptureResult
import com.simprints.feature.enrollast.FingerTemplateCaptureResult
import com.simprints.id.domain.moduleapi.face.requests.FaceCaptureRequest
import com.simprints.id.domain.moduleapi.face.requests.FaceMatchRequest
import com.simprints.id.domain.moduleapi.face.responses.FaceCaptureResponse
import com.simprints.id.domain.moduleapi.face.responses.FaceMatchResponse
import com.simprints.id.domain.moduleapi.face.responses.entities.FaceCaptureResult
import com.simprints.id.domain.moduleapi.face.responses.entities.FaceCaptureSample
import com.simprints.id.domain.moduleapi.fingerprint.requests.FingerprintCaptureRequest
import com.simprints.id.domain.moduleapi.fingerprint.requests.FingerprintConfigurationRequest
import com.simprints.id.domain.moduleapi.fingerprint.requests.FingerprintMatchRequest
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintCaptureResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintConfigurationResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintMatchResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintResponseType
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.FingerprintCaptureResult
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.FingerprintCaptureSample
import com.simprints.id.orchestrator.steps.core.requests.EnrolLastBiometricsRequest
import com.simprints.id.orchestrator.steps.core.response.EnrolLastBiometricsResponse
import com.simprints.infra.config.domain.models.Finger
import io.mockk.mockk
import org.junit.Before
import org.junit.Test

class MapStepResultsForLastBiometricEnrolUseCaseTest {

    private lateinit var useCase: MapStepResultsForLastBiometricEnrolUseCase

    @Before
    fun setUp() {
        useCase = MapStepResultsForLastBiometricEnrolUseCase()
    }

    @Test
    fun `maps EnrolLastBiometricRequest correctly`() {
        val result = useCase(listOf(createStep(
            mockk<EnrolLastBiometricsRequest>(),
            EnrolLastBiometricsResponse("subjectId")
        )))

        assertThat(result.first()).isEqualTo(EnrolLastBiometricStepResult.EnrolLastBiometricsResult("subjectId"))
    }

    @Test
    fun `maps FingerprintMatchResponse correctly`() {
        val result = useCase(listOf(createStep(
            mockk<FingerprintMatchRequest>(),
            FingerprintMatchResponse(emptyList())
        )))

        assertThat(result.first()).isEqualTo(EnrolLastBiometricStepResult.FingerprintMatchResult(emptyList()))
    }

    @Test
    fun `maps FingerprintCaptureResponse correctly`() {
        val result = useCase(listOf(createStep(
            mockk<FingerprintCaptureRequest>(),
            FingerprintCaptureResponse(captureResult = listOf(
                FingerprintCaptureResult(Finger.LEFT_THUMB, sample = null),
                FingerprintCaptureResult(Finger.RIGHT_THUMB, sample = FingerprintCaptureSample(
                    fingerIdentifier = Finger.RIGHT_THUMB,
                    template = byteArrayOf(),
                    templateQualityScore = 42,
                    format = "NEC_1",
                    imageRef = null
                )),
            ))
        )))

        assertThat(result.first()).isEqualTo(EnrolLastBiometricStepResult.FingerprintCaptureResult(listOf(
            FingerTemplateCaptureResult(
                finger = Finger.RIGHT_THUMB,
                template = byteArrayOf(),
                templateQualityScore = 42,
                format = "NEC_1",
            )
        )))
    }

    @Test
    fun `skips Fingerprint steps correctly`() {
        val result = useCase(listOf(createStep(
            mockk<FingerprintConfigurationRequest>(),
            FingerprintConfigurationResponse(FingerprintResponseType.CONFIGURATION)
        )))

        assertThat(result).isEmpty()
    }

    @Test
    fun `maps FaceMatchResponse correctly`() {
        val result = useCase(listOf(createStep(
            mockk<FaceMatchRequest>(),
            FaceMatchResponse(emptyList())
        )))

        assertThat(result.first()).isEqualTo(EnrolLastBiometricStepResult.FaceMatchResult(emptyList()))
    }

    @Test
    fun `maps mapFaceCaptureResult correctly`() {
        val result = useCase(listOf(createStep(
            mockk<FaceCaptureRequest>(),
            FaceCaptureResponse(capturingResult = listOf(
                FaceCaptureResult(0, null),
                FaceCaptureResult(1, FaceCaptureSample(
                    faceId = "faceId",
                    template = byteArrayOf(),
                    imageRef = null,
                    format = "format")
                ),
            ))
        )))

        assertThat(result.first()).isEqualTo(EnrolLastBiometricStepResult.FaceCaptureResult(listOf(
            FaceTemplateCaptureResult(
                template = byteArrayOf(),
                format = "format",
            )
        )))
    }

    private fun createStep(request: Step.Request, result: Step.Result) = Step(
        requestCode = 1,
        activityName = "",
        bundleKey = "",
        status = Step.Status.NOT_STARTED,
        payloadType = Step.PayloadType.REQUEST,
        payload = request,
        result = result,
    )
}
