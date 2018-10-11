package com.simprints.id.tools.extensions

import com.google.android.gms.tasks.Task
import kotlin.coroutines.experimental.suspendCoroutine

suspend fun <T> Task<T>.await(): T = suspendCoroutine { continuation ->
    addOnSuccessListener(continuation::resume)
    addOnFailureListener(continuation::resumeWithException)
}
