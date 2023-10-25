package com.simprints.id.orchestrator.steps

import com.simprints.feature.enrollast.EnrolLastBiometricStepResult
import com.simprints.feature.enrollast.FaceTemplateCaptureResult
import com.simprints.feature.enrollast.FingerTemplateCaptureResult
import com.simprints.feature.enrollast.MatchResult
import com.simprints.id.domain.moduleapi.face.responses.FaceCaptureResponse
import com.simprints.id.domain.moduleapi.face.responses.FaceMatchResponse
import com.simprints.id.domain.moduleapi.face.responses.entities.FaceCaptureResult
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintCaptureResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintMatchResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.FingerprintCaptureResult
import com.simprints.id.orchestrator.steps.core.response.EnrolLastBiometricsResponse
import javax.inject.Inject

// Last biometric enrolment heavily depends on the previous execution step results
class MapStepsForLastBiometricEnrolUseCase @Inject constructor() {

    operator fun invoke(steps: List<Step>) = steps.mapNotNull { step ->
        when (val response = step.getResult()) {
            is EnrolLastBiometricsResponse -> mapEnrolLastBiometricsStep(response)
            is FingerprintMatchResponse -> mapFingerprintMatchStep(response)
            is FingerprintCaptureResponse -> mapFingerprintCaptureStep(response)
            is FaceMatchResponse -> mapFaceMatchStep(response)
            is FaceCaptureResponse -> mapFaceCaptureStep(response)
            else -> null
        }
    }

    private fun mapEnrolLastBiometricsStep(response: EnrolLastBiometricsResponse) =
        EnrolLastBiometricStepResult.EnrolLastBiometricsResult(response.newSubjectId)

    private fun mapFingerprintMatchStep(response: FingerprintMatchResponse) =
        EnrolLastBiometricStepResult.FingerprintMatchResult(response.result.map { MatchResult(it.personId, it.confidenceScore) })

    private fun mapFingerprintCaptureStep(response: FingerprintCaptureResponse) =
        EnrolLastBiometricStepResult.FingerprintCaptureResult(response.captureResult.mapNotNull { mapFingerprintCaptureResult(it) })

    private fun mapFingerprintCaptureResult(capture: FingerprintCaptureResult) = capture.sample
        ?.let { FingerTemplateCaptureResult(it.fingerIdentifier, it.template, it.templateQualityScore, it.format) }

    private fun mapFaceMatchStep(response: FaceMatchResponse) =
        EnrolLastBiometricStepResult.FaceMatchResult(response.result.map { MatchResult(it.guidFound, it.confidence) })

    private fun mapFaceCaptureStep(response: FaceCaptureResponse) =
        EnrolLastBiometricStepResult.FaceCaptureResult(response.capturingResult.mapNotNull { mapFaceCaptureResult(it) })

    private fun mapFaceCaptureResult(capture: FaceCaptureResult) = capture.result
        ?.let { FaceTemplateCaptureResult(it.template, it.format) }
}
