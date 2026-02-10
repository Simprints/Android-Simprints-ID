package com.simprints.feature.dashboard.settings.syncinfo.usecase.internal

import com.google.common.truth.Truth.*
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.feature.dashboard.settings.syncinfo.RecordSyncVisibleState
import com.simprints.feature.dashboard.settings.syncinfo.modulecount.ModuleCount
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.canSyncDataToSimprints
import com.simprints.infra.config.store.models.isCommCareEventDownSyncAllowed
import com.simprints.infra.config.store.models.isModuleSelectionAvailable
import com.simprints.infra.config.store.models.isSimprintsEventDownSyncAllowed
import com.simprints.infra.eventsync.permission.CommCarePermissionChecker
import com.simprints.infra.eventsync.status.models.EventSyncState
import com.simprints.infra.sync.ImageSyncStatus
import com.simprints.infra.sync.SyncableCounts
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

internal class GetSyncInfoSectionRecordsUseCaseTest {
    private val timeHelper = mockk<TimeHelper>()
    private val commCarePermissionChecker = mockk<CommCarePermissionChecker>()

    private val mockProjectConfiguration = mockk<ProjectConfiguration>(relaxed = true)

    private lateinit var useCase: GetSyncInfoSectionRecordsUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        mockkStatic("com.simprints.infra.config.store.models.ProjectConfigurationKt")

        every { timeHelper.readableBetweenNowAndTime(any()) } returns "0 minutes ago"
        every { commCarePermissionChecker.hasCommCarePermissions() } returns true

        every { any<ProjectConfiguration>().isModuleSelectionAvailable() } returns false
        every { any<ProjectConfiguration>().isSimprintsEventDownSyncAllowed() } returns true
        every { any<ProjectConfiguration>().isCommCareEventDownSyncAllowed() } returns false
        every { any<ProjectConfiguration>().canSyncDataToSimprints() } returns false

