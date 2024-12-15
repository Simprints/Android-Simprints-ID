package com.simprints.logging.persistent.tools

import javax.inject.Inject

/**
 * Module cannot rely on Kronos or core for timestamps,
 * but system clock is sufficient for the basic sorting and log pruning.
 */
internal class TimestampProvider @Inject constructor() {
    fun nowMs(): Long = System.currentTimeMillis()
}
