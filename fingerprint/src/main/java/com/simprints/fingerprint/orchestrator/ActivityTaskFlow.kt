package com.simprints.fingerprint.orchestrator

import android.app.Activity
import android.content.Intent
import com.simprints.fingerprint.activities.ActRequest
import com.simprints.fingerprint.activities.ActResult
import com.simprints.fingerprint.activities.collect.request.CollectFingerprintsActRequest
import com.simprints.fingerprint.activities.collect.result.CollectFingerprintsActResult
import com.simprints.fingerprint.activities.launch.request.LaunchActRequest
import com.simprints.fingerprint.activities.matching.request.MatchingActIdentifyRequest
import com.simprints.fingerprint.activities.matching.request.MatchingActVerifyRequest
import com.simprints.fingerprint.activities.matching.result.MatchingActIdentifyResult
import com.simprints.fingerprint.activities.matching.result.MatchingActVerifyResult
import com.simprints.fingerprint.activities.orchestrator.OrchestratorViewModel.ActivityResult
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.DomainToFingerprintResponse
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintIdentifyRequest
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintRequest
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintVerifyRequest
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.MatchGroup
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.responses.FingerprintEnrolResponse
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.responses.FingerprintIdentifyResponse
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.responses.FingerprintVerifyResponse
import com.simprints.fingerprint.data.domain.toAction
import com.simprints.moduleapi.fingerprint.responses.IFingerprintResponse

sealed class ActivityTaskFlow {

    protected val actResults: MutableMap<String, ActResult> = mutableMapOf()

    protected lateinit var activityTasks: List<ActivityTask<*, *>>
    private var currentActivityCallIndex = 0

    @Suppress("unchecked_cast")
    fun getCurrentActivity() = activityTasks[currentActivityCallIndex] as ActivityTask<ActRequest, ActResult>
    fun isFlowFinished() = currentActivityCallIndex >= activityTasks.size

    abstract fun computeFlow(fingerprintRequest: FingerprintRequest)
    abstract fun getFinalResult(): ActivityResult // TODO : should have clean separation between VM classes and here

    fun saveActResultAndCycle(actResult: (bundleKey: String) -> ActResult) {
        with(getCurrentActivity()) {
            saveActResult(actResult(resultBundleKey))
        }
        currentActivityCallIndex++
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
                }, { actResults["launch"] = it }),
                CollectFingerprints({
                    CollectFingerprintsActRequest(
                        projectId, userId, moduleId, this.toAction(), language, fingerStatus
                    )
                }, { actResults["collect"] = it })
            )
        }
    }

    override fun getFinalResult(): ActivityResult =
        ActivityResult(Activity.RESULT_OK, Intent().apply {
            putExtra(IFingerprintResponse.BUNDLE_KEY, DomainToFingerprintResponse.fromDomainToFingerprintEnrolResponse(
                FingerprintEnrolResponse((actResults["collect"] as CollectFingerprintsActResult).probe.patientId))
            )
        })
}

class Identify : ActivityTaskFlow() {

    override fun computeFlow(fingerprintRequest: FingerprintRequest) {
        with(fingerprintRequest as FingerprintIdentifyRequest) {
            activityTasks = listOf(
                Launch({
                    LaunchActRequest(
                        projectId, this.toAction(), language, logoExists, programName, organizationName
                    )
                }, { actResults["launch"] = it }),
                CollectFingerprints({
                    CollectFingerprintsActRequest(
                        projectId, userId, moduleId, this.toAction(), language, fingerStatus
                    )
                }, { actResults["collect"] = it }),
                Matching({
                    with(actResults["collect"] as CollectFingerprintsActResult) {
                        MatchingActIdentifyRequest(
                            language, probe, buildQueryForIdentifyPool(), returnIdCount
                        )
                    }
                }, { actResults["matching"] = it })
            )
        }
    }

    override fun getFinalResult(): ActivityResult =
        ActivityResult(Activity.RESULT_OK, Intent().apply {
            putExtra(IFingerprintResponse.BUNDLE_KEY, DomainToFingerprintResponse.fromDomainToFingerprintIdentifyResponse(
                FingerprintIdentifyResponse((actResults["matching"] as MatchingActIdentifyResult).identifications)
            ))
        })
}

class Verify : ActivityTaskFlow() {

    override fun computeFlow(fingerprintRequest: FingerprintRequest) {
        with(fingerprintRequest as FingerprintVerifyRequest) {
            activityTasks = listOf(
                Launch({
                    LaunchActRequest(
                        projectId, this.toAction(), language, logoExists, programName, organizationName
                    )
                }, { actResults["launch"] = it }),
                CollectFingerprints({
                    CollectFingerprintsActRequest(
                        projectId, userId, moduleId, this.toAction(), language, fingerStatus
                    )
                }, { actResults["collect"] = it }),
                Matching({
                    with(actResults["collect"] as CollectFingerprintsActResult) {
                        MatchingActVerifyRequest(
                            language, probe, buildQueryForVerifyPool(), verifyGuid
                        )
                    }
                }, { actResults["matching"] = it })
            )
        }
    }

    override fun getFinalResult(): ActivityResult =
        ActivityResult(Activity.RESULT_OK, Intent().apply {
            putExtra(IFingerprintResponse.BUNDLE_KEY, DomainToFingerprintResponse.fromDomainToFingerprintVerifyResponse(
                with(actResults["matching"] as MatchingActVerifyResult) {
                    FingerprintVerifyResponse(guid, confidence, tier)
                }
            ))
        })
}

fun FingerprintIdentifyRequest.buildQueryForIdentifyPool(): MatchingActIdentifyRequest.QueryForIdentifyPool =
    when (matchGroup) {
        MatchGroup.GLOBAL -> MatchingActIdentifyRequest.QueryForIdentifyPool(projectId)
        MatchGroup.USER -> MatchingActIdentifyRequest.QueryForIdentifyPool(projectId, userId = userId)
        MatchGroup.MODULE -> MatchingActIdentifyRequest.QueryForIdentifyPool(projectId, moduleId = moduleId)
    }

fun FingerprintVerifyRequest.buildQueryForVerifyPool(): MatchingActVerifyRequest.QueryForVerifyPool =
    MatchingActVerifyRequest.QueryForVerifyPool(projectId)
