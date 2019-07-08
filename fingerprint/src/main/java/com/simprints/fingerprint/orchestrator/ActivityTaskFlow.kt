package com.simprints.fingerprint.orchestrator

import android.app.Activity
import android.content.Intent
import com.simprints.fingerprint.activities.ActResult
import com.simprints.fingerprint.activities.alert.result.AlertActResult
import com.simprints.fingerprint.activities.collect.request.CollectFingerprintsActRequest
import com.simprints.fingerprint.activities.collect.result.CollectFingerprintsActResult
import com.simprints.fingerprint.activities.launch.request.LaunchActRequest
import com.simprints.fingerprint.activities.matching.request.MatchingActIdentifyRequest
import com.simprints.fingerprint.activities.matching.request.MatchingActVerifyRequest
import com.simprints.fingerprint.activities.matching.result.MatchingActIdentifyResult
import com.simprints.fingerprint.activities.matching.result.MatchingActVerifyResult
import com.simprints.fingerprint.activities.orchestrator.OrchestratorViewModel.ActivityResult
import com.simprints.fingerprint.activities.refusal.result.RefusalActResult
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.DomainToFingerprintResponse
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.*
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.responses.*
import com.simprints.fingerprint.data.domain.refusal.toFingerprintRefusalFormReason
import com.simprints.fingerprint.data.domain.toAction
import com.simprints.moduleapi.fingerprint.responses.IFingerprintResponse

sealed class ActivityTaskFlow {

    protected val actResults: MutableMap<String, ActResult> = mutableMapOf()

    protected lateinit var activityTasks: List<ActivityTask>
    private var currentActivityTaskIndex = 0
    private var lastResultCode = ResultCode.OK

    fun getCurrentActivityTask() = activityTasks[currentActivityTaskIndex]

    fun isFlowFinished() = isFlowFinishedPrematurely() || currentActivityTaskIndex >= activityTasks.size
    private fun isFlowFinishedPrematurely() = lastResultCode != ResultCode.OK

    abstract fun computeFlow(fingerprintRequest: FingerprintRequest)
    protected abstract fun getFinalOkResult(): ActivityResult // TODO : should have clean separation between VM classes and here. Perhaps a dedicated class for arranging this result for the Platform

    fun handleActResult(resultCode: ResultCode, getActResult: (bundleKey: String) -> ActResult) {
        lastResultCode = resultCode
        when (resultCode) {
            ResultCode.OK -> {
                with(getCurrentActivityTask()) {
                    actResults[actResultKey] = getActResult(resultBundleKey)
                }
                currentActivityTaskIndex++
            }
            ResultCode.CANCELLED -> {
            }
            ResultCode.ALERT -> {
                actResults[ALERT] = getActResult(AlertActResult.BUNDLE_KEY)
            }
            ResultCode.REFUSED -> {
                actResults[REFUSED] = getActResult(RefusalActResult.BUNDLE_KEY)
            }
        }
    }

    fun getFinalResult() =
        when (lastResultCode) {
            ResultCode.OK -> getFinalOkResult()
            ResultCode.CANCELLED -> ActivityResult(Activity.RESULT_CANCELED, null)
            ResultCode.ALERT -> ActivityResult(Activity.RESULT_CANCELED, Intent().apply {
                putExtra(IFingerprintResponse.BUNDLE_KEY, DomainToFingerprintResponse.fromDomainToFingerprintErrorResponse(
                    with(actResults[ALERT] as AlertActResult) {
                        FingerprintErrorReason.fromFingerprintAlertToErrorResponse(alert)
                    }
                ))
            })
            ResultCode.REFUSED -> ActivityResult(Activity.RESULT_OK, Intent().apply {
                putExtra(IFingerprintResponse.BUNDLE_KEY, DomainToFingerprintResponse.fromDomainToFingerprintRefusalFormResponse(
                    with(actResults[REFUSED] as RefusalActResult) {
                        FingerprintRefusalFormResponse(
                            answer.reason.toFingerprintRefusalFormReason(),
                            answer.optionalText
                        )
                    }
                ))
            })
        }

    companion object {
        private const val REFUSED = "refused"
        private const val ALERT = "alert"
    }
}

class Enrol : ActivityTaskFlow() {

    override fun computeFlow(fingerprintRequest: FingerprintRequest) {
        with(fingerprintRequest) {
            activityTasks = listOf(
                Launch({
                    LaunchActRequest(
                        projectId, this.toAction(), language, logoExists, programName, organizationName
                    )
                }, LAUNCH),
                CollectFingerprints({
                    CollectFingerprintsActRequest(
                        projectId, userId, moduleId, this.toAction(), language, fingerStatus
                    )
                }, COLLECT)
            )
        }
    }

