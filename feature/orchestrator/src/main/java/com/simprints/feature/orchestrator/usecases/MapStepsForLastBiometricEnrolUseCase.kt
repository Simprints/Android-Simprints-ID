package com.simprints.feature.orchestrator.usecases

import com.simprints.face.capture.FaceCaptureResult
import com.simprints.feature.enrollast.EnrolLastBiometricResult
import com.simprints.feature.enrollast.EnrolLastBiometricStepResult
import com.simprints.feature.enrollast.FaceTemplateCaptureResult
import com.simprints.feature.enrollast.FingerTemplateCaptureResult
import com.simprints.feature.enrollast.MatchResult
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

            is FingerprintCaptureResult -> EnrolLastBiometricStepResult.FingerprintCaptureResult(
                result.referenceId,
                result.results.map {
                    FingerTemplateCaptureResult(
                        finger = it.identifier,
                        template = it.template,
                        format = it.format,
                    )
                },
            )

            is FingerprintMatchResult -> EnrolLastBiometricStepResult.FingerprintMatchResult(
                result.results.map { MatchResult(it.subjectId, it.confidence) },
                result.sdk,
            )

            is FaceCaptureResult -> EnrolLastBiometricStepResult.FaceCaptureResult(
                result.referenceId,
                result.results.map { FaceTemplateCaptureResult(it.template, it.format) },
            )

            is FaceMatchResult -> EnrolLastBiometricStepResult.FaceMatchResult(
                result.results.map { MatchResult(it.subjectId, it.confidence) },
                result.sdk,
            )

            else -> null
        }
    }
}
