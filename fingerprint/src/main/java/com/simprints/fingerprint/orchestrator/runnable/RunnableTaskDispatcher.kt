package com.simprints.fingerprint.orchestrator.runnable

import com.simprints.fingerprint.controllers.fingerprint.config.ConfigurationController
import com.simprints.fingerprint.controllers.fingerprint.config.ConfigurationTaskRequest
import com.simprints.fingerprint.exceptions.unexpected.FingerprintUnexpectedException
import com.simprints.fingerprint.orchestrator.task.FingerprintTask
import com.simprints.fingerprint.orchestrator.task.TaskResult
import timber.log.Timber

class RunnableTaskDispatcher(
    private val configurationController: ConfigurationController
) {

    fun dispatch(runnableTask: FingerprintTask.RunnableTask, withResult: (TaskResult?) -> Unit) {
        try {
            val result = when (runnableTask) {
                is FingerprintTask.Configuration ->
                    configurationController.runConfiguration(runnableTask.createTaskRequest() as ConfigurationTaskRequest)
                else -> throw FingerprintUnexpectedException("Unknown task $runnableTask for RunnableTaskDispatcher")
            }
            withResult(result)
        } catch (e: Throwable) {
            Timber.e(e, "Error running task $runnableTask")
            withResult(null)
        }
    }
}
