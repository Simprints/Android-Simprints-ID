package com.simprints.fingerprint.activities.orchestrator

import android.content.Context
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintRequest
import com.simprints.fingerprint.exceptions.unexpected.result.NoTaskResultException
import com.simprints.fingerprint.orchestrator.Orchestrator
import com.simprints.fingerprint.orchestrator.domain.ResultCode
import com.simprints.fingerprint.orchestrator.models.FinalResult
import com.simprints.fingerprint.orchestrator.state.OrchestratorState
import com.simprints.fingerprint.orchestrator.task.FingerprintTask
import com.simprints.fingerprint.orchestrator.task.TaskResult
import com.simprints.fingerprint.infra.scanner.data.worker.FirmwareFileUpdateScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrchestratorViewModel @Inject constructor(
    private val orchestrator: Orchestrator,
    private val firmwareFileUpdateScheduler: FirmwareFileUpdateScheduler,
) : ViewModel() {

    val nextActivityCall = MutableLiveData<ActivityCall>()
    val finishedResult = MutableLiveData<ActivityResult>()

    fun start(fingerprintRequest: FingerprintRequest) {
        viewModelScope.launch {
            firmwareFileUpdateScheduler.scheduleOrCancelWorkIfNecessary()
            orchestrator.start(fingerprintRequest)
            executeNextTaskOrFinish()
        }
    }

    fun restoreState(orchestratorState: OrchestratorState) {
        orchestrator.restoreState(orchestratorState)
    }

    fun getState() = orchestrator.getState()

    fun handleActivityResult(activityResult: ActivityResult) {
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

    private fun executeNextTask() = postNextActivityTask(orchestrator.getNextTask())

    private fun postNextActivityTask(activityTask: FingerprintTask) {
        nextActivityCall.postValue(activityTask.toActivityCall())
    }

    private fun postFinishedResult(finalResult: FinalResult) {
        finishedResult.postValue(finalResult.toActivityResult())
    }

    data class ActivityCall(val requestCode: Int, val createIntent: (Context) -> Intent)

    private fun FingerprintTask.toActivityCall() =
        ActivityCall(requestCode.value) { context ->
            Intent(context, targetActivity).apply {
                putExtra(
                    requestBundleKey,
                    createTaskRequest()
                )
            }
        }

    data class ActivityResult(val resultCode: Int, val resultData: Intent?) {

        fun toTaskResult(bundleKey: String): TaskResult = resultData?.getParcelableExtra(bundleKey)
            ?: throw NoTaskResultException.inIntent(resultData)
    }

    private fun FinalResult.toActivityResult() = ActivityResult(resultCode, resultData)
}
