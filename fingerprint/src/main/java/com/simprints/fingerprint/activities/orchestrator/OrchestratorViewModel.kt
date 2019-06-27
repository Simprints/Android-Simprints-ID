package com.simprints.fingerprint.activities.orchestrator

import android.content.Context
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.simprints.fingerprint.activities.collect.CollectFingerprintsActivity
import com.simprints.fingerprint.activities.launch.LaunchActivity
import com.simprints.fingerprint.activities.matching.MatchingActivity
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintEnrolRequest
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintIdentifyRequest
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintRequest
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintVerifyRequest

class OrchestratorViewModel : ViewModel() {

    val nextActivity = MutableLiveData<ActivityRequest>()
    val finishedResult = MutableLiveData<ActivityResult>()

    private lateinit var activityRequestFlow: List<ActivityRequest>
    private var nextActivityRequestIndex = 0

    fun start(fingerprintRequest: FingerprintRequest) {
        activityRequestFlow = fingerprintRequest.toActivityRequestFlow()
        cycleToNextActivityRequest()
    }

    fun handleActivityResult(activityResult: ActivityResult) {
        if (isFlowFinished()) {
            handleFlowFinished(activityResult)
        } else {
            cycleToNextActivityRequest()
        }
    }

    private fun isFlowFinished() = nextActivityRequestIndex >= activityRequestFlow.size

    private fun cycleToNextActivityRequest() {
        nextActivity.postValue(activityRequestFlow[nextActivityRequestIndex++])
    }

    private fun handleFlowFinished(activityResult: ActivityResult) {
        finishedResult.postValue(activityResult)
    }
}

fun FingerprintRequest.toActivityRequestFlow(): List<ActivityRequest> =
    when (this) {
        is FingerprintEnrolRequest, is FingerprintVerifyRequest -> listOf(
            ActivityRequest.Launch(this),
            ActivityRequest.CollectFingerprints(this)
        )
        is FingerprintIdentifyRequest -> listOf(
            ActivityRequest.Launch(this),
            ActivityRequest.CollectFingerprints(this),
            ActivityRequest.Matching(this)
        )
        else -> throw Throwable("Woops") // TODO
    }

sealed class ActivityRequest(
    private val fingerprintRequest: FingerprintRequest,
    private val targetClass: Class<*>,
    val resultCode: Int
) {

    class Launch(fingerprintRequest: FingerprintRequest) :
        ActivityRequest(fingerprintRequest, LaunchActivity::class.java, 0)

    class CollectFingerprints(fingerprintRequest: FingerprintRequest) :
        ActivityRequest(fingerprintRequest, CollectFingerprintsActivity::class.java, 1)

    class Matching(fingerprintRequest: FingerprintRequest) :
        ActivityRequest(fingerprintRequest, MatchingActivity::class.java, 2)

    fun toIntent(packageContext: Context) =
        Intent(packageContext, targetClass).apply {
            putExtra(FingerprintRequest.BUNDLE_KEY, fingerprintRequest)
        }
}

data class ActivityResult(val resultCode: Int, val resultData: Intent?)
