package com.simprints.id.orchestrator.responsebuilders.adjudication

import com.simprints.id.domain.moduleapi.face.responses.FaceMatchResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintMatchResponse
import com.simprints.id.orchestrator.steps.Step
import com.simprints.infra.config.domain.models.ProjectConfiguration
import javax.inject.Inject

class EnrolResponseAdjudicationHelperImpl @Inject constructor(): EnrolResponseAdjudicationHelper {

    override fun getAdjudicationAction(
        projectConfiguration: ProjectConfiguration,
        steps: List<Step>
    ): EnrolAdjudicationAction {
        if (projectConfiguration.general.duplicateBiometricEnrolmentCheck) {
            val results = steps.map { it.getResult() }
            val faceResponse = getFaceMatchResponseFromResultsOrNull(results)
            val fingerprintResponse = getFingerprintMatchResponseFromResultsOrNull(results)

            return when {
                fingerprintResponse != null && faceResponse != null -> {
                    performAdjudicationForFingerprintAndFace(
                        projectConfiguration,
                        fingerprintResponse,
                        faceResponse
                    )
                }
                fingerprintResponse != null -> {
                    performAdjudicationForFingerprint(projectConfiguration, fingerprintResponse)
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

    private fun getFingerprintMatchResponseFromResultsOrNull(results: List<Step.Result?>) =
        results.filterIsInstance(FingerprintMatchResponse::class.java).lastOrNull()

    private fun getFaceMatchResponseFromResultsOrNull(results: List<Step.Result?>) =
        results.filterIsInstance(FaceMatchResponse::class.java).lastOrNull()

    private fun performAdjudicationForFingerprintAndFace(
        projectConfiguration: ProjectConfiguration,
        fingerprintResponse: FingerprintMatchResponse,
        faceResponse: FaceMatchResponse
    ): EnrolAdjudicationAction {
        val fingerprintAdjudication = performAdjudicationForFingerprint(
            projectConfiguration,
            fingerprintResponse
        )
        val faceAdjudication = performAdjudicationForFace(projectConfiguration, faceResponse)
        return if (fingerprintAdjudication == EnrolAdjudicationAction.ENROL &&
            faceAdjudication == EnrolAdjudicationAction.ENROL
        ) {
            EnrolAdjudicationAction.ENROL
        } else {
            EnrolAdjudicationAction.IDENTIFY
        }
    }

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
