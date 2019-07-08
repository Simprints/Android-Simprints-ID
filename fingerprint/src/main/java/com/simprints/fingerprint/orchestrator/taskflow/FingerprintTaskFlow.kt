package com.simprints.fingerprint.orchestrator.taskflow

import android.app.Activity
import android.content.Intent
import com.simprints.fingerprint.activities.alert.result.AlertTaskResult
import com.simprints.fingerprint.activities.collect.request.CollectFingerprintsTaskRequest
import com.simprints.fingerprint.activities.collect.result.CollectFingerprintsTaskResult
import com.simprints.fingerprint.activities.launch.request.LaunchTaskRequest
import com.simprints.fingerprint.activities.matching.request.MatchingTaskIdentifyRequest
import com.simprints.fingerprint.activities.matching.request.MatchingTaskVerifyRequest
import com.simprints.fingerprint.activities.matching.result.MatchingTaskIdentifyResult
import com.simprints.fingerprint.activities.matching.result.MatchingTaskVerifyResult
import com.simprints.fingerprint.activities.refusal.result.RefusalTaskResult
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.DomainToFingerprintResponse
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintIdentifyRequest
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintRequest
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintVerifyRequest
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.MatchGroup
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.responses.*
import com.simprints.fingerprint.data.domain.refusal.toFingerprintRefusalFormReason
import com.simprints.fingerprint.data.domain.toAction
import com.simprints.fingerprint.orchestrator.FinalResult
import com.simprints.fingerprint.orchestrator.task.FingerprintTask
import com.simprints.fingerprint.orchestrator.task.ResultCode
import com.simprints.fingerprint.orchestrator.task.TaskResult
import com.simprints.moduleapi.fingerprint.responses.IFingerprintResponse

sealed class FingerprintTaskFlow {

    protected val taskResults: MutableMap<String, TaskResult> = mutableMapOf()

    protected lateinit var fingerprintTasks: List<FingerprintTask>
    private var currentActivityTaskIndex = 0
    private var lastResultCode = ResultCode.OK

    fun getCurrentTask() = fingerprintTasks[currentActivityTaskIndex]

    fun isFlowFinished() = isFlowFinishedPrematurely() or isPastFinalTask()
    private fun isFlowFinishedPrematurely() = lastResultCode != ResultCode.OK
    private fun isPastFinalTask() = currentActivityTaskIndex >= fingerprintTasks.size

    abstract fun computeFlow(fingerprintRequest: FingerprintRequest)

    fun handleTaskResult(resultCode: ResultCode, getTaskResult: (bundleKey: String) -> TaskResult) {
        lastResultCode = resultCode
        when (resultCode) {
            ResultCode.OK -> {
                with(getCurrentTask()) {
                    taskResults[actResultKey] = getTaskResult(resultBundleKey)
                }
                currentActivityTaskIndex++
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

    fun getFinalResult() =
        when (lastResultCode) {
            ResultCode.OK -> getFinalOkResult()
            ResultCode.CANCELLED -> FinalResult(Activity.RESULT_CANCELED, null)
            ResultCode.ALERT -> FinalResult(Activity.RESULT_CANCELED, Intent().apply {
                putExtra(IFingerprintResponse.BUNDLE_KEY, DomainToFingerprintResponse.fromDomainToFingerprintErrorResponse(
                    with(taskResults[ALERT] as AlertTaskResult) {
                        FingerprintErrorReason.fromFingerprintAlertToErrorResponse(alert)
                    }
                ))
            })
            ResultCode.REFUSED -> FinalResult(Activity.RESULT_OK, Intent().apply {
                putExtra(IFingerprintResponse.BUNDLE_KEY, DomainToFingerprintResponse.fromDomainToFingerprintRefusalFormResponse(
                    with(taskResults[REFUSED] as RefusalTaskResult) {
                        FingerprintRefusalFormResponse(
                            answer.reason.toFingerprintRefusalFormReason(),
                            answer.optionalText
                        )
                    }
                ))
            })
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
                    FingerprintTask.Launch({
                        LaunchTaskRequest(
                            projectId, this.toAction(), language, logoExists, programName, organizationName
                        )
                    }, LAUNCH),
                    FingerprintTask.CollectFingerprints({
                        CollectFingerprintsTaskRequest(
                            projectId, userId, moduleId, this.toAction(), language, fingerStatus
                        )
                    }, COLLECT)
                )
            }
        }

        override fun getFinalOkResult(): FinalResult =
            FinalResult(Activity.RESULT_OK, Intent().apply {
                putExtra(IFingerprintResponse.BUNDLE_KEY, DomainToFingerprintResponse.fromDomainToFingerprintEnrolResponse(
                    FingerprintEnrolResponse((taskResults[COLLECT] as CollectFingerprintsTaskResult).probe.patientId))
                )
            })

        companion object {
            private const val LAUNCH = "launch"
            private const val COLLECT = "collect"
        }
    }

