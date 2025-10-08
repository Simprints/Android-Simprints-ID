package com.simprints.feature.orchestrator.usecases.response

import com.simprints.feature.externalcredential.ExternalCredentialSearchResult
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.matching.FaceMatchResult
import com.simprints.infra.matching.FingerprintMatchResult
import java.io.Serializable
import javax.inject.Inject

internal class IsNewEnrolmentUseCase @Inject constructor() {
    /**
     * Returns true if the result is an actual enrollment and false if there is
     * a duplicate record and the result should be returned as an identification instead.
     */
    operator fun invoke(
        projectConfiguration: ProjectConfiguration,
        results: List<Serializable>,
    ): Boolean {
        val hasCredentialMatchResults =
            results
                .filterIsInstance<ExternalCredentialSearchResult>()
                .firstOrNull()
                ?.matchResults
                ?.isNotEmpty() ?: false
        if (hasCredentialMatchResults) {
            return false
        }

        if (!projectConfiguration.general.duplicateBiometricEnrolmentCheck) {
            // Duplicate check on enrolment is disabled
            return true
        }

        val faceResult = results.lastOrNull { it is FaceMatchResult } as? FaceMatchResult
        val fingerprintResult = results.lastOrNull { it is FingerprintMatchResult } as? FingerprintMatchResult

        val isNewFaceEnrolment = isNewEnrolmentFaceResult(projectConfiguration, faceResult)
        val isNewFingerprintEnrolment = isValidEnrolmentFingerprintResult(projectConfiguration, fingerprintResult)

        return isNewFaceEnrolment && isNewFingerprintEnrolment
    }

    // Missing results and configuration are ignored as "valid" to allow creating new records.
    private fun isValidEnrolmentFingerprintResult(
        projectConfiguration: ProjectConfiguration,
        fingerprintResult: FingerprintMatchResult?,
    ): Boolean = fingerprintResult?.let {
        projectConfiguration.fingerprint
            ?.getSdkConfiguration(fingerprintResult.sdk)
            ?.decisionPolicy
            ?.medium
            ?.toFloat()
            ?.let { threshold -> fingerprintResult.results.all { it.confidence < threshold } }
    } != false

    // Missing results and configuration are ignored as "valid" to allow creating new records.
    private fun isNewEnrolmentFaceResult(
        projectConfiguration: ProjectConfiguration,
        faceResult: FaceMatchResult?,
    ): Boolean = faceResult?.let {
        projectConfiguration.face
            ?.getSdkConfiguration(faceResult.sdk)
            ?.decisionPolicy
            ?.medium
            ?.toFloat()
            ?.let { threshold -> faceResult.results.all { it.confidence < threshold } }
    } != false
}
