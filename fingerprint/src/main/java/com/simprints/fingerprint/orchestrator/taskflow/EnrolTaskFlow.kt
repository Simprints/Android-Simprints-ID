package com.simprints.fingerprint.orchestrator.taskflow

import com.simprints.fingerprint.activities.collect.request.CollectFingerprintsTaskRequest
import com.simprints.fingerprint.activities.collect.result.CollectFingerprintsTaskResult
import com.simprints.fingerprint.activities.connect.request.ConnectScannerTaskRequest
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.FinalResultBuilder
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintRequest
import com.simprints.fingerprint.data.domain.toAction
import com.simprints.fingerprint.orchestrator.models.FinalResult
import com.simprints.fingerprint.orchestrator.task.FingerprintTask
import com.simprints.fingerprint.tasks.saveperson.SavePersonTaskRequest

class EnrolTaskFlow(fingerprintRequest: FingerprintRequest) : FingerprintTaskFlow(fingerprintRequest) {

    init {
        with(fingerprintRequest) {
            fingerprintTasks = listOf(
                FingerprintTask.ConnectScanner(CONNECT) { createConnectScannerTaskRequest() },
                FingerprintTask.CollectFingerprints(COLLECT) { createCollectFingerprintsTaskRequest() },
                FingerprintTask.SavePerson(SAVE) { createSavePersonTaskRequest() }
            )
        }
    }

    private fun FingerprintRequest.createConnectScannerTaskRequest() =
        ConnectScannerTaskRequest(
            language
        )

    private fun FingerprintRequest.createCollectFingerprintsTaskRequest() =
        CollectFingerprintsTaskRequest(
            projectId, userId, moduleId, this.toAction(), language, fingerStatus
        )

    private fun createSavePersonTaskRequest() =
        with(taskResults[COLLECT] as CollectFingerprintsTaskResult) {
            SavePersonTaskRequest(
                probe
            )
        }

    override fun getFinalOkResult(finalResultBuilder: FinalResultBuilder): FinalResult =
        finalResultBuilder.createEnrolResult(taskResults[COLLECT] as CollectFingerprintsTaskResult)

    companion object {
        private const val CONNECT = "connect"
        private const val COLLECT = "collect"
        private const val SAVE = "save"
    }
}
