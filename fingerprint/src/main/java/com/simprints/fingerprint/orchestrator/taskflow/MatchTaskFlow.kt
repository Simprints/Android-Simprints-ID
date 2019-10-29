package com.simprints.fingerprint.orchestrator.taskflow

import com.simprints.fingerprint.activities.matching.request.MatchingTaskRequest
import com.simprints.fingerprint.activities.matching.result.MatchingTaskResult
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.FinalResultBuilder
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintMatchRequest
import com.simprints.fingerprint.orchestrator.models.FinalResult
import com.simprints.fingerprint.orchestrator.task.FingerprintTask

class MatchTaskFlow(matchRequest: FingerprintMatchRequest) : FingerprintTaskFlow(matchRequest) {

    init {
        with(matchRequest) {
            fingerprintTasks = listOf(
                FingerprintTask.Matching(MATCHING) { createMatchingTaskRequest() }
            )
        }
    }

    private fun FingerprintMatchRequest.createMatchingTaskRequest() =
        MatchingTaskRequest(probeFingerprintSamples, queryForCandidates)

    override fun getFinalOkResult(finalResultBuilder: FinalResultBuilder): FinalResult =
        finalResultBuilder.createMatchResult(taskResults[MATCHING] as MatchingTaskResult)

    companion object {
        private const val MATCHING = "matching"
    }
}
