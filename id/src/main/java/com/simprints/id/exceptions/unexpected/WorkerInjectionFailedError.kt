package com.simprints.id.exceptions.unexpected

class WorkerInjectionFailedError(message: String = "WorkerInjectionFailedError") : UnexpectedException(message) {

    companion object {
        inline fun <reified T> forWorker() =
            WorkerInjectionFailedError("Worker injection failed for worker: ${T::class.java.simpleName}")
    }
}
