package com.simprints.core.tools.extentions

import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow


/**
 * Runs the provided [handler] on a caught exception,
 * but still throws the exception that occurred.
 */

@OptIn(InternalCoroutinesApi::class)
fun <T> Flow<T>.onError(
    handler: suspend (cause: Throwable) -> Unit
): Flow<T> {
    return flow {
        try {
            collect { value ->
                emit(value)
            }
        } catch (e: Exception) {
            handler(e)
            throw e
        }
    }
}

/**
 * Runs the provided [action] only when a flow completes successfully,
 * if a cancellation or an exception occurs, the [action] is not run.
 */

@OptIn(InternalCoroutinesApi::class)
fun <T> Flow<T>.onSuccess(
    action: suspend () -> Unit
): Flow<T> {
    return flow {
        collect { value ->
            emit(value)
        }
        action()
    }
}
