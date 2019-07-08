package com.simprints.fingerprint.orchestrator

import com.simprints.fingerprint.activities.orchestrator.OrchestratorViewModel
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintEnrolRequest
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintIdentifyRequest
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintRequest
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintVerifyRequest
import com.simprints.fingerprint.orchestrator.task.ResultCode
import com.simprints.fingerprint.orchestrator.task.TaskResult
import com.simprints.fingerprint.orchestrator.taskflow.FingerprintTaskFlow

class Orchestrator(private val viewModel: OrchestratorViewModel) {

    private lateinit var taskFlow: FingerprintTaskFlow

    fun start(fingerprintRequest: FingerprintRequest) {

        taskFlow = fingerprintRequest.toFingerprintTaskFlow()

        viewModel.postNextTask(taskFlow.getCurrentTask())
    }

    fun handleTaskResult(resultCode: ResultCode, getTaskResult: (bundleKey: String) -> TaskResult) {

        taskFlow.handleTaskResult(resultCode, getTaskResult)

        if (taskFlow.isFlowFinished()) {
            viewModel.handleFlowFinished(taskFlow.getFinalResult())
        } else {
            viewModel.postNextTask(taskFlow.getCurrentTask())
        }
    }

    private fun FingerprintRequest.toFingerprintTaskFlow(): FingerprintTaskFlow =
        when (this) {
            is FingerprintEnrolRequest -> FingerprintTaskFlow.Enrol()
            is FingerprintIdentifyRequest -> FingerprintTaskFlow.Identify()
            is FingerprintVerifyRequest -> FingerprintTaskFlow.Verify()
            else -> throw Throwable("Woops") // TODO
        }.also { it.computeFlow(this) }
}
