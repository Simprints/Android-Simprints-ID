package com.simprints.feature.dashboard.settings.syncinfo

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import com.google.common.truth.Truth.*
import com.simprints.core.domain.common.Modality
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.feature.dashboard.logout.usecase.LogoutUseCase
import com.simprints.feature.dashboard.settings.syncinfo.usecase.ObserveSyncInfoUseCase
import com.simprints.feature.login.LoginResult
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.DeviceConfiguration
import com.simprints.infra.config.store.models.GeneralConfiguration
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.ProjectState
import com.simprints.infra.config.store.models.isModuleSelectionAvailable
import com.simprints.infra.config.store.models.isSimprintsEventDownSyncAllowed
import com.simprints.infra.eventsync.EventSyncManager
import com.simprints.infra.eventsync.status.models.DownSyncCounts
import com.simprints.infra.eventsync.status.models.EventSyncState
import com.simprints.infra.recent.user.activity.RecentUserActivityManager
import com.simprints.infra.sync.ImageSyncStatus
import com.simprints.infra.sync.SyncOrchestrator
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.livedata.getOrAwaitValue
import com.simprints.testtools.common.livedata.getOrAwaitValues
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.fail

@OptIn(ExperimentalCoroutinesApi::class)
class SyncInfoViewModelTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var configRepository: ConfigRepository

    @MockK
    private lateinit var authStore: AuthStore

    @MockK
    private lateinit var eventSyncManager: EventSyncManager

    @MockK
    private lateinit var syncOrchestrator: SyncOrchestrator

    @MockK
    private lateinit var recentUserActivityManager: RecentUserActivityManager

    @MockK
    private lateinit var timeHelper: TimeHelper

    @MockK
    private lateinit var observeSyncInfo: ObserveSyncInfoUseCase

    @MockK
    private lateinit var logoutUseCase: LogoutUseCase

    @MockK
    private lateinit var mockProjectConfiguration: ProjectConfiguration

    @MockK
    private lateinit var mockDeviceConfiguration: DeviceConfiguration

    @MockK
    private lateinit var mockProject: Project

    @MockK
    private lateinit var mockEventSyncState: EventSyncState

    @MockK
    private lateinit var mockImageSyncStatus: ImageSyncStatus

    private lateinit var viewModel: SyncInfoViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        every { mockProjectConfiguration.general } returns mockk<GeneralConfiguration>(relaxed = true) {
            every { modalities } returns emptyList()
        }
        every { mockDeviceConfiguration.selectedModules } returns emptyList()
        every { mockProject.state } returns ProjectState.RUNNING

        every { mockEventSyncState.isSyncCompleted() } returns false
        every { mockEventSyncState.isSyncInProgress() } returns false
        every { mockEventSyncState.isSyncConnecting() } returns false
        every { mockEventSyncState.isSyncRunning() } returns false
        every { mockEventSyncState.isSyncFailed() } returns false
        every { mockEventSyncState.isSyncFailedBecauseReloginRequired() } returns false
        every { mockEventSyncState.isSyncFailedBecauseBackendMaintenance() } returns false
        every { mockEventSyncState.isSyncFailedBecauseTooManyRequests() } returns false
        every { mockEventSyncState.getEstimatedBackendMaintenanceOutage() } returns null
        every { mockEventSyncState.isThereNotSyncHistory() } returns false
        every { mockEventSyncState.progress } returns null
        every { mockEventSyncState.total } returns null

        every { mockImageSyncStatus.isSyncing } returns false
        every { mockImageSyncStatus.progress } returns null
        every { mockImageSyncStatus.lastUpdateTimeMillis } returns null

        mockkStatic("androidx.lifecycle.FlowLiveDataConversions")
        mockkStatic("com.simprints.infra.config.store.models.ProjectConfigurationKt")
        mockkStatic("com.simprints.core.tools.extentions.Flow_extKt")
        setupDefaultMocks()
        createViewModel()
    }

    private fun setupDefaultMocks() {
        every { authStore.signedInProjectId } returns TEST_PROJECT_ID
        every { authStore.signedInUserId } returns TokenizableString.Raw(TEST_USER_ID)
        every { authStore.observeSignedInProjectId() } returns MutableStateFlow(TEST_PROJECT_ID)

        val connectivityLiveData = MutableLiveData(true)
        every { connectivityLiveData.asFlow() } returns flowOf(true)

        every { configRepository.observeIsProjectRefreshing() } returns MutableStateFlow(false)
        every { configRepository.observeProjectConfiguration() } returns MutableStateFlow(mockProjectConfiguration)
        every { configRepository.observeDeviceConfiguration() } returns MutableStateFlow(mockDeviceConfiguration)
        coEvery { configRepository.getProjectConfiguration() } returns mockProjectConfiguration
        coEvery { configRepository.getDeviceConfiguration() } returns mockDeviceConfiguration
        coEvery { configRepository.getProject() } returns mockProject

        val eventSyncFlow = flowOf(mockEventSyncState)
        every { eventSyncManager.getLastSyncState() } returns eventSyncFlow
        every { eventSyncManager.getLastSyncState(any()) } returns eventSyncFlow

        coEvery { eventSyncManager.getLastSyncTime() } returns TEST_TIMESTAMP
        coEvery { eventSyncManager.countEventsToUpload(any()) } returns flowOf(0)
        coEvery { eventSyncManager.countEventsToDownload() } returns DownSyncCounts(0, isLowerBound = false)

        every { syncOrchestrator.observeImageSyncStatus() } returns MutableStateFlow(mockImageSyncStatus)
        coEvery { syncOrchestrator.startEventSync(any()) } returns Unit
        coEvery { syncOrchestrator.stopEventSync() } returns Unit
        coEvery { syncOrchestrator.startImageSync() } returns Unit
        coEvery { syncOrchestrator.stopImageSync() } returns Unit

        every { timeHelper.now() } returns TEST_TIMESTAMP
        every { timeHelper.msBetweenNowAndTime(any()) } returns 0L

        coEvery { recentUserActivityManager.getRecentUserActivity() } returns mockk {
            every { lastUserUsed } returns TokenizableString.Raw(TEST_RECENT_USER_ID)
        }

        every { any<ProjectConfiguration>().isModuleSelectionAvailable() } returns false
        every { any<ProjectConfiguration>().isSimprintsEventDownSyncAllowed() } returns true

        every { observeSyncInfo(any()) } returns flowOf(createDefaultSyncInfo())
    }

    private fun createViewModel() {
        viewModel = SyncInfoViewModel(
            configRepository = configRepository,
            authStore = authStore,
            eventSyncManager = eventSyncManager,
            syncOrchestrator = syncOrchestrator,
            recentUserActivityManager = recentUserActivityManager,
            timeHelper = timeHelper,
            observeSyncInfo = observeSyncInfo,
            logoutUseCase = logoutUseCase,
            ioDispatcher = testCoroutineRule.testCoroutineDispatcher,
        )
    }

    private fun createDefaultSyncInfo() = SyncInfo(
        isLoggedIn = true,
        isConfigurationLoadingProgressBarVisible = false,
        isLoginPromptSectionVisible = false,
        syncInfoSectionRecords = SyncInfoSectionRecords(
            counterTotalRecords = "0",
            counterRecordsToUpload = "0",
            isCounterRecordsToDownloadVisible = false,
            counterRecordsToDownload = "0",
            isCounterImagesToUploadVisible = false,
            counterImagesToUpload = "0",
            isInstructionDefaultVisible = true,
            isInstructionNoModulesVisible = false,
            isInstructionOfflineVisible = false,
            isInstructionErrorVisible = false,
            instructionPopupErrorInfo = SyncInfoError(
                isBackendMaintenance = false,
                backendMaintenanceEstimatedOutage = -1,
                isTooManyRequests = false,
            ),
            isProgressVisible = false,
            progress = SyncInfoProgress(),
            isSyncButtonVisible = true,
            isSyncButtonEnabled = true,
            isSyncButtonForRetry = false,
            isFooterSyncInProgressVisible = false,
            isFooterReadyToLogOutVisible = false,
            isFooterSyncIncompleteVisible = false,
            isFooterLastSyncTimeVisible = false,
            footerLastSyncMinutesAgo = "",
        ),
        syncInfoSectionImages = SyncInfoSectionImages(
            counterImagesToUpload = "0",
            isInstructionDefaultVisible = true,
            isInstructionOfflineVisible = false,
            isProgressVisible = false,
            progress = SyncInfoProgress(),
            isSyncButtonEnabled = true,
            isFooterLastSyncTimeVisible = false,
            footerLastSyncMinutesAgo = "",
        ),
        syncInfoSectionModules = SyncInfoSectionModules(
            isSectionAvailable = false,
            moduleCounts = emptyList(),
        ),
    )

    // LiveData loginNavigationEventLiveData tests

    @Test
    fun `should show login navigation when user requests login`() = runTest {
        viewModel.requestNavigationToLogin()
        val result = viewModel.loginNavigationEventLiveData.getOrAwaitValue()

        assertThat(result).isNotNull()
    }

    // LiveData logoutEventLiveData tests

    @Test
    fun `should trigger logout when pre-logout sync completes successfully`() = runTest {
        val mockCompletedEventSyncState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncCompleted() } returns true
        }
        val mockNotSyncingImageStatus = mockk<ImageSyncStatus>(relaxed = true) {
            every { isSyncing } returns false
            every { progress } returns null
            every { lastUpdateTimeMillis } returns 0
        }
        every { eventSyncManager.getLastSyncState(any()) } returns flowOf(mockCompletedEventSyncState)
        every { syncOrchestrator.observeImageSyncStatus() } returns flowOf(mockNotSyncingImageStatus)
        createViewModel()
        viewModel.isPreLogoutUpSync = true

        var numberOfEmissions = 0
        val flowCollector = async {
            viewModel.logoutEventFlow.collect {
                numberOfEmissions++
            }
        }
        advanceTimeBy(3100L) // after the logout delay (3000ms)
        assertThat(numberOfEmissions).isEqualTo(1)
        flowCollector.cancel()
    }

    @Test
    fun `should emit a logout event after the intended delay since ready to logout`() = runTest {
        val mockCompletedEventSyncState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncCompleted() } returns true
        }
        val mockNotSyncingImageStatus = mockk<ImageSyncStatus>(relaxed = true) {
            every { isSyncing } returns false
            every { progress } returns null
            every { lastUpdateTimeMillis } returns 0
        }
        every { eventSyncManager.getLastSyncState(any()) } returns flowOf(mockCompletedEventSyncState)
        every { syncOrchestrator.observeImageSyncStatus() } returns flowOf(mockNotSyncingImageStatus)
        createViewModel()
        viewModel.isPreLogoutUpSync = true

        var numberOfEmissions = 0
        val flowCollector = async {
            viewModel.logoutEventFlow.collect {
                numberOfEmissions++
            }
        }
        advanceTimeBy(2900L) // still during the debounce delay
        assertThat(numberOfEmissions).isEqualTo(0)
        advanceTimeBy(200L) // after the debounce delay (total 3100ms > 3000ms)
        assertThat(numberOfEmissions).isEqualTo(1)
        flowCollector.cancel()
    }

    @Test
    fun `should emit a logout event when auth store is cleared`() = runTest {
        val projectIdFlow = MutableStateFlow(TEST_PROJECT_ID)
        every { authStore.observeSignedInProjectId() } returns projectIdFlow
        createViewModel()

        var numberOfEmissions = 0
        val flowCollector = async {
            viewModel.logoutEventFlow.collect {
                numberOfEmissions++
            }
        }
        projectIdFlow.value = ""
        advanceUntilIdle()

        assertThat(numberOfEmissions).isEqualTo(1)
        flowCollector.cancel()
    }

    @Test
    fun `should not trigger logout when not in pre-logout mode`() = runTest {
        val mockCompletedEventSyncState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncCompleted() } returns true
        }
        val mockNotSyncingImageStatus = mockk<ImageSyncStatus>(relaxed = true) {
            every { isSyncing } returns false
            every { progress } returns null
            every { lastUpdateTimeMillis } returns 0
        }
        every { eventSyncManager.getLastSyncState(any()) } returns flowOf(mockCompletedEventSyncState)
        every { syncOrchestrator.observeImageSyncStatus() } returns flowOf(mockNotSyncingImageStatus)
        createViewModel()
        viewModel.isPreLogoutUpSync = false

        val flowCollector = async {
            viewModel.logoutEventFlow.collect {
                // fail if any logout event is emitted
                fail("should not emit logout event")
            }
        }
        advanceTimeBy(3100L) // after the logout delay (3000ms)
        flowCollector.cancel()
    }

    @Test
    fun `should not trigger logout when records still syncing`() = runTest {
        val mockInProgressEventSyncState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncCompleted() } returns false
            every { isSyncInProgress() } returns true
        }
        val mockNotSyncingImageStatus = mockk<ImageSyncStatus>(relaxed = true) {
            every { isSyncing } returns false
            every { progress } returns null
            every { lastUpdateTimeMillis } returns 0
        }
        every { eventSyncManager.getLastSyncState(any()) } returns flowOf(mockInProgressEventSyncState)
        every { syncOrchestrator.observeImageSyncStatus() } returns MutableStateFlow(mockNotSyncingImageStatus)
        createViewModel()
        viewModel.isPreLogoutUpSync = true

        val flowCollector = async {
            viewModel.logoutEventFlow.collect {
                // fail if any logout event is emitted
                fail("should not emit logout event")
            }
        }
        advanceTimeBy(3100L) // after the logout delay (3000ms)
        flowCollector.cancel()
    }

    @Test
    fun `should not trigger logout when images still syncing`() = runTest {
        val mockCompletedEventSyncState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncCompleted() } returns true
        }
        val mockSyncingImageStatus = mockk<ImageSyncStatus>(relaxed = true) {
            every { isSyncing } returns true
            every { progress } returns Pair(1, 2)
            every { lastUpdateTimeMillis } returns null
        }
        every { eventSyncManager.getLastSyncState(any()) } returns flowOf(mockCompletedEventSyncState)
        every { syncOrchestrator.observeImageSyncStatus() } returns MutableStateFlow(mockSyncingImageStatus)
        createViewModel()
        viewModel.isPreLogoutUpSync = true

        val flowCollector = async {
            viewModel.logoutEventFlow.collect {
                // fail test if any logout event is emitted
                fail("should not emit logout event")
            }
        }
        advanceTimeBy(3100L) // after the logout delay (3000ms)
        flowCollector.cancel()
    }

    // forceEventSync() tests

    @Test
    fun `should start event sync with down sync allowed when not pre-logout`() = runTest {
        viewModel.isPreLogoutUpSync = false

        viewModel.forceEventSync()

        coVerify { syncOrchestrator.stopEventSync() }
        coVerify { syncOrchestrator.startEventSync(isDownSyncAllowed = true) }
    }

    @Test
    fun `should start event sync with down sync disabled when pre-logout`() = runTest {
        viewModel.isPreLogoutUpSync = true

        viewModel.forceEventSync()

        coVerify { syncOrchestrator.stopEventSync() }
        coVerify { syncOrchestrator.startEventSync(isDownSyncAllowed = false) }
    }

    @Test
    fun `should start event sync with down sync disabled when project paused`() = runTest {
        val mockPausedProject = mockk<Project> {
            every { state } returns ProjectState.PROJECT_PAUSED
        }
        coEvery { configRepository.getProject() } returns mockPausedProject
        createViewModel()
        viewModel.isPreLogoutUpSync = false

        viewModel.forceEventSync()

        coVerify { syncOrchestrator.stopEventSync() }
        coVerify { syncOrchestrator.startEventSync(isDownSyncAllowed = false) }
    }

    @Test
    fun `should start event sync with down sync disabled when project ending`() = runTest {
        val mockEndingProject = mockk<Project> {
            every { state } returns ProjectState.PROJECT_ENDING
        }
        coEvery { configRepository.getProject() } returns mockEndingProject
        createViewModel()
        viewModel.isPreLogoutUpSync = false

        viewModel.forceEventSync()

        coVerify { syncOrchestrator.stopEventSync() }
        coVerify { syncOrchestrator.startEventSync(isDownSyncAllowed = false) }
    }

    @Test
    fun `should start event sync with down sync disabled event sync when logged out`() = runTest {
        coEvery { configRepository.getProject() } returns null
        createViewModel()
        viewModel.isPreLogoutUpSync = false

        viewModel.forceEventSync()

        coVerify { syncOrchestrator.stopEventSync() }
        coVerify { syncOrchestrator.startEventSync(isDownSyncAllowed = false) }
    }

    @Test
    fun `should stop current event sync before starting new one`() = runTest {
        viewModel.forceEventSync()

        coVerify { syncOrchestrator.stopEventSync() }
        coVerify { syncOrchestrator.startEventSync(any()) }
    }

    // toggleImageSync() tests

    @Test
    fun `should start image sync when not currently syncing images`() = runTest {
        val mockNotSyncingImageStatus = mockk<ImageSyncStatus>(relaxed = true) {
            every { isSyncing } returns false
        }
        every { syncOrchestrator.observeImageSyncStatus() } returns MutableStateFlow(mockNotSyncingImageStatus)
        createViewModel()

        viewModel.toggleImageSync()

        coVerify { syncOrchestrator.startImageSync() }
        coVerify(exactly = 0) { syncOrchestrator.stopImageSync() }
    }

    @Test
    fun `should stop image sync when currently syncing images`() = runTest {
        val mockSyncingImageStatus = mockk<ImageSyncStatus>(relaxed = true) {
            every { isSyncing } returns true
        }
        every { syncOrchestrator.observeImageSyncStatus() } returns MutableStateFlow(mockSyncingImageStatus)
        createViewModel()

        viewModel.toggleImageSync()

        coVerify { syncOrchestrator.stopImageSync() }
        coVerify(exactly = 0) { syncOrchestrator.startImageSync() }
    }

    // logout() tests

    @Test
    fun `should call logout use case when logout invoked`() = runTest {
        viewModel.performLogout()

        coVerify { logoutUseCase() }
    }

    // requestNavigationToLogin() tests

    @Test
    fun `should emit login navigation event with signed in project ID`() = runTest {
        viewModel.requestNavigationToLogin()

        val result = viewModel.loginNavigationEventLiveData.getOrAwaitValue()

        assertThat(result.projectId).isEqualTo(TEST_PROJECT_ID)
        assertThat(result).isNotNull()
    }

    @Test
    fun `should emit login navigation event with signed in user ID when available`() = runTest {
        viewModel.requestNavigationToLogin()

        val result = viewModel.loginNavigationEventLiveData.getOrAwaitValue()

        assertThat(result.userId.value).isEqualTo(TEST_USER_ID)
    }

    @Test
    fun `should emit login navigation event with recent user ID when signed in user unavailable`() = runTest {
        every { authStore.signedInUserId } returns null
        createViewModel()

        viewModel.requestNavigationToLogin()

        val result = viewModel.loginNavigationEventLiveData.getOrAwaitValue()
        assertThat(result.userId.value).isEqualTo(TEST_RECENT_USER_ID)
    }

    // handleLoginResult() tests

    @Test
    fun `should trigger forceEventSync when login result is success`() = runTest {
        val successResult = mockk<LoginResult> {
            every { isSuccess } returns true
        }

        viewModel.handleLoginResult(successResult)

        coVerify { syncOrchestrator.startEventSync(any()) }
    }

    @Test
    fun `should not trigger forceEventSync when login result is failure`() = runTest {
        val failureResult = mockk<LoginResult> {
            every { isSuccess } returns false
        }

        viewModel.handleLoginResult(failureResult)

        coVerify(exactly = 0) { syncOrchestrator.startEventSync(any()) }
    }

    // Sync button responsiveness optimization

    @Test
    fun `should immediately show event progress snapshot when forcing event sync`() = runTest {
        createViewModel()

        val values = viewModel.syncInfoLiveData.getOrAwaitValues(number = 2) {
            viewModel.forceEventSync()
        }

        val initial = values[0]
        val forced = values[1]
        assertThat(initial.syncInfoSectionRecords.isProgressVisible).isFalse()
        assertThat(forced.syncInfoSectionRecords.isProgressVisible).isTrue()
    }

    @Test
    fun `should not emit forced event progress when events already syncing`() = runTest {
        val mockInProgressEventSyncState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncInProgress() } returns true
        }
        every { eventSyncManager.getLastSyncState(any()) } returns flowOf(mockInProgressEventSyncState)
        createViewModel()

        val values = viewModel.syncInfoLiveData.getOrAwaitValues(number = 1) {
            viewModel.forceEventSync()
        }

        val initial = values[0]
        assertThat(initial.syncInfoSectionRecords.isProgressVisible).isFalse()
    }

    @Test
    fun `should immediately show image progress snapshot when starting image sync`() = runTest {
        val mockNotSyncingImageStatus = mockk<ImageSyncStatus>(relaxed = true) {
            every { isSyncing } returns false
        }
        every { syncOrchestrator.observeImageSyncStatus() } returns MutableStateFlow(mockNotSyncingImageStatus)
        createViewModel()

        val values = viewModel.syncInfoLiveData.getOrAwaitValues(number = 2) {
            viewModel.toggleImageSync()
        }

        val initial = values[0]
        val forced = values[1]
        assertThat(initial.syncInfoSectionImages.isProgressVisible).isFalse()
        assertThat(forced.syncInfoSectionImages.isProgressVisible).isTrue()
    }

    @Test
    fun `should not emit forced image progress when stopping image sync`() = runTest {
        val mockSyncingImageStatus = mockk<ImageSyncStatus>(relaxed = true) {
            every { isSyncing } returns true
        }
        every { syncOrchestrator.observeImageSyncStatus() } returns MutableStateFlow(mockSyncingImageStatus)
        createViewModel()

        val values = viewModel.syncInfoLiveData.getOrAwaitValues(number = 1) {
            viewModel.toggleImageSync()
        }

        val initial = values[0]
        assertThat(initial.syncInfoSectionImages.isProgressVisible).isFalse()
    }

    @Test
    fun `should switch from forced to data-driven event sync progress once available`() = runTest {
        val base = createDefaultSyncInfo()
        val dataFlow = MutableStateFlow(base)
        every { observeSyncInfo(any()) } returns dataFlow
        createViewModel()

        val values = viewModel.syncInfoLiveData.getOrAwaitValues(number = 3) {
            viewModel.forceEventSync()
            dataFlow.value = base.copy(
                syncInfoSectionRecords = base.syncInfoSectionRecords.copy(
                    isProgressVisible = true,
                    counterTotalRecords = "123",
                ),
            )
        }

        val initial = values[0]
        val forced = values[1]
        val dataDriven = values[2]
        assertThat(initial.syncInfoSectionRecords.isProgressVisible).isFalse()
        assertThat(forced.syncInfoSectionRecords.isProgressVisible).isTrue()
        assertThat(forced.syncInfoSectionRecords.counterTotalRecords).isEmpty()
        assertThat(dataDriven.syncInfoSectionRecords.isProgressVisible).isTrue()
        assertThat(dataDriven.syncInfoSectionRecords.counterTotalRecords).isEqualTo("123")
    }

    @Test
    fun `should switch from forced to data-driven image sync progress once available`() = runTest {
        val base = createDefaultSyncInfo()
        val dataFlow = MutableStateFlow(base)
        every { observeSyncInfo(any()) } returns dataFlow
        createViewModel()

        val values = viewModel.syncInfoLiveData.getOrAwaitValues(number = 3) {
            viewModel.toggleImageSync()
            dataFlow.value = base.copy(
                syncInfoSectionImages = base.syncInfoSectionImages.copy(
                    isProgressVisible = true,
                    counterImagesToUpload = "123",
                ),
            )
        }

        val initial = values[0]
        val forced = values[1]
        val dataDriven = values[2]
        assertThat(initial.syncInfoSectionImages.isProgressVisible).isFalse()
        assertThat(forced.syncInfoSectionImages.isProgressVisible).isTrue()
        assertThat(forced.syncInfoSectionImages.counterImagesToUpload).isEmpty()
        assertThat(dataDriven.syncInfoSectionImages.isProgressVisible).isTrue()
        assertThat(dataDriven.syncInfoSectionImages.counterImagesToUpload).isEqualTo("123")
    }

    // Other/combined UX case tests

    @Test
    fun `should trigger initial sync when no previous sync history`() = runTest {
        val mockIdleEventSyncState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncRunning() } returns false
        }
        every { eventSyncManager.getLastSyncState() } returns flowOf(mockIdleEventSyncState)
        coEvery { eventSyncManager.getLastSyncTime() } returns null
        createViewModel()

        viewModel.syncInfoLiveData.getOrAwaitValue()

        coVerify { syncOrchestrator.startEventSync(any()) }
    }

    @Test
    fun `should trigger initial sync when last sync too old`() = runTest {
        val oldTimestamp = Timestamp(TEST_TIMESTAMP.ms - 600000) // 10 minutes ago, over threshold of 5
        val mockIdleEventSyncState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncRunning() } returns false
        }
        every { eventSyncManager.getLastSyncState() } returns flowOf(mockIdleEventSyncState)
        coEvery { eventSyncManager.getLastSyncTime() } returns oldTimestamp
        every { timeHelper.msBetweenNowAndTime(oldTimestamp) } returns 600000L // 10 minutes
        createViewModel()

        viewModel.syncInfoLiveData.getOrAwaitValue()

        coVerify { syncOrchestrator.startEventSync(any()) }
    }

    @Test
    fun `should not trigger initial sync when recently synced`() = runTest {
        val recentTimestamp = Timestamp(TEST_TIMESTAMP.ms - 60000) // 1 minute ago, under threshold of 5
        val mockIdleEventSyncState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncRunning() } returns false
        }
        every { eventSyncManager.getLastSyncState() } returns flowOf(mockIdleEventSyncState)
        coEvery { eventSyncManager.getLastSyncTime() } returns recentTimestamp
        every { timeHelper.msBetweenNowAndTime(recentTimestamp) } returns 60000L // 1 minute
        createViewModel()

        viewModel.syncInfoLiveData.getOrAwaitValue()

        coVerify(exactly = 0) { syncOrchestrator.startEventSync(any()) }
    }

    @Test
    fun `should not trigger initial sync when sync already running`() = runTest {
        val mockRunningSyncState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncRunning() } returns true
        }
        every { eventSyncManager.getLastSyncState() } returns flowOf(mockRunningSyncState)
        coEvery { eventSyncManager.getLastSyncTime() } returns null
        createViewModel()

        viewModel.syncInfoLiveData.getOrAwaitValue()

        coVerify(exactly = 0) { syncOrchestrator.startEventSync(any()) }
    }

    @Test
    fun `should trigger initial sync in pre-logout mode regardless of history`() = runTest {
        val recentTimestamp = Timestamp(TEST_TIMESTAMP.ms - 60000) // 1 minute ago, under threshold of 5
        val mockIdleEventSyncState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncRunning() } returns false
        }
        every { eventSyncManager.getLastSyncState() } returns flowOf(mockIdleEventSyncState)
        coEvery { eventSyncManager.getLastSyncTime() } returns recentTimestamp
        every { timeHelper.msBetweenNowAndTime(recentTimestamp) } returns 60000L // 1 minute
        createViewModel()
        viewModel.isPreLogoutUpSync = true

        viewModel.syncInfoLiveData.getOrAwaitValue()

        coVerify(atLeast = 0) { syncOrchestrator.startEventSync(any()) }
    }

    @Test
    fun `should trigger initial sync when in pre-logout mode and module selection required`() = runTest {
        val mockProjectConfigRequiringModules = mockk<ProjectConfiguration> {
            every { general } returns mockk<GeneralConfiguration> {
                every { modalities } returns listOf(Modality.FINGERPRINT)
            }
        }
        val mockEmptyDeviceConfig = mockk<DeviceConfiguration> {
            every { selectedModules } returns emptyList()
        }
        coEvery { configRepository.getProjectConfiguration() } returns mockProjectConfigRequiringModules
        coEvery { configRepository.getDeviceConfiguration() } returns mockEmptyDeviceConfig
        every { mockProjectConfigRequiringModules.isModuleSelectionAvailable() } returns true
        coEvery { eventSyncManager.getLastSyncTime() } returns null
        createViewModel()
        viewModel.isPreLogoutUpSync = true

        viewModel.syncInfoLiveData.getOrAwaitValue()

        coVerify(exactly = 1) { syncOrchestrator.startEventSync(any()) }
    }

    @Test
    fun `should not trigger initial sync when not in pre-logout mode and module selection required`() = runTest {
        val mockProjectConfigRequiringModules = mockk<ProjectConfiguration> {
            every { general } returns mockk<GeneralConfiguration> {
                every { modalities } returns listOf(Modality.FINGERPRINT)
            }
        }
        val mockEmptyDeviceConfig = mockk<DeviceConfiguration> {
            every { selectedModules } returns emptyList()
        }
        coEvery { configRepository.getProjectConfiguration() } returns mockProjectConfigRequiringModules
        coEvery { configRepository.getDeviceConfiguration() } returns mockEmptyDeviceConfig
        every { mockProjectConfigRequiringModules.isModuleSelectionAvailable() } returns true
        coEvery { eventSyncManager.getLastSyncTime() } returns null
        createViewModel()
        viewModel.isPreLogoutUpSync = false

        viewModel.syncInfoLiveData.getOrAwaitValue()

        coVerify(exactly = 0) { syncOrchestrator.startEventSync(any()) }
    }

    private companion object {
        const val TEST_PROJECT_ID = "test_project_id"
        const val TEST_USER_ID = "test_user_id"
        const val TEST_RECENT_USER_ID = "recent_user_id"
        val TEST_TIMESTAMP = Timestamp(1000L)
    }
}
