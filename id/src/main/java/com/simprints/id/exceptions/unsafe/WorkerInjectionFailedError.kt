package com.simprints.id.exceptions.unsafe

class WorkerInjectionFailedError(message: String = "WorkerInjectionFailedError") : SimprintsError(message) {

    companion object {
        inline fun <reified T> forWorker() =
            WorkerInjectionFailedError("Worker injection failed for worker: ${T::class.java.simpleName}")
    }
}
