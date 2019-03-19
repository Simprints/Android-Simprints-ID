package com.simprints.id.exceptions.unexpected

class WorkerInjectionFailedException(message: String = "WorkerInjectionFailedException") : UnexpectedException(message) {

    companion object {
        inline fun <reified T> forWorker() =
            WorkerInjectionFailedException("Worker injection failed for worker: ${T::class.java.simpleName}")
    }
}
