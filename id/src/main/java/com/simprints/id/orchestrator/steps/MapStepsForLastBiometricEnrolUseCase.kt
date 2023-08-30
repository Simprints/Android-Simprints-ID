package com.simprints.id.orchestrator.steps

import com.simprints.feature.enrollast.EnrolLastBiometricStepResult
import com.simprints.feature.enrollast.FaceTemplateCaptureResult
import com.simprints.feature.enrollast.FingerTemplateCaptureResult
import com.simprints.feature.enrollast.MatchResult
import com.simprints.id.domain.moduleapi.face.requests.FaceRequest
import com.simprints.id.domain.moduleapi.face.responses.FaceCaptureResponse
import com.simprints.id.domain.moduleapi.face.responses.FaceMatchResponse
import com.simprints.id.domain.moduleapi.face.responses.entities.FaceCaptureResult
import com.simprints.id.domain.moduleapi.fingerprint.requests.FingerprintRequest
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintCaptureResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintMatchResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.FingerprintCaptureResult
import com.simprints.id.orchestrator.steps.core.requests.EnrolLastBiometricsRequest
import com.simprints.id.orchestrator.steps.core.response.EnrolLastBiometricsResponse
import javax.inject.Inject

// Last biometric enrolment heavily depends on the previous execution step results
class MapStepsForLastBiometricEnrolUseCase @Inject constructor() {

    operator fun invoke(steps: List<Step>) = steps.mapNotNull { step ->
        when (step.payload) {
            is EnrolLastBiometricsRequest -> mapEnrolLastBiometricsStep(step)
            is FingerprintRequest -> mapFingerprintRequestStep(step)
            is FaceRequest -> mapFaceRequestStep(step)
            else -> null
        }
    }

    private fun mapEnrolLastBiometricsStep(step: Step) = step.getResult()
        ?.let { it as? EnrolLastBiometricsResponse }
        ?.let { EnrolLastBiometricStepResult.EnrolLastBiometricsResult(it.newSubjectId) }

    private fun mapFingerprintRequestStep(step: Step) = when (val stepResult = step.getResult()) {
        is FingerprintMatchResponse -> EnrolLastBiometricStepResult.FingerprintMatchResult(
            stepResult.result.map { MatchResult(it.personId, it.confidenceScore) }
        )

        is FingerprintCaptureResponse -> EnrolLastBiometricStepResult.FingerprintCaptureResult(
            stepResult.captureResult.mapNotNull { mapFingerprintCaptureResult(it) }
        )

        else -> null
    }

    private fun mapFingerprintCaptureResult(capture: FingerprintCaptureResult) = capture.sample
        ?.let { FingerTemplateCaptureResult(it.fingerIdentifier, it.template, it.templateQualityScore, it.format) }

    private fun mapFaceRequestStep(step: Step) = when (val stepResult = step.getResult()) {
        is FaceMatchResponse -> EnrolLastBiometricStepResult.FaceMatchResult(
            stepResult.result.map { MatchResult(it.guidFound, it.confidence) }
        )

        is FaceCaptureResponse -> EnrolLastBiometricStepResult.FaceCaptureResult(
            stepResult.capturingResult.mapNotNull { mapFaceCaptureResult(it) }
        )

        else -> null
    }

    private fun mapFaceCaptureResult(capture: FaceCaptureResult) = capture.result
        ?.let { FaceTemplateCaptureResult(it.template, it.format) }
}
