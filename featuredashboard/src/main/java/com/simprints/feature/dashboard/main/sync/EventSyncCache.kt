package com.simprints.feature.dashboard.main.sync

import java.util.*

interface EventSyncCache {
    suspend fun readLastSuccessfulSyncTime(): Date?
}
