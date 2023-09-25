package com.simprints.feature.orchestrator.usecases

import android.os.Parcelable
import com.simprints.face.matcher.FaceMatchResult
import com.simprints.infra.config.domain.models.ProjectConfiguration
import javax.inject.Inject

internal class IsNewEnrolmentUseCase @Inject constructor() {

    /**
     * Returns true if the result is an actual enrollment and false if there is
     * a duplicate record and the result should be returned as an identification instead.
     */
    operator fun invoke(
        projectConfiguration: ProjectConfiguration,
        results: List<Parcelable>,
    ): Boolean {
        if (!projectConfiguration.general.duplicateBiometricEnrolmentCheck) {
            // Duplicate check on enrolment is disabled
            return true
        }

        val faceResult = results.lastOrNull { it is FaceMatchResult } as? FaceMatchResult
        // TODO val fingerprintResult = results.lastOrNull { it is FingerprintMatchResult } as? FingerprintMatchResult

        val isNewFaceEnrolment = isNewEnrolmentFaceResult(projectConfiguration, faceResult)
        val isNewFingerprintEnrolment = true
        // TODO val isNewFingerprintEnrolment = isValidEnrolmentFaceResult(projectConfiguration, fingerprintResult)

        return isNewFaceEnrolment && isNewFingerprintEnrolment
    }

    // Missing results and configuration are ignored as "valid" to allow creating new records.
    private fun isNewEnrolmentFaceResult(
        projectConfiguration: ProjectConfiguration,
        faceResult: FaceMatchResult?
    ): Boolean = projectConfiguration.face
        ?.decisionPolicy
        ?.medium
        ?.toFloat()
        ?.let { threshold -> faceResult?.results?.all { it.confidence < threshold } }
        ?: true

    // TODO
    //    // Missing results and configuration are ignored as "valid" to allow creating new records.
    //    private fun isValidEnrolmentFaceResult(
    //        projectConfiguration: ProjectConfiguration,
    //        fingerpringResult: FingerprintMatchResult?
    //    ): Boolean? = projectConfiguration.face
    //        ?.decisionPolicy
    //        ?.medium?.toFloat()
    //        ?.let { threshold -> fingerpringResult?.results?.all { it.confidence < threshold } }
}
