package com.simprints.infra.sync

data class ImageSyncStatus(
    val isSyncing: Boolean,
    val progress: Pair<Int, Int>?,
    val secondsSinceLastUpdate: Long?,
)
