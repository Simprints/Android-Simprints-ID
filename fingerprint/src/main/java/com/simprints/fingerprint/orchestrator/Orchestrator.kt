package com.simprints.fingerprint.orchestrator

import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.FinalResultBuilder
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintRequest
import com.simprints.fingerprint.orchestrator.task.ResultCode
import com.simprints.fingerprint.orchestrator.task.TaskResult
import com.simprints.fingerprint.orchestrator.taskflow.FingerprintTaskFlow

class Orchestrator(private val finalResultBuilder: FinalResultBuilder) {

    private lateinit var taskFlow: FingerprintTaskFlow

    fun start(fingerprintRequest: FingerprintRequest) {
        taskFlow = fingerprintRequest.toFingerprintTaskFlow()
    }

    fun handleActivityTaskResult(resultCode: ResultCode, getTaskResult: (bundleKey: String) -> TaskResult) {
        taskFlow.handleActivityTaskResult(resultCode, getTaskResult)
    }

    fun handleRunnableTaskResult(taskResult: TaskResult) {
        taskFlow.handleRunnableTaskResult(taskResult)
    }

    fun isFinished() = taskFlow.isFlowFinished()

    fun getNextTask() = taskFlow.getCurrentTask()

    fun getFinalResult() = taskFlow.getFinalResult(finalResultBuilder)
}
