package com.simprints.feature.dashboard.settings.syncinfo.usecase.internal

import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.feature.dashboard.settings.syncinfo.SyncInfoProgress
import com.simprints.feature.dashboard.settings.syncinfo.SyncInfoProgressPart
import com.simprints.feature.dashboard.settings.syncinfo.SyncInfoSectionImages
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
        val currentImages = imageSyncStatus.progress?.first?.coerceAtLeast(0) ?: 0
        val totalImages = imageSyncStatus.progress?.second?.takeIf { it >= 1 } ?: 0

        val imageProgressProportion = calculateProportion(currentImages, totalImages)

        val imagesNormalizedProgress = if (imageSyncStatus.isSyncing && totalImages > 0) imageProgressProportion else 1f

        val imagesToUpload = if (imageSyncStatus.isSyncing) {
            null
        } else {
            syncableCounts.samplesToUpload // internal term is sample, user-facing (within sync info) term is image
        }

        val imageSyncProgressPart = SyncInfoProgressPart(
            isPending = eventSyncState.isSyncInProgress() && !imageSyncStatus.isSyncing,
            isDone = !eventSyncState.isSyncInProgress() && !imageSyncStatus.isSyncing && imagesToUpload == 0,
            areNumbersVisible = imageSyncStatus.isSyncing && totalImages > 0,
            currentNumber = currentImages,
            totalNumber = totalImages,
        )

        val imageSyncProgress = if (imageSyncStatus.isSyncing) {
            SyncInfoProgress(
                progressParts = listOf(imageSyncProgressPart),
                progressBarPercentage = (imagesNormalizedProgress * 100).roundToInt(),
            )
        } else {
            SyncInfoProgress()
        }

        val imageLastSyncTimestamp = Timestamp(imageSyncStatus.lastUpdateTimeMillis ?: -1)

        val isReLoginRequired = eventSyncState.isSyncFailedBecauseReloginRequired()

        return SyncInfoSectionImages(
            counterImagesToUpload = imagesToUpload?.toString().orEmpty(),
            isInstructionDefaultVisible = !imageSyncStatus.isSyncing && isOnline,
            isInstructionOfflineVisible = !isOnline,
            isProgressVisible = imageSyncStatus.isSyncing,
            progress = imageSyncProgress,
            isSyncButtonEnabled = isOnline && !isReLoginRequired,
            isFooterLastSyncTimeVisible = !imageSyncStatus.isSyncing && imageLastSyncTimestamp.ms >= 0,
            footerLastSyncMinutesAgo = timeHelper.readableBetweenNowAndTime(imageLastSyncTimestamp),
        )
    }

    private fun calculateProportion(
        current: Int,
        total: Int,
    ): Float = if (total == 0) 0f else (current.toFloat() / total).coerceIn(0f, 1f)
}
