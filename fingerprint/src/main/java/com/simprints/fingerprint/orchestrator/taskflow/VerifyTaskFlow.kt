package com.simprints.fingerprint.orchestrator.taskflow

import com.simprints.fingerprint.activities.collect.request.CollectFingerprintsTaskRequest
import com.simprints.fingerprint.activities.collect.result.CollectFingerprintsTaskResult
import com.simprints.fingerprint.activities.connect.request.ConnectScannerTaskRequest
import com.simprints.fingerprint.activities.matching.request.MatchingTaskVerifyRequest
import com.simprints.fingerprint.activities.matching.result.MatchingTaskVerifyResult
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.FinalResultBuilder
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintRequest
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintVerifyRequest
import com.simprints.fingerprint.orchestrator.models.FinalResult
import com.simprints.fingerprint.orchestrator.task.FingerprintTask

class VerifyTaskFlow(fingerprintRequest: FingerprintRequest) : FingerprintTaskFlow(fingerprintRequest) {

    init {
        with(fingerprintRequest as FingerprintVerifyRequest) {
            fingerprintTasks = listOf(
                FingerprintTask.ConnectScanner(CONNECT) { createConnectScannerTaskRequest() },
                FingerprintTask.CollectFingerprints(COLLECT) { createCollectFingerprintsTaskRequest() },
                FingerprintTask.Matching(MATCHING) { createMatchingTaskRequest() }
            )
        }
    }

    private fun FingerprintVerifyRequest.createConnectScannerTaskRequest() =
        ConnectScannerTaskRequest(
            language
        )

    private fun FingerprintVerifyRequest.createCollectFingerprintsTaskRequest() =
        CollectFingerprintsTaskRequest(
            projectId, userId, moduleId, language, fingerStatus
        )

    private fun FingerprintVerifyRequest.createMatchingTaskRequest() =
        with(taskResults[COLLECT] as CollectFingerprintsTaskResult) {
            MatchingTaskVerifyRequest(
                language, probe, buildQueryForVerifyPool(), verifyGuid
            )
        }

    private fun FingerprintVerifyRequest.buildQueryForVerifyPool() =
        MatchingTaskVerifyRequest.QueryForVerifyPool(projectId)

    override fun getFinalOkResult(finalResultBuilder: FinalResultBuilder): FinalResult =
        finalResultBuilder.createVerifyResult(taskResults[MATCHING] as MatchingTaskVerifyResult)

    companion object {
        private const val CONNECT = "connect"
        private const val COLLECT = "collect"
        private const val MATCHING = "matching"
    }
}