    override fun getFinalOkResult(): ActivityResult =
        ActivityResult(Activity.RESULT_OK, Intent().apply {
            putExtra(IFingerprintResponse.BUNDLE_KEY, DomainToFingerprintResponse.fromDomainToFingerprintEnrolResponse(
                FingerprintEnrolResponse((actResults[COLLECT] as CollectFingerprintsActResult).probe.patientId))
            )
        })

    companion object {
        private const val LAUNCH = "launch"
        private const val COLLECT = "collect"
    }
}

class Identify : ActivityTaskFlow() {

    override fun computeFlow(fingerprintRequest: FingerprintRequest) {
        with(fingerprintRequest as FingerprintIdentifyRequest) {
            activityTasks = listOf(
                Launch({
                    LaunchActRequest(
                        projectId, this.toAction(), language, logoExists, programName, organizationName
                    )
                }, LAUNCH),
                CollectFingerprints({
                    CollectFingerprintsActRequest(
                        projectId, userId, moduleId, this.toAction(), language, fingerStatus
                    )
                }, COLLECT),
                Matching({
                    with(actResults[COLLECT] as CollectFingerprintsActResult) {
                        MatchingActIdentifyRequest(
                            language, probe, buildQueryForIdentifyPool(), returnIdCount
                        )
                    }
                }, MATCHING)
            )
        }
    }

    override fun getFinalOkResult(): ActivityResult =
        ActivityResult(Activity.RESULT_OK, Intent().apply {
            putExtra(IFingerprintResponse.BUNDLE_KEY, DomainToFingerprintResponse.fromDomainToFingerprintIdentifyResponse(
                FingerprintIdentifyResponse((actResults[MATCHING] as MatchingActIdentifyResult).identifications)
            ))
        })

    private fun FingerprintIdentifyRequest.buildQueryForIdentifyPool() =
        when (matchGroup) {
            MatchGroup.GLOBAL -> MatchingActIdentifyRequest.QueryForIdentifyPool(projectId)
            MatchGroup.USER -> MatchingActIdentifyRequest.QueryForIdentifyPool(projectId, userId = userId)
            MatchGroup.MODULE -> MatchingActIdentifyRequest.QueryForIdentifyPool(projectId, moduleId = moduleId)
        }

    companion object {
        private const val LAUNCH = "launch"
        private const val COLLECT = "collect"
        private const val MATCHING = "matching"
    }
}

class Verify : ActivityTaskFlow() {

    override fun computeFlow(fingerprintRequest: FingerprintRequest) {
        with(fingerprintRequest as FingerprintVerifyRequest) {
            activityTasks = listOf(
                Launch({
                    LaunchActRequest(
                        projectId, this.toAction(), language, logoExists, programName, organizationName
                    )
                }, LAUNCH),
                CollectFingerprints({
                    CollectFingerprintsActRequest(
                        projectId, userId, moduleId, this.toAction(), language, fingerStatus
                    )
                }, COLLECT),
                Matching({
                    with(actResults[COLLECT] as CollectFingerprintsActResult) {
                        MatchingActVerifyRequest(
                            language, probe, buildQueryForVerifyPool(), verifyGuid
                        )
                    }
                }, MATCHING)
            )
        }
    }

    override fun getFinalOkResult(): ActivityResult =
        ActivityResult(Activity.RESULT_OK, Intent().apply {
            putExtra(IFingerprintResponse.BUNDLE_KEY, DomainToFingerprintResponse.fromDomainToFingerprintVerifyResponse(
                with(actResults[MATCHING] as MatchingActVerifyResult) {
                    FingerprintVerifyResponse(guid, confidence, tier)
                }
            ))
        })

    private fun FingerprintVerifyRequest.buildQueryForVerifyPool() =
        MatchingActVerifyRequest.QueryForVerifyPool(projectId)

    companion object {
        private const val LAUNCH = "launch"
        private const val COLLECT = "collect"
        private const val MATCHING = "matching"
    }
}

fun FingerprintRequest.toActivityTaskFlow(): ActivityTaskFlow =
    when (this) {
        is FingerprintEnrolRequest -> Enrol()
        is FingerprintIdentifyRequest -> Identify()
        is FingerprintVerifyRequest -> Verify()
        else -> throw Throwable("Woops") // TODO
    }.also { it.computeFlow(this) }
