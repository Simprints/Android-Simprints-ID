package com.simprints.fingerprint.activities.orchestrator

import android.content.Context
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintRequest
import com.simprints.fingerprint.orchestrator.taskflow.FinalResult
import com.simprints.fingerprint.orchestrator.Orchestrator
import com.simprints.fingerprint.orchestrator.task.FingerprintTask
import com.simprints.fingerprint.orchestrator.task.ResultCode
import com.simprints.fingerprint.orchestrator.task.TaskResult

class OrchestratorViewModel : ViewModel() {

    internal val nextActivityCall = MutableLiveData<ActivityCall>()
    internal val finishedResult = MutableLiveData<ActivityResult>()

    private val orchestrator = Orchestrator(this)

    fun start(fingerprintRequest: FingerprintRequest) {
        orchestrator.start(fingerprintRequest)
    }

    internal fun handleActivityResult(activityResult: ActivityResult) {
        orchestrator.handleActivityTaskResult(
            ResultCode.fromValue(activityResult.resultCode),
            activityResult::toTaskResult
        )
    }

    fun postNextTask(activityTask: FingerprintTask.ActivityTask) {
        nextActivityCall.postValue(activityTask.toActivityCall())
    }

    fun handleFlowFinished(finalResult: FinalResult) {
        finishedResult.postValue(finalResult.toActivityResult())
    }

    internal data class ActivityCall(val requestCode: Int, val createIntent: (Context) -> Intent)

    private fun FingerprintTask.ActivityTask.toActivityCall() =
        ActivityCall(requestCode.value) { context ->
            Intent(context, targetClass).apply { putExtra(requestBundleKey, createTaskRequest()) }
        }

    internal data class ActivityResult(val resultCode: Int, val resultData: Intent?) {

        fun toTaskResult(bundleKey: String): TaskResult =
            resultData?.getParcelableExtra(bundleKey) ?: throw Throwable("Woops") // TODO
    }

    private fun FinalResult.toActivityResult() = ActivityResult(resultCode, resultData)
}
