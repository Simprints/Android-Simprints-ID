package com.simprints.infra.sync

data class ImageSyncStatus(
    val isSyncing: Boolean,
    val progress: Pair<Int, Int>?,
    val lastUpdateTimeMillis: Long?,
) {
    val nonNegativeProgress: Pair<Int, Int>
        get() = with(progress) {
            if (this != null) {
                first.coerceAtLeast(0) to second.coerceAtLeast(0)
            } else {
                0 to 0
            }
        }

    val normalizedProgressProportion: Float
        get() = nonNegativeProgress.let { (current, total) ->
            when {
                !isSyncing -> 0f
                total == 0 -> 1f // nothing to sync? - done
                else -> (current.toFloat() / total).coerceIn(0f, 1f)
            }
        }
}
