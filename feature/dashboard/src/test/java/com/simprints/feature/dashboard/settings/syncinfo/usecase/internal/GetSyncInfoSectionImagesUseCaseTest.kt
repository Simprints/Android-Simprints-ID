package com.simprints.feature.dashboard.settings.syncinfo.usecase.internal

import com.google.common.truth.Truth.*
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.eventsync.status.models.EventSyncState
import com.simprints.infra.sync.ImageSyncStatus
import com.simprints.infra.sync.SyncableCounts
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

internal class GetSyncInfoSectionImagesUseCaseTest {
    private val timeHelper = mockk<TimeHelper>()
    private val mockEventSyncState = mockk<EventSyncState>(relaxed = true) {
        every { isSyncInProgress() } returns false
        every { isSyncFailedBecauseReloginRequired() } returns false
    }
    private val mockImageSyncStatus = mockk<ImageSyncStatus>(relaxed = true) {
        every { isSyncing } returns false
        every { progress } returns null
        every { lastUpdateTimeMillis } returns null
    }
    private val mockSyncableCounts = SyncableCounts(
        totalRecords = 0,
        recordEventsToDownload = 0,
        isRecordEventsToDownloadLowerBound = false,
        eventsToUpload = 0,
        enrolmentsToUpload = 0,
        samplesToUpload = 0,
    )

    private lateinit var useCase: GetSyncInfoSectionImagesUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        every { timeHelper.readableBetweenNowAndTime(any()) } returns "0 minutes ago"

