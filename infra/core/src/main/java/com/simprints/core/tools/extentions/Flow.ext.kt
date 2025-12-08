package com.simprints.core.tools.extentions

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan

fun <T> Flow<T>.onChange(
    comparator: (T, T) -> Boolean,
    action: suspend (T) -> Unit,
) = windowed(2, partial = true).map { window ->
    val previousOrCurrent = window.first()
    val current = window.last()
    if (comparator(previousOrCurrent, current)) {
        action(current)
    }
    current
}

fun <T> Flow<T>.windowed(
    size: Int,
    partial: Boolean = false,
): Flow<List<T>> = scan(emptyList<T>()) { acc, value ->
    (acc + value).takeLast(size)
}.drop(
    if (partial) 1 else size,
)
