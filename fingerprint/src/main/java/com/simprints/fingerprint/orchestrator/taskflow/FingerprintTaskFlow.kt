package com.simprints.fingerprint.orchestrator.taskflow

import com.simprints.fingerprint.activities.alert.result.AlertTaskResult
import com.simprints.fingerprint.activities.collect.request.CollectFingerprintsTaskRequest
import com.simprints.fingerprint.activities.collect.result.CollectFingerprintsTaskResult
import com.simprints.fingerprint.activities.launch.request.LaunchTaskRequest
import com.simprints.fingerprint.activities.matching.request.MatchingTaskIdentifyRequest
import com.simprints.fingerprint.activities.matching.request.MatchingTaskVerifyRequest
import com.simprints.fingerprint.activities.matching.result.MatchingTaskIdentifyResult
import com.simprints.fingerprint.activities.matching.result.MatchingTaskVerifyResult
import com.simprints.fingerprint.activities.refusal.result.RefusalTaskResult
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.FinalResultBuilder
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintIdentifyRequest
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintRequest
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintVerifyRequest
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.MatchGroup
import com.simprints.fingerprint.data.domain.toAction
import com.simprints.fingerprint.orchestrator.task.FingerprintTask
import com.simprints.fingerprint.orchestrator.task.ResultCode
import com.simprints.fingerprint.orchestrator.task.TaskResult
import com.simprints.fingerprint.tasks.saveperson.SavePersonTaskRequest

sealed class FingerprintTaskFlow {

    protected val finalResultBuilder = FinalResultBuilder() // TODO : Koin this

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

    fun getFinalResult() =
        when (lastResultCode) {
            ResultCode.OK -> getFinalOkResult()
            ResultCode.CANCELLED -> finalResultBuilder.createCancelledResult()
            ResultCode.ALERT -> finalResultBuilder.createAlertResult(taskResults[ALERT] as AlertTaskResult)
            ResultCode.REFUSED -> finalResultBuilder.createRefusalResult(taskResults[REFUSED] as RefusalTaskResult)
        }

    protected abstract fun getFinalOkResult(): FinalResult

    companion object {
        private const val REFUSED = "refused"
        private const val ALERT = "alert"
    }

    class Enrol : FingerprintTaskFlow() {

        override fun computeFlow(fingerprintRequest: FingerprintRequest) {
            with(fingerprintRequest) {
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
                    FingerprintTask.SavePerson(SAVE) {
                        with(taskResults[COLLECT] as CollectFingerprintsTaskResult) {
                            SavePersonTaskRequest(
                                probe
                            )
                        }
                    }
                )
            }
        }

        override fun getFinalOkResult(): FinalResult =
            finalResultBuilder.createEnrolResult(taskResults[COLLECT] as CollectFingerprintsTaskResult)

        companion object {
            private const val LAUNCH = "launch"
            private const val COLLECT = "collect"
            private const val SAVE = "save"
        }
    }

    class Identify : FingerprintTaskFlow() {

        override fun computeFlow(fingerprintRequest: FingerprintRequest) {
            with(fingerprintRequest as FingerprintIdentifyRequest) {
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
                    FingerprintTask.Matching(MATCHING, FingerprintTask.Matching.SubAction.IDENTIFY) {
                        with(taskResults[COLLECT] as CollectFingerprintsTaskResult) {
                            MatchingTaskIdentifyRequest(
                                language, probe, buildQueryForIdentifyPool(), returnIdCount
                            )
                        }
                    }
                )
            }
        }

        override fun getFinalOkResult(): FinalResult =
            finalResultBuilder.createIdentifyResult(taskResults[MATCHING] as MatchingTaskIdentifyResult)

        private fun FingerprintIdentifyRequest.buildQueryForIdentifyPool() =
            when (matchGroup) {
                MatchGroup.GLOBAL -> MatchingTaskIdentifyRequest.QueryForIdentifyPool(projectId)
                MatchGroup.USER -> MatchingTaskIdentifyRequest.QueryForIdentifyPool(projectId, userId = userId)
                MatchGroup.MODULE -> MatchingTaskIdentifyRequest.QueryForIdentifyPool(projectId, moduleId = moduleId)
            }

        companion object {
            private const val LAUNCH = "launch"
            private const val COLLECT = "collect"
            private const val MATCHING = "matching"
        }
    }

    class Verify : FingerprintTaskFlow() {

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
                    FingerprintTask.Matching(MATCHING, FingerprintTask.Matching.SubAction.VERIFY) {
                        with(taskResults[COLLECT] as CollectFingerprintsTaskResult) {
                            MatchingTaskVerifyRequest(
                                language, probe, buildQueryForVerifyPool(), verifyGuid
                            )
                        }
                    }
                )
            }
        }

        override fun getFinalOkResult(): FinalResult =
            finalResultBuilder.createVerifyResult(taskResults[MATCHING] as MatchingTaskVerifyResult)

        private fun FingerprintVerifyRequest.buildQueryForVerifyPool() =
            MatchingTaskVerifyRequest.QueryForVerifyPool(projectId)

        companion object {
            private const val LAUNCH = "launch"
            private const val COLLECT = "collect"
            private const val MATCHING = "matching"
        }
    }
}
