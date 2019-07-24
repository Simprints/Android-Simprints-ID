package com.simprints.fingerprint.orchestrator

import com.simprints.fingerprint.activities.orchestrator.OrchestratorViewModel
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintRequest
import com.simprints.fingerprint.orchestrator.task.FingerprintTask
import com.simprints.fingerprint.orchestrator.task.ResultCode
import com.simprints.fingerprint.orchestrator.task.TaskResult
import com.simprints.fingerprint.orchestrator.taskflow.FingerprintTaskFlow

class Orchestrator(private val viewModel: OrchestratorViewModel,
                   private val runnableTaskDispatcher: RunnableTaskDispatcher = RunnableTaskDispatcher()) {

    private lateinit var taskFlow: FingerprintTaskFlow

    fun start(fingerprintRequest: FingerprintRequest) {

        taskFlow = fingerprintRequest.toFingerprintTaskFlow()

        startNextTask()
    }

    private fun startNextTask() {
        taskFlow.getCurrentTask().also {
            when (it) {
                is FingerprintTask.RunnableTask -> runnableTaskDispatcher.runTask(it, ::handleRunnableTaskResult)
                is FingerprintTask.ActivityTask -> viewModel.postNextTask(it)
            }
        }
    }

    internal fun handleRunnableTaskResult(taskResult: TaskResult) {
        taskFlow.handleRunnableTaskResult(taskResult)
        cycleToNextTaskOrFinishFlow()
    }

    fun handleActivityTaskResult(resultCode: ResultCode, getTaskResult: (bundleKey: String) -> TaskResult) {
        taskFlow.handleActivityTaskResult(resultCode, getTaskResult)
        cycleToNextTaskOrFinishFlow()
    }

    private fun cycleToNextTaskOrFinishFlow() {
        if (taskFlow.isFlowFinished()) {
            viewModel.handleFlowFinished(taskFlow.getFinalResult())
        } else {
            startNextTask()
        }
    }
}
