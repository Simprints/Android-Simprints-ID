package com.simprints.fingerprint.orchestrator.taskflow

import com.simprints.fingerprint.activities.collect.request.CollectFingerprintsTaskRequest
import com.simprints.fingerprint.activities.collect.result.CollectFingerprintsTaskResult
import com.simprints.fingerprint.activities.launch.request.LaunchTaskRequest
import com.simprints.fingerprint.activities.matching.request.MatchingTaskVerifyRequest
import com.simprints.fingerprint.activities.matching.result.MatchingTaskVerifyResult
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.FinalResultBuilder
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintRequest
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintVerifyRequest
import com.simprints.fingerprint.data.domain.toAction
import com.simprints.fingerprint.orchestrator.models.FinalResult
import com.simprints.fingerprint.orchestrator.task.FingerprintTask

class VerifyTaskFlow : FingerprintTaskFlow() {

    override fun computeFlow(fingerprintRequest: FingerprintRequest) {
        with(fingerprintRequest as FingerprintVerifyRequest) {
            fingerprintTasks = listOf(
                FingerprintTask.Launch(LAUNCH) {
                    LaunchTaskRequest(
                        projectId, this.toAction(), language, logoExists, programName, organizationName
                    )
                },
                FingerprintTask.CollectFingerprints(COLLECT) {
                    CollectFingerprintsTaskRequest(
                        projectId, userId, moduleId, this.toAction(), language, fingerStatus
                    )
                },
                FingerprintTask.Matching(MATCHING) {
                    with(taskResults[COLLECT] as CollectFingerprintsTaskResult) {
                        MatchingTaskVerifyRequest(
                            language, probe, buildQueryForVerifyPool(), verifyGuid
                        )
                    }
                }
            )
        }
    }

    override fun getFinalOkResult(finalResultBuilder: FinalResultBuilder): FinalResult =
        finalResultBuilder.createVerifyResult(taskResults[MATCHING] as MatchingTaskVerifyResult)

    private fun FingerprintVerifyRequest.buildQueryForVerifyPool() =
        MatchingTaskVerifyRequest.QueryForVerifyPool(projectId)

    companion object {
        private const val LAUNCH = "launch"
        private const val COLLECT = "collect"
        private const val MATCHING = "matching"
    }
}
