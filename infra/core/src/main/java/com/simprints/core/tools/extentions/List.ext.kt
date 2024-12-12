package com.simprints.core.tools.extentions

fun <T> List<T>.updateOnIndex(
    index: Int,
    newItem: (T) -> T,
): List<T> = mapIndexed { i, item ->
    when (i) {
        index -> newItem(item)
        else -> item
    }
}
