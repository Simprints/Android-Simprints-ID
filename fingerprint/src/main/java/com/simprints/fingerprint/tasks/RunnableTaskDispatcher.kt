package com.simprints.fingerprint.tasks

import com.simprints.fingerprint.di.FingerprintComponent
import com.simprints.fingerprint.di.FingerprintComponentBuilder
import com.simprints.fingerprint.orchestrator.task.FingerprintTask
import com.simprints.fingerprint.orchestrator.task.TaskResult
import com.simprints.id.Application

class RunnableTaskDispatcher(private val component: FingerprintComponent) {

    fun runTask(task: FingerprintTask.RunnableTask,
                onResult: (getTaskResult: TaskResult) -> Unit) {
        val request = task.createTaskRequest()
        val result = task.runTask(component, request)
        onResult(result)
    }

    companion object {

        fun build(app: Application) =
            RunnableTaskDispatcher(FingerprintComponentBuilder.getComponent(app))
    }
}
