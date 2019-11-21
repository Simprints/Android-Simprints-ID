package com.simprints.fingerprint.orchestrator.taskflow

import com.simprints.fingerprint.activities.collect.request.CollectFingerprintsTaskRequest
import com.simprints.fingerprint.activities.collect.result.CollectFingerprintsTaskResult
import com.simprints.fingerprint.activities.connect.request.ConnectScannerTaskRequest
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.FinalResultBuilder
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintCaptureRequest
import com.simprints.fingerprint.orchestrator.models.FinalResult
import com.simprints.fingerprint.orchestrator.task.FingerprintTask

class CaptureTaskFlow(captureRequest: FingerprintCaptureRequest) : FingerprintTaskFlow(captureRequest) {

    init {
        with(captureRequest) {
            fingerprintTasks = listOf(
                FingerprintTask.ConnectScanner(CONNECT_TASK_KEY) { createConnectScannerTaskRequest() },
                FingerprintTask.CollectFingerprints(COLLECT_TASK_KEY) { createCollectFingerprintsTaskRequest() }
            )
        }
    }

    private fun createConnectScannerTaskRequest() =
        ConnectScannerTaskRequest()

    private fun FingerprintCaptureRequest.createCollectFingerprintsTaskRequest() =
        CollectFingerprintsTaskRequest(fingerprintsToCapture)

    override fun getFinalOkResult(finalResultBuilder: FinalResultBuilder): FinalResult =
        finalResultBuilder.createCaptureResult(taskResults[COLLECT_TASK_KEY] as CollectFingerprintsTaskResult)

    companion object {
        private const val CONNECT_TASK_KEY = "connect"
        private const val COLLECT_TASK_KEY = "collect"
    }
}
