package com.simprints.feature.orchestrator.usecases

import com.simprints.core.domain.sample.CaptureIdentity
import com.simprints.core.domain.sample.MatchConfidence
import com.simprints.feature.enrollast.EnrolLastBiometricResult
import com.simprints.feature.enrollast.EnrolLastBiometricStepResult
import com.simprints.infra.matching.MatchResult
import java.io.Serializable
import javax.inject.Inject

// Last biometric enrolment heavily depends on the previous execution step results
internal class MapStepsForLastBiometricEnrolUseCase @Inject constructor() {
    operator fun invoke(results: List<Serializable>) = results.mapNotNull { result ->
        when (result) {
            is EnrolLastBiometricResult -> EnrolLastBiometricStepResult.EnrolLastBiometricsResult(
                result.newSubjectId,
            )

            is CaptureIdentity -> EnrolLastBiometricStepResult.CaptureResult(
                result.referenceId,
                result.results,
            )

            is MatchResult -> EnrolLastBiometricStepResult.MatchResult(
                result.results.map { MatchConfidence(it.subjectId, it.confidence) },
                result.sdk,
            )

            else -> null
        }
    }
}
