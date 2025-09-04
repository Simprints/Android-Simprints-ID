package com.simprints.feature.orchestrator.usecases.response

import com.simprints.feature.externalcredential.ExternalCredentialSearchResult
import com.simprints.infra.config.store.models.FaceConfiguration
import com.simprints.infra.config.store.models.FingerprintConfiguration
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.matching.MatchResult
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

        val faceResult = results.lastOrNull { it is MatchResult && it.sdk is FaceConfiguration.BioSdk } as? MatchResult
        val fingerprintResult = results.lastOrNull { it is MatchResult && it.sdk is FingerprintConfiguration.BioSdk } as? MatchResult

        val isNewFaceEnrolment = isNewEnrolmentFaceResult(projectConfiguration, faceResult)
        val isNewFingerprintEnrolment = isValidEnrolmentFingerprintResult(projectConfiguration, fingerprintResult)

        return isNewFaceEnrolment && isNewFingerprintEnrolment
    }

    // Missing results and configuration are ignored as "valid" to allow creating new records.
    private fun isValidEnrolmentFingerprintResult(
        projectConfiguration: ProjectConfiguration,
        fingerprintResult: MatchResult?,
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
        faceResult: MatchResult?,
    ): Boolean = faceResult?.let {
        projectConfiguration.face
            ?.getSdkConfiguration(faceResult.sdk)
            ?.decisionPolicy
            ?.medium
            ?.toFloat()
            ?.let { threshold -> faceResult.results.all { it.confidence < threshold } }
    } != false
}
