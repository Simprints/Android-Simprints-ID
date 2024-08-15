package com.simprints.feature.orchestrator.usecases

import com.simprints.face.capture.FaceCaptureResult
import com.simprints.feature.enrollast.EnrolLastBiometricResult
import com.simprints.feature.enrollast.EnrolLastBiometricStepResult
import com.simprints.feature.enrollast.FaceTemplateCaptureResult
import com.simprints.feature.enrollast.FingerTemplateCaptureResult
import com.simprints.feature.enrollast.MatchResult
import com.simprints.fingerprint.capture.FingerprintCaptureResult
import com.simprints.infra.config.store.models.fromModuleApiToDomain
import com.simprints.matcher.FaceMatchResult
import com.simprints.matcher.FingerprintMatchResult
import java.io.Serializable
import javax.inject.Inject

// Last biometric enrolment heavily depends on the previous execution step results
internal class MapStepsForLastBiometricEnrolUseCase @Inject constructor() {

    operator fun invoke(results: List<Serializable>) = results.mapNotNull { result ->
        when (result) {
            is EnrolLastBiometricResult -> EnrolLastBiometricStepResult.EnrolLastBiometricsResult(
                result.newSubjectId
            )

            is FingerprintCaptureResult -> EnrolLastBiometricStepResult.FingerprintCaptureResult(
                result.results.mapNotNull { it.sample }.map {
                    FingerTemplateCaptureResult(
                        it.fingerIdentifier.fromModuleApiToDomain(),
                        it.template,
                        it.templateQualityScore,
                        it.format,
                    )
                }
            )

            is FingerprintMatchResult -> EnrolLastBiometricStepResult.FingerprintMatchResult(
                result.results.map { MatchResult(it.subjectId, it.confidence) },
                result.sdk,
            )

            is FaceCaptureResult -> EnrolLastBiometricStepResult.FaceCaptureResult(
                result.results.mapNotNull { it.sample }.map { FaceTemplateCaptureResult(it.template, it.format) }
            )

            is FaceMatchResult -> EnrolLastBiometricStepResult.FaceMatchResult(
                result.results.map { MatchResult(it.subjectId, it.confidence) }
            )

            else -> null
        }
    }

}
