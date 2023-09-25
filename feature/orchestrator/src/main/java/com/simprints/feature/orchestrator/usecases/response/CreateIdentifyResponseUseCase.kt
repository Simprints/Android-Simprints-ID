package com.simprints.feature.orchestrator.usecases.response

import android.os.Parcelable
import com.simprints.face.matcher.FaceMatchResult
import com.simprints.feature.orchestrator.model.responses.AppIdentifyResponse
import com.simprints.feature.orchestrator.model.responses.AppMatchResult
import com.simprints.infra.config.domain.models.DecisionPolicy
import com.simprints.infra.config.domain.models.ProjectConfiguration
import com.simprints.infra.events.EventRepository
import com.simprints.moduleapi.app.responses.IAppResponse
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

        // TODO fingerprint match results
        val fingerprintResults = emptyList<AppMatchResult>()
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

    private fun getFaceMatchResults(
        faceDecisionPolicy: DecisionPolicy?,
        results: List<Parcelable>,
        projectConfiguration: ProjectConfiguration,
    ) = if (faceDecisionPolicy != null) {
        val faceMatches = results.filterIsInstance(FaceMatchResult::class.java).lastOrNull()?.results.orEmpty()
        val lowFilteredResults = faceMatches
            .filter { it.confidence >= faceDecisionPolicy.low }
            .take(projectConfiguration.identification.maxNbOfReturnedCandidates)
            .sortedByDescending { it.confidence }
        // Attempt to include only high confidence matches
        lowFilteredResults
            .filter { it.confidence >= faceDecisionPolicy.high }
            .ifEmpty { lowFilteredResults }
            .map { AppMatchResult(it.guid, it.confidence, faceDecisionPolicy) }
    } else emptyList()
}
