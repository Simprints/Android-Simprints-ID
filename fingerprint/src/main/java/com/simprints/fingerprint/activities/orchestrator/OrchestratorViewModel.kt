package com.simprints.fingerprint.activities.orchestrator

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.simprints.fingerprint.activities.ActRequest
import com.simprints.fingerprint.activities.ActResult
import com.simprints.fingerprint.activities.collect.CollectFingerprintsActivity
import com.simprints.fingerprint.activities.collect.request.CollectFingerprintsActRequest
import com.simprints.fingerprint.activities.collect.result.CollectFingerprintsActResult
import com.simprints.fingerprint.activities.launch.LaunchActivity
import com.simprints.fingerprint.activities.launch.request.LaunchActRequest
import com.simprints.fingerprint.activities.launch.request.toAction
import com.simprints.fingerprint.activities.launch.result.LaunchActResult
import com.simprints.fingerprint.activities.matching.MatchingActivity
import com.simprints.fingerprint.activities.matching.request.MatchingActIdentifyRequest
import com.simprints.fingerprint.activities.matching.request.MatchingActRequest
import com.simprints.fingerprint.activities.matching.request.MatchingActVerifyRequest
import com.simprints.fingerprint.activities.matching.result.MatchingActIdentifyResult
import com.simprints.fingerprint.activities.matching.result.MatchingActResult
import com.simprints.fingerprint.activities.matching.result.MatchingActVerifyResult
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.DomainToFingerprintResponse.fromDomainToFingerprintEnrolResponse
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.DomainToFingerprintResponse.fromDomainToFingerprintIdentifyResponse
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.DomainToFingerprintResponse.fromDomainToFingerprintVerifyResponse
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.*
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.responses.FingerprintEnrolResponse
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.responses.FingerprintIdentifyResponse
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.responses.FingerprintVerifyResponse
import com.simprints.moduleapi.fingerprint.responses.IFingerprintResponse

class OrchestratorViewModel : ViewModel() {

    val nextActivity = MutableLiveData<ActivityCall<*, *>>()
    val finishedResult = MutableLiveData<ActivityResult>()

    private lateinit var activityCallFlow: ActivityCallFlow


    fun start(fingerprintRequest: FingerprintRequest) {
        activityCallFlow = fingerprintRequest.toActivityCallFlow()
        cycleToNextActivityRequest()
    }

    fun handleActivityResult(activityResult: ActivityResult) {
        activityCallFlow.saveActResult(activityResult)
        if (activityCallFlow.isFlowFinished()) {
            handleFlowFinished()
        } else {
            cycleToNextActivityRequest()
        }
    }

    private fun cycleToNextActivityRequest() {
        nextActivity.postValue(activityCallFlow.getNextActivityCallAndCycle())
    }

    private fun handleFlowFinished() {
        finishedResult.postValue(activityCallFlow.getFinalResult())
    }
}

fun FingerprintRequest.toActivityCallFlow(): ActivityCallFlow =
    when (this) {
        is FingerprintEnrolRequest -> ActivityCallFlow.Enrol()
        is FingerprintIdentifyRequest -> ActivityCallFlow.Identify()
        is FingerprintVerifyRequest -> ActivityCallFlow.Verify()
        else -> throw Throwable("Woops") // TODO
    }.also { it.computeFlow(this) }

sealed class ActivityCallFlow {

    protected val actResults: MutableMap<String, ActResult> = mutableMapOf()

    protected lateinit var activityCalls: List<ActivityCall<*, *>>
    private var currentActivityCallIndex = 0

    fun getNextActivityCallAndCycle() = activityCalls[currentActivityCallIndex++]
    fun isFlowFinished() = currentActivityCallIndex >= activityCalls.size

    abstract fun computeFlow(fingerprintRequest: FingerprintRequest)
    abstract fun getFinalResult(): ActivityResult

    fun saveActResult(activityResult: ActivityResult) {
        with(activityCalls[currentActivityCallIndex]) {
            //            actResult(activityResult.toActResult(resultBundleKey))
            actResult(activityResult.resultData!!.getParcelableExtra(resultBundleKey)) // TODO
        }
    }

    class Enrol : ActivityCallFlow() {

        override fun computeFlow(fingerprintRequest: FingerprintRequest) {
            with(fingerprintRequest) {
                activityCalls = listOf(
                    Launch({
                        LaunchActRequest(
                            projectId, this.toAction(), language, logoExists, programName, organizationName
                        )
                    }, { actResults["launch"] = it }),
                    CollectFingerprints({
                        CollectFingerprintsActRequest(
                            projectId, userId, moduleId, fingerStatus
                        )
                    }, { actResults["collect"] = it })
                )
            }
        }

