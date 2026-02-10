package com.simprints.feature.dashboard.settings.syncinfo.usecase

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.*
import com.simprints.core.domain.common.Modality
import com.simprints.core.lifecycle.AppForegroundStateTracker
import com.simprints.core.tools.time.Ticker
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.feature.dashboard.settings.syncinfo.SyncInfo
import com.simprints.feature.dashboard.settings.syncinfo.SyncInfoSectionImages
import com.simprints.feature.dashboard.settings.syncinfo.SyncInfoSectionRecords
import com.simprints.feature.dashboard.settings.syncinfo.modulecount.ModuleCount
import com.simprints.feature.dashboard.settings.syncinfo.usecase.internal.GetSyncInfoSectionImagesUseCase
import com.simprints.feature.dashboard.settings.syncinfo.usecase.internal.GetSyncInfoSectionRecordsUseCase
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.models.DownSynchronizationConfiguration
import com.simprints.infra.config.store.models.GeneralConfiguration
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.SynchronizationConfiguration
import com.simprints.infra.config.store.models.UpSynchronizationConfiguration
import com.simprints.infra.config.store.models.isCommCareEventDownSyncAllowed
import com.simprints.infra.config.store.models.isModuleSelectionAvailable
import com.simprints.infra.config.store.models.isSampleUploadEnabledInProject
import com.simprints.infra.config.store.models.isSimprintsEventDownSyncAllowed
import com.simprints.infra.eventsync.permission.CommCarePermissionChecker
import com.simprints.infra.eventsync.status.models.EventSyncState
import com.simprints.infra.network.ConnectivityTracker
import com.simprints.infra.sync.ImageSyncStatus
import com.simprints.infra.sync.SyncOrchestrator
import com.simprints.infra.sync.SyncStatus
import com.simprints.infra.sync.SyncableCounts
import com.simprints.infra.sync.usecase.ObserveSyncableCountsUseCase
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
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
    private val observeSyncableCounts = mockk<ObserveSyncableCountsUseCase>()
    private val syncOrchestrator = mockk<SyncOrchestrator>()
    private val ticker = mockk<Ticker>()
    private val appForegroundStateTracker = mockk<AppForegroundStateTracker>()
    private val observeConfigurationFlow = mockk<ObserveConfigurationChangesUseCase>()
    private val getSyncInfoSectionImages = mockk<GetSyncInfoSectionImagesUseCase>()
    private val getSyncInfoSectionRecords = mockk<GetSyncInfoSectionRecordsUseCase>()

    private val syncStatusFlow = MutableStateFlow(
        SyncStatus(eventSyncState = mockk(relaxed = true), imageSyncStatus = mockk(relaxed = true)),
    )
    private val syncableCountsFlow = MutableStateFlow(
        SyncableCounts(
            totalRecords = 0,
            recordEventsToDownload = 0,
            isRecordEventsToDownloadLowerBound = false,
            eventsToUpload = 0,
            enrolmentsToUpload = 0,
            samplesToUpload = 0,
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
        every { syncOrchestrator.observeSyncState() } returns syncStatusFlow

        every { mockEventSyncState.lastSyncTime } returns TEST_TIMESTAMP
        syncableCountsFlow.value = SyncableCounts(
            totalRecords = 0,
            recordEventsToDownload = 0,
            isRecordEventsToDownloadLowerBound = false,
            eventsToUpload = 0,
            enrolmentsToUpload = 0,
            samplesToUpload = 0,
        )
        every { observeSyncableCounts.invoke() } returns syncableCountsFlow

        every { ticker.observeTicks(any()) } returns MutableStateFlow(Unit)

        every { appForegroundStateTracker.observeAppInForeground() } returns flowOf(true)

        every { observeConfigurationFlow.invoke() } returns flowOf(createConfigurationState())

        every { any<ProjectConfiguration>().isModuleSelectionAvailable() } returns false
        every { any<ProjectConfiguration>().isSimprintsEventDownSyncAllowed() } returns true
        every { any<ProjectConfiguration>().isCommCareEventDownSyncAllowed() } returns false
        every { any<ProjectConfiguration>().isSampleUploadEnabledInProject() } returns true

        every {
            getSyncInfoSectionRecords(any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns SyncInfoSectionRecords()

        every {
            getSyncInfoSectionImages(any(), any(), any(), any())
        } returns SyncInfoSectionImages()
    }

    private fun createUseCase() {
        useCase = ObserveSyncInfoUseCase(
            connectivityTracker = connectivityTracker,
            authStore = authStore,
            ticker = ticker,
            appForegroundStateTracker = appForegroundStateTracker,
            getSyncInfoSectionImages = getSyncInfoSectionImages,
            getSyncInfoSectionRecords = getSyncInfoSectionRecords,
            observeConfigurationFlow = observeConfigurationFlow,
            observeSyncableCounts = observeSyncableCounts,
            syncOrchestrator = syncOrchestrator,
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
    fun `should show correct login prompt visibility when not logged in`() = runTest {
        every { authStore.observeSignedInProjectId() } returns MutableStateFlow("")
        createUseCase()

        val result = useCase().first()

        assertThat(result.isLoggedIn).isFalse()
    }

    // Section-specific tests

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
    fun `should handle network errors indication`() = runTest {
        val connectivityFlow = MutableStateFlow(false) // start offline
        every { connectivityTracker.observeIsConnected() } returns connectivityFlow
        createUseCase()

        useCase().first()

        verify { getSyncInfoSectionRecords(any(), isOnline = false, any(), any(), any(), any(), any(), any(), any()) }
        verify { getSyncInfoSectionImages(isOnline = false, any(), any(), any()) }

        connectivityFlow.value = true

        useCase().first()

        verify { getSyncInfoSectionRecords(any(), isOnline = true, any(), any(), any(), any(), any(), any(), any()) }
        verify { getSyncInfoSectionImages(isOnline = true, any(), any(), any()) }
    }

    // Flow combination tests

    @Test
    fun `should handle changes in connectivity stream`() = runTest {
        val connectivityFlow = MutableStateFlow(false) // started offline
        every { connectivityTracker.observeIsConnected() } returns connectivityFlow
        createUseCase()

        useCase().first()

        verify { getSyncInfoSectionRecords(any(), isOnline = false, any(), any(), any(), any(), any(), any(), any()) }
        verify { getSyncInfoSectionImages(isOnline = false, any(), any(), any()) }

        connectivityFlow.value = true // changed to online

        useCase().first()

        verify { getSyncInfoSectionRecords(any(), isOnline = true, any(), any(), any(), any(), any(), any(), any()) }
        verify { getSyncInfoSectionImages(isOnline = true, any(), any(), any()) }
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

        useCase().first()

        verify { getSyncInfoSectionRecords(any(), any(), any(), mockIdleState, any(), any(), any(), any(), any()) }

        val mockSyncingState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncInProgress() } returns true
            every { progress } returns 1
            every { total } returns 2
        }
        syncStatusFlow.value = SyncStatus(eventSyncState = mockSyncingState, imageSyncStatus = mockImageSyncStatus)

        useCase().first()

        verify { getSyncInfoSectionRecords(any(), any(), any(), mockSyncingState, any(), any(), any(), any(), any()) }
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

        useCase().first()

        verify { getSyncInfoSectionImages(any(), any(), notSyncingImageStatus, any()) }

        val syncingImageStatus = mockk<ImageSyncStatus> {
            every { isSyncing } returns true
            every { progress } returns Pair(1, 2)
            every { lastUpdateTimeMillis } returns null
        }
        syncStatusFlow.value = SyncStatus(eventSyncState = mockEventSyncState, imageSyncStatus = syncingImageStatus)

        useCase().first()

        verify { getSyncInfoSectionImages(any(), any(), syncingImageStatus, any()) }
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

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `should handle changes in time pacing stream`() = runTest {
        val mockIdleEventSyncState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncRunning() } returns false
            every { lastSyncTime } returns TEST_TIMESTAMP
        }
        syncStatusFlow.value = SyncStatus(eventSyncState = mockIdleEventSyncState, imageSyncStatus = mockImageSyncStatus)
        val timeHelper = mockk<TimeHelper>()
        val commCarePermissionChecker = mockk<CommCarePermissionChecker>()
        var nowMs = TEST_TIMESTAMP.ms
        every { commCarePermissionChecker.hasCommCarePermissions() } returns true
        every { timeHelper.readableBetweenNowAndTime(any()) } answers {
            val timestamp = firstArg<Timestamp>()
            when {
                timestamp.ms < 0 -> ""
                nowMs - timestamp.ms >= 60_000 -> "1 minute ago"
                else -> "0 minutes ago"
            }
        }

        val getSyncInfoSectionImagesUseCase = GetSyncInfoSectionImagesUseCase(timeHelper)
        val getSyncInfoSectionRecordsUseCase = GetSyncInfoSectionRecordsUseCase(timeHelper, commCarePermissionChecker)
        val syncInfoFlow = ObserveSyncInfoUseCase(
            connectivityTracker = connectivityTracker,
            authStore = authStore,
            ticker = ticker,
            appForegroundStateTracker = appForegroundStateTracker,
            getSyncInfoSectionImages = getSyncInfoSectionImagesUseCase,
            getSyncInfoSectionRecords = getSyncInfoSectionRecordsUseCase,
            observeConfigurationFlow = observeConfigurationFlow,
            observeSyncableCounts = observeSyncableCounts,
            syncOrchestrator = syncOrchestrator,
            dispatcher = testCoroutineRule.testCoroutineDispatcher,
        )

        val tickerTickFlow = MutableStateFlow(0)
        every { ticker.observeTicks(any()) } returns tickerTickFlow.map { }
        val emissions = mutableListOf<SyncInfo>()
        val collectJob = launch {
            syncInfoFlow()
                .take(2)
                .toList(emissions)
        }

        advanceUntilIdle()
        nowMs += 60_000
        tickerTickFlow.value = 1
        advanceUntilIdle()
        collectJob.join()

        assertThat(emissions).hasSize(2)
        assertThat(emissions[0].syncInfoSectionRecords.footerLastSyncMinutesAgo).isEqualTo("0 minutes ago")
        assertThat(emissions[1].syncInfoSectionRecords.footerLastSyncMinutesAgo).isEqualTo("1 minute ago")
    }

    private fun createConfigurationState(
        isRefreshing: Boolean = false,
        isProjectRunning: Boolean = true,
        selectedModules: List<ModuleCount> = emptyList(),
        projectConfig: ProjectConfiguration = mockProjectConfiguration,
    ) = ConfigurationState(isRefreshing, isProjectRunning, selectedModules, projectConfig)
}
