package com.simprints.feature.orchestrator.usecases

import android.os.Parcelable
import com.simprints.face.capture.FaceCaptureResult
import com.simprints.face.matcher.FaceMatchResult
import com.simprints.feature.enrollast.EnrolLastBiometricResult
import com.simprints.feature.enrollast.EnrolLastBiometricStepResult
import com.simprints.feature.enrollast.FaceTemplateCaptureResult
import com.simprints.feature.enrollast.MatchResult
import javax.inject.Inject

// Last biometric enrolment heavily depends on the previous execution step results
class MapStepsForLastBiometricEnrolUseCase @Inject constructor() {

    operator fun invoke(results: List<Parcelable>) = results.mapNotNull { result ->
        when (result) {
            is EnrolLastBiometricResult -> EnrolLastBiometricStepResult.EnrolLastBiometricsResult(
                result.newSubjectId
            )

            is FaceCaptureResult -> EnrolLastBiometricStepResult.FaceCaptureResult(
                result.results.mapNotNull { it.sample }.map { FaceTemplateCaptureResult(it.template, it.format) }
            )

            is FaceMatchResult -> EnrolLastBiometricStepResult.FaceMatchResult(
                result.results.map { MatchResult(it.guid, it.confidence) }
            )

            // TODO Map fingerprint results

            else -> null
        }
    }

}