        useCase = GetSyncInfoSectionRecordsUseCase(
            timeHelper = timeHelper,
            commCarePermissionChecker = commCarePermissionChecker,
        )
    }

    @After
    fun tearDown() {
        unmockkStatic("com.simprints.infra.config.store.models.ProjectConfigurationKt")
    }

    @Test
    fun `should handle non-running project state correctly in sync info`() = runTest {
        val result = invokeUseCase(
            isProjectRunning = false,
        )

        assertThat(result.isCounterRecordsToDownloadVisible).isFalse()
    }

    @Test
    fun `should emit SyncInfo section with correct syncInfoSectionRecords instruction visibility`() = runTest {
        val mockOfflineEventSyncState = createEventSyncState(
            syncFailed = false,
            syncInProgress = false,
        )

        val result = invokeUseCase(
            isOnline = false,
            eventSyncState = mockOfflineEventSyncState,
        )

        assertThat(result.recordSyncVisibleState).isEqualTo(RecordSyncVisibleState.OFFLINE_ERROR)
        assertThat(result.isSyncButtonEnabled).isFalse()
    }

    @Test
    fun `should emit SyncInfo section with correct syncInfoSectionRecords button states`() = runTest {
        val mockNormalEventSyncState = createEventSyncState(
            syncRunning = false,
            syncFailedBecauseReloginRequired = false,
            syncFailed = false,
        )

        val result = invokeUseCase(
            isOnline = true,
            eventSyncState = mockNormalEventSyncState,
        )

        assertThat(result.isSyncButtonEnabled).isTrue()
        assertThat(result.isSyncButtonVisible).isTrue()
        assertThat(result.isSyncButtonForRetry).isFalse()
    }

    @Test
    fun `should emit SyncInfo section with correct syncInfoSectionRecords footer states`() = runTest {
        val mockCompletedEventSyncState = createEventSyncState(
            syncInProgress = false,
            lastSyncTime = TEST_TIMESTAMP,
        )

        val result = invokeUseCase(
            eventSyncState = mockCompletedEventSyncState,
        )

        assertThat(result.isFooterLastSyncTimeVisible).isTrue()
        assertThat(result.footerLastSyncMinutesAgo).isEqualTo("0 minutes ago")
        assertThat(result.isFooterSyncInProgressVisible).isFalse()
    }

    @Test
    fun `should calculate correct event sync progress when sync in progress`() = runTest {
        val mockInProgressEventSyncState = createEventSyncState(
            syncInProgress = true,
            syncCompleted = false,
            progress = 5,
            total = 10,
        )

        val result = invokeUseCase(
            eventSyncState = mockInProgressEventSyncState,
        )

        assertThat(result.isProgressVisible).isTrue()
        assertThat(result.progress.progressBarPercentage).isEqualTo(50)
    }

    @Test
    fun `should calculate correct event sync progress when sync connecting`() = runTest {
        val mockConnectingEventSyncState = createEventSyncState(
            syncConnecting = true,
            syncInProgress = false,
            syncCompleted = false,
            hasSyncHistory = true,
        )

        val result = invokeUseCase(
            eventSyncState = mockConnectingEventSyncState,
        )

        assertThat(result.progress.progressBarPercentage).isEqualTo(0)
    }

    @Test
    fun `should calculate correct event sync progress when sync approached completion`() = runTest {
        val mockCompletedEventSyncState = createEventSyncState(
            syncInProgress = true,
            progress = 10,
            total = 10,
        )

        val result = invokeUseCase(
            eventSyncState = mockCompletedEventSyncState,
        )

        assertThat(result.progress.progressBarPercentage).isEqualTo(100)
    }

    @Test
    fun `should not show event sync progress when sync completed`() = runTest {
        val mockCompletedEventSyncState = createEventSyncState(
            syncCompleted = true,
        )

        val result = invokeUseCase(
            eventSyncState = mockCompletedEventSyncState,
        )

        assertThat(result.isProgressVisible).isFalse()
    }

    @Test
    fun `should calculate correct combined progress during pre-logout sync events phase`() = runTest {
        val mockInProgressEventSyncState = createEventSyncState(
            syncInProgress = true,
            syncCompleted = false,
            progress = 3,
            total = 6,
        )
        val mockNotSyncingImageStatus = createImageSyncStatus(
            isSyncing = false,
            progress = null,
        )

        val result = invokeUseCase(
            isPreLogoutUpSync = true,
            eventSyncState = mockInProgressEventSyncState,
            imageSyncStatus = mockNotSyncingImageStatus,
        )

        assertThat(result.progress.progressBarPercentage).isEqualTo(25)
    }

    @Test
    fun `should calculate correct combined progress during pre-logout sync images phase`() = runTest {
        val mockCompletedEventSyncState = createEventSyncState(
            syncCompleted = true,
            syncInProgress = false,
        )
        val mockSyncingImageStatus = createImageSyncStatus(
            isSyncing = true,
            progress = Pair(2, 4),
        )

        val result = invokeUseCase(
            isPreLogoutUpSync = true,
            eventSyncState = mockCompletedEventSyncState,
            imageSyncStatus = mockSyncingImageStatus,
        )

        assertThat(result.progress.progressBarPercentage).isEqualTo(75)
    }

    @Test
    fun `should emit SyncInfo section with correct record counters when sync not in progress`() = runTest {
        val mockIdleEventSyncState = createEventSyncState(
            syncInProgress = false,
            syncRunning = false,
        )
        val syncableCounts = createSyncableCounts(
            totalRecords = 25,
            recordEventsToDownload = 8,
            isRecordEventsToDownloadLowerBound = false,
            eventsToUpload = 0,
            enrolmentsToUpload = 5,
            samplesToUpload = 0,
        )

        val result = invokeUseCase(
            eventSyncState = mockIdleEventSyncState,
            syncableCounts = syncableCounts,
        )

        assertThat(result.counterTotalRecords).isEqualTo("25")
        assertThat(result.counterRecordsToUpload).isEqualTo("5")
        assertThat(result.counterRecordsToDownload).isEqualTo("8")
    }

    @Test
    fun `should not count records when project id blank`() = runTest {
        val mockIdleEventSyncState = createEventSyncState(
            syncInProgress = false,
            syncRunning = false,
        )

        val result = invokeUseCase(
            projectId = "",
            eventSyncState = mockIdleEventSyncState,
        )

        assertThat(result.counterTotalRecords).isEmpty()
    }

    @Test
    fun `should emit SyncInfo section with empty record counters when sync in progress`() = runTest {
        val mockInProgressEventSyncState = createEventSyncState(
            syncInProgress = true,
        )

        val result = invokeUseCase(
            eventSyncState = mockInProgressEventSyncState,
        )

        assertThat(result.counterTotalRecords).isEmpty()
        assertThat(result.counterRecordsToUpload).isEmpty()
        assertThat(result.counterRecordsToDownload).isEmpty()
    }

    @Test
    fun `should emit SyncInfo section with correct images to upload counter when sync not in progress`() = runTest {
        val mockNotSyncingImageStatus = createImageSyncStatus(
            isSyncing = false,
            progress = null,
        )
        val syncableCounts = createSyncableCounts(
            samplesToUpload = 15,
        )

        val result = invokeUseCase(
            imageSyncStatus = mockNotSyncingImageStatus,
            syncableCounts = syncableCounts,
        )

        assertThat(result.counterImagesToUpload).isEqualTo("15")
    }

    @Test
    fun `should emit SyncInfo section with empty images counter when sync in progress`() = runTest {
        val mockSyncingImageStatus = createImageSyncStatus(
            isSyncing = true,
            progress = null,
        )

        val result = invokeUseCase(
            imageSyncStatus = mockSyncingImageStatus,
        )

        assertThat(result.counterImagesToUpload).isEmpty()
    }

    @Test
    fun `should emit SyncInfo section with correct records to download counter visible when allowed`() = runTest {
        val mockProjectConfigWithDownSync = mockk<ProjectConfiguration>(relaxed = true)
        val mockIdleEventSyncState = createEventSyncState(
            syncInProgress = false,
        )
        val syncableCounts = createSyncableCounts(
            totalRecords = 0,
            recordEventsToDownload = 42,
            isRecordEventsToDownloadLowerBound = false,
            eventsToUpload = 0,
            enrolmentsToUpload = 0,
            samplesToUpload = 0,
        )
        every { mockProjectConfigWithDownSync.isSimprintsEventDownSyncAllowed() } returns true
        every { mockProjectConfigWithDownSync.isModuleSelectionAvailable() } returns false

        val result = invokeUseCase(
            projectConfig = mockProjectConfigWithDownSync,
            eventSyncState = mockIdleEventSyncState,
            syncableCounts = syncableCounts,
        )

        assertThat(result.isCounterRecordsToDownloadVisible).isTrue()
        assertThat(result.counterRecordsToDownload).isEqualTo("42")
    }

    @Test
    fun `should emit SyncInfo section with hidden records to download counter when pre-logout mode`() = runTest {
        val mockIdleEventSyncState = createEventSyncState(
            syncInProgress = false,
        )

        val result = invokeUseCase(
            isPreLogoutUpSync = true,
            eventSyncState = mockIdleEventSyncState,
        )

        assertThat(result.isCounterRecordsToDownloadVisible).isFalse()
    }

    @Test
    fun `should show CommCare permission missing instruction when sync failed due to missing permission`() = runTest {
        val mockFailedEventSyncState = createEventSyncState(
            syncFailedBecauseCommCarePermissionIsMissing = true,
            syncFailed = true,
            syncInProgress = false,
        )
        every { mockProjectConfiguration.isCommCareEventDownSyncAllowed() } returns true
        every { commCarePermissionChecker.hasCommCarePermissions() } returns false

        val result = invokeUseCase(
            isOnline = true,
            eventSyncState = mockFailedEventSyncState,
        )

        assertThat(result.recordSyncVisibleState).isEqualTo(RecordSyncVisibleState.COMM_CARE_ERROR)
    }

    @Test
    fun `should hide CommCare permission missing instruction when permission is granted`() = runTest {
        val mockNormalEventSyncState = createEventSyncState(
            syncFailedBecauseCommCarePermissionIsMissing = false,
            syncFailed = false,
            syncInProgress = false,
        )

        val result = invokeUseCase(
            isOnline = true,
            eventSyncState = mockNormalEventSyncState,
        )

        assertThat(result.recordSyncVisibleState).isEqualTo(RecordSyncVisibleState.ON_STANDBY)
    }

    @Test
    fun `should hide default instruction for pre-logout sync`() = runTest {
        val result = invokeUseCase(
            isPreLogoutUpSync = true,
        )

        assertThat(result.recordSyncVisibleState).isEqualTo(RecordSyncVisibleState.NOTHING)
    }

    @Test
    fun `should not hide default instruction for regular non-pre-logout sync`() = runTest {
        val result = invokeUseCase()

        assertThat(result.recordSyncVisibleState).isEqualTo(RecordSyncVisibleState.ON_STANDBY)
    }

    @Test
    fun `sync button should be disabled when not on standby`() = runTest {
        val mockSyncingEventSyncState = createEventSyncState(
            syncInProgress = true,
        )

        val result = invokeUseCase(
            eventSyncState = mockSyncingEventSyncState,
        )

        assertThat(result.isSyncButtonEnabled).isFalse()
    }

    @Test
    fun `sync button should be disabled when this is logout screen and offline`() = runTest {
        val result = invokeUseCase(
            isPreLogoutUpSync = true,
            isOnline = false,
        )

        assertThat(result.isSyncButtonEnabled).isFalse()
    }

    @Test
    fun `sync button should be enabled when online and there is sync to Simprints`() = runTest {
        every { any<ProjectConfiguration>().canSyncDataToSimprints() } returns true

        val result = invokeUseCase(
            isOnline = true,
        )

        assertThat(result.isSyncButtonEnabled).isTrue()
    }

    @Test
    fun `sync button should be enabled when offline but CommCare down-sync allowed`() = runTest {
        every { any<ProjectConfiguration>().isCommCareEventDownSyncAllowed() } returns true

        val result = invokeUseCase(
            isOnline = false,
        )

        assertThat(result.isSyncButtonEnabled).isTrue()
    }

    @Test
    fun `sync button should be enabled when Simprints down-sync allowed and re-login not required`() = runTest {
        val mockNormalEventSyncState = createEventSyncState(
            syncFailedBecauseReloginRequired = false,
        )
        every { any<ProjectConfiguration>().isSimprintsEventDownSyncAllowed() } returns true

        val result = invokeUseCase(
            eventSyncState = mockNormalEventSyncState,
        )

        assertThat(result.isSyncButtonEnabled).isTrue()
    }

    @Test
    fun `sync button should be enabled when CommCare down-sync allowed and no CommCare permission error`() = runTest {
        val mockNormalEventSyncState = createEventSyncState(
            syncFailedBecauseCommCarePermissionIsMissing = false,
        )
        every { any<ProjectConfiguration>().isCommCareEventDownSyncAllowed() } returns true

        val result = invokeUseCase(
            eventSyncState = mockNormalEventSyncState,
        )

        assertThat(result.isSyncButtonEnabled).isTrue()
    }

    @Test
    fun `sync button should be disabled when there is neither Simprints nor ComCare down-sync`() = runTest {
        every { any<ProjectConfiguration>().isSimprintsEventDownSyncAllowed() } returns false
        every { any<ProjectConfiguration>().isCommCareEventDownSyncAllowed() } returns false
        every { any<ProjectConfiguration>().canSyncDataToSimprints() } returns false

        val result = invokeUseCase()

        assertThat(result.isSyncButtonEnabled).isFalse()
    }

    @Test
    fun `sync button should be disabled when only Simprints down-sync allowed but re-login required`() = runTest {
        val mockReLoginRequiredEventSyncState = createEventSyncState(
            syncFailedBecauseReloginRequired = true,
        )
        every { any<ProjectConfiguration>().isSimprintsEventDownSyncAllowed() } returns true
        every { any<ProjectConfiguration>().isCommCareEventDownSyncAllowed() } returns false
        every { any<ProjectConfiguration>().canSyncDataToSimprints() } returns false

        val result = invokeUseCase(
            eventSyncState = mockReLoginRequiredEventSyncState,
        )

        assertThat(result.isSyncButtonEnabled).isFalse()
    }

    @Test
    fun `sync button should be disabled when only CommCare down-sync allowed but there is CommCare permission error`() = runTest {
        val mockCommCarePermissionErrorEventSyncState = createEventSyncState(
            syncFailedBecauseCommCarePermissionIsMissing = true,
        )
        every { any<ProjectConfiguration>().isCommCareEventDownSyncAllowed() } returns true
        every { any<ProjectConfiguration>().isSimprintsEventDownSyncAllowed() } returns false
        every { any<ProjectConfiguration>().canSyncDataToSimprints() } returns false
        every { commCarePermissionChecker.hasCommCarePermissions() } returns false

        val result = invokeUseCase(
            eventSyncState = mockCommCarePermissionErrorEventSyncState,
        )

        assertThat(result.isSyncButtonEnabled).isFalse()
    }

    @Test
    fun `sync button should be enabled when sync has failed for non-CommCare and non-network reasons`() = runTest {
        val mockCommCarePermissionErrorEventSyncState = createEventSyncState(
            syncFailed = true,
            syncFailedBecauseCommCarePermissionIsMissing = false,
        )

        val result = invokeUseCase(
            eventSyncState = mockCommCarePermissionErrorEventSyncState,
        )

        assertThat(result.isSyncButtonEnabled).isTrue()
    }

    @Test
    fun `should calculate correct record last sync time when sync time available`() = runTest {
        val timestamp = Timestamp(0L)
        val mockEventSyncState = createEventSyncState(
            lastSyncTime = timestamp,
        )
        every { timeHelper.readableBetweenNowAndTime(timestamp) } returns "5 minutes ago"

        val result = invokeUseCase(
            eventSyncState = mockEventSyncState,
        )

        assertThat(result.isFooterLastSyncTimeVisible).isTrue()
        assertThat(result.footerLastSyncMinutesAgo).isEqualTo("5 minutes ago")
    }

    @Test
    fun `should have hidden record last sync time footer when no sync history`() = runTest {
        val mockEventSyncState = createEventSyncState(
            lastSyncTime = null,
        )

        val result = invokeUseCase(
            eventSyncState = mockEventSyncState,
        )

        assertThat(result.isFooterLastSyncTimeVisible).isFalse()
    }

    @Test
    fun `should show correct visibility states for error instructions`() = runTest {
        val mockFailedEventSyncState = createEventSyncState(
            syncFailed = true,
            syncInProgress = false,
        )

        val result = invokeUseCase(
            isOnline = true,
            eventSyncState = mockFailedEventSyncState,
        )

        assertThat(result.recordSyncVisibleState).isEqualTo(RecordSyncVisibleState.ERROR)
    }

    @Test
    fun `should show correct visibility states for module selection instructions`() = runTest {
        val mockProjectConfigRequiringModules = mockk<ProjectConfiguration>(relaxed = true)
        val mockIdleEventSyncState = createEventSyncState(
            syncFailed = false,
            syncInProgress = false,
        )
        every { mockProjectConfigRequiringModules.isModuleSelectionAvailable() } returns true

        val result = invokeUseCase(
            projectConfig = mockProjectConfigRequiringModules,
            moduleCounts = emptyList(),
            isOnline = true,
            eventSyncState = mockIdleEventSyncState,
        )

        assertThat(result.recordSyncVisibleState).isEqualTo(RecordSyncVisibleState.NO_MODULES_ERROR)
    }

    @Test
    fun `should handle failed sync retry indication correctly`() = runTest {
        val mockFailedState = createEventSyncState(
            syncFailed = true,
            syncInProgress = false,
            syncFailedBecauseReloginRequired = false,
        )

        val result = invokeUseCase(
            eventSyncState = mockFailedState,
        )

        assertThat(result.recordSyncVisibleState).isEqualTo(RecordSyncVisibleState.ERROR)
        assertThat(result.isSyncButtonForRetry).isTrue()
    }

    @Test
    fun `should allow sync without network connection when CommCare down sync is configured`() = runTest {
        val mockProjectConfigWithCommCareDownSync = mockk<ProjectConfiguration>(relaxed = true)
        val mockNormalEventSyncState = createEventSyncState(
            syncFailedBecauseCommCarePermissionIsMissing = false,
            syncRunning = false,
            syncFailedBecauseReloginRequired = false,
            syncFailed = false,
            syncInProgress = false,
        )
        every { mockProjectConfigWithCommCareDownSync.isCommCareEventDownSyncAllowed() } returns true

        val result = invokeUseCase(
            projectConfig = mockProjectConfigWithCommCareDownSync,
            eventSyncState = mockNormalEventSyncState,
            isOnline = false,
        )

        assertThat(result.isSyncButtonEnabled).isTrue()
        assertThat(result.recordSyncVisibleState).isEqualTo(RecordSyncVisibleState.ON_STANDBY)
    }

    @Test
    fun `should show CommCare permission missing when does not have permission`() = runTest {
        val mockNormalEventSyncState = createEventSyncState(
            syncFailedBecauseCommCarePermissionIsMissing = true,
        )
        every { mockProjectConfiguration.isCommCareEventDownSyncAllowed() } returns true
        every { commCarePermissionChecker.hasCommCarePermissions() } returns false

        val result = invokeUseCase(
            eventSyncState = mockNormalEventSyncState,
        )

        assertThat(result.recordSyncVisibleState).isEqualTo(RecordSyncVisibleState.COMM_CARE_ERROR)
        assertThat(result.isSyncButtonEnabled).isFalse()
    }

    @Test
    fun `should hide CommCare permission instruction when does not have permission sync error`() = runTest {
        val mockNormalEventSyncState = createEventSyncState(
            syncFailedBecauseCommCarePermissionIsMissing = false,
        )

        val result = invokeUseCase(
            isOnline = true,
            eventSyncState = mockNormalEventSyncState,
        )

        assertThat(result.isSyncButtonEnabled).isTrue()
        assertThat(result.recordSyncVisibleState).isEqualTo(RecordSyncVisibleState.ON_STANDBY)
    }

    private fun invokeUseCase(
        isPreLogoutUpSync: Boolean = false,
        isOnline: Boolean = true,
        projectId: String = TEST_PROJECT_ID,
        eventSyncState: EventSyncState = createEventSyncState(),
        imageSyncStatus: ImageSyncStatus = createImageSyncStatus(),
        syncableCounts: SyncableCounts = createSyncableCounts(),
        isProjectRunning: Boolean = true,
        moduleCounts: List<ModuleCount> = emptyList(),
        projectConfig: ProjectConfiguration = mockProjectConfiguration,
    ) = useCase(
        isPreLogoutUpSync = isPreLogoutUpSync,
        isOnline = isOnline,
        projectId = projectId,
        eventSyncState = eventSyncState,
        imageSyncStatus = imageSyncStatus,
        syncableCounts = syncableCounts,
        isProjectRunning = isProjectRunning,
        moduleCounts = moduleCounts,
        projectConfig = projectConfig,
    )

    private fun createEventSyncState(
        syncCompleted: Boolean = false,
        syncInProgress: Boolean = false,
        syncConnecting: Boolean = false,
        syncRunning: Boolean = false,
        syncFailed: Boolean = false,
        syncFailedBecauseReloginRequired: Boolean = false,
        syncFailedBecauseCommCarePermissionIsMissing: Boolean = false,
        syncFailedBecauseBackendMaintenance: Boolean = false,
        syncFailedBecauseTooManyRequests: Boolean = false,
        estimatedBackendMaintenanceOutage: Long? = null,
        hasSyncHistory: Boolean = true,
        progress: Int? = null,
        total: Int? = null,
        lastSyncTime: Timestamp? = TEST_TIMESTAMP,
    ): EventSyncState = mockk(relaxed = true) {
        every { isSyncCompleted() } returns syncCompleted
        every { isSyncInProgress() } returns syncInProgress
        every { isSyncConnecting() } returns syncConnecting
        every { isSyncRunning() } returns syncRunning
        every { isSyncFailed() } returns syncFailed
        every { isSyncFailedBecauseReloginRequired() } returns syncFailedBecauseReloginRequired
        every { isSyncFailedBecauseCommCarePermissionIsMissing() } returns syncFailedBecauseCommCarePermissionIsMissing
        every { isSyncFailedBecauseBackendMaintenance() } returns syncFailedBecauseBackendMaintenance
        every { isSyncFailedBecauseTooManyRequests() } returns syncFailedBecauseTooManyRequests
        every { getEstimatedBackendMaintenanceOutage() } returns estimatedBackendMaintenanceOutage
        every { hasSyncHistory() } returns hasSyncHistory
        every { this@mockk.progress } returns progress
        every { this@mockk.total } returns total
        every { this@mockk.lastSyncTime } returns lastSyncTime
    }

    private fun createImageSyncStatus(
        isSyncing: Boolean = false,
        progress: Pair<Int, Int>? = null,
        lastUpdateTimeMillis: Long? = null,
    ): ImageSyncStatus = ImageSyncStatus(
        isSyncing = isSyncing,
        progress = progress,
        lastUpdateTimeMillis = lastUpdateTimeMillis,
    )

    private fun createSyncableCounts(
        totalRecords: Int = 0,
        recordEventsToDownload: Int = 0,
        isRecordEventsToDownloadLowerBound: Boolean = false,
        eventsToUpload: Int = 0,
        enrolmentsToUpload: Int = 0,
        samplesToUpload: Int = 0,
    ): SyncableCounts = SyncableCounts(
        totalRecords = totalRecords,
        recordEventsToDownload = recordEventsToDownload,
        isRecordEventsToDownloadLowerBound = isRecordEventsToDownloadLowerBound,
        eventsToUpload = eventsToUpload,
        enrolmentsToUpload = enrolmentsToUpload,
        samplesToUpload = samplesToUpload,
    )

    private companion object {
        const val TEST_PROJECT_ID = "test_project_id"
        val TEST_TIMESTAMP = Timestamp(1000L)
    }
}
