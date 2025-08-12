package com.simprints.feature.dashboard.settings.syncinfo

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.simprints.core.livedata.LiveDataEventWithContent
import androidx.lifecycle.asFlow
import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.feature.dashboard.logout.usecase.LogoutUseCase
import com.simprints.feature.dashboard.settings.syncinfo.usecase.ObserveSyncInfoUseCase
import com.simprints.feature.login.LoginResult
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.models.DeviceConfiguration
import com.simprints.infra.config.store.models.GeneralConfiguration
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.ProjectState
import com.simprints.infra.config.store.models.isEventDownSyncAllowed
import com.simprints.infra.config.store.models.isModuleSelectionAvailable
import com.simprints.infra.config.store.models.isMissingModulesToChooseFrom
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.eventsync.EventSyncManager
import com.simprints.infra.eventsync.status.models.DownSyncCounts
import com.simprints.infra.eventsync.status.models.EventSyncState
import com.simprints.infra.recent.user.activity.RecentUserActivityManager
import com.simprints.infra.sync.SyncOrchestrator
import com.simprints.infra.sync.ImageSyncStatus
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.livedata.getOrAwaitValue
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SyncInfoViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private val configManager = mockk<ConfigManager>()
    private val authStore = mockk<AuthStore>()
    private val eventSyncManager = mockk<EventSyncManager>()
    private val syncOrchestrator = mockk<SyncOrchestrator>()
    private val recentUserActivityManager = mockk<RecentUserActivityManager>()
    private val timeHelper = mockk<TimeHelper>()
    private val observeSyncInfo = mockk<ObserveSyncInfoUseCase>()
    private val logoutUseCase = mockk<LogoutUseCase>(relaxed = true)

    private lateinit var viewModel: SyncInfoViewModel

    private companion object {
        const val TEST_PROJECT_ID = "test_project_id"
        const val TEST_USER_ID = "test_user_id"
        const val TEST_RECENT_USER_ID = "recent_user_id"
        val TEST_TIMESTAMP = Timestamp(1000L)
    }

    private val mockProjectConfiguration = mockk<ProjectConfiguration>(relaxed = true) {
        every { general } returns mockk<GeneralConfiguration>(relaxed = true) {
            every { modalities } returns emptyList()
        }
    }
    private val mockDeviceConfiguration = mockk<DeviceConfiguration>(relaxed = true) {
        every { selectedModules } returns emptyList()
    }
    private val mockProject = mockk<Project>(relaxed = true) {
        every { state } returns ProjectState.RUNNING
    }
    private val mockEventSyncState = mockk<EventSyncState>(relaxed = true) {
        every { isSyncCompleted() } returns false
        every { isSyncInProgress() } returns false
        every { isSyncConnecting() } returns false
        every { isSyncRunning() } returns false
        every { isSyncFailed() } returns false
        every { isSyncFailedBecauseReloginRequired() } returns false
        every { isSyncFailedBecauseBackendMaintenance() } returns false
        every { isSyncFailedBecauseTooManyRequests() } returns false
        every { getEstimatedBackendMaintenanceOutage() } returns null
        every { isThereNotSyncHistory() } returns false
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

        every { configManager.observeIsProjectRefreshing() } returns MutableStateFlow(false)
        every { configManager.observeProjectConfiguration() } returns MutableStateFlow(mockProjectConfiguration)
        every { configManager.observeDeviceConfiguration() } returns MutableStateFlow(mockDeviceConfiguration)
        coEvery { configManager.getProjectConfiguration() } returns mockProjectConfiguration
        coEvery { configManager.getDeviceConfiguration() } returns mockDeviceConfiguration
        coEvery { configManager.getProject(any()) } returns mockProject

        val eventSyncLiveData = MutableLiveData(mockEventSyncState)
        every { eventSyncManager.getLastSyncState() } returns eventSyncLiveData
        every { eventSyncManager.getLastSyncState(any()) } returns eventSyncLiveData
        every { eventSyncLiveData.asFlow() } returns flowOf(mockEventSyncState)
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
        every { any<ProjectConfiguration>().isEventDownSyncAllowed() } returns true
        every { any<ProjectConfiguration>().isMissingModulesToChooseFrom() } returns false

        every { observeSyncInfo(any()) } returns flowOf(createDefaultSyncInfo())
    }

    private fun createViewModel() {
        viewModel = SyncInfoViewModel(
            configManager = configManager,
            authStore = authStore,
            eventSyncManager = eventSyncManager,
            syncOrchestrator = syncOrchestrator,
            recentUserActivityManager = recentUserActivityManager,
            timeHelper = timeHelper,
            observeSyncInfo = observeSyncInfo,
            logoutUseCase = logoutUseCase,
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
                isTooManyRequests = false
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
            moduleCounts = emptyList()
        )
    )

    // LiveData loginNavigationEventLiveData tests

    @Test
    fun `should show login navigation when user requests login`() = runTest {
        viewModel.requestNavigationToLogin()
        val result = viewModel.loginNavigationEventLiveData.getOrAwaitValue()

        assertThat(result).isNotNull()
    }

    // LiveData logoutEventLiveData tests

    @OptIn(ExperimentalCoroutinesApi::class)
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
        every { eventSyncManager.getLastSyncState(any()) } returns MutableLiveData(mockCompletedEventSyncState)
        every { syncOrchestrator.observeImageSyncStatus() } returns MutableStateFlow(mockNotSyncingImageStatus)
        createViewModel()
        viewModel.isPreLogoutUpSync = true
        val observer = mockk<Observer<LiveDataEventWithContent<Unit>>>(relaxed = true)
        val slot = slot<LiveDataEventWithContent<Unit>>()
        val capturedValues = mutableListOf<LiveDataEventWithContent<Unit>>()
        every { observer.onChanged(capture(slot)) } answers {
            capturedValues.add(slot.captured)
        }

        viewModel.logoutEventLiveData.observeForever(observer)
        advanceTimeBy(3100L) // after the logout delay (3000ms)

        assertThat(capturedValues.map { it.peekContent() }).contains(Unit)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
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
        every { eventSyncManager.getLastSyncState(any()) } returns MutableLiveData(mockCompletedEventSyncState)
        every { syncOrchestrator.observeImageSyncStatus() } returns MutableStateFlow(mockNotSyncingImageStatus)
        createViewModel()
        viewModel.isPreLogoutUpSync = true
        val observer = mockk<Observer<LiveDataEventWithContent<Unit>>>(relaxed = true)
        val slot = slot<LiveDataEventWithContent<Unit>>()
        val capturedValues = mutableListOf<LiveDataEventWithContent<Unit>>()
        every { observer.onChanged(capture(slot)) } answers {
            capturedValues.add(slot.captured)
        }

        viewModel.logoutEventLiveData.observeForever(observer)
        advanceTimeBy(2900L) // still during the debounce delay

        assertThat(capturedValues).isEmpty() // no logout event yet

        advanceTimeBy(200L) // after the debounce delay (total 3100ms > 3000ms)

        assertThat(capturedValues).hasSize(1)
        assertThat(capturedValues[0].peekContent()).isEqualTo(Unit)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
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
        every { eventSyncManager.getLastSyncState(any()) } returns MutableLiveData(mockCompletedEventSyncState)
        every { syncOrchestrator.observeImageSyncStatus() } returns MutableStateFlow(mockNotSyncingImageStatus)
        createViewModel()
        viewModel.isPreLogoutUpSync = false
        val observer = mockk<Observer<LiveDataEventWithContent<Unit>>>(relaxed = true)
        val slot = slot<LiveDataEventWithContent<Unit>>()
        val capturedValues = mutableListOf<LiveDataEventWithContent<Unit>>()
        every { observer.onChanged(capture(slot)) } answers {
            capturedValues.add(slot.captured)
        }

        viewModel.logoutEventLiveData.observeForever(observer)
        advanceTimeBy(3100L) // after the logout delay (3000ms)

        assertThat(capturedValues).isEmpty()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
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
        every { eventSyncManager.getLastSyncState(any()) } returns MutableLiveData(mockInProgressEventSyncState)
        every { syncOrchestrator.observeImageSyncStatus() } returns MutableStateFlow(mockNotSyncingImageStatus)
        createViewModel()
        viewModel.isPreLogoutUpSync = true
        val observer = mockk<Observer<LiveDataEventWithContent<Unit>>>(relaxed = true)
        val slot = slot<LiveDataEventWithContent<Unit>>()
        val capturedValues = mutableListOf<LiveDataEventWithContent<Unit>>()
        every { observer.onChanged(capture(slot)) } answers {
            capturedValues.add(slot.captured)
        }

        viewModel.logoutEventLiveData.observeForever(observer)
        advanceTimeBy(3100L) // after the logout delay (3000ms)

        assertThat(capturedValues).isEmpty()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
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
        every { eventSyncManager.getLastSyncState(any()) } returns MutableLiveData(mockCompletedEventSyncState)
        every { syncOrchestrator.observeImageSyncStatus() } returns MutableStateFlow(mockSyncingImageStatus)
        createViewModel()
        viewModel.isPreLogoutUpSync = true
        val observer = mockk<Observer<LiveDataEventWithContent<Unit>>>(relaxed = true)
        val slot = slot<LiveDataEventWithContent<Unit>>()
        val capturedValues = mutableListOf<LiveDataEventWithContent<Unit>>()
        every { observer.onChanged(capture(slot)) } answers {
            capturedValues.add(slot.captured)
        }

        viewModel.logoutEventLiveData.observeForever(observer)
        advanceTimeBy(3100L) // after the logout delay (3000ms)

        assertThat(capturedValues).isEmpty()
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
    fun `should start event sync with down sync disabled when project ending`() = runTest {
        val mockEndingProject = mockk<Project> {
            every { state } returns ProjectState.PROJECT_ENDING
        }
        coEvery { configManager.getProject(any()) } returns mockEndingProject
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

        verify { logoutUseCase() }
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

    // Other/combined UX case tests

    @Test
    fun `should trigger initial sync when no previous sync history`() = runTest {
        val mockIdleEventSyncState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncRunning() } returns false
        }
        every { eventSyncManager.getLastSyncState() } returns MutableLiveData(mockIdleEventSyncState)
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
        every { eventSyncManager.getLastSyncState() } returns MutableLiveData(mockIdleEventSyncState)
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
        every { eventSyncManager.getLastSyncState() } returns MutableLiveData(mockIdleEventSyncState)
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
        every { eventSyncManager.getLastSyncState() } returns MutableLiveData(mockRunningSyncState)
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
        every { eventSyncManager.getLastSyncState() } returns MutableLiveData(mockIdleEventSyncState)
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
                every { modalities } returns listOf(GeneralConfiguration.Modality.FINGERPRINT)
            }
        }
        val mockEmptyDeviceConfig = mockk<DeviceConfiguration> {
            every { selectedModules } returns emptyList()
        }
        coEvery { configManager.getProjectConfiguration() } returns mockProjectConfigRequiringModules
        coEvery { configManager.getDeviceConfiguration() } returns mockEmptyDeviceConfig
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
                every { modalities } returns listOf(GeneralConfiguration.Modality.FINGERPRINT)
            }
        }
        val mockEmptyDeviceConfig = mockk<DeviceConfiguration> {
            every { selectedModules } returns emptyList()
        }
        coEvery { configManager.getProjectConfiguration() } returns mockProjectConfigRequiringModules
        coEvery { configManager.getDeviceConfiguration() } returns mockEmptyDeviceConfig
        every { mockProjectConfigRequiringModules.isModuleSelectionAvailable() } returns true
        coEvery { eventSyncManager.getLastSyncTime() } returns null
        createViewModel()
        viewModel.isPreLogoutUpSync = false

        viewModel.syncInfoLiveData.getOrAwaitValue()

        coVerify(exactly = 0) { syncOrchestrator.startEventSync(any()) }
    }

}
