package com.simprints.fingerprint.orchestrator

import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.FinalResultBuilder
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintRequest
import com.simprints.fingerprint.orchestrator.domain.ResultCode
import com.simprints.fingerprint.orchestrator.state.OrchestratorState
import com.simprints.fingerprint.orchestrator.task.TaskResult
import com.simprints.fingerprint.orchestrator.taskflow.FingerprintTaskFlow
import com.simprints.fingerprint.orchestrator.taskflow.toFingerprintTaskFlow

/**
 * Principle class for co-ordinating tasks within the fingerprint module. See neighbouring README
 * for details.
 */
class Orchestrator(private val finalResultBuilder: FinalResultBuilder) {

    private lateinit var taskFlow: FingerprintTaskFlow

    fun start(fingerprintRequest: FingerprintRequest) {
        taskFlow = fingerprintRequest.toFingerprintTaskFlow()
    }

    fun handleActivityTaskResult(resultCode: ResultCode, getTaskResult: (bundleKey: String) -> TaskResult) {
        taskFlow.handleActivityTaskResult(resultCode, getTaskResult)
    }

    fun handleRunnableTaskResult(taskResult: TaskResult?) {
        taskFlow.handleRunnableTaskResult(taskResult)
    }

    fun isFinished() = taskFlow.isFlowFinished()

    fun getNextTask() = taskFlow.getCurrentTask()

    fun getFinalResult() = taskFlow.getFinalResult(finalResultBuilder)

    fun restoreState(orchestratorState: OrchestratorState) {
        taskFlow = FingerprintTaskFlow.fromState(orchestratorState.fingerprintTaskFlowState)
    }

    fun getState(): OrchestratorState? =
        if (::taskFlow.isInitialized) OrchestratorState(taskFlow.getState()) else null
}
