package com.simprints.feature.orchestrator.usecases

import com.simprints.core.domain.modality.Modality
import com.simprints.face.capture.FaceCaptureResult
import com.simprints.feature.enrollast.EnrolLastBiometricResult
import com.simprints.feature.enrollast.EnrolLastBiometricStepResult
import com.simprints.fingerprint.capture.FingerprintCaptureResult
import com.simprints.matcher.MatchResult
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
                Modality.FINGERPRINT,
                result.results.mapNotNull { it.sample },
            )

            is FaceCaptureResult -> EnrolLastBiometricStepResult.CaptureResult(
                result.referenceId,
                Modality.FACE,
                result.results.mapNotNull { it.sample },
            )

            is MatchResult -> EnrolLastBiometricStepResult.MatchResult(
                result.results.map { EnrolLastBiometricStepResult.MatchResult.Item(it.subjectId, it.confidence) },
                result.modality,
                result.bioSdk,
            )

            else -> null
        }
    }
}
