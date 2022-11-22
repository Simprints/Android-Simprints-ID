package com.simprints.feature.dashboard.sync

import java.util.*

interface EventSyncCache {
    fun readLastSuccessfulSyncTime(): Date?
}
