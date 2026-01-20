package com.simprints.feature.dashboard.settings.syncinfo.usecase

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.*
import com.simprints.core.domain.common.Modality
import com.simprints.core.lifecycle.AppForegroundStateTracker
import com.simprints.core.tools.time.Ticker
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.feature.dashboard.settings.syncinfo.modulecount.ModuleCount
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.models.DownSynchronizationConfiguration
import com.simprints.infra.config.store.models.GeneralConfiguration
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.SynchronizationConfiguration
import com.simprints.infra.config.store.models.UpSynchronizationConfiguration
import com.simprints.infra.config.store.models.canSyncDataToSimprints
import com.simprints.infra.config.store.models.isCommCareEventDownSyncAllowed
import com.simprints.infra.config.store.models.isModuleSelectionAvailable
import com.simprints.infra.config.store.models.isSampleUploadEnabledInProject
import com.simprints.infra.config.store.models.isSimprintsEventDownSyncAllowed
import com.simprints.infra.eventsync.permission.CommCarePermissionChecker
import com.simprints.infra.eventsync.status.models.EventSyncState
import com.simprints.infra.network.ConnectivityTracker
import com.simprints.infra.sync.ImageSyncStatus
import com.simprints.infra.sync.SyncStatus
import com.simprints.infra.sync.SyncableCounts
import com.simprints.infra.sync.usecase.CountSyncableUseCase
import com.simprints.infra.sync.usecase.SyncUseCase
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

internal class ObserveSyncInfoUseCaseTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private val connectivityTracker = mockk<ConnectivityTracker>()
    private val authStore = mockk<AuthStore>()
    private val countSyncable = mockk<CountSyncableUseCase>()
    private val sync = mockk<SyncUseCase>()
    private val timeHelper = mockk<TimeHelper>()
    private val ticker = mockk<Ticker>()
    private val appForegroundStateTracker = mockk<AppForegroundStateTracker>()
    private val commCarePermissionChecker = mockk<CommCarePermissionChecker>()
    private val observeConfigurationFlow = mockk<ObserveConfigurationChangesUseCase>()

    private val syncStatusFlow = MutableStateFlow(
        SyncStatus(eventSyncState = mockk(relaxed = true), imageSyncStatus = mockk(relaxed = true)),
    )
    private val syncableCountsFlow = MutableStateFlow(
        SyncableCounts(
            recordsTotal = 0,
            eventsToDownload = 0,
            isEventsToDownloadLowerBound = false,
            eventsToUpload = 0,
            eventsToUploadEnrolmentV2 = 0,
            eventsToUploadEnrolmentV4 = 0,
            imagesToUpload = 0,
        ),
    )

    private lateinit var useCase: ObserveSyncInfoUseCase

    private companion object {
        const val TEST_PROJECT_ID = "test_project_id"
        const val TEST_MODULE_NAME = "test_module"
        val TEST_TIMESTAMP = Timestamp(1000L)

        fun createMockSynchronizationConfiguration(): SynchronizationConfiguration = mockk<SynchronizationConfiguration>(relaxed = true) {
            every { down } returns mockk<DownSynchronizationConfiguration>(relaxed = true) {
                every { commCare } returns null
            }
            every { up } returns mockk<UpSynchronizationConfiguration>(relaxed = true) {
                every { coSync } returns mockk<UpSynchronizationConfiguration.CoSyncUpSynchronizationConfiguration>(relaxed = true) {
                    every { kind } returns UpSynchronizationConfiguration.UpSynchronizationKind.NONE
                }
            }
        }
    }

    private val mockProjectConfiguration = mockk<ProjectConfiguration>(relaxed = true) {
        every { general } returns mockk<GeneralConfiguration>(relaxed = true) {
            every { modalities } returns emptyList()
        }
        every { synchronization } returns createMockSynchronizationConfiguration()
    }

    private val mockEventSyncState = mockk<EventSyncState>(relaxed = true) {
        every { isSyncCompleted() } returns false
        every { isSyncInProgress() } returns false
        every { isSyncConnecting() } returns false
        every { isSyncRunning() } returns false
        every { isSyncFailed() } returns false
        every { isSyncFailedBecauseReloginRequired() } returns false
        every { isSyncFailedBecauseCommCarePermissionIsMissing() } returns false
        every { isSyncFailedBecauseBackendMaintenance() } returns false
        every { isSyncFailedBecauseTooManyRequests() } returns false
        every { getEstimatedBackendMaintenanceOutage() } returns null
        every { hasSyncHistory() } returns true
        every { progress } returns null
        every { total } returns null
    }
    private val mockImageSyncStatus = mockk<ImageSyncStatus>(relaxed = true) {
        every { isSyncing } returns false
        every { progress } returns null
        every { lastUpdateTimeMillis } returns null
    }

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        mockkStatic("com.simprints.infra.config.store.models.ProjectConfigurationKt")
        mockkStatic("com.simprints.core.tools.extentions.Flow_extKt")
        setupDefaultMocks()
        createUseCase()
    }

    private fun setupDefaultMocks() {
        every { authStore.observeSignedInProjectId() } returns MutableStateFlow(TEST_PROJECT_ID)

        every { connectivityTracker.observeIsConnected() } returns flowOf(true)

        syncStatusFlow.value = SyncStatus(eventSyncState = mockEventSyncState, imageSyncStatus = mockImageSyncStatus)
        every { sync.invoke(any(), any()) } returns syncStatusFlow

        every { mockEventSyncState.lastSyncTime } returns TEST_TIMESTAMP
        syncableCountsFlow.value = SyncableCounts(
            recordsTotal = 0,
            eventsToDownload = 0,
            isEventsToDownloadLowerBound = false,
            eventsToUpload = 0,
            eventsToUploadEnrolmentV2 = 0,
            eventsToUploadEnrolmentV4 = 0,
            imagesToUpload = 0,
        )
        every { countSyncable.invoke() } returns syncableCountsFlow

        every { ticker.observeTicks(any()) } returns MutableStateFlow(Unit)
        every { timeHelper.now() } returns TEST_TIMESTAMP
        every { timeHelper.msBetweenNowAndTime(any()) } returns 0L
        every { timeHelper.readableBetweenNowAndTime(any()) } returns "0 minutes ago"

        every { appForegroundStateTracker.observeAppInForeground() } returns flowOf(true)

        every { observeConfigurationFlow.invoke() } returns flowOf(createConfigurationState())

        every { any<ProjectConfiguration>().isModuleSelectionAvailable() } returns false
        every { any<ProjectConfiguration>().isSimprintsEventDownSyncAllowed() } returns true
        every { any<ProjectConfiguration>().isCommCareEventDownSyncAllowed() } returns false
        every { any<ProjectConfiguration>().isSampleUploadEnabledInProject() } returns true
        every { commCarePermissionChecker.hasCommCarePermissions() } returns true
    }

    private fun createUseCase() {
        useCase = ObserveSyncInfoUseCase(
            connectivityTracker = connectivityTracker,
            authStore = authStore,
            timeHelper = timeHelper,
            ticker = ticker,
            appForegroundStateTracker = appForegroundStateTracker,
            commCarePermissionChecker = commCarePermissionChecker,
            observeConfigurationFlow = observeConfigurationFlow,
            countSyncable = countSyncable,
            sync = sync,
            dispatcher = testCoroutineRule.testCoroutineDispatcher,
        )
    }

    @Test
    fun `should not show re-login prompt when sync has not failed due to authentication`() = runTest {
        val mockNormalEventSyncState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncFailedBecauseReloginRequired() } returns false
        }
        syncStatusFlow.value = SyncStatus(eventSyncState = mockNormalEventSyncState, imageSyncStatus = mockImageSyncStatus)
        createUseCase()

        val result = useCase().first()

        assertThat(result.isLoginPromptSectionVisible).isFalse()
    }

    @Test
    fun `should show configuration loading when project is refreshing`() = runTest {
        every { observeConfigurationFlow.invoke() } returns flowOf(createConfigurationState(isRefreshing = true))
        createUseCase()

        val result = useCase().first()

        assertThat(result.isConfigurationLoadingProgressBarVisible).isTrue()
    }

    @Test
    fun `should show re-login prompt when sync failed due to authentication required`() = runTest {
        every { observeConfigurationFlow.invoke() } returns flowOf(createConfigurationState())
        val mockFailedEventSyncState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncFailedBecauseReloginRequired() } returns true
        }
        syncStatusFlow.value = SyncStatus(eventSyncState = mockFailedEventSyncState, imageSyncStatus = mockImageSyncStatus)
        createUseCase()

        val result = useCase().first()

        assertThat(result.isLoginPromptSectionVisible).isTrue()
    }

    @Test
    fun `should show re-login prompt correctly based on pre-logout mode`() = runTest {
        val mockFailedEventSyncState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncFailedBecauseReloginRequired() } returns true
        }
        syncStatusFlow.value = SyncStatus(eventSyncState = mockFailedEventSyncState, imageSyncStatus = mockImageSyncStatus)
        createUseCase()

        val result = useCase(
            isPreLogoutUpSync = true, // This should hide the login prompt
        ).first()

        assertThat(result.isLoginPromptSectionVisible).isFalse()
    }

    @Test
    fun `should handle non-running project state correctly in sync info`() = runTest {
        every { observeConfigurationFlow.invoke() } returns flowOf(createConfigurationState(isProjectRunning = false))
        createUseCase()

        val result = useCase().first()

        assertThat(result.syncInfoSectionRecords.isCounterRecordsToDownloadVisible).isFalse()
    }

    @Test
    fun `should show correct login prompt visibility when not logged in`() = runTest {
        every { authStore.observeSignedInProjectId() } returns MutableStateFlow("")
        createUseCase()

        val result = useCase().first()

        assertThat(result.isLoggedIn).isFalse()
    }

    // Section-specific tests

    @Test
    fun `should emit SyncInfo with correct syncInfoSectionRecords instruction visibility`() = runTest {
        val mockOfflineEventSyncState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncFailed() } returns false
            every { isSyncInProgress() } returns false
        }
        syncStatusFlow.value = SyncStatus(eventSyncState = mockOfflineEventSyncState, imageSyncStatus = mockImageSyncStatus)
        every { connectivityTracker.observeIsConnected() } returns flowOf(false)
        createUseCase()

        val result = useCase().first()

        assertThat(result.syncInfoSectionRecords.isInstructionOfflineVisible).isTrue()
        assertThat(result.syncInfoSectionRecords.isInstructionErrorVisible).isFalse()
        assertThat(result.syncInfoSectionRecords.isInstructionDefaultVisible).isFalse()
    }

    @Test
    fun `should emit SyncInfo with correct syncInfoSectionRecords button states`() = runTest {
        val mockNormalEventSyncState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncRunning() } returns false
            every { isSyncFailedBecauseReloginRequired() } returns false
            every { isSyncFailed() } returns false
        }
        syncStatusFlow.value = SyncStatus(eventSyncState = mockNormalEventSyncState, imageSyncStatus = mockImageSyncStatus)
        every { connectivityTracker.observeIsConnected() } returns flowOf(true)
        createUseCase()

        val result = useCase().first()

        assertThat(result.syncInfoSectionRecords.isSyncButtonEnabled).isTrue()
        assertThat(result.syncInfoSectionRecords.isSyncButtonVisible).isTrue()
        assertThat(result.syncInfoSectionRecords.isSyncButtonForRetry).isFalse()
    }

    @Test
    fun `should emit SyncInfo with correct syncInfoSectionRecords footer states`() = runTest {
        val mockCompletedEventSyncState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncInProgress() } returns false
            every { lastSyncTime } returns TEST_TIMESTAMP
        }
        syncStatusFlow.value = SyncStatus(eventSyncState = mockCompletedEventSyncState, imageSyncStatus = mockImageSyncStatus)
        createUseCase()

        val result = useCase().first()

        assertThat(result.syncInfoSectionRecords.isFooterLastSyncTimeVisible).isTrue()
        assertThat(result.syncInfoSectionRecords.footerLastSyncMinutesAgo).isEqualTo("0 minutes ago")
        assertThat(result.syncInfoSectionRecords.isFooterSyncInProgressVisible).isFalse()
    }

    @Test
    fun `should emit SyncInfo with correct syncInfoSectionImages instruction visibility`() = runTest {
        val mockNotSyncingImageStatus = mockk<ImageSyncStatus>(relaxed = true) {
            every { isSyncing } returns false
            every { progress } returns null
        }
        syncStatusFlow.value = SyncStatus(eventSyncState = mockEventSyncState, imageSyncStatus = mockNotSyncingImageStatus)
        every { connectivityTracker.observeIsConnected() } returns flowOf(false)
        createUseCase()

        val result = useCase().first()

        assertThat(result.syncInfoSectionImages.isInstructionOfflineVisible).isTrue()
        assertThat(result.syncInfoSectionImages.isInstructionDefaultVisible).isFalse()
    }

    @Test
    fun `should emit SyncInfo with correct syncInfoSectionImages button states`() = runTest {
        val mockNormalEventSyncState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncFailedBecauseReloginRequired() } returns false
        }
        syncStatusFlow.value = SyncStatus(eventSyncState = mockNormalEventSyncState, imageSyncStatus = mockImageSyncStatus)
        every { connectivityTracker.observeIsConnected() } returns flowOf(true)
        createUseCase()

        val result = useCase().first()

        assertThat(result.syncInfoSectionImages.isSyncButtonEnabled).isTrue()
    }

    @Test
    fun `should emit SyncInfo with correct syncInfoSectionImages footer states`() = runTest {
        val mockImageStatusWithLastSync = mockk<ImageSyncStatus>(relaxed = true) {
            every { isSyncing } returns false
            every { progress } returns null
            every { lastUpdateTimeMillis } returns 120_000 // 2 minutes
        }
        syncStatusFlow.value = SyncStatus(eventSyncState = mockEventSyncState, imageSyncStatus = mockImageStatusWithLastSync)
        every { timeHelper.readableBetweenNowAndTime(Timestamp(120 * 1000)) } returns "2 minutes ago"
        createUseCase()

        val result = useCase().first()

        assertThat(result.syncInfoSectionImages.isFooterLastSyncTimeVisible).isTrue()
        assertThat(result.syncInfoSectionImages.footerLastSyncMinutesAgo).isEqualTo("2 minutes ago")
    }

    @Test
    fun `should emit SyncInfo with correct syncInfoSectionModules data`() = runTest {
        val mockProjectConfigWithModules = mockk<ProjectConfiguration> {
            every { general } returns mockk<GeneralConfiguration> {
                every { modalities } returns listOf(Modality.FINGERPRINT)
            }
            every { synchronization } returns createMockSynchronizationConfiguration()
        }
        every { observeConfigurationFlow.invoke() } returns flowOf(
            createConfigurationState(
                selectedModules = listOf(ModuleCount(TEST_MODULE_NAME, 50)),
                projectConfig = mockProjectConfigWithModules,
            ),
        )

        every { mockProjectConfigWithModules.isModuleSelectionAvailable() } returns true
        createUseCase()

        val result = useCase().first()

        assertThat(result.syncInfoSectionModules.isSectionAvailable).isTrue()
        assertThat(result.syncInfoSectionModules.moduleCounts).hasSize(2) // total + module
        assertThat(result.syncInfoSectionModules.moduleCounts[0].count).isEqualTo(50)
    }

    // Progress calculation tests

    @Test
    fun `should calculate correct event sync progress when sync in progress`() = runTest {
        val mockInProgressEventSyncState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncInProgress() } returns true
            every { isSyncCompleted() } returns false
            every { progress } returns 5
            every { total } returns 10
        }
        syncStatusFlow.value = SyncStatus(eventSyncState = mockInProgressEventSyncState, imageSyncStatus = mockImageSyncStatus)
        createUseCase()

        val result = useCase().first()

        assertThat(result.syncInfoSectionRecords.isProgressVisible).isTrue()
        assertThat(result.syncInfoSectionRecords.progress.progressBarPercentage).isEqualTo(50) // half
    }

    @Test
    fun `should calculate correct event sync progress when sync connecting`() = runTest {
        val mockConnectingEventSyncState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncConnecting() } returns true
            every { isSyncInProgress() } returns false
            every { isSyncCompleted() } returns false
            every { hasSyncHistory() } returns true
        }
        syncStatusFlow.value = SyncStatus(eventSyncState = mockConnectingEventSyncState, imageSyncStatus = mockImageSyncStatus)
        createUseCase()

        val result = useCase().first()

        assertThat(result.syncInfoSectionRecords.progress.progressBarPercentage).isEqualTo(0) // not started
    }

    @Test
    fun `should calculate correct event sync progress when sync approached completion`() = runTest {
        val mockCompletedEventSyncState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncInProgress() } returns true
            every { progress } returns 10
            every { total } returns 10
        }
        syncStatusFlow.value = SyncStatus(eventSyncState = mockCompletedEventSyncState, imageSyncStatus = mockImageSyncStatus)
        createUseCase()

        val result = useCase().first()

        assertThat(result.syncInfoSectionRecords.progress.progressBarPercentage).isEqualTo(100)
    }

    @Test
    fun `should not show event sync progress when sync completed`() = runTest {
        val mockCompletedEventSyncState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncCompleted() } returns true
        }
        syncStatusFlow.value = SyncStatus(eventSyncState = mockCompletedEventSyncState, imageSyncStatus = mockImageSyncStatus)
        createUseCase()

        val result = useCase().first()

        assertThat(result.syncInfoSectionRecords.isProgressVisible).isFalse()
    }

    @Test
    fun `should calculate correct combined progress during pre-logout sync events phase`() = runTest {
        val mockInProgressEventSyncState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncInProgress() } returns true
            every { isSyncCompleted() } returns false
            every { progress } returns 3
            every { total } returns 6
        }
        val mockNotSyncingImageStatus = mockk<ImageSyncStatus>(relaxed = true) {
            every { isSyncing } returns false
            every { progress } returns null
        }
        syncStatusFlow.value = SyncStatus(eventSyncState = mockInProgressEventSyncState, imageSyncStatus = mockNotSyncingImageStatus)
        createUseCase()

        val result = useCase(isPreLogoutUpSync = true).first()

        // 50% of the first half (0-50%) of scale dedicated to the records, so 25% total
        assertThat(result.syncInfoSectionRecords.progress.progressBarPercentage).isEqualTo(25)
    }

    @Test
    fun `should calculate correct combined progress during pre-logout sync images phase`() = runTest {
        val mockCompletedEventSyncState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncCompleted() } returns true
            every { isSyncInProgress() } returns false
        }
        val mockSyncingImageStatus = mockk<ImageSyncStatus>(relaxed = true) {
            every { isSyncing } returns true
            every { progress } returns Pair(2, 4) // 2 out of 4 images
        }
        syncStatusFlow.value = SyncStatus(eventSyncState = mockCompletedEventSyncState, imageSyncStatus = mockSyncingImageStatus)
        createUseCase()

        val result = useCase(isPreLogoutUpSync = true).first()

        // 50% of the second half (50-75%) of scale dedicated to the images, so 75% total
        assertThat(result.syncInfoSectionRecords.progress.progressBarPercentage).isEqualTo(75)
    }

    @Test
    fun `should calculate correct image sync progress when images syncing`() = runTest {
        val mockSyncingImageStatus = mockk<ImageSyncStatus>(relaxed = true) {
            every { isSyncing } returns true
            every { progress } returns Pair(3, 10) // 3 out of 10 images
        }
        syncStatusFlow.value = SyncStatus(eventSyncState = mockEventSyncState, imageSyncStatus = mockSyncingImageStatus)
        createUseCase()

        val result = useCase().first()

        assertThat(result.syncInfoSectionImages.isProgressVisible).isTrue()
        assertThat(result.syncInfoSectionImages.progress.progressBarPercentage).isEqualTo(30)
    }

    @Test
    fun `should calculate correct image sync progress when images not syncing`() = runTest {
        val mockNotSyncingImageStatus = mockk<ImageSyncStatus>(relaxed = true) {
            every { isSyncing } returns false
            every { progress } returns null
        }
        syncStatusFlow.value = SyncStatus(eventSyncState = mockEventSyncState, imageSyncStatus = mockNotSyncingImageStatus)
        createUseCase()

        val result = useCase().first()

        assertThat(result.syncInfoSectionImages.isProgressVisible).isFalse()
        assertThat(result.syncInfoSectionImages.progress.progressBarPercentage).isEqualTo(0)
    }

    // Counter tests

    @Test
    fun `should emit SyncInfo with correct record counters when sync not in progress`() = runTest {
        val mockIdleEventSyncState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncInProgress() } returns false
            every { isSyncRunning() } returns false
        }
        syncStatusFlow.value = SyncStatus(eventSyncState = mockIdleEventSyncState, imageSyncStatus = mockImageSyncStatus)
        syncableCountsFlow.value = SyncableCounts(
            recordsTotal = 25,
            eventsToDownload = 8,
            isEventsToDownloadLowerBound = false,
            eventsToUpload = 0,
            eventsToUploadEnrolmentV2 = 2,
            eventsToUploadEnrolmentV4 = 3,
            imagesToUpload = 0,
        )
        createUseCase()

        val result = useCase().first()

        assertThat(result.syncInfoSectionRecords.counterTotalRecords).isEqualTo("25")
        assertThat(result.syncInfoSectionRecords.counterRecordsToUpload).isEqualTo("5")
        assertThat(result.syncInfoSectionRecords.counterRecordsToDownload).isEqualTo("8")
    }

    @Test
    fun `should not count records when project id blank`() = runTest {
        every { authStore.signedInProjectId } returns ""
        every { authStore.observeSignedInProjectId() } returns MutableStateFlow("")
        val mockIdleEventSyncState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncInProgress() } returns false
            every { isSyncRunning() } returns false
        }
        syncStatusFlow.value = SyncStatus(eventSyncState = mockIdleEventSyncState, imageSyncStatus = mockImageSyncStatus)
        createUseCase()

        val result = useCase().first()

        assertThat(result.syncInfoSectionRecords.counterTotalRecords).isEmpty()
    }

    @Test
    fun `should emit SyncInfo with empty record counters when sync in progress`() = runTest {
        val mockInProgressEventSyncState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncInProgress() } returns true
        }
        syncStatusFlow.value = SyncStatus(eventSyncState = mockInProgressEventSyncState, imageSyncStatus = mockImageSyncStatus)
        createUseCase()

        val result = useCase().first()

        assertThat(result.syncInfoSectionRecords.counterTotalRecords).isEmpty()
        assertThat(result.syncInfoSectionRecords.counterRecordsToUpload).isEmpty()
        assertThat(result.syncInfoSectionRecords.counterRecordsToDownload).isEmpty()
    }

    @Test
    fun `should emit SyncInfo with correct images to upload counter when sync not in progress`() = runTest {
        val mockNotSyncingImageStatus = mockk<ImageSyncStatus>(relaxed = true) {
            every { isSyncing } returns false
            every { progress } returns null
        }
        syncStatusFlow.value = SyncStatus(eventSyncState = mockEventSyncState, imageSyncStatus = mockNotSyncingImageStatus)
        syncableCountsFlow.value = SyncableCounts(
            recordsTotal = 0,
            eventsToDownload = 0,
            isEventsToDownloadLowerBound = false,
            eventsToUpload = 0,
            eventsToUploadEnrolmentV2 = 0,
            eventsToUploadEnrolmentV4 = 0,
            imagesToUpload = 15,
        )
        createUseCase()

        val result = useCase().first()

        assertThat(result.syncInfoSectionRecords.counterImagesToUpload).isEqualTo("15") // may be shown within records
        assertThat(result.syncInfoSectionImages.counterImagesToUpload).isEqualTo("15")
    }

    @Test
    fun `should emit SyncInfo with empty images counter when sync in progress`() = runTest {
        val mockSyncingImageStatus = mockk<ImageSyncStatus>(relaxed = true) {
            every { isSyncing } returns true
            every { progress } returns null
        }
        syncStatusFlow.value = SyncStatus(eventSyncState = mockEventSyncState, imageSyncStatus = mockSyncingImageStatus)
        createUseCase()

        val result = useCase().first()

        assertThat(result.syncInfoSectionRecords.counterImagesToUpload).isEmpty() // may be shown within records
        assertThat(result.syncInfoSectionImages.counterImagesToUpload).isEmpty()
    }

    @Test
    fun `should emit SyncInfo with correct module counts when modules selected`() = runTest {
        val mockProjectConfigWithModules = mockk<ProjectConfiguration> {
            every { general } returns mockk<GeneralConfiguration> {
                every { modalities } returns listOf(Modality.FINGERPRINT)
            }
            every { synchronization } returns createMockSynchronizationConfiguration()
        }
        every { observeConfigurationFlow.invoke() } returns flowOf(
            createConfigurationState(
                selectedModules = listOf(
                    ModuleCount("module_1", 15),
                    ModuleCount("module_2", 25),
                ),
                projectConfig = mockProjectConfigWithModules,
            ),
        )

        every { mockProjectConfigWithModules.isModuleSelectionAvailable() } returns true
        createUseCase()

        val result = useCase().first()

        assertThat(result.syncInfoSectionModules.isSectionAvailable).isTrue()
        assertThat(result.syncInfoSectionModules.moduleCounts).hasSize(3) // sum of modules + the 2 modules
        // sum of modules
        assertThat(result.syncInfoSectionModules.moduleCounts[0].count).isEqualTo(40)
        // module_1
        assertThat(result.syncInfoSectionModules.moduleCounts[1]).isEqualTo(
            ModuleCount(name = "module_1", count = 15),
        )
        // module_2
        assertThat(result.syncInfoSectionModules.moduleCounts[2]).isEqualTo(
            ModuleCount(name = "module_2", count = 25),
        )
    }

    @Test
    fun `should emit SyncInfo with empty module counts when no modules selected`() = runTest {
        val mockProjectConfigWithoutModules = mockk<ProjectConfiguration> {
            every { general } returns mockk<GeneralConfiguration> {
                every { modalities } returns emptyList()
            }
            every { synchronization } returns createMockSynchronizationConfiguration()
        }
        every { observeConfigurationFlow.invoke() } returns
            flowOf(createConfigurationState(projectConfig = mockProjectConfigWithoutModules))
        every { mockProjectConfigWithoutModules.isModuleSelectionAvailable() } returns false

        createUseCase()

        val result = useCase().first()

        assertThat(result.syncInfoSectionModules.isSectionAvailable).isFalse()
        assertThat(result.syncInfoSectionModules.moduleCounts).isEmpty()
    }

    @Test
    fun `should emit SyncInfo with correct records to download counter visible when allowed`() = runTest {
        val mockProjectConfigWithDownSync = mockk<ProjectConfiguration> {
            every { general } returns mockk<GeneralConfiguration> {
                every { modalities } returns emptyList()
            }
            every { synchronization } returns createMockSynchronizationConfiguration()
        }
        val mockIdleEventSyncState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncInProgress() } returns false
        }
        every { observeConfigurationFlow.invoke() } returns flowOf(createConfigurationState(projectConfig = mockProjectConfigWithDownSync))
        syncStatusFlow.value = SyncStatus(eventSyncState = mockIdleEventSyncState, imageSyncStatus = mockImageSyncStatus)
        syncableCountsFlow.value = SyncableCounts(
            recordsTotal = 0,
            eventsToDownload = 42,
            isEventsToDownloadLowerBound = false,
            eventsToUpload = 0,
            eventsToUploadEnrolmentV2 = 0,
            eventsToUploadEnrolmentV4 = 0,
            imagesToUpload = 0,
        )
        every { mockProjectConfigWithDownSync.isSimprintsEventDownSyncAllowed() } returns true
        every { mockProjectConfigWithDownSync.isModuleSelectionAvailable() } returns false

        createUseCase()

        val result = useCase().first()

        assertThat(result.syncInfoSectionRecords.isCounterRecordsToDownloadVisible).isTrue()
        assertThat(result.syncInfoSectionRecords.counterRecordsToDownload).isEqualTo("42")
    }

    @Test
    fun `should emit SyncInfo with hidden records to download counter when pre-logout mode`() = runTest {
        val mockIdleEventSyncState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncInProgress() } returns false
        }

        syncStatusFlow.value = SyncStatus(eventSyncState = mockIdleEventSyncState, imageSyncStatus = mockImageSyncStatus)
        createUseCase()

        val result = useCase(isPreLogoutUpSync = true).first()

        assertThat(result.syncInfoSectionRecords.isCounterRecordsToDownloadVisible).isFalse()
    }

    @Test
    fun `should handle network errors indication`() = runTest {
        val connectivityFlow = MutableStateFlow(false) // start offline
        every { connectivityTracker.observeIsConnected() } returns connectivityFlow
        createUseCase()

        val offlineResult = useCase().first()

        assertThat(offlineResult.syncInfoSectionRecords.isInstructionOfflineVisible).isTrue()
        assertThat(offlineResult.syncInfoSectionRecords.isSyncButtonEnabled).isFalse()
        assertThat(offlineResult.syncInfoSectionImages.isSyncButtonEnabled).isFalse()

        connectivityFlow.value = true

        val onlineResult = useCase().first()

        assertThat(onlineResult.syncInfoSectionRecords.isInstructionOfflineVisible).isFalse()
        assertThat(onlineResult.syncInfoSectionRecords.isSyncButtonEnabled).isTrue()
        assertThat(onlineResult.syncInfoSectionImages.isSyncButtonEnabled).isTrue()
    }

    // Flow combination tests

    @Test
    fun `should handle changes in connectivity stream`() = runTest {
        val connectivityFlow = MutableStateFlow(false) // started offline
        every { connectivityTracker.observeIsConnected() } returns connectivityFlow
        createUseCase()

        val offlineResult = useCase().first()

        assertThat(offlineResult.syncInfoSectionRecords.isInstructionOfflineVisible).isTrue()

        connectivityFlow.value = true // changed to online

        val onlineResult = useCase().first()

        assertThat(onlineResult.syncInfoSectionRecords.isInstructionOfflineVisible).isFalse()
    }

    @Test
    fun `should handle changes in auth stream`() = runTest {
        val authFlow = MutableStateFlow("") // started not signed in
        every { authStore.observeSignedInProjectId() } returns authFlow
        createUseCase()

        val loggedOutResult = useCase().first()

        assertThat(loggedOutResult.isLoggedIn).isFalse()

        authFlow.value = TEST_PROJECT_ID // changed to signed in

        val loggedInResult = useCase().first()

        assertThat(loggedInResult.isLoggedIn).isTrue()
    }

    @Test
    fun `should handle changes in project refreshing stream`() = runTest {
        val refreshingFlow = MutableStateFlow(createConfigurationState(isRefreshing = false)) // started non refreshing
        every { observeConfigurationFlow.invoke() } returns refreshingFlow
        createUseCase()

        val notRefreshingResult = useCase().first()

        assertThat(notRefreshingResult.isConfigurationLoadingProgressBarVisible).isFalse()

        refreshingFlow.value = createConfigurationState(isRefreshing = true) // changed to refreshing

        val refreshingResult = useCase().first()

        assertThat(refreshingResult.isConfigurationLoadingProgressBarVisible).isTrue()
    }

    @Test
    fun `should handle changes in event sync state stream`() = runTest {
        val mockIdleState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncInProgress() } returns false
        }
        syncStatusFlow.value = SyncStatus(eventSyncState = mockIdleState, imageSyncStatus = mockImageSyncStatus)
        createUseCase()

        val idleResult = useCase().first()

        assertThat(idleResult.syncInfoSectionRecords.isProgressVisible).isFalse()

        val mockSyncingState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncInProgress() } returns true
            every { progress } returns 1
            every { total } returns 2
        }
        syncStatusFlow.value = SyncStatus(eventSyncState = mockSyncingState, imageSyncStatus = mockImageSyncStatus)

        val syncingResult = useCase().first()

        assertThat(syncingResult.syncInfoSectionRecords.isProgressVisible).isTrue()
    }

    @Test
    fun `should handle changes in image sync status stream`() = runTest {
        val notSyncingImageStatus = mockk<ImageSyncStatus> {
            every { isSyncing } returns false
            every { progress } returns null
            every { lastUpdateTimeMillis } returns null
        }
        syncStatusFlow.value = SyncStatus(eventSyncState = mockEventSyncState, imageSyncStatus = notSyncingImageStatus)
        createUseCase()

        val notSyncingResult = useCase().first()

        assertThat(notSyncingResult.syncInfoSectionImages.isProgressVisible).isFalse()

        val syncingImageStatus = mockk<ImageSyncStatus> {
            every { isSyncing } returns true
            every { progress } returns Pair(1, 2)
            every { lastUpdateTimeMillis } returns null
        }
        syncStatusFlow.value = SyncStatus(eventSyncState = mockEventSyncState, imageSyncStatus = syncingImageStatus)

        val syncingResult = useCase().first()

        assertThat(syncingResult.syncInfoSectionImages.isProgressVisible).isTrue()
    }

    @Test
    fun `should handle changes in project config stream`() = runTest {
        val projectConfigFlow = MutableStateFlow(createConfigurationState(projectConfig = mockProjectConfiguration))
        every { observeConfigurationFlow.invoke() } returns projectConfigFlow

        createUseCase()

        val initialResult = useCase().first()
        assertThat(initialResult.syncInfoSectionModules.isSectionAvailable).isFalse()

        val mockConfigWithModules = mockk<ProjectConfiguration> {
            every { general } returns mockk<GeneralConfiguration> {
                every { modalities } returns listOf(Modality.FINGERPRINT)
            }
            every { synchronization } returns createMockSynchronizationConfiguration()
        }
        every { mockConfigWithModules.isModuleSelectionAvailable() } returns true
        projectConfigFlow.value = createConfigurationState(projectConfig = mockConfigWithModules) // now with modules

        val moduleConfigResult = useCase().first()

        assertThat(moduleConfigResult.syncInfoSectionModules.isSectionAvailable).isTrue()
    }

    @Test
    fun `should handle changes in device config stream`() = runTest {
        val projectConfig = mockk<ProjectConfiguration> {
            every { general } returns mockk<GeneralConfiguration> {
                every { modalities } returns emptyList()
            }
            every { synchronization } returns mockk<SynchronizationConfiguration>(relaxed = true) {
                every { up } returns mockk<UpSynchronizationConfiguration>(relaxed = true) {
                    every { coSync } returns
                        mockk<UpSynchronizationConfiguration.CoSyncUpSynchronizationConfiguration>(relaxed = true) {
                            every { kind } returns UpSynchronizationConfiguration.UpSynchronizationKind.NONE
                        }
                }
            }
        }
        val deviceConfigFlow = MutableStateFlow(
            createConfigurationState(
                selectedModules = emptyList(), // started without selected modules
                projectConfig = projectConfig,
            ),
        )
        every { observeConfigurationFlow.invoke() } returns deviceConfigFlow
        createUseCase()

        val noModulesResult = useCase().first()

        assertThat(noModulesResult.syncInfoSectionModules.moduleCounts).isEmpty()

        deviceConfigFlow.emit(
            createConfigurationState(
                selectedModules = listOf(ModuleCount(TEST_MODULE_NAME, 0)), // now with selected modules
                projectConfig = projectConfig,
            ),
        )

        val withModulesResult = useCase().first()

        assertThat(withModulesResult.syncInfoSectionModules.moduleCounts).isNotEmpty()
    }

    @Test
    fun `should handle changes in time pacing stream`() = runTest {
        val mockIdleEventSyncState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncRunning() } returns false
            every { lastSyncTime } returns TEST_TIMESTAMP
        }
        syncStatusFlow.value = SyncStatus(eventSyncState = mockIdleEventSyncState, imageSyncStatus = mockImageSyncStatus)
        every { timeHelper.now() } returnsMany listOf(TEST_TIMESTAMP, Timestamp(TEST_TIMESTAMP.ms + 60_000))
        every { timeHelper.readableBetweenNowAndTime(any()) } returnsMany listOf("0 minutes ago", "1 minute ago")
        // MutableStateFlow of Unit won't emit another (identical) Unit, so we'll count minutes and map to Units
        val timePaceFlow = MutableStateFlow(0)
        every { ticker.observeTicks(any()) } returns timePaceFlow.map { }
        createUseCase()

        val initialResult = useCase().first()

        assertThat(initialResult.syncInfoSectionRecords.footerLastSyncMinutesAgo).isEqualTo("0 minutes ago")

        timePaceFlow.value = -1 // just a different value for a time beat, doesn't matter which

        val updatedResult = useCase().first()

        assertThat(updatedResult.syncInfoSectionRecords.footerLastSyncMinutesAgo).isEqualTo("1 minute ago")
    }

    // UI state tests

    @Test
    fun `should show CommCare permission missing instruction when sync failed due to missing permission`() = runTest {
        val mockFailedEventSyncState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncFailedBecauseCommCarePermissionIsMissing() } returns true
            every { isSyncFailed() } returns true
            every { isSyncInProgress() } returns false
        }
        syncStatusFlow.value = SyncStatus(eventSyncState = mockFailedEventSyncState, imageSyncStatus = mockImageSyncStatus)
        every { connectivityTracker.observeIsConnected() } returns flowOf(true)
        every { mockProjectConfiguration.isCommCareEventDownSyncAllowed() } returns true
        every { commCarePermissionChecker.hasCommCarePermissions() } returns false // Permission still denied
        createUseCase()

        val result = useCase().first()

        assertThat(result.syncInfoSectionRecords.isInstructionCommCarePermissionVisible).isTrue()
        assertThat(result.syncInfoSectionRecords.isInstructionDefaultVisible).isFalse()
        assertThat(result.syncInfoSectionRecords.isInstructionOfflineVisible).isFalse()
        assertThat(result.syncInfoSectionRecords.isInstructionErrorVisible).isFalse()
        assertThat(result.syncInfoSectionRecords.isInstructionNoModulesVisible).isFalse()
        assertThat(result.syncInfoSectionRecords.isInstructionDefaultVisible).isFalse()
    }

    @Test
    fun `should hide CommCare permission missing instruction when permission is granted`() = runTest {
        val mockNormalEventSyncState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncFailedBecauseCommCarePermissionIsMissing() } returns false
            every { isSyncFailed() } returns false
            every { isSyncInProgress() } returns false
        }
        syncStatusFlow.value = SyncStatus(eventSyncState = mockNormalEventSyncState, imageSyncStatus = mockImageSyncStatus)
        every { connectivityTracker.observeIsConnected() } returns flowOf(true)
        createUseCase()

        val result = useCase().first()

        assertThat(result.syncInfoSectionRecords.isInstructionCommCarePermissionVisible).isFalse()
        assertThat(result.syncInfoSectionRecords.isInstructionDefaultVisible).isTrue()
    }

    @Test
    fun `should hide default instruction for pre-logout sync`() = runTest {
        createUseCase()

        val result = useCase(isPreLogoutUpSync = true).first()

        assertThat(result.syncInfoSectionRecords.isInstructionDefaultVisible).isFalse()
    }

    @Test
    fun `should not hide default instruction for regular non-pre-logout sync`() = runTest {
        createUseCase()

        val result = useCase().first()

        assertThat(result.syncInfoSectionRecords.isInstructionDefaultVisible).isTrue()
    }

    @Test
    fun `sync button should be disabled when not on standby`() = runTest {
        val mockSyncingEventSyncState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncInProgress() } returns true
        }
        syncStatusFlow.value = SyncStatus(eventSyncState = mockSyncingEventSyncState, imageSyncStatus = mockImageSyncStatus)
        createUseCase()

        val result = useCase().first()

        assertThat(result.syncInfoSectionRecords.isSyncButtonEnabled).isFalse()
    }

    @Test
    fun `sync button should be disabled when this is logout screen and offline`() = runTest {
        every { connectivityTracker.observeIsConnected() } returns flowOf(false)
        createUseCase()

        val result = useCase(isPreLogoutUpSync = true).first()

        assertThat(result.syncInfoSectionRecords.isSyncButtonEnabled).isFalse()
    }

    @Test
    fun `sync button should be enabled when online and there is sync to Simprints`() = runTest {
        every { connectivityTracker.observeIsConnected() } returns flowOf(true)
        every { any<ProjectConfiguration>().canSyncDataToSimprints() } returns true
        createUseCase()

        val result = useCase().first() // here and on for the sync button state: assuming not the logout screen

        assertThat(result.syncInfoSectionRecords.isSyncButtonEnabled).isTrue()
    }

    @Test
    fun `sync button should be enabled when offline but CommCare down-sync allowed`() = runTest {
        every { connectivityTracker.observeIsConnected() } returns flowOf(false)
        every { any<ProjectConfiguration>().isCommCareEventDownSyncAllowed() } returns true
        createUseCase()

        val result = useCase().first()

        assertThat(result.syncInfoSectionRecords.isSyncButtonEnabled).isTrue()
    }

    @Test
    fun `sync button should be enabled when Simprints down-sync allowed and re-login not required`() = runTest {
        val mockNormalEventSyncState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncFailedBecauseReloginRequired() } returns false
        }
        syncStatusFlow.value = SyncStatus(eventSyncState = mockNormalEventSyncState, imageSyncStatus = mockImageSyncStatus)
        every { any<ProjectConfiguration>().isSimprintsEventDownSyncAllowed() } returns true
        createUseCase()

        val result = useCase().first()

        assertThat(result.syncInfoSectionRecords.isSyncButtonEnabled).isTrue()
    }

    @Test
    fun `sync button should be enabled when CommCare down-sync allowed and no CommCare permission error`() = runTest {
        val mockNormalEventSyncState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncFailedBecauseCommCarePermissionIsMissing() } returns false
        }
        syncStatusFlow.value = SyncStatus(eventSyncState = mockNormalEventSyncState, imageSyncStatus = mockImageSyncStatus)
        every { any<ProjectConfiguration>().isCommCareEventDownSyncAllowed() } returns true
        createUseCase()

        val result = useCase().first()

        assertThat(result.syncInfoSectionRecords.isSyncButtonEnabled).isTrue()
    }

    @Test
    fun `sync button should be disabled when there is neither Simprints nor ComCare down-sync`() = runTest {
        every { any<ProjectConfiguration>().isSimprintsEventDownSyncAllowed() } returns false
        every { any<ProjectConfiguration>().isCommCareEventDownSyncAllowed() } returns false
        every { any<ProjectConfiguration>().canSyncDataToSimprints() } returns false
        createUseCase()

        val result = useCase().first()

        assertThat(result.syncInfoSectionRecords.isSyncButtonEnabled).isFalse()
    }

    @Test
    fun `sync button should be disabled when only Simprints down-sync allowed but re-login required`() = runTest {
        val mockReLoginRequiredEventSyncState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncFailedBecauseReloginRequired() } returns true
        }
        syncStatusFlow.value = SyncStatus(eventSyncState = mockReLoginRequiredEventSyncState, imageSyncStatus = mockImageSyncStatus)
        every { any<ProjectConfiguration>().isSimprintsEventDownSyncAllowed() } returns true
        every { any<ProjectConfiguration>().isCommCareEventDownSyncAllowed() } returns false
        every { any<ProjectConfiguration>().canSyncDataToSimprints() } returns false
        createUseCase()

        val result = useCase().first()

        assertThat(result.syncInfoSectionRecords.isSyncButtonEnabled).isFalse()
    }

    @Test
    fun `sync button should be disabled when only CommCare down-sync allowed but there is CommCare permission error`() = runTest {
        val mockCommCarePermissionErrorEventSyncState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncFailedBecauseCommCarePermissionIsMissing() } returns true
        }
        syncStatusFlow.value = SyncStatus(eventSyncState = mockCommCarePermissionErrorEventSyncState, imageSyncStatus = mockImageSyncStatus)
        every { any<ProjectConfiguration>().isCommCareEventDownSyncAllowed() } returns true
        every { any<ProjectConfiguration>().isSimprintsEventDownSyncAllowed() } returns false
        every { any<ProjectConfiguration>().canSyncDataToSimprints() } returns false
        every { commCarePermissionChecker.hasCommCarePermissions() } returns false // Permission still denied
        createUseCase()

        val result = useCase().first()

        assertThat(result.syncInfoSectionRecords.isSyncButtonEnabled).isFalse()
    }

    @Test
    fun `sync button should be enabled when sync has failed for non-CommCare and non-network reasons`() = runTest {
        val mockCommCarePermissionErrorEventSyncState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncFailed() } returns true
            every { isSyncFailedBecauseCommCarePermissionIsMissing() } returns false
        }
        syncStatusFlow.value = SyncStatus(eventSyncState = mockCommCarePermissionErrorEventSyncState, imageSyncStatus = mockImageSyncStatus)

        createUseCase()
        val result = useCase().first()

        assertThat(result.syncInfoSectionRecords.isSyncButtonEnabled).isTrue()
    }

    @Test
    fun `should calculate correct record last sync time when sync time available`() = runTest {
        val timestamp = Timestamp(0L)
        every { mockEventSyncState.lastSyncTime } returns timestamp
        every { timeHelper.readableBetweenNowAndTime(timestamp) } returns "5 minutes ago"
        createUseCase()

        val result = useCase().first()

        assertThat(result.syncInfoSectionRecords.isFooterLastSyncTimeVisible).isTrue()
        assertThat(result.syncInfoSectionRecords.footerLastSyncMinutesAgo).isEqualTo("5 minutes ago")
    }

    @Test
    fun `should have hidden record last sync time footer when no sync history`() = runTest {
        every { mockEventSyncState.lastSyncTime } returns null
        createUseCase()

        val result = useCase().first()

        assertThat(result.syncInfoSectionRecords.isFooterLastSyncTimeVisible).isFalse()
    }

    @Test
    fun `should calculate correct image last sync time when available`() = runTest {
        val mockImageStatusWithLastSync = mockk<ImageSyncStatus>(relaxed = true) {
            every { isSyncing } returns false
            every { progress } returns null
            every { lastUpdateTimeMillis } returns 180_000 // 3 minutes
        }
        syncStatusFlow.value = SyncStatus(eventSyncState = mockEventSyncState, imageSyncStatus = mockImageStatusWithLastSync)
        every { timeHelper.readableBetweenNowAndTime(Timestamp(180 * 1000)) } returns "3 minutes ago"
        createUseCase()

        val result = useCase().first()

        assertThat(result.syncInfoSectionImages.isFooterLastSyncTimeVisible).isTrue()
        assertThat(result.syncInfoSectionImages.footerLastSyncMinutesAgo).isEqualTo("3 minutes ago")
    }

    @Test
    fun `should have hidden image last sync time footer when unavailable`() = runTest {
        val mockImageStatusWithoutLastSync = mockk<ImageSyncStatus>(relaxed = true) {
            every { isSyncing } returns false
            every { progress } returns null
            every { lastUpdateTimeMillis } returns null
        }
        syncStatusFlow.value = SyncStatus(eventSyncState = mockEventSyncState, imageSyncStatus = mockImageStatusWithoutLastSync)
        createUseCase()

        val result = useCase().first()

        assertThat(result.syncInfoSectionImages.isFooterLastSyncTimeVisible).isFalse()
    }

    @Test
    fun `should have hidden image last sync time footer when timestamp is negative`() = runTest {
        val mockImageStatusWithNegativeTimestamp = mockk<ImageSyncStatus>(relaxed = true) {
            every { isSyncing } returns false
            every { progress } returns null
            every { lastUpdateTimeMillis } returns -1L
        }
        syncStatusFlow.value = SyncStatus(eventSyncState = mockEventSyncState, imageSyncStatus = mockImageStatusWithNegativeTimestamp)
        createUseCase()

        val result = useCase().first()

        assertThat(result.syncInfoSectionImages.isFooterLastSyncTimeVisible).isFalse()
    }

    @Test
    fun `should show correct visibility states for offline instructions`() = runTest {
        every { connectivityTracker.observeIsConnected() } returns flowOf(false)
        createUseCase()

        val result = useCase().first()

        assertThat(result.syncInfoSectionRecords.isInstructionOfflineVisible).isTrue()
        assertThat(result.syncInfoSectionRecords.isInstructionDefaultVisible).isFalse()
        assertThat(result.syncInfoSectionRecords.isInstructionErrorVisible).isFalse()
        assertThat(result.syncInfoSectionImages.isInstructionOfflineVisible).isTrue()
        assertThat(result.syncInfoSectionImages.isInstructionDefaultVisible).isFalse()
    }

    @Test
    fun `should show correct visibility states for error instructions`() = runTest {
        val mockFailedEventSyncState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncFailed() } returns true
            every { isSyncInProgress() } returns false
        }
        syncStatusFlow.value = SyncStatus(eventSyncState = mockFailedEventSyncState, imageSyncStatus = mockImageSyncStatus)
        every { connectivityTracker.observeIsConnected() } returns flowOf(true)
        createUseCase()

        val result = useCase().first()

        assertThat(result.syncInfoSectionRecords.isInstructionErrorVisible).isTrue()
        assertThat(result.syncInfoSectionRecords.isInstructionDefaultVisible).isFalse()
        assertThat(result.syncInfoSectionRecords.isInstructionOfflineVisible).isFalse()
    }

    @Test
    fun `should show correct visibility states for module selection instructions`() = runTest {
        val mockProjectConfigRequiringModules = mockk<ProjectConfiguration> {
            every { general } returns mockk<GeneralConfiguration> {
                every { modalities } returns listOf(Modality.FINGERPRINT)
            }
            every { synchronization } returns createMockSynchronizationConfiguration()
        }
        val mockIdleEventSyncState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncFailed() } returns false
            every { isSyncInProgress() } returns false
        }

        every { observeConfigurationFlow.invoke() } returns flowOf(
            createConfigurationState(projectConfig = mockProjectConfigRequiringModules),
        )

        syncStatusFlow.value = SyncStatus(eventSyncState = mockIdleEventSyncState, imageSyncStatus = mockImageSyncStatus)
        every { connectivityTracker.observeIsConnected() } returns flowOf(true)
        every { mockProjectConfigRequiringModules.isModuleSelectionAvailable() } returns true
        createUseCase()

        val result = useCase().first()

        assertThat(result.syncInfoSectionRecords.isInstructionNoModulesVisible).isTrue()
        assertThat(result.syncInfoSectionRecords.isInstructionDefaultVisible).isFalse()
        assertThat(result.syncInfoSectionRecords.isInstructionOfflineVisible).isFalse()
        assertThat(result.syncInfoSectionRecords.isInstructionErrorVisible).isFalse()
    }

    @Test
    fun `should show correct visibility states for default instructions`() = runTest {
        val mockIdleEventSyncState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncFailed() } returns false
            every { isSyncInProgress() } returns false
        }
        syncStatusFlow.value = SyncStatus(eventSyncState = mockIdleEventSyncState, imageSyncStatus = mockImageSyncStatus)
        every { connectivityTracker.observeIsConnected() } returns flowOf(true)
        createUseCase()

        val result = useCase().first()

        assertThat(result.syncInfoSectionRecords.isInstructionDefaultVisible).isTrue()
        assertThat(result.syncInfoSectionRecords.isInstructionOfflineVisible).isFalse()
        assertThat(result.syncInfoSectionRecords.isInstructionErrorVisible).isFalse()
        assertThat(result.syncInfoSectionRecords.isInstructionNoModulesVisible).isFalse()
        assertThat(result.syncInfoSectionImages.isInstructionDefaultVisible).isTrue()
        assertThat(result.syncInfoSectionImages.isInstructionOfflineVisible).isFalse()
    }

    @Test
    fun `should handle failed sync retry indication correctly`() = runTest {
        val mockFailedState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncFailed() } returns true
            every { isSyncInProgress() } returns false
            every { isSyncFailedBecauseReloginRequired() } returns false
        }
        syncStatusFlow.value = SyncStatus(eventSyncState = mockFailedState, imageSyncStatus = mockImageSyncStatus)
        createUseCase()

        val failedResult = useCase().first()

        assertThat(failedResult.syncInfoSectionRecords.isInstructionErrorVisible).isTrue()
        assertThat(failedResult.syncInfoSectionRecords.isSyncButtonForRetry).isTrue()
    }

    // CommCare-specific tests

    @Test
    fun `should allow sync without network connection when CommCare down sync is configured`() = runTest {
        val mockProjectConfigWithCommCareDownSync = mockk<ProjectConfiguration>(relaxed = true) {
            every { general } returns mockk<GeneralConfiguration>(relaxed = true) {
                every { modalities } returns emptyList()
            }
            every { synchronization } returns mockk<SynchronizationConfiguration>(relaxed = true) {
                every { down } returns mockk<DownSynchronizationConfiguration>(relaxed = true) {
                    every { commCare } returns mockk()
                }
            }
            every { isCommCareEventDownSyncAllowed() } returns true
        }
        every { observeConfigurationFlow.invoke() } returns flowOf(
            createConfigurationState(
                projectConfig = mockProjectConfigWithCommCareDownSync,
            ),
        )

        val mockNormalEventSyncState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncFailedBecauseCommCarePermissionIsMissing() } returns false
            every { isSyncRunning() } returns false
            every { isSyncFailedBecauseReloginRequired() } returns false
            every { isSyncFailed() } returns false
            every { isSyncInProgress() } returns false
        }
        syncStatusFlow.value = SyncStatus(eventSyncState = mockNormalEventSyncState, imageSyncStatus = mockImageSyncStatus)
        every { connectivityTracker.observeIsConnected() } returns flowOf(false)
        createUseCase()

        val result = useCase().first()

        assertThat(result.syncInfoSectionRecords.isSyncButtonEnabled).isTrue()
        assertThat(result.syncInfoSectionRecords.isInstructionOfflineVisible).isFalse()
        assertThat(result.syncInfoSectionRecords.isInstructionDefaultVisible).isTrue()
    }

    @Test
    fun `should show CommCare permission missing when does not have permission`() = runTest {
        val mockNormalEventSyncState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncFailedBecauseCommCarePermissionIsMissing() } returns true
        }
        syncStatusFlow.value = SyncStatus(eventSyncState = mockNormalEventSyncState, imageSyncStatus = mockImageSyncStatus)
        every { mockProjectConfiguration.isCommCareEventDownSyncAllowed() } returns true
        every { commCarePermissionChecker.hasCommCarePermissions() } returns false // Permission still denied
        createUseCase()

        val result = useCase().first()

        assertThat(result.syncInfoSectionRecords.isInstructionCommCarePermissionVisible).isTrue()
        assertThat(result.syncInfoSectionRecords.isSyncButtonEnabled).isFalse()
        assertThat(result.syncInfoSectionRecords.isInstructionDefaultVisible).isFalse()
    }

    @Test
    fun `should hide CommCare permission instruction when does not have permission sync error`() = runTest {
        val mockNormalEventSyncState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncFailedBecauseCommCarePermissionIsMissing() } returns false
        }
        syncStatusFlow.value = SyncStatus(eventSyncState = mockNormalEventSyncState, imageSyncStatus = mockImageSyncStatus)
        every { connectivityTracker.observeIsConnected() } returns flowOf(true)
        createUseCase()

        val result = useCase().first()

        assertThat(result.syncInfoSectionRecords.isInstructionCommCarePermissionVisible).isFalse()
        assertThat(result.syncInfoSectionRecords.isSyncButtonEnabled).isTrue()
        assertThat(result.syncInfoSectionRecords.isInstructionDefaultVisible).isTrue()
    }

    private fun createConfigurationState(
        isRefreshing: Boolean = false,
        isProjectRunning: Boolean = true,
        selectedModules: List<ModuleCount> = emptyList(),
        projectConfig: ProjectConfiguration = mockProjectConfiguration,
    ) = ConfigurationState(isRefreshing, isProjectRunning, selectedModules, projectConfig)
}
