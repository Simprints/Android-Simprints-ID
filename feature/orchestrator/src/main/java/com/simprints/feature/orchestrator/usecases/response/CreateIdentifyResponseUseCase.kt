package com.simprints.feature.orchestrator.usecases.response

import android.os.Parcelable
import com.simprints.feature.orchestrator.model.responses.AppIdentifyResponse
import com.simprints.feature.orchestrator.model.responses.AppMatchResult
import com.simprints.infra.config.store.models.DecisionPolicy
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.events.EventRepository
import com.simprints.matcher.FaceMatchResult
import com.simprints.matcher.FingerprintMatchResult
import com.simprints.infra.orchestration.moduleapi.app.responses.IAppResponse
import javax.inject.Inject

internal class CreateIdentifyResponseUseCase @Inject constructor(
    private val eventRepository: EventRepository,
) {

    suspend operator fun invoke(
        projectConfiguration: ProjectConfiguration,
        results: List<Parcelable>,
    ): IAppResponse {
        val currentSessionId = eventRepository.getCurrentCaptureSessionEvent().id

        val faceDecisionPolicy = projectConfiguration.face?.decisionPolicy
        val faceResults = getFaceMatchResults(faceDecisionPolicy, results, projectConfiguration)
        val bestFaceConfidence = faceResults.firstOrNull()?.confidenceScore ?: 0

        val fingerprintDecisionPolicy = projectConfiguration.fingerprint?.decisionPolicy
        val fingerprintResults = getFingerprintResults(fingerprintDecisionPolicy, results, projectConfiguration)
        val bestFingerprintConfidence = fingerprintResults.firstOrNull()?.confidenceScore ?: 0

        return AppIdentifyResponse(
            sessionId = currentSessionId,
            // Return the results with the highest confidence score
            identifications = if (bestFingerprintConfidence > bestFaceConfidence) {
                fingerprintResults
            } else {
                faceResults
            },
        )
    }

    private fun getFingerprintResults(
        fingerprintDecisionPolicy: DecisionPolicy?,
        results: List<Parcelable>,
        projectConfiguration: ProjectConfiguration,
    ) = if (fingerprintDecisionPolicy != null) {
        val matches = results.filterIsInstance(FingerprintMatchResult::class.java).lastOrNull()?.results.orEmpty()
        val goodResults = matches
            .filter { it.confidence >= fingerprintDecisionPolicy.low }
            .sortedByDescending { it.confidence }
        // Attempt to include only high confidence matches
        goodResults
            .filter { it.confidence >= fingerprintDecisionPolicy.high }
            .ifEmpty { goodResults }
            .take(projectConfiguration.identification.maxNbOfReturnedCandidates)
            .map { AppMatchResult(it.subjectId, it.confidence, fingerprintDecisionPolicy) }
    } else emptyList()

    private fun getFaceMatchResults(
        faceDecisionPolicy: DecisionPolicy?,
        results: List<Parcelable>,
        projectConfiguration: ProjectConfiguration,
    ) = if (faceDecisionPolicy != null) {
        val matches = results.filterIsInstance(FaceMatchResult::class.java).lastOrNull()?.results.orEmpty()
        val goodResults = matches
            .filter { it.confidence >= faceDecisionPolicy.low }
            .sortedByDescending { it.confidence }
        // Attempt to include only high confidence matches
        goodResults
            .filter { it.confidence >= faceDecisionPolicy.high }
            .ifEmpty { goodResults }
            .take(projectConfiguration.identification.maxNbOfReturnedCandidates)
            .map { AppMatchResult(it.subjectId, it.confidence, faceDecisionPolicy) }
    } else emptyList()
}
