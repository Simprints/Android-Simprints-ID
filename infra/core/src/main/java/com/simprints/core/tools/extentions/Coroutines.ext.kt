package com.simprints.core.tools.extentions

import kotlinx.coroutines.CancellableContinuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

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
