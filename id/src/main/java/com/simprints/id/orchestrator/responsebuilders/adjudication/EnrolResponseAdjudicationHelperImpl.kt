package com.simprints.id.orchestrator.responsebuilders.adjudication

import com.simprints.id.domain.moduleapi.face.responses.FaceMatchResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintMatchResponse
import com.simprints.id.orchestrator.responsebuilders.FaceConfidenceThresholds
import com.simprints.id.orchestrator.responsebuilders.FingerprintConfidenceThresholds
import com.simprints.id.orchestrator.steps.Step

class EnrolResponseAdjudicationHelperImpl(
    private val fingerprintThresholds: Map<FingerprintConfidenceThresholds, Int>,
    private val faceThresholds: Map<FaceConfidenceThresholds, Int>
) : EnrolResponseAdjudicationHelper {
    override fun getAdjudicationAction(isEnrolmentPlus: Boolean, steps: List<Step>): EnrolAdjudicationAction {
        if (isEnrolmentPlus) {
            val results = steps.map { it.getResult() }
            val faceResponse = getFaceMatchResponseFromResultsOrNull(results)
            val fingerResponse = getFingerMatchResponseFromResultsOrNull(results)

           return when {
                fingerResponse != null && faceResponse != null -> {
                    performAdjudicationForFingerprint(fingerResponse)
                }
                fingerResponse != null -> {
                    performAdjudicationForFingerprint(fingerResponse)
                }
                faceResponse != null -> {
                    performAdjudicationForFace(faceResponse)
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

    private fun performAdjudicationForFingerprint(fingerprintResponse: FingerprintMatchResponse) =
        if (allFingerprintConfidenceScoresAreBelowLowerThreshold(fingerprintResponse)) {
            EnrolAdjudicationAction.ENROL
        } else {
            EnrolAdjudicationAction.IDENTIFY
        }

    private fun allFingerprintConfidenceScoresAreBelowLowerThreshold(fingerprintResponse: FingerprintMatchResponse) =
        fingerprintResponse.result.all {
            it.confidenceScore < fingerprintThresholds.getValue(FingerprintConfidenceThresholds.MEDIUM)
        }

    private fun performAdjudicationForFace(faceResponse: FaceMatchResponse) =
        if (allFaceConfidenceScoresAreBelowLowerThreshold(faceResponse)) {
            EnrolAdjudicationAction.ENROL
        } else {
            EnrolAdjudicationAction.IDENTIFY
        }

    private fun allFaceConfidenceScoresAreBelowLowerThreshold(faceResponse: FaceMatchResponse) =
        faceResponse.result.all {
            it.confidence < faceThresholds.getValue(FaceConfidenceThresholds.MEDIUM)
        }

}
