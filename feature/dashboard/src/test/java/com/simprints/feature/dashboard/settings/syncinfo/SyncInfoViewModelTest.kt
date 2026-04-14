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
import com.simprints.infra.eventsync.status.models.DownSyncState
import com.simprints.infra.eventsync.status.models.UpSyncState
import com.simprints.infra.recent.user.activity.RecentUserActivityManager
import com.simprints.infra.sync.ImageSyncStatus
import com.simprints.infra.sync.OneTime
import com.simprints.infra.sync.SyncOrchestrator
import com.simprints.infra.sync.SyncStatus
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.livedata.getOrAwaitValue
import com.simprints.testtools.common.livedata.getOrAwaitValues
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
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
    private lateinit var recentUserActivityManager: RecentUserActivityManager

    @MockK
    private lateinit var timeHelper: TimeHelper

    @MockK
    private lateinit var observeSyncInfo: ObserveSyncInfoUseCase

    @MockK
    private lateinit var syncOrchestrator: SyncOrchestrator

    @MockK
    private lateinit var logoutUseCase: LogoutUseCase

    @MockK
    private lateinit var mockProjectConfiguration: ProjectConfiguration

    @MockK
    private lateinit var mockDeviceConfiguration: DeviceConfiguration

    @MockK
    private lateinit var mockProject: Project

    @MockK
    private lateinit var mockUpSyncState: UpSyncState

    @MockK
    private lateinit var mockDownSyncState: DownSyncState

    @MockK
    private lateinit var mockImageSyncStatus: ImageSyncStatus

    private lateinit var syncStatusFlow: MutableStateFlow<SyncStatus>

    private lateinit var viewModel: SyncInfoViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        every { mockProjectConfiguration.general } returns mockk<GeneralConfiguration>(relaxed = true) {
            every { modalities } returns emptyList()
        }
        every { mockDeviceConfiguration.selectedModules } returns emptyList()
        every { mockProject.state } returns ProjectState.RUNNING

        every { mockUpSyncState.isSyncCompleted() } returns false
        every { mockUpSyncState.isSyncInProgress() } returns false
        every { mockUpSyncState.isSyncConnecting() } returns false
        every { mockUpSyncState.isSyncRunning() } returns false
        every { mockUpSyncState.isSyncFailed() } returns false
        every { mockUpSyncState.isSyncFailedBecauseReloginRequired() } returns false
        every { mockUpSyncState.isSyncFailedBecauseBackendMaintenance() } returns false
        every { mockUpSyncState.isSyncFailedBecauseTooManyRequests() } returns false
        every { mockUpSyncState.hasSyncHistory() } returns true
        every { mockUpSyncState.progress } returns null
        every { mockUpSyncState.total } returns null
        every { mockUpSyncState.lastSyncTime } returns TEST_TIMESTAMP

        every { mockDownSyncState.isSyncCompleted() } returns false
        every { mockDownSyncState.isSyncInProgress() } returns false
        every { mockDownSyncState.isSyncConnecting() } returns false
        every { mockDownSyncState.isSyncRunning() } returns false
        every { mockDownSyncState.isSyncFailed() } returns false
        every { mockDownSyncState.isSyncFailedBecauseReloginRequired() } returns false
        every { mockDownSyncState.isSyncFailedBecauseBackendMaintenance() } returns false
        every { mockDownSyncState.isSyncFailedBecauseTooManyRequests() } returns false
        every { mockDownSyncState.getEstimatedBackendMaintenanceOutage() } returns null
        every { mockDownSyncState.hasSyncHistory() } returns true
        every { mockDownSyncState.progress } returns null
        every { mockDownSyncState.total } returns null
        every { mockDownSyncState.lastSyncTime } returns TEST_TIMESTAMP

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

        syncStatusFlow = MutableStateFlow(
            SyncStatus(upSyncState = mockUpSyncState, downSyncState = mockDownSyncState, imageSyncStatus = mockImageSyncStatus),
        )
        every { syncOrchestrator.observeSyncState() } returns syncStatusFlow
        every { syncOrchestrator.execute(any<OneTime>()) } returns Job().apply { complete() }

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
            recentUserActivityManager = recentUserActivityManager,
            timeHelper = timeHelper,
            observeSyncInfo = observeSyncInfo,
            syncOrchestrator = syncOrchestrator,
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
            recordSyncVisibleState = RecordSyncVisibleState.ON_STANDBY,
            instructionPopupErrorInfo = SyncInfoError(
                isBackendMaintenance = false,
                backendMaintenanceEstimatedOutage = -1,
                isTooManyRequests = false,
            ),
            isProgressVisible = false,
            progress = SyncProgressInfo(),
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
            progress = SyncProgressInfo(),
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
        val mockCompletedUpSyncState = mockk<UpSyncState>(relaxed = true) {
            every { isSyncCompleted() } returns true
        }
        val mockCompletedDownSyncState = mockk<DownSyncState>(relaxed = true) {
            every { isSyncCompleted() } returns true
        }
        val mockNotSyncingImageStatus = mockk<ImageSyncStatus>(relaxed = true) {
            every { isSyncing } returns false
            every { progress } returns null
            every { lastUpdateTimeMillis } returns 0
        }
        syncStatusFlow.value = SyncStatus(upSyncState = mockCompletedUpSyncState, downSyncState = mockCompletedDownSyncState, imageSyncStatus = mockNotSyncingImageStatus)
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
        val mockCompletedUpSyncState = mockk<UpSyncState>(relaxed = true) {
            every { isSyncCompleted() } returns true
        }
        val mockCompletedDownSyncState = mockk<DownSyncState>(relaxed = true) {
            every { isSyncCompleted() } returns true
        }
        val mockNotSyncingImageStatus = mockk<ImageSyncStatus>(relaxed = true) {
            every { isSyncing } returns false
            every { progress } returns null
            every { lastUpdateTimeMillis } returns 0
        }
        syncStatusFlow.value = SyncStatus(upSyncState = mockCompletedUpSyncState, downSyncState = mockCompletedDownSyncState, imageSyncStatus = mockNotSyncingImageStatus)
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
        val mockCompletedUpSyncState = mockk<UpSyncState>(relaxed = true) {
            every { isSyncCompleted() } returns true
        }
        val mockCompletedDownSyncState = mockk<DownSyncState>(relaxed = true) {
            every { isSyncCompleted() } returns true
        }
        val mockNotSyncingImageStatus = mockk<ImageSyncStatus>(relaxed = true) {
            every { isSyncing } returns false
            every { progress } returns null
            every { lastUpdateTimeMillis } returns 0
        }
        syncStatusFlow.value = SyncStatus(upSyncState = mockCompletedUpSyncState, downSyncState = mockCompletedDownSyncState, imageSyncStatus = mockNotSyncingImageStatus)
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
        val mockInProgressUpSyncState = mockk<UpSyncState>(relaxed = true) {
            every { isSyncCompleted() } returns false
            every { isSyncInProgress() } returns true
        }
        val mockInProgressDownSyncState = mockk<DownSyncState>(relaxed = true) {
            every { isSyncCompleted() } returns false
            every { isSyncInProgress() } returns true
        }
        val mockNotSyncingImageStatus = mockk<ImageSyncStatus>(relaxed = true) {
            every { isSyncing } returns false
            every { progress } returns null
            every { lastUpdateTimeMillis } returns 0
        }
        syncStatusFlow.value = SyncStatus(upSyncState = mockInProgressUpSyncState, downSyncState = mockInProgressDownSyncState, imageSyncStatus = mockNotSyncingImageStatus)
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
        val mockCompletedUpSyncState = mockk<UpSyncState>(relaxed = true) {
            every { isSyncCompleted() } returns true
        }
        val mockCompletedDownSyncState = mockk<DownSyncState>(relaxed = true) {
            every { isSyncCompleted() } returns true
        }
        val mockSyncingImageStatus = mockk<ImageSyncStatus>(relaxed = true) {
            every { isSyncing } returns true
            every { progress } returns Pair(1, 2)
            every { lastUpdateTimeMillis } returns null
        }
        syncStatusFlow.value = SyncStatus(upSyncState = mockCompletedUpSyncState, downSyncState = mockCompletedDownSyncState, imageSyncStatus = mockSyncingImageStatus)
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

        verify { syncOrchestrator.execute(OneTime.UpSync.restart()) }
        verify { syncOrchestrator.execute(OneTime.DownSync.restart()) }
    }

    @Test
    fun `should start event sync with down sync disabled when pre-logout`() = runTest {
        viewModel.isPreLogoutUpSync = true

        viewModel.forceEventSync()

        verify { syncOrchestrator.execute(OneTime.UpSync.restart()) }
        verify(exactly = 0) { syncOrchestrator.execute(OneTime.DownSync.restart()) }
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

        verify { syncOrchestrator.execute(OneTime.UpSync.restart()) }
        verify(exactly = 0) { syncOrchestrator.execute(OneTime.DownSync.restart()) }
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

        verify { syncOrchestrator.execute(OneTime.UpSync.restart()) }
        verify(exactly = 0) { syncOrchestrator.execute(OneTime.DownSync.restart()) }
    }

    @Test
    fun `should start event sync with down sync disabled event sync when logged out`() = runTest {
        coEvery { configRepository.getProject() } returns null
        createViewModel()
        viewModel.isPreLogoutUpSync = false

        viewModel.forceEventSync()

        verify { syncOrchestrator.execute(OneTime.UpSync.restart()) }
        verify(exactly = 0) { syncOrchestrator.execute(OneTime.DownSync.restart()) }
    }

    @Test
    fun `should stop current event sync before starting new one`() = runTest {
        viewModel.forceEventSync()

        verify { syncOrchestrator.execute(OneTime.UpSync.restart()) }
        verify { syncOrchestrator.execute(OneTime.DownSync.restart()) }
    }

    // toggleImageSync() tests

    @Test
    fun `should start image sync when not currently syncing images`() = runTest {
        val mockNotSyncingImageStatus = mockk<ImageSyncStatus>(relaxed = true) {
            every { isSyncing } returns false
        }
        syncStatusFlow.value = SyncStatus(upSyncState = mockUpSyncState, downSyncState = mockDownSyncState, imageSyncStatus = mockNotSyncingImageStatus)
        createViewModel()

        viewModel.toggleImageSync()

        verify { syncOrchestrator.execute(OneTime.Images.start()) }
        verify(exactly = 0) { syncOrchestrator.execute(OneTime.Images.stop()) }
    }

    @Test
    fun `should stop image sync when currently syncing images`() = runTest {
        val mockSyncingImageStatus = mockk<ImageSyncStatus>(relaxed = true) {
            every { isSyncing } returns true
        }
        syncStatusFlow.value = SyncStatus(upSyncState = mockUpSyncState, downSyncState = mockDownSyncState, imageSyncStatus = mockSyncingImageStatus)
        createViewModel()

        viewModel.toggleImageSync()

        verify { syncOrchestrator.execute(OneTime.Images.stop()) }
        verify(exactly = 0) { syncOrchestrator.execute(OneTime.Images.start()) }
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

        verify { syncOrchestrator.execute(OneTime.UpSync.restart()) }
        verify { syncOrchestrator.execute(OneTime.DownSync.restart()) }
    }

    @Test
    fun `should not trigger forceEventSync when login result is failure`() = runTest {
        val failureResult = mockk<LoginResult> {
            every { isSuccess } returns false
        }

        viewModel.handleLoginResult(failureResult)

        verify(exactly = 0) { syncOrchestrator.execute(OneTime.UpSync.restart()) }
        verify(exactly = 0) { syncOrchestrator.execute(OneTime.DownSync.restart()) }
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
        val mockInProgressUpSyncState = mockk<UpSyncState>(relaxed = true) {
            every { isSyncInProgress() } returns true
        }
        val mockInProgressDownSyncState = mockk<DownSyncState>(relaxed = true) {
            every { isSyncInProgress() } returns true
        }
        syncStatusFlow.value = SyncStatus(upSyncState = mockInProgressUpSyncState, downSyncState = mockInProgressDownSyncState, imageSyncStatus = mockImageSyncStatus)
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
        syncStatusFlow.value = SyncStatus(upSyncState = mockUpSyncState, downSyncState = mockDownSyncState, imageSyncStatus = mockNotSyncingImageStatus)
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
        syncStatusFlow.value = SyncStatus(upSyncState = mockUpSyncState, downSyncState = mockDownSyncState, imageSyncStatus = mockSyncingImageStatus)
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
        val mockIdleUpSyncState = mockk<UpSyncState>(relaxed = true) {
            every { isSyncRunning() } returns false
            every { lastSyncTime } returns null
        }
        val mockIdleDownSyncState = mockk<DownSyncState>(relaxed = true) {
            every { isSyncRunning() } returns false
            every { lastSyncTime } returns null
        }
        syncStatusFlow.value = SyncStatus(upSyncState = mockIdleUpSyncState, downSyncState = mockIdleDownSyncState, imageSyncStatus = mockImageSyncStatus)
        createViewModel()

        viewModel.syncInfoLiveData.getOrAwaitValue()

        verify { syncOrchestrator.execute(OneTime.UpSync.restart()) }
        verify { syncOrchestrator.execute(OneTime.DownSync.restart()) }
    }

    @Test
    fun `should trigger initial sync when last sync too old`() = runTest {
        val oldTimestamp = Timestamp(TEST_TIMESTAMP.ms - 600000) // 10 minutes ago, over threshold of 5
        val mockIdleUpSyncState = mockk<UpSyncState>(relaxed = true) {
            every { isSyncRunning() } returns false
            every { lastSyncTime } returns oldTimestamp
        }
        val mockIdleDownSyncState = mockk<DownSyncState>(relaxed = true) {
            every { isSyncRunning() } returns false
            every { lastSyncTime } returns oldTimestamp
        }
        syncStatusFlow.value = SyncStatus(upSyncState = mockIdleUpSyncState, downSyncState = mockIdleDownSyncState, imageSyncStatus = mockImageSyncStatus)
        every { timeHelper.msBetweenNowAndTime(oldTimestamp) } returns 600000L // 10 minutes
        createViewModel()

        viewModel.syncInfoLiveData.getOrAwaitValue()

        verify { syncOrchestrator.execute(OneTime.UpSync.restart()) }
        verify { syncOrchestrator.execute(OneTime.DownSync.restart()) }
    }

    @Test
    fun `should not trigger initial sync when recently synced`() = runTest {
        val recentTimestamp = Timestamp(TEST_TIMESTAMP.ms - 60000) // 1 minute ago, under threshold of 5
        val mockIdleUpSyncState = mockk<UpSyncState>(relaxed = true) {
            every { isSyncRunning() } returns false
            every { lastSyncTime } returns recentTimestamp
        }
        val mockIdleDownSyncState = mockk<DownSyncState>(relaxed = true) {
            every { isSyncRunning() } returns false
            every { lastSyncTime } returns recentTimestamp
        }
        syncStatusFlow.value = SyncStatus(upSyncState = mockIdleUpSyncState, downSyncState = mockIdleDownSyncState, imageSyncStatus = mockImageSyncStatus)
        every { syncOrchestrator.observeUpSyncState() } returns MutableStateFlow(mockIdleUpSyncState)
        every { syncOrchestrator.observeDownSyncState() } returns MutableStateFlow(mockIdleDownSyncState)
        every { timeHelper.msBetweenNowAndTime(recentTimestamp) } returns 60000L // 1 minute
        createViewModel()

        viewModel.syncInfoLiveData.getOrAwaitValue()

        verify(exactly = 0) { syncOrchestrator.execute(OneTime.UpSync.restart()) }
        verify(exactly = 0) { syncOrchestrator.execute(OneTime.DownSync.restart()) }
    }

    @Test
    fun `should not trigger initial sync when sync already running`() = runTest {
        val mockRunningUpSyncState = mockk<UpSyncState>(relaxed = true) {
            every { isSyncRunning() } returns true
            every { lastSyncTime } returns null
        }
        val mockRunningDownSyncState = mockk<DownSyncState>(relaxed = true) {
            every { isSyncRunning() } returns true
            every { lastSyncTime } returns null
        }
        syncStatusFlow.value = SyncStatus(upSyncState = mockRunningUpSyncState, downSyncState = mockRunningDownSyncState, imageSyncStatus = mockImageSyncStatus)
        every { syncOrchestrator.observeUpSyncState() } returns MutableStateFlow(mockRunningUpSyncState)
        every { syncOrchestrator.observeDownSyncState() } returns MutableStateFlow(mockRunningDownSyncState)
        createViewModel()

        viewModel.syncInfoLiveData.getOrAwaitValue()

        verify(exactly = 0) { syncOrchestrator.execute(OneTime.UpSync.restart()) }
        verify(exactly = 0) { syncOrchestrator.execute(OneTime.DownSync.restart()) }
    }

    @Test
    fun `should trigger initial sync in pre-logout mode regardless of history`() = runTest {
        val recentTimestamp = Timestamp(TEST_TIMESTAMP.ms - 60000) // 1 minute ago, under threshold of 5
        val mockIdleUpSyncState = mockk<UpSyncState>(relaxed = true) {
            every { isSyncRunning() } returns false
            every { lastSyncTime } returns recentTimestamp
        }
        val mockIdleDownSyncState = mockk<DownSyncState>(relaxed = true) {
            every { isSyncRunning() } returns false
            every { lastSyncTime } returns recentTimestamp
        }
        syncStatusFlow.value = SyncStatus(upSyncState = mockIdleUpSyncState, downSyncState = mockIdleDownSyncState, imageSyncStatus = mockImageSyncStatus)
        every { timeHelper.msBetweenNowAndTime(recentTimestamp) } returns 60000L // 1 minute
        createViewModel()
        viewModel.isPreLogoutUpSync = true

        viewModel.syncInfoLiveData.getOrAwaitValue()

        verify { syncOrchestrator.execute(OneTime.UpSync.restart()) }
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
        createViewModel()
        viewModel.isPreLogoutUpSync = true

        viewModel.syncInfoLiveData.getOrAwaitValue()

        verify(exactly = 1) { syncOrchestrator.execute(OneTime.UpSync.restart()) }
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
        createViewModel()
        viewModel.isPreLogoutUpSync = false

        viewModel.syncInfoLiveData.getOrAwaitValue()

        verify(exactly = 0) { syncOrchestrator.execute(OneTime.UpSync.restart()) }
        verify(exactly = 0) { syncOrchestrator.execute(OneTime.DownSync.restart()) }
    }

    private companion object {
        const val TEST_PROJECT_ID = "test_project_id"
        const val TEST_USER_ID = "test_user_id"
        const val TEST_RECENT_USER_ID = "recent_user_id"
        val TEST_TIMESTAMP = Timestamp(1000L)
    }
}
