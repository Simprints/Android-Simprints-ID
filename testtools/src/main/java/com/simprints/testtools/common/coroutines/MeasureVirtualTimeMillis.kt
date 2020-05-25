package com.simprints.testtools.common.coroutines

import kotlinx.coroutines.test.TestCoroutineScope

inline fun TestCoroutineScope.measureVirtualTimeMillis(block: () -> Unit): Long {
    val start = currentTime
    block()
    return currentTime - start
}
