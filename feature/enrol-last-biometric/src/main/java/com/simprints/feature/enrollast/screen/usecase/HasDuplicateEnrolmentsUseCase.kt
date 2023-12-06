package com.simprints.feature.enrollast.screen.usecase

import com.simprints.feature.enrollast.EnrolLastBiometricStepResult
import com.simprints.feature.enrollast.MatchResult
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.ENROLMENT
import com.simprints.infra.logging.Simber
import javax.inject.Inject

internal class HasDuplicateEnrolmentsUseCase @Inject constructor() {

    operator fun invoke(
        projectConfig: ProjectConfiguration,
        steps: List<EnrolLastBiometricStepResult>,
    ): Boolean {
        if (!projectConfig.general.duplicateBiometricEnrolmentCheck) return false

        val fingerprintResponse = getFingerprintMatchResult(steps)
        val faceResponse = getFaceMatchResult(steps)

        return when {
            fingerprintResponse == null && faceResponse == null -> {
                Simber.tag(ENROLMENT.name)
                    .i("No capture response. Must be either fingerprint, face or both")
                true
            }

            isAnyResponseWithHighConfidence(projectConfig, fingerprintResponse, faceResponse) -> {
                Simber.tag(ENROLMENT.name)
                    .i("There is a subject with confidence score above the high confidence level")
                true
            }

            else -> false
        }
    }

    private fun getFingerprintMatchResult(steps: List<EnrolLastBiometricStepResult>) =
        steps.filterIsInstance<EnrolLastBiometricStepResult.FingerprintMatchResult>()
            .lastOrNull()?.results

    private fun getFaceMatchResult(steps: List<EnrolLastBiometricStepResult>) =
        steps.filterIsInstance<EnrolLastBiometricStepResult.FaceMatchResult>().lastOrNull()?.results

    private fun isAnyResponseWithHighConfidence(
        configuration: ProjectConfiguration,
        fingerprintResponse: List<MatchResult>?,
        faceResponse: List<MatchResult>?,
    ): Boolean {
        val fingerprintThreshold = configuration.fingerprint
            ?.bioSdkConfiguration
            ?.decisionPolicy
            ?.high?.toFloat()
            ?: Float.MAX_VALUE
        val faceThreshold = configuration.face
            ?.decisionPolicy
            ?.high?.toFloat()
            ?: Float.MAX_VALUE

        return fingerprintResponse?.any { it.confidenceScore >= fingerprintThreshold } == true
            || faceResponse?.any { it.confidenceScore >= faceThreshold } == true
    }

}
