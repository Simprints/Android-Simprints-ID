package com.simprints.fingerprint.activities.orchestrator

import android.content.Context
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.simprints.fingerprint.activities.ActResult
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintRequest
import com.simprints.fingerprint.orchestrator.ActivityTask
import com.simprints.fingerprint.orchestrator.ActivityTaskFlow
import com.simprints.fingerprint.orchestrator.ResultCode
import com.simprints.fingerprint.orchestrator.toActivityTaskFlow

class OrchestratorViewModel : ViewModel() {

    val nextActivityCall = MutableLiveData<ActivityCall>()
    val finishedResult = MutableLiveData<ActivityResult>()

    private lateinit var activityTaskFlow: ActivityTaskFlow

    fun start(fingerprintRequest: FingerprintRequest) {
        activityTaskFlow = fingerprintRequest.toActivityTaskFlow()
        postNextActivityCall()
    }

    fun handleActivityResult(activityResult: ActivityResult) { // TODO : move content of this function to orchestrator?
        activityTaskFlow.handleActResult(
            ResultCode.fromValue(activityResult.resultCode),
            activityResult::toActResult
        )
        if (activityTaskFlow.isFlowFinished()) {
            handleFlowFinished()
        } else {
            postNextActivityCall()
        }
    }

    private fun postNextActivityCall() {
        nextActivityCall.postValue(activityTaskFlow.getCurrentActivityTask().toActivityCall())
    }

    private fun handleFlowFinished() {
        finishedResult.postValue(activityTaskFlow.getFinalResult())
    }

    data class ActivityCall(val requestCode: Int, val createIntent: (Context) -> Intent)

    private fun ActivityTask.toActivityCall() =
        ActivityCall(requestCode.value) { context ->
            Intent(context, targetClass).apply { putExtra(requestBundleKey, createActRequest()) }
        }

    data class ActivityResult(val resultCode: Int, val resultData: Intent?) {

        fun toActResult(bundleKey: String): ActResult =
            resultData?.getParcelableExtra(bundleKey) ?: throw Throwable("Woops") // TODO
    }
}
