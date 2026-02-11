package com.simprints.feature.dashboard.settings.syncinfo.usecase.internal

import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.feature.dashboard.settings.syncinfo.SyncInfoSectionImages
import com.simprints.feature.dashboard.settings.syncinfo.SyncProgressInfo
import com.simprints.feature.dashboard.settings.syncinfo.SyncProgressInfoPart
import com.simprints.infra.eventsync.status.models.EventSyncState
import com.simprints.infra.sync.ImageSyncStatus
import com.simprints.infra.sync.SyncableCounts
import javax.inject.Inject
import kotlin.math.roundToInt

internal class GetSyncInfoSectionImagesUseCase @Inject constructor(
    private val timeHelper: TimeHelper,
) {
    operator fun invoke(
        isOnline: Boolean,
        eventSyncState: EventSyncState,
        imageSyncStatus: ImageSyncStatus,
        syncableCounts: SyncableCounts,
    ): SyncInfoSectionImages {
        val imagesToUploadOrNull = if (imageSyncStatus.isSyncing) {
            null
        } else {
            syncableCounts.samplesToUpload // internal term is sample, user-facing (within sync info) term is image
        }
        val progress = getImageSyncProgress(imageSyncStatus)
        val imageLastSyncTimestamp = Timestamp(imageSyncStatus.lastUpdateTimeMillis ?: -1)
        val isReLoginRequired = eventSyncState.isSyncFailedBecauseReloginRequired()

        return SyncInfoSectionImages(
            counterImagesToUpload = imagesToUploadOrNull?.toString().orEmpty(),
            isInstructionDefaultVisible = !imageSyncStatus.isSyncing && isOnline,
            isInstructionOfflineVisible = !isOnline,
            isProgressVisible = imageSyncStatus.isSyncing,
            progress,
            isSyncButtonEnabled = isOnline && !isReLoginRequired,
            isFooterLastSyncTimeVisible = !imageSyncStatus.isSyncing && imageLastSyncTimestamp.ms >= 0,
            footerLastSyncMinutesAgo = timeHelper.readableBetweenNowAndTime(imageLastSyncTimestamp),
        )
    }

    private fun getImageSyncProgress(imageSyncStatus: ImageSyncStatus): SyncProgressInfo {
        if (!imageSyncStatus.isSyncing) {
            return SyncProgressInfo()
        }
        val (currentImages, totalImages) = imageSyncStatus.nonNegativeProgress

        val imageSyncProgressPart = SyncProgressInfoPart(
            isPending = false,
            isDone = false,
            areNumbersVisible = totalImages > 0,
            currentNumber = currentImages,
            totalNumber = totalImages,
        )
        return SyncProgressInfo(
            progressParts = listOf(imageSyncProgressPart), // that's the only part of progress here
            progressBarPercentage = (imageSyncStatus.normalizedProgressProportion * 100).roundToInt(),
        )
    }
}
