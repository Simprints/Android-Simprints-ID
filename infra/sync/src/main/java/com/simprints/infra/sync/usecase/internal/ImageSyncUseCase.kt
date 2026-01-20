package com.simprints.infra.sync.usecase.internal

import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkQuery
import com.simprints.infra.sync.ImageSyncStatus
import com.simprints.infra.sync.ImageSyncTimestampProvider
import com.simprints.infra.sync.SyncConstants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transformLatest
import javax.inject.Inject

class ImageSyncUseCase @Inject constructor(
    private val workManager: WorkManager,
    private val imageSyncTimestampProvider: ImageSyncTimestampProvider,
) {
    internal operator fun invoke(): Flow<ImageSyncStatus> = workManager
        .getWorkInfosFlow(WorkQuery.fromUniqueWorkNames(SyncConstants.FILE_UP_SYNC_WORK_NAME))
        .associateWithIfSyncing()
        .map { (workInfos, isSyncing) ->
            val lastUpdateTimestamp = imageSyncTimestampProvider.getLastImageSyncTimestamp()
            val currentIndex = workInfos
                .firstOrNull()
                ?.progress
                ?.getInt(SyncConstants.PROGRESS_CURRENT, 0)
                ?.coerceAtLeast(0) ?: 0
            val totalCount = workInfos
                .firstOrNull()
                ?.progress
                ?.getInt(SyncConstants.PROGRESS_MAX, 0)
                ?.takeIf { it >= 1 }
            val progress = totalCount?.let { currentIndex to totalCount }
            ImageSyncStatus(isSyncing, progress, lastUpdateTimestamp)
        }.distinctUntilChanged()

    /**
     * Converts the flow of WorkInfo in the receiver into a flow of WorkInfo paired to whether sync is ongoing or not.
     *
     * Whether sync is ongoing or not - is calculated from the WorkInfo.
     * A special case is handled for a job that succeeds promptly: a "pulse" of positive sync is emitted additionally.
     * This allows immediately succeeding syncs to be detected in the return flow.
     */
    private fun Flow<List<WorkInfo>>.associateWithIfSyncing() = transformLatest { workInfos ->
        val isJustUpdated = imageSyncTimestampProvider.getMillisSinceLastImageSync() == 0L
        when {
            workInfos.any {
                it.state == WorkInfo.State.RUNNING
            } -> {
                emit(workInfos to true)
            }

            workInfos.any {
                it.state == WorkInfo.State.SUCCEEDED
            } &&
                isJustUpdated -> {
                emit(workInfos to true) // at least for a moment, in case if RUNNING was missed
                emit(workInfos to false)
            }

            else -> {
                emit(workInfos to false)
            }
        }
    }
}