    class Identify : FingerprintTaskFlow() {

        override fun computeFlow(fingerprintRequest: FingerprintRequest) {
            with(fingerprintRequest as FingerprintIdentifyRequest) {
                fingerprintTasks = listOf(
                    FingerprintTask.Launch({
                        LaunchTaskRequest(
                            projectId, this.toAction(), language, logoExists, programName, organizationName
                        )
                    }, LAUNCH),
                    FingerprintTask.CollectFingerprints({
                        CollectFingerprintsTaskRequest(
                            projectId, userId, moduleId, this.toAction(), language, fingerStatus
                        )
                    }, COLLECT),
                    FingerprintTask.Matching({
                        with(taskResults[COLLECT] as CollectFingerprintsTaskResult) {
                            MatchingTaskIdentifyRequest(
                                language, probe, buildQueryForIdentifyPool(), returnIdCount
                            )
                        }
                    }, MATCHING)
                )
            }
        }

        override fun getFinalOkResult(): FinalResult =
            FinalResult(Activity.RESULT_OK, Intent().apply {
                putExtra(IFingerprintResponse.BUNDLE_KEY, DomainToFingerprintResponse.fromDomainToFingerprintIdentifyResponse(
                    FingerprintIdentifyResponse((taskResults[MATCHING] as MatchingTaskIdentifyResult).identifications)
                ))
            })

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
                    FingerprintTask.Launch({
                        LaunchTaskRequest(
                            projectId, this.toAction(), language, logoExists, programName, organizationName
                        )
                    }, LAUNCH),
                    FingerprintTask.CollectFingerprints({
                        CollectFingerprintsTaskRequest(
                            projectId, userId, moduleId, this.toAction(), language, fingerStatus
                        )
                    }, COLLECT),
                    FingerprintTask.Matching({
                        with(taskResults[COLLECT] as CollectFingerprintsTaskResult) {
                            MatchingTaskVerifyRequest(
                                language, probe, buildQueryForVerifyPool(), verifyGuid
                            )
                        }
                    }, MATCHING)
                )
            }
        }

        override fun getFinalOkResult(): FinalResult =
            FinalResult(Activity.RESULT_OK, Intent().apply {
                putExtra(IFingerprintResponse.BUNDLE_KEY, DomainToFingerprintResponse.fromDomainToFingerprintVerifyResponse(
                    with(taskResults[MATCHING] as MatchingTaskVerifyResult) {
                        FingerprintVerifyResponse(guid, confidence, tier)
                    }
                ))
            })

        private fun FingerprintVerifyRequest.buildQueryForVerifyPool() =
            MatchingTaskVerifyRequest.QueryForVerifyPool(projectId)

        companion object {
            private const val LAUNCH = "launch"
            private const val COLLECT = "collect"
            private const val MATCHING = "matching"
        }
    }
}