        override fun getFinalResult(): ActivityResult =
            ActivityResult(Activity.RESULT_OK, Intent().apply {
                putExtra(IFingerprintResponse.BUNDLE_KEY, fromDomainToFingerprintEnrolResponse(
                    FingerprintEnrolResponse((actResults["collect"] as CollectFingerprintsActResult).probe.patientId))
                )
            })
    }

    class Identify : ActivityCallFlow() {

        override fun computeFlow(fingerprintRequest: FingerprintRequest) {
            with(fingerprintRequest as FingerprintIdentifyRequest) {
                activityCalls = listOf(
                    Launch({
                        LaunchActRequest(
                            projectId, this.toAction(), language, logoExists, programName, organizationName
                        )
                    }, { actResults["launch"] = it }),
                    CollectFingerprints({
                        CollectFingerprintsActRequest(
                            projectId, userId, moduleId, fingerStatus
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
                putExtra(IFingerprintResponse.BUNDLE_KEY, fromDomainToFingerprintIdentifyResponse(
                    FingerprintIdentifyResponse((actResults["matching"] as MatchingActIdentifyResult).identifications)
                ))
            })
    }

    class Verify : ActivityCallFlow() {

        override fun computeFlow(fingerprintRequest: FingerprintRequest) {
            with(fingerprintRequest as FingerprintVerifyRequest) {
                activityCalls = listOf(
                    Launch({
                        LaunchActRequest(
                            projectId, this.toAction(), language, logoExists, programName, organizationName
                        )
                    }, { actResults["launch"] = it }),
                    CollectFingerprints({
                        CollectFingerprintsActRequest(
                            projectId, userId, moduleId, fingerStatus
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
                putExtra(IFingerprintResponse.BUNDLE_KEY, fromDomainToFingerprintVerifyResponse(
                    with(actResults["matching"] as MatchingActVerifyResult) {
                        FingerprintVerifyResponse(guid, confidence, tier)
                    }
                ))
            })
    }
}

sealed class ActivityCall<out Request : ActRequest, in Result : ActResult>(
    val actRequest: () -> Request,
    val actResult: (Result) -> Unit,
    private val targetClass: Class<*>,
    val requestCode: Int,
    private val requestBundleKey: String,
    val resultBundleKey: String
) {

    fun createRequestIntent(packageContext: Context) =
        Intent(packageContext, targetClass).apply {
            putExtra(requestBundleKey, actRequest())
        }
}

class Launch(launchActRequest: () -> LaunchActRequest, launchActResult: (LaunchActResult) -> Unit) :
    ActivityCall<LaunchActRequest, LaunchActResult>(
        launchActRequest,
        launchActResult,
        LaunchActivity::class.java,
        0,
        LaunchActRequest.BUNDLE_KEY,
        LaunchActResult.BUNDLE_KEY
    )

class CollectFingerprints(collectFingerprintsActRequest: () -> CollectFingerprintsActRequest,
                          collectFingerprintsActResult: (CollectFingerprintsActResult) -> Unit) :
    ActivityCall<CollectFingerprintsActRequest, CollectFingerprintsActResult>(
        collectFingerprintsActRequest,
        collectFingerprintsActResult,
        CollectFingerprintsActivity::class.java,
        1,
        LaunchActRequest.BUNDLE_KEY,
        LaunchActResult.BUNDLE_KEY
    )

class Matching(matchingActRequest: () -> MatchingActRequest, matchingActResult: (MatchingActResult) -> Unit) :
    ActivityCall<MatchingActRequest, MatchingActResult>(
        matchingActRequest,
        matchingActResult,
        MatchingActivity::class.java,
        2,
        MatchingActRequest.BUNDLE_KEY,
        MatchingActResult.BUNDLE_KEY
    )

data class ActivityResult(val resultCode: Int, val resultData: Intent?)

fun ActivityResult.toActResult(bundleKey: String): ActResult =
    resultData?.getParcelableExtra(bundleKey) ?: throw Throwable("Woops") // TODO

fun FingerprintIdentifyRequest.buildQueryForIdentifyPool(): MatchingActIdentifyRequest.QueryForIdentifyPool =
    when (matchGroup) {
        MatchGroup.GLOBAL -> MatchingActIdentifyRequest.QueryForIdentifyPool(projectId)
        MatchGroup.USER -> MatchingActIdentifyRequest.QueryForIdentifyPool(projectId, userId = userId)
        MatchGroup.MODULE -> MatchingActIdentifyRequest.QueryForIdentifyPool(projectId, moduleId = moduleId)
    }

fun FingerprintVerifyRequest.buildQueryForVerifyPool(): MatchingActVerifyRequest.QueryForVerifyPool =
    MatchingActVerifyRequest.QueryForVerifyPool(projectId)
