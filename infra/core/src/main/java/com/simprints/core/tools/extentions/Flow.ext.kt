package com.simprints.core.tools.extentions

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan

fun <T1, T2, T3, T4, T5, T6, T7, T8, R> combine8(
    flow1: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    flow4: Flow<T4>,
    flow5: Flow<T5>,
    flow6: Flow<T6>,
    flow7: Flow<T7>,
    flow8: Flow<T8>,
    transform: suspend (T1, T2, T3, T4, T5, T6, T7, T8) -> R
): Flow<R> = combine(flow1, flow2, flow3, flow4, flow5, flow6, flow7, flow8) { args: Array<*> ->
    transform(
        args[0] as T1,
        args[1] as T2,
        args[2] as T3,
        args[3] as T4,
        args[4] as T5,
        args[5] as T6,
        args[6] as T7,
        args[7] as T8,
    )
}

fun <T> Flow<T>.onChange(comparator: (T, T) -> Boolean, action: suspend (T) -> Unit) =
    windowed(2, partial = true).map { window ->
        val previousOrCurrent = window.first()
        val current = window.last()
        if (comparator(previousOrCurrent, current)) {
            action(current)
        }
        current
    }

fun <T> Flow<T>.windowed(size: Int, partial: Boolean = false): Flow<List<T>> =
    scan(emptyList<T>()) { acc, value ->
        (acc + value).takeLast(size)
    }.drop(
        if (partial) 1 else size
    )
