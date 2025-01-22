package com.simprints.feature.enrollast.screen.usecase

import com.simprints.feature.enrollast.EnrolLastBiometricStepResult
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
                Simber.i("No capture response. Must be either fingerprint, face or both", tag = ENROLMENT)
                true
            }

            isAnyResponseWithHighConfidence(projectConfig, fingerprintResponse, faceResponse) -> {
                Simber.i("There is a subject with confidence score above the high confidence level", tag = ENROLMENT)
                true
            }

            else -> false
        }
    }

    private fun getFingerprintMatchResult(steps: List<EnrolLastBiometricStepResult>) = steps
        .filterIsInstance<EnrolLastBiometricStepResult.FingerprintMatchResult>()
        .lastOrNull()

    private fun getFaceMatchResult(steps: List<EnrolLastBiometricStepResult>) = steps
        .filterIsInstance<EnrolLastBiometricStepResult.FaceMatchResult>()
        .lastOrNull()

    private fun isAnyResponseWithHighConfidence(
        configuration: ProjectConfiguration,
        fingerprintResponse: EnrolLastBiometricStepResult.FingerprintMatchResult?,
        faceResponse: EnrolLastBiometricStepResult.FaceMatchResult?,
    ): Boolean {
        val fingerprintThreshold = fingerprintResponse?.let {
            configuration.fingerprint
                ?.getSdkConfiguration(fingerprintResponse.sdk)
                ?.decisionPolicy
                ?.high
                ?.toFloat()
        } ?: Float.MAX_VALUE

        val faceThreshold = configuration.face
            ?.decisionPolicy
            ?.high
            ?.toFloat()
            ?: Float.MAX_VALUE

        return fingerprintResponse?.results?.any { it.confidenceScore >= fingerprintThreshold } == true ||
            faceResponse?.results?.any { it.confidenceScore >= faceThreshold } == true
    }
}
