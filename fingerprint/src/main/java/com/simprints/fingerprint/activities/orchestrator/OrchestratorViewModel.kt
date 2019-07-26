package com.simprints.fingerprint.activities.orchestrator

import android.content.Context
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintRequest
import com.simprints.fingerprint.orchestrator.Orchestrator
import com.simprints.fingerprint.orchestrator.domain.ResultCode
import com.simprints.fingerprint.orchestrator.models.FinalResult
import com.simprints.fingerprint.orchestrator.task.FingerprintTask
import com.simprints.fingerprint.orchestrator.task.TaskResult
import com.simprints.fingerprint.tasks.RunnableTaskDispatcher

class OrchestratorViewModel(private val orchestrator: Orchestrator,
                            private val runnableTaskDispatcher: RunnableTaskDispatcher) : ViewModel() {

    internal val nextActivityCall = MutableLiveData<ActivityCall>()
    internal val finishedResult = MutableLiveData<ActivityResult>()

    fun start(fingerprintRequest: FingerprintRequest) {
        orchestrator.start(fingerprintRequest)
        executeNextTaskOrFinish()
    }

    internal fun handleActivityResult(activityResult: ActivityResult) {
        orchestrator.handleActivityTaskResult(
            ResultCode.fromValue(activityResult.resultCode),
            activityResult::toTaskResult
        )
        executeNextTaskOrFinish()
    }

    private fun executeNextTaskOrFinish() {
        if (orchestrator.isFinished()) {
            postFinishedResult(orchestrator.getFinalResult())
        } else {
            executeNextTask()
        }
    }

    private fun executeNextTask() =
        when (val task = orchestrator.getNextTask()) {
            is FingerprintTask.ActivityTask -> postNextTask(task)
            is FingerprintTask.RunnableTask -> runnableTaskDispatcher.runTask(task) {
                orchestrator.handleRunnableTaskResult(it)
                executeNextTaskOrFinish()
            }
        }

    private fun postNextTask(activityTask: FingerprintTask.ActivityTask) {
        nextActivityCall.postValue(activityTask.toActivityCall())
    }

    private fun postFinishedResult(finalResult: FinalResult) {
        finishedResult.postValue(finalResult.toActivityResult())
    }

    internal data class ActivityCall(val requestCode: Int, val createIntent: (Context) -> Intent)

    private fun FingerprintTask.ActivityTask.toActivityCall() =
        ActivityCall(requestCode.value) { context ->
            Intent(context, targetActivity).apply { putExtra(requestBundleKey, createTaskRequest()) }
        }

    internal data class ActivityResult(val resultCode: Int, val resultData: Intent?) {

        fun toTaskResult(bundleKey: String): TaskResult =
            resultData?.getParcelableExtra(bundleKey) ?: throw Throwable("Woops") // TODO
    }

    private fun FinalResult.toActivityResult() = ActivityResult(resultCode, resultData)
}
