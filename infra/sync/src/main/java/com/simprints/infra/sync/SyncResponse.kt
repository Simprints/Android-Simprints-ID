package com.simprints.infra.sync

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class SyncResponse(
    val syncCommandJob: Job,
    val syncStatusFlow: StateFlow<SyncStatus>,
)

/**
 * Waits for the sync command job to complete, passes exceptions (incl. cancellations) to the caller.
 */
suspend fun SyncResponse.await() {
    suspendCancellableCoroutine { continuation ->
        val handle = syncCommandJob.invokeOnCompletion { cause ->
            when (cause) {
                null -> continuation.resume(Unit)
                else -> continuation.resumeWithException(cause)
            }
        }
        continuation.invokeOnCancellation { handle.dispose() }
    }
}
