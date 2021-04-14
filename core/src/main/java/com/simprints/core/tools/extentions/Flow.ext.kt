package com.simprints.core.tools.extentions

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*

fun <T, R> Flow<T>.concurrentMap(
    dispatcher: CoroutineDispatcher,
    transform: suspend (T) -> R
): Flow<R> {
    return flatMapMerge { value ->
        flow { emit(transform(value)) }
    }.flowOn(dispatcher)
}


suspend fun <T> Flow<T>.toMutableList(destination: MutableList<T> = ArrayList()): MutableList<T> =
    toCollection(destination)