        useCase = GetSyncInfoSectionImagesUseCase(timeHelper)
    }

    @Test
    fun `should emit SyncInfo section with correct syncInfoSectionImages instruction visibility`() = runTest {
        val mockNotSyncingImageStatus = mockk<ImageSyncStatus>(relaxed = true) {
            every { isSyncing } returns false
            every { progress } returns null
        }

        val result = useCase(
            isOnline = false,
            eventSyncState = mockEventSyncState,
            imageSyncStatus = mockNotSyncingImageStatus,
            syncableCounts = mockSyncableCounts,
        )

        assertThat(result.isInstructionOfflineVisible).isTrue()
        assertThat(result.isInstructionDefaultVisible).isFalse()
    }

    @Test
    fun `should emit SyncInfo section with correct syncInfoSectionImages button states`() = runTest {
        val mockNormalEventSyncState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncFailedBecauseReloginRequired() } returns false
        }

        val result = useCase(
            isOnline = true,
            eventSyncState = mockNormalEventSyncState,
            imageSyncStatus = mockImageSyncStatus,
            syncableCounts = mockSyncableCounts,
        )

        assertThat(result.isSyncButtonEnabled).isTrue()
    }

    @Test
    fun `should emit SyncInfo section with correct syncInfoSectionImages footer states`() = runTest {
        val mockImageStatusWithLastSync = mockk<ImageSyncStatus>(relaxed = true) {
            every { isSyncing } returns false
            every { progress } returns null
            every { lastUpdateTimeMillis } returns 120_000
        }
        every { timeHelper.readableBetweenNowAndTime(Timestamp(120 * 1000)) } returns "2 minutes ago"

        val result = useCase(
            isOnline = true,
            eventSyncState = mockEventSyncState,
            imageSyncStatus = mockImageStatusWithLastSync,
            syncableCounts = mockSyncableCounts,
        )

        assertThat(result.isFooterLastSyncTimeVisible).isTrue()
        assertThat(result.footerLastSyncMinutesAgo).isEqualTo("2 minutes ago")
    }

    @Test
    fun `should calculate correct image sync progress when images syncing`() = runTest {
        val imageSyncStatus = ImageSyncStatus(
            isSyncing = true,
            progress = Pair(3, 10),
            lastUpdateTimeMillis = null,
        )

        val result = useCase(
            isOnline = true,
            eventSyncState = mockEventSyncState,
            imageSyncStatus = imageSyncStatus,
            syncableCounts = mockSyncableCounts,
        )

        assertThat(result.isProgressVisible).isTrue()
        assertThat(result.progress.progressBarPercentage).isEqualTo(30)
    }

    @Test
    fun `should calculate correct image sync progress when images not syncing`() = runTest {
        val mockNotSyncingImageStatus = mockk<ImageSyncStatus>(relaxed = true) {
            every { isSyncing } returns false
            every { progress } returns null
        }

        val result = useCase(
            isOnline = true,
            eventSyncState = mockEventSyncState,
            imageSyncStatus = mockNotSyncingImageStatus,
            syncableCounts = mockSyncableCounts,
        )

        assertThat(result.isProgressVisible).isFalse()
        assertThat(result.progress.progressBarPercentage).isEqualTo(0)
    }

    @Test
    fun `should emit SyncInfo section with correct images to upload counter when sync not in progress`() = runTest {
        val mockNotSyncingImageStatus = mockk<ImageSyncStatus>(relaxed = true) {
            every { isSyncing } returns false
            every { progress } returns null
        }
        val syncableCounts = mockSyncableCounts.copy(samplesToUpload = 15)

        val result = useCase(
            isOnline = true,
            eventSyncState = mockEventSyncState,
            imageSyncStatus = mockNotSyncingImageStatus,
            syncableCounts = syncableCounts,
        )

        assertThat(result.counterImagesToUpload).isEqualTo("15")
    }

    @Test
    fun `should emit SyncInfo section with empty images counter when sync in progress`() = runTest {
        val imageSyncStatus = ImageSyncStatus(
            isSyncing = true,
            progress = null,
            lastUpdateTimeMillis = null,
        )

        val result = useCase(
            isOnline = true,
            eventSyncState = mockEventSyncState,
            imageSyncStatus = imageSyncStatus,
            syncableCounts = mockSyncableCounts,
        )

        assertThat(result.counterImagesToUpload).isEmpty()
    }

    @Test
    fun `should calculate correct image last sync time when available`() = runTest {
        val mockImageStatusWithLastSync = mockk<ImageSyncStatus>(relaxed = true) {
            every { isSyncing } returns false
            every { progress } returns null
            every { lastUpdateTimeMillis } returns 180_000
        }
        every { timeHelper.readableBetweenNowAndTime(Timestamp(180 * 1000)) } returns "3 minutes ago"

        val result = useCase(
            isOnline = true,
            eventSyncState = mockEventSyncState,
            imageSyncStatus = mockImageStatusWithLastSync,
            syncableCounts = mockSyncableCounts,
        )

        assertThat(result.isFooterLastSyncTimeVisible).isTrue()
        assertThat(result.footerLastSyncMinutesAgo).isEqualTo("3 minutes ago")
    }

    @Test
    fun `should have hidden image last sync time footer when unavailable`() = runTest {
        val mockImageStatusWithoutLastSync = mockk<ImageSyncStatus>(relaxed = true) {
            every { isSyncing } returns false
            every { progress } returns null
            every { lastUpdateTimeMillis } returns null
        }

        val result = useCase(
            isOnline = true,
            eventSyncState = mockEventSyncState,
            imageSyncStatus = mockImageStatusWithoutLastSync,
            syncableCounts = mockSyncableCounts,
        )

        assertThat(result.isFooterLastSyncTimeVisible).isFalse()
    }

    @Test
    fun `should have hidden image last sync time footer when timestamp is negative`() = runTest {
        val mockImageStatusWithNegativeTimestamp = mockk<ImageSyncStatus>(relaxed = true) {
            every { isSyncing } returns false
            every { progress } returns null
            every { lastUpdateTimeMillis } returns -1L
        }

        val result = useCase(
            isOnline = true,
            eventSyncState = mockEventSyncState,
            imageSyncStatus = mockImageStatusWithNegativeTimestamp,
            syncableCounts = mockSyncableCounts,
        )

        assertThat(result.isFooterLastSyncTimeVisible).isFalse()
    }
}
