package com.simprints.feature.orchestrator.usecases

import com.simprints.core.domain.sample.MatchConfidence
import com.simprints.face.capture.FaceCaptureResult
import com.simprints.feature.enrollast.EnrolLastBiometricResult
import com.simprints.feature.enrollast.EnrolLastBiometricStepResult
import com.simprints.fingerprint.capture.FingerprintCaptureResult
import com.simprints.infra.matching.FaceMatchResult
import com.simprints.infra.matching.FingerprintMatchResult
import java.io.Serializable
import javax.inject.Inject

// Last biometric enrolment heavily depends on the previous execution step results
internal class MapStepsForLastBiometricEnrolUseCase @Inject constructor() {
    operator fun invoke(results: List<Serializable>) = results.mapNotNull { result ->
        when (result) {
            is EnrolLastBiometricResult -> EnrolLastBiometricStepResult.EnrolLastBiometricsResult(
                result.newSubjectId,
            )

            is FingerprintCaptureResult -> EnrolLastBiometricStepResult.CaptureResult(
                result.referenceId,
                result.results,
            )

            is FaceCaptureResult -> EnrolLastBiometricStepResult.CaptureResult(
                result.referenceId,
                result.results,
            )

            is FingerprintMatchResult -> EnrolLastBiometricStepResult.FingerprintMatchResult(
                result.results.map { MatchConfidence(it.subjectId, it.confidence) },
                result.sdk,
            )

            is FaceMatchResult -> EnrolLastBiometricStepResult.FaceMatchResult(
                result.results.map { MatchConfidence(it.subjectId, it.confidence) },
                result.sdk,
            )

            else -> null
        }
    }
}
