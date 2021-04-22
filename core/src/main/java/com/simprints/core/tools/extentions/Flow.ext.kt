package com.simprints.core.tools.extentions

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

@FlowPreview
fun <T, R> Flow<T>.concurrentMap(
    dispatcher: CoroutineDispatcher,
    transform: suspend (T) -> R
): Flow<R> {
    return flatMapMerge { value ->
        flow { emit(transform(value)) }
    }.flowOn(dispatcher)
}
