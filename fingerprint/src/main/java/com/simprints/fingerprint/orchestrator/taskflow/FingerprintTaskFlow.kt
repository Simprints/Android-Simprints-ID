package com.simprints.fingerprint.orchestrator.taskflow

import com.simprints.fingerprint.activities.alert.result.AlertTaskResult
import com.simprints.fingerprint.activities.refusal.result.RefusalTaskResult
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.FinalResultBuilder
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintRequest
import com.simprints.fingerprint.orchestrator.domain.ResultCode
import com.simprints.fingerprint.orchestrator.models.FinalResult
import com.simprints.fingerprint.orchestrator.state.FingerprintTaskFlowState
import com.simprints.fingerprint.orchestrator.task.FingerprintTask
import com.simprints.fingerprint.orchestrator.task.TaskResult

abstract class FingerprintTaskFlow(private val fingerprintRequest: FingerprintRequest) {

    protected var taskResults: MutableMap<String, TaskResult> = mutableMapOf()

    protected lateinit var fingerprintTasks: List<FingerprintTask>
    private var currentTaskIndex = 0
    private var lastResultCode = ResultCode.OK

    fun getCurrentTask() = fingerprintTasks[currentTaskIndex]

    fun isFlowFinished() = isFlowFinishedPrematurely() or isPastFinalTask()
    private fun isFlowFinishedPrematurely() = lastResultCode != ResultCode.OK
    private fun isPastFinalTask() = currentTaskIndex >= fingerprintTasks.size

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
                    taskResults[ALERT_TASK_KEY] = getTaskResult(AlertTaskResult.BUNDLE_KEY)
                }
                ResultCode.REFUSED -> {
                    taskResults[REFUSED_TASK_KEY] = getTaskResult(RefusalTaskResult.BUNDLE_KEY)
                }
            }
        }
    }

    fun getFinalResult(finalResultBuilder: FinalResultBuilder) =
        when (lastResultCode) {
            ResultCode.OK -> getFinalOkResult(finalResultBuilder)
            ResultCode.CANCELLED -> finalResultBuilder.createCancelledResult()
            ResultCode.ALERT -> finalResultBuilder.createAlertResult(taskResults[ALERT_TASK_KEY] as AlertTaskResult)
            ResultCode.REFUSED -> finalResultBuilder.createRefusalResult(taskResults[REFUSED_TASK_KEY] as RefusalTaskResult)
        }

    protected abstract fun getFinalOkResult(finalResultBuilder: FinalResultBuilder): FinalResult

    fun getState() =
        FingerprintTaskFlowState(
            fingerprintRequest, currentTaskIndex, taskResults
        )

    companion object {
        private const val REFUSED_TASK_KEY = "refused"
        private const val ALERT_TASK_KEY = "alert"

        fun fromState(fingerprintTaskFlowState: FingerprintTaskFlowState) =
            with(fingerprintTaskFlowState) {
                fingerprintRequest.toFingerprintTaskFlow().also {
                    it.currentTaskIndex = currentTaskIndex
                    it.taskResults = taskResults
                }
            }

    }
}
