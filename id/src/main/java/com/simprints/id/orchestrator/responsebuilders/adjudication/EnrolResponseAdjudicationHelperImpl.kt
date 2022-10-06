package com.simprints.id.orchestrator.responsebuilders.adjudication

import com.simprints.id.domain.moduleapi.face.responses.FaceMatchResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintMatchResponse
import com.simprints.id.orchestrator.steps.Step
import com.simprints.infra.config.domain.models.ProjectConfiguration

class EnrolResponseAdjudicationHelperImpl : EnrolResponseAdjudicationHelper {

    override fun getAdjudicationAction(
        projectConfiguration: ProjectConfiguration,
        steps: List<Step>
    ): EnrolAdjudicationAction {
        if (projectConfiguration.general.duplicateBiometricEnrolmentCheck) {
            val results = steps.map { it.getResult() }
            val faceResponse = getFaceMatchResponseFromResultsOrNull(results)
            val fingerResponse = getFingerMatchResponseFromResultsOrNull(results)

            return when {
                fingerResponse != null && faceResponse != null -> {
                    performAdjudicationForFingerprint(projectConfiguration, fingerResponse)
                }
                fingerResponse != null -> {
                    performAdjudicationForFingerprint(projectConfiguration, fingerResponse)
                }
                faceResponse != null -> {
                    performAdjudicationForFace(projectConfiguration, faceResponse)
                }
                else -> EnrolAdjudicationAction.ENROL
            }

        } else {
            return EnrolAdjudicationAction.ENROL
        }
    }

    private fun getFingerMatchResponseFromResultsOrNull(results: List<Step.Result?>) =
        results.filterIsInstance(FingerprintMatchResponse::class.java).lastOrNull()

    private fun getFaceMatchResponseFromResultsOrNull(results: List<Step.Result?>) =
        results.filterIsInstance(FaceMatchResponse::class.java).lastOrNull()

    private fun performAdjudicationForFingerprint(
        projectConfiguration: ProjectConfiguration,
        fingerprintResponse: FingerprintMatchResponse
    ) =
        if (allFingerprintConfidenceScoresAreBelowMediumThreshold(
                projectConfiguration,
                fingerprintResponse
            )
        ) {
            EnrolAdjudicationAction.ENROL
        } else {
            EnrolAdjudicationAction.IDENTIFY
        }

    private fun allFingerprintConfidenceScoresAreBelowMediumThreshold(
        projectConfiguration: ProjectConfiguration,
        fingerprintResponse: FingerprintMatchResponse
    ) =
        fingerprintResponse.result.all {
            it.confidenceScore < projectConfiguration.fingerprint!!.decisionPolicy.medium
        }

    private fun performAdjudicationForFace(
        projectConfiguration: ProjectConfiguration,
        faceResponse: FaceMatchResponse
    ) =
        if (allFaceConfidenceScoresAreBelowMediumThreshold(projectConfiguration, faceResponse)) {
            EnrolAdjudicationAction.ENROL
        } else {
            EnrolAdjudicationAction.IDENTIFY
        }

    private fun allFaceConfidenceScoresAreBelowMediumThreshold(
        projectConfiguration: ProjectConfiguration,
        faceResponse: FaceMatchResponse
    ) =
        faceResponse.result.all {
            it.confidence < projectConfiguration.face!!.decisionPolicy.medium
        }

}
