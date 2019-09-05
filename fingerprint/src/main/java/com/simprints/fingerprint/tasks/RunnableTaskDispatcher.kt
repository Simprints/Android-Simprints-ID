package com.simprints.fingerprint.tasks

import com.simprints.fingerprint.exceptions.unexpected.request.InvalidRunnableTaskRequestException
import com.simprints.fingerprint.orchestrator.task.FingerprintTask
import com.simprints.fingerprint.orchestrator.task.TaskResult
import com.simprints.fingerprint.tasks.saveperson.SavePersonTask
import com.simprints.fingerprint.tasks.saveperson.SavePersonTaskRequest
import org.koin.core.KoinComponent
import org.koin.core.get

class RunnableTaskDispatcher: KoinComponent {

    fun runTask(task: FingerprintTask.RunnableTask,
                onResult: (getTaskResult: TaskResult) -> Unit) {

        val result: TaskResult = when (val request = task.createTaskRequest()) {
            is SavePersonTaskRequest -> SavePersonTask(request, get()).savePerson()
            else -> throw InvalidRunnableTaskRequestException()
        }

        onResult(result)
    }
}
