package com.simprints.core.tools.extentions

import com.simprints.core.tools.coroutines.EspressoIdlingResource
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

fun CoroutineScope.launchIdling(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit
): Job {
    EspressoIdlingResource.increment()
    val job = this.launch(context, start, block)
    job.invokeOnCompletion { EspressoIdlingResource.decrement() }
    return job
}


fun <T> CancellableContinuation<T>.resumeSafely(queryResult: T) {
    if (this.isActive) {
        this.resume(queryResult)
    }
}

fun <T> CancellableContinuation<T>.resumeWithExceptionSafely(t: Throwable) {
    if (this.isActive) {
        this.resumeWithException(t)
    }
}
