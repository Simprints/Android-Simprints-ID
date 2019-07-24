package com.simprints.fingerprint.orchestrator

import com.simprints.fingerprint.orchestrator.task.FingerprintTask
import com.simprints.fingerprint.orchestrator.task.TaskResult

class RunnableTaskDispatcher {

    fun runTask(task: FingerprintTask.RunnableTask,
                onResult: (getTaskResult: TaskResult) -> Unit) {
        val request = task.createTaskRequest()
        val result = task.runTask(request)
        onResult(result)
    }
}
