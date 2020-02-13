package com.simprints.id.tools.extensions

import com.google.android.gms.tasks.Task
import com.simprints.core.tools.extentions.resumeSafely
import com.simprints.core.tools.extentions.resumeWithExceptionSafely
import kotlinx.coroutines.suspendCancellableCoroutine

suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { continuation ->
    this
        .addOnSuccessListener(continuation::resumeSafely)
        .addOnFailureListener {
            continuation.resumeWithExceptionSafely(it)
        }
        .addOnCanceledListener { continuation.cancel() }
}
