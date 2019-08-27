package com.simprints.fingerprint.orchestrator.taskflow

import com.simprints.fingerprint.activities.alert.result.AlertTaskResult
import com.simprints.fingerprint.activities.refusal.result.RefusalTaskResult
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.FinalResultBuilder
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintRequest
import com.simprints.fingerprint.orchestrator.models.FinalResult
import com.simprints.fingerprint.orchestrator.task.FingerprintTask
import com.simprints.fingerprint.orchestrator.domain.ResultCode
import com.simprints.fingerprint.orchestrator.task.TaskResult

abstract class FingerprintTaskFlow {

    protected val taskResults: MutableMap<String, TaskResult> = mutableMapOf()

    protected lateinit var fingerprintTasks: List<FingerprintTask>
    private var currentTaskIndex = 0
    private var lastResultCode = ResultCode.OK

    fun getCurrentTask() = fingerprintTasks[currentTaskIndex]

    fun isFlowFinished() = isFlowFinishedPrematurely() or isPastFinalTask()
    private fun isFlowFinishedPrematurely() = lastResultCode != ResultCode.OK
    private fun isPastFinalTask() = currentTaskIndex >= fingerprintTasks.size

    abstract fun computeFlow(fingerprintRequest: FingerprintRequest)

    fun handleActivityTaskResult(resultCode: ResultCode, getTaskResult: (bundleKey: String) -> TaskResult) {
        (getCurrentTask() as FingerprintTask.ActivityTask).apply {
            lastResultCode = resultCode
            when (resultCode) {
                ResultCode.OK -> {
                    taskResults[taskResultKey] = getTaskResult(resultBundleKey)
                    currentTaskIndex++
                }
                ResultCode.CANCELLED -> {
                }
                ResultCode.ALERT -> {
                    taskResults[ALERT] = getTaskResult(AlertTaskResult.BUNDLE_KEY)
                }
                ResultCode.REFUSED -> {
                    taskResults[REFUSED] = getTaskResult(RefusalTaskResult.BUNDLE_KEY)
                }
            }
        }
    }

    fun handleRunnableTaskResult(taskResult: TaskResult) {
        (getCurrentTask() as FingerprintTask.RunnableTask).apply {
            taskResults[taskResultKey] = taskResult
            currentTaskIndex++
        }
    }

    fun getFinalResult(finalResultBuilder: FinalResultBuilder) =
        when (lastResultCode) {
            ResultCode.OK -> getFinalOkResult(finalResultBuilder)
            ResultCode.CANCELLED -> finalResultBuilder.createCancelledResult()
            ResultCode.ALERT -> finalResultBuilder.createAlertResult(taskResults[ALERT] as AlertTaskResult)
            ResultCode.REFUSED -> finalResultBuilder.createRefusalResult(taskResults[REFUSED] as RefusalTaskResult)
        }

    protected abstract fun getFinalOkResult(finalResultBuilder: FinalResultBuilder): FinalResult

    companion object {
        private const val REFUSED = "refused"
        private const val ALERT = "alert"
    }
}
