package com.simprints.infra.sync.extensions

import kotlinx.coroutines.Job
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Waits for a Job to complete and rethrows failures (including cancellations).
 */
suspend fun Job.await() {
    suspendCancellableCoroutine { continuation ->
        val handle = invokeOnCompletion { cause ->
            when (cause) {
                null -> continuation.resume(Unit)
                else -> continuation.resumeWithException(cause)
            }
        }
        continuation.invokeOnCancellation { handle.dispose() }
    }
}
