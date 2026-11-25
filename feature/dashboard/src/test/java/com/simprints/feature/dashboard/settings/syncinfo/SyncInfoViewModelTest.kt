package com.simprints.feature.dashboard.settings.syncinfo

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.google.common.truth.Truth.*
import com.simprints.core.domain.common.Modality
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.feature.dashboard.settings.syncinfo.usecase.ObserveSyncInfoUseCase
import com.simprints.feature.dashboard.tools.ViewModelScopeRule
import com.simprints.feature.login.LoginParams
import com.simprints.feature.login.LoginResult
import com.simprints.feature.moduleselector.ModuleSelectorResult
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.DeviceConfiguration
import com.simprints.infra.config.store.models.GeneralConfiguration
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.ProjectState
import com.simprints.infra.config.store.models.isModuleSelectionAvailable
import com.simprints.infra.config.store.models.isSimprintsEventDownSyncAllowed
import com.simprints.infra.eventsync.status.models.EventSyncState
import com.simprints.infra.recent.user.activity.RecentUserActivityManager
import com.simprints.infra.sync.ImageSyncStatus
import com.simprints.infra.sync.OneTime
import com.simprints.infra.sync.SyncOrchestrator
import com.simprints.infra.sync.SyncStatus
import com.simprints.infra.sync.devicestate.DeviceDataState
import com.simprints.infra.sync.devicestate.DeviceStateDataTracker
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SyncInfoViewModelTest {
    @get:Rule(order = 0)
    val testCoroutineRule = TestCoroutineRule()

    // Inside TestCoroutineRule: scopes are cancelled while the test Main dispatcher is still set
    @get:Rule(order = 1)
    val viewModels = ViewModelScopeRule()

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
    private lateinit var deviceStateDataTracker: DeviceStateDataTracker

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

    @MockK
    private lateinit var mockDeviceDataState: DeviceDataState

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

        every { mockEventSyncState.isSyncCompleted() } returns false
        every { mockEventSyncState.isSyncInProgress() } returns false
        every { mockEventSyncState.isSyncConnecting() } returns false
        every { mockEventSyncState.isSyncRunning() } returns false
        every { mockEventSyncState.isSyncFailed() } returns false
        every { mockEventSyncState.isSyncFailedBecauseReloginRequired() } returns false
        every { mockEventSyncState.isSyncFailedBecauseBackendMaintenance() } returns false
        every { mockEventSyncState.isSyncFailedBecauseTooManyRequests() } returns false
        every { mockEventSyncState.getEstimatedBackendMaintenanceOutage() } returns null
        every { mockEventSyncState.hasSyncHistory() } returns true
        every { mockEventSyncState.progress } returns null
        every { mockEventSyncState.total } returns null
        every { mockEventSyncState.lastSyncTime } returns TEST_TIMESTAMP

        every { mockImageSyncStatus.isSyncing } returns false
        every { mockImageSyncStatus.progress } returns null
        every { mockImageSyncStatus.lastUpdateTimeMillis } returns null

        every { mockDeviceDataState.hasDataToUpload } returns false

        mockkStatic("com.simprints.infra.config.store.models.ProjectConfigurationKt")
        mockkStatic("com.simprints.core.tools.extensions.Flow_extKt")
        setupDefaultMocks()

        // Created after TestCoroutineRule has set the test Main dispatcher. Mocks are read lazily (on attach, subscription or action),
        // so tests can still change them before acting.
        createViewModel()
    }

    private fun setupDefaultMocks() {
        every { authStore.signedInProjectId } returns TEST_PROJECT_ID
        every { authStore.signedInUserId } returns TokenizableString.Raw(TEST_USER_ID)
        every { authStore.observeSignedInProjectId() } returns MutableStateFlow(TEST_PROJECT_ID)

        every { configRepository.observeIsProjectRefreshing() } returns MutableStateFlow(false)
        every { configRepository.observeProjectConfiguration() } returns MutableStateFlow(mockProjectConfiguration)
        every { configRepository.observeDeviceConfiguration() } returns MutableStateFlow(mockDeviceConfiguration)
        coEvery { configRepository.getProjectConfiguration() } returns mockProjectConfiguration
        coEvery { configRepository.getDeviceConfiguration() } returns mockDeviceConfiguration
        coEvery { configRepository.getProject() } returns mockProject

        syncStatusFlow = MutableStateFlow(
            SyncStatus(eventSyncState = mockEventSyncState, imageSyncStatus = mockImageSyncStatus),
        )
        every { syncOrchestrator.observeSyncState() } returns syncStatusFlow
        every { deviceStateDataTracker.observeDeviceDataState() } returns MutableStateFlow(mockDeviceDataState)
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
        viewModel = viewModels.track(
            SyncInfoViewModel(
                configRepository = configRepository,
                authStore = authStore,
                recentUserActivityManager = recentUserActivityManager,
                timeHelper = timeHelper,
                observeSyncInfo = observeSyncInfo,
                syncOrchestrator = syncOrchestrator,
                deviceStateDataTracker = deviceStateDataTracker,
                ioDispatcher = testCoroutineRule.testCoroutineDispatcher,
            ),
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

    // Re-login request: a one-time effect (KI-1 fix)

    // S-5
    @Test
    fun `each login request sends one navigation effect even with equal params`() = runTest {
        attachHost(isPreLogoutUpSync = false)
        viewModel.uiEffect.test {
            viewModel.onAction(SyncInfoUiAction.RequestLogin)
            viewModel.onAction(SyncInfoUiAction.RequestLogin)

            assertThat(awaitItem()).isEqualTo(SyncInfoUiEffect.NavigateToLogin(EXPECTED_LOGIN_PARAMS))
            assertThat(awaitItem()).isEqualTo(SyncInfoUiEffect.NavigateToLogin(EXPECTED_LOGIN_PARAMS))
            expectNoEvents()
        }
    }

    // KI-1 fix: an effect is consumed once, so a collector that arrives later (a re-created view) gets nothing
    @Test
    fun `a delivered login request is not replayed to a later collector`() = runTest {
        attachHost(isPreLogoutUpSync = false)
        viewModel.onAction(SyncInfoUiAction.RequestLogin)

        viewModel.uiEffect.test {
            assertThat(awaitItem()).isEqualTo(SyncInfoUiEffect.NavigateToLogin(EXPECTED_LOGIN_PARAMS))
            expectNoEvents()
        }
        viewModel.uiEffect.test { expectNoEvents() }
    }

    // The host mode keeps its first value; a login request leaves the state untouched
    @Test
    fun `the host mode survives every other action and a login request does not change the state`() = runTest {
        attachHost(isPreLogoutUpSync = false)
        syncInfo().test {
            awaitInitialSyncInfo()
            viewModel.onAction(SyncInfoUiAction.RequestLogin)
            assertThat(viewModel.uiState.value).isEqualTo(
                SyncInfoUiState(isPreLogoutUpSync = false, syncInfo = createDefaultSyncInfo()),
            )

            attachHost(isPreLogoutUpSync = true)
            viewModel.onAction(SyncInfoUiAction.SyncRecordsClicked)
            viewModel.onAction(SyncInfoUiAction.SyncImagesClicked)
            viewModel.onAction(SyncInfoUiAction.LoginResultReceived(LoginResult(isSuccess = true)))
            viewModel.onAction(SyncInfoUiAction.LoginResultReceived(LoginResult(isSuccess = false)))
            viewModel.onAction(SyncInfoUiAction.ModuleSelectionResultReceived(ModuleSelectorResult(isConfirmed = true)))
            viewModel.onAction(SyncInfoUiAction.ModuleSelectionResultReceived(ModuleSelectorResult(isConfirmed = false)))

            // The clicks did run (restarts with down sync, so still in normal mode)
            verify(exactly = 3) { syncOrchestrator.execute(OneTime.Events.restart(isDownSyncAllowed = true)) }
            assertThat(viewModel.uiState.value.isPreLogoutUpSync).isFalse()
            // The sync and image clicks show their optimistic progress; nothing here is asserted about it
            cancelAndIgnoreRemainingEvents()
        }
    }

    // Pre-logout auto-logout

    @Test
    fun `should trigger logout when pre-logout sync completes successfully`() = runTest {
        val mockCompletedEventSyncState = completedEventSyncState()
        val mockNotSyncingImageStatus = notSyncingImageStatus()
        attachHost(isPreLogoutUpSync = true)
        syncStatusFlow.value = SyncStatus(eventSyncState = mockCompletedEventSyncState, imageSyncStatus = mockNotSyncingImageStatus)

        advanceTimeBy(4100L) // after the logout delay (3000ms)
        verify(exactly = 1) { syncOrchestrator.execute(OneTime.Logout.start()) }
        verify(exactly = 1) { syncOrchestrator.execute(OneTime.Images.start()) }
        verify(exactly = 2) { syncOrchestrator.execute(any<OneTime>()) }
    }

    @Test
    fun `should emit a logout event after the intended delay since ready to logout`() = runTest {
        val mockCompletedEventSyncState = completedEventSyncState()
        val mockNotSyncingImageStatus = notSyncingImageStatus()
        attachHost(isPreLogoutUpSync = true)
        syncStatusFlow.value = SyncStatus(eventSyncState = mockCompletedEventSyncState, imageSyncStatus = mockNotSyncingImageStatus)

        advanceTimeBy(2900L) // still during the debounce delay
        verify(exactly = 1) { syncOrchestrator.execute(OneTime.Images.start()) }
        verify(exactly = 1) { syncOrchestrator.execute(any<OneTime>()) }
        advanceTimeBy(200L) // after the debounce delay (total 3100ms > 3000ms)
        verify(exactly = 1) { syncOrchestrator.execute(OneTime.Logout.start()) }
        verify(exactly = 2) { syncOrchestrator.execute(any<OneTime>()) }
    }

    @Test
    fun `should not emit a logout event when auth store is cleared (handled by DashboardActivity broadcast)`() = runTest {
        // The view model does not observe the auth store itself: sign-out reaches it through ObserveSyncInfoUseCase, which maps the
        // signed-in project id to SyncInfo.isLoggedIn. Mocked here with the same mapping.
        val projectIdFlow = MutableStateFlow(TEST_PROJECT_ID)
        every { authStore.observeSignedInProjectId() } returns projectIdFlow
        every { observeSyncInfo(any()) } returns authStore.observeSignedInProjectId().map { projectId ->
            createDefaultSyncInfo().copy(isLoggedIn = projectId.isNotEmpty())
        }
        attachHost(isPreLogoutUpSync = false)

        syncInfo().test {
            awaitInitialSyncInfo()

            projectIdFlow.value = ""
            advanceUntilIdle()

            assertThat(awaitItem()).isEqualTo(createDefaultSyncInfo().copy(isLoggedIn = false))
            verify(exactly = 0) { syncOrchestrator.execute(any<OneTime>()) }
            expectNoEvents()
        }
    }

    @Test
    fun `should not trigger logout when not in pre-logout mode`() = runTest {
        val mockCompletedEventSyncState = completedEventSyncState()
        val mockNotSyncingImageStatus = notSyncingImageStatus()
        attachHost(isPreLogoutUpSync = false)
        syncStatusFlow.value = SyncStatus(eventSyncState = mockCompletedEventSyncState, imageSyncStatus = mockNotSyncingImageStatus)

        advanceTimeBy(3100L) // after the logout delay (3000ms)
        verify(exactly = 0) { syncOrchestrator.execute(any<OneTime>()) }
    }

    @Test
    fun `device data state is observed only in pre-logout mode`() = runTest {
        val deviceDataStates = MutableStateFlow(mockDeviceDataState)
        every { deviceStateDataTracker.observeDeviceDataState() } returns deviceDataStates

        createViewModel()
        assertThat(deviceDataStates.subscriptionCount.value).isEqualTo(0) // waits for the host mode
        attachHost(isPreLogoutUpSync = false)
        assertThat(deviceDataStates.subscriptionCount.value).isEqualTo(0)

        createViewModel()
        attachHost(isPreLogoutUpSync = true)
        assertThat(deviceDataStates.subscriptionCount.value).isEqualTo(1)
    }

    @Test
    fun `should not trigger logout when records still syncing`() = runTest {
        val mockInProgressEventSyncState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncCompleted() } returns false
            every { isSyncInProgress() } returns true
        }
        val mockNotSyncingImageStatus = notSyncingImageStatus()
        attachHost(isPreLogoutUpSync = true)
        syncStatusFlow.value = SyncStatus(eventSyncState = mockInProgressEventSyncState, imageSyncStatus = mockNotSyncingImageStatus)

        advanceTimeBy(3100L) // after the logout delay (3000ms)
        verify(exactly = 0) { syncOrchestrator.execute(any<OneTime>()) }
    }

    @Test
    fun `should not trigger logout when images still syncing`() = runTest {
        val mockCompletedEventSyncState = completedEventSyncState()
        val mockSyncingImageStatus = mockk<ImageSyncStatus>(relaxed = true) {
            every { isSyncing } returns true
            every { progress } returns Pair(1, 2)
            every { lastUpdateTimeMillis } returns null
        }
        attachHost(isPreLogoutUpSync = true)
        syncStatusFlow.value = SyncStatus(eventSyncState = mockCompletedEventSyncState, imageSyncStatus = mockSyncingImageStatus)

        advanceTimeBy(3100L) // after the logout delay (3000ms)
        // the pre-logout image auto-start runs from init, so the completion also starts images (once)
        verify(exactly = 1) { syncOrchestrator.execute(OneTime.Images.start()) }
        // and nothing else: no logout
        verify(exactly = 1) { syncOrchestrator.execute(any<OneTime>()) }
    }

    @Test
    fun `should not trigger logout when sync completed but there is still data to upload`() = runTest {
        val mockCompletedEventSyncState = completedEventSyncState()
        val mockNotSyncingImageStatus = notSyncingImageStatus()
        every { mockDeviceDataState.hasDataToUpload } returns true
        attachHost(isPreLogoutUpSync = true)
        syncStatusFlow.value = SyncStatus(eventSyncState = mockCompletedEventSyncState, imageSyncStatus = mockNotSyncingImageStatus)

        advanceTimeBy(3100L) // after the logout delay (3000ms)
        // the pre-logout image auto-start runs from init, so the completion also starts images (once)
        verify(exactly = 1) { syncOrchestrator.execute(OneTime.Images.start()) }
        // and nothing else: no logout
        verify(exactly = 1) { syncOrchestrator.execute(any<OneTime>()) }
    }

    @Test
    fun `should not trigger logout if images started syncing after pause between events and images`() = runTest {
        val mockCompletedEventSyncState = completedEventSyncState()
        val mockNotSyncingImageStatus = notSyncingImageStatus()
        val mockSyncingImageStatus = mockk<ImageSyncStatus>(relaxed = true) {
            every { isSyncing } returns true
            every { progress } returns Pair(1, 2)
            every { lastUpdateTimeMillis } returns null
        }
        attachHost(isPreLogoutUpSync = true)
        syncStatusFlow.value = SyncStatus(eventSyncState = mockCompletedEventSyncState, imageSyncStatus = mockNotSyncingImageStatus)

        advanceTimeBy(1000L)
        syncStatusFlow.value = SyncStatus(eventSyncState = mockCompletedEventSyncState, imageSyncStatus = mockSyncingImageStatus)

        advanceTimeBy(3100L)
        // the image auto-start (from init) started images once, at the events completion; events stayed completed after
        verify(exactly = 1) { syncOrchestrator.execute(OneTime.Images.start()) }
        // and nothing else: no logout
        verify(exactly = 1) { syncOrchestrator.execute(any<OneTime>()) }
    }

    // SyncRecordsClicked tests

    @Test
    fun `should start event sync with down sync allowed when not pre-logout`() = runTest {
        attachHost(isPreLogoutUpSync = false)

        viewModel.onAction(SyncInfoUiAction.SyncRecordsClicked)

        verify { syncOrchestrator.execute(OneTime.Events.restart(isDownSyncAllowed = true)) }
    }

    @Test
    fun `should start event sync with down sync disabled when pre-logout`() = runTest {
        attachHost(isPreLogoutUpSync = true)

        viewModel.onAction(SyncInfoUiAction.SyncRecordsClicked)

        verify { syncOrchestrator.execute(OneTime.Events.restart(isDownSyncAllowed = false)) }
    }

    @Test
    fun `should start event sync with down sync disabled when project paused`() = runTest {
        val mockPausedProject = mockk<Project> {
            every { state } returns ProjectState.PROJECT_PAUSED
        }
        coEvery { configRepository.getProject() } returns mockPausedProject
        attachHost(isPreLogoutUpSync = false)

        viewModel.onAction(SyncInfoUiAction.SyncRecordsClicked)

        verify { syncOrchestrator.execute(OneTime.Events.restart(isDownSyncAllowed = false)) }
    }

    @Test
    fun `should start event sync with down sync disabled when project ending`() = runTest {
        val mockEndingProject = mockk<Project> {
            every { state } returns ProjectState.PROJECT_ENDING
        }
        coEvery { configRepository.getProject() } returns mockEndingProject
        attachHost(isPreLogoutUpSync = false)

        viewModel.onAction(SyncInfoUiAction.SyncRecordsClicked)

        verify { syncOrchestrator.execute(OneTime.Events.restart(isDownSyncAllowed = false)) }
    }

    @Test
    fun `should start event sync with down sync disabled event sync when logged out`() = runTest {
        coEvery { configRepository.getProject() } returns null
        attachHost(isPreLogoutUpSync = false)

        viewModel.onAction(SyncInfoUiAction.SyncRecordsClicked)

        verify { syncOrchestrator.execute(OneTime.Events.restart(isDownSyncAllowed = false)) }
    }

    @Test
    fun `should refresh device data state before restarting event sync`() = runTest {
        val project = CompletableDeferred<Project?>()
        coEvery { configRepository.getProject() } coAnswers { project.await() }
        attachHost(isPreLogoutUpSync = false)

        viewModel.onAction(SyncInfoUiAction.SyncRecordsClicked)
        verify(exactly = 1) { deviceStateDataTracker.refresh() }
        verify(exactly = 0) { syncOrchestrator.execute(any<OneTime>()) }

        project.complete(mockProject)

        verify(exactly = 1) { syncOrchestrator.execute(OneTime.Events.restart(isDownSyncAllowed = true)) }
        verify(exactly = 1) { deviceStateDataTracker.refresh() }
    }

    @Test
    fun `should stop current event sync before starting new one`() = runTest {
        attachHost(isPreLogoutUpSync = false)
        viewModel.onAction(SyncInfoUiAction.SyncRecordsClicked)

        verify { syncOrchestrator.execute(OneTime.Events.restart()) }
    }

    // SyncImagesClicked tests

    @Test
    fun `should start image sync when not currently syncing images`() = runTest {
        val mockNotSyncingImageStatus = mockk<ImageSyncStatus>(relaxed = true) {
            every { isSyncing } returns false
        }
        syncStatusFlow.value = SyncStatus(eventSyncState = mockEventSyncState, imageSyncStatus = mockNotSyncingImageStatus)
        attachHost(isPreLogoutUpSync = false)

        viewModel.onAction(SyncInfoUiAction.SyncImagesClicked)

        verify { syncOrchestrator.execute(OneTime.Images.start()) }
        verify(exactly = 0) { syncOrchestrator.execute(OneTime.Images.stop()) }
    }

    @Test
    fun `should stop image sync when currently syncing images`() = runTest {
        val mockSyncingImageStatus = mockk<ImageSyncStatus>(relaxed = true) {
            every { isSyncing } returns true
        }
        syncStatusFlow.value = SyncStatus(eventSyncState = mockEventSyncState, imageSyncStatus = mockSyncingImageStatus)
        attachHost(isPreLogoutUpSync = false)

        viewModel.onAction(SyncInfoUiAction.SyncImagesClicked)

        verify { syncOrchestrator.execute(OneTime.Images.stop()) }
        verify(exactly = 0) { syncOrchestrator.execute(OneTime.Images.start()) }
    }

    @Test
    fun `auto logout issues exactly one logout start command while the ready state stays the same`() = runTest {
        attachHost(isPreLogoutUpSync = true)
        syncStatusFlow.value = SyncStatus(eventSyncState = completedEventSyncState(), imageSyncStatus = mockImageSyncStatus)

        advanceTimeBy(10_000L)

        verify(exactly = 1) { syncOrchestrator.execute(OneTime.Logout.start()) }
        // the pre-logout image auto-start runs from init, so the completion also starts images (once)
        verify(exactly = 1) { syncOrchestrator.execute(OneTime.Images.start()) }
        verify(exactly = 2) { syncOrchestrator.execute(any<OneTime>()) }
    }

    // RequestLogin params

    @Test
    fun `should request login with signed in project ID`() = runTest {
        attachHost(isPreLogoutUpSync = false)
        viewModel.uiEffect.test {
            viewModel.onAction(SyncInfoUiAction.RequestLogin)

            val result = (awaitItem() as SyncInfoUiEffect.NavigateToLogin).params

            assertThat(result.projectId).isEqualTo(TEST_PROJECT_ID)
            expectNoEvents()
        }
    }

    @Test
    fun `should request login with signed in user ID when available`() = runTest {
        attachHost(isPreLogoutUpSync = false)
        viewModel.uiEffect.test {
            viewModel.onAction(SyncInfoUiAction.RequestLogin)

            val result = (awaitItem() as SyncInfoUiEffect.NavigateToLogin).params

            assertThat(result.userId.value).isEqualTo(TEST_USER_ID)
            coVerify(exactly = 0) { recentUserActivityManager.getRecentUserActivity() }
            expectNoEvents()
        }
    }

    @Test
    fun `should request login with recent user ID when signed in user unavailable`() = runTest {
        every { authStore.signedInUserId } returns null

        attachHost(isPreLogoutUpSync = false)
        viewModel.uiEffect.test {
            viewModel.onAction(SyncInfoUiAction.RequestLogin)

            val result = (awaitItem() as SyncInfoUiEffect.NavigateToLogin).params
            assertThat(result.userId.value).isEqualTo(TEST_RECENT_USER_ID)
            expectNoEvents()
        }
    }

    // LoginResultReceived tests

    @Test
    fun `should trigger forceEventSync when login result is success`() = runTest {
        val successResult = mockk<LoginResult> {
            every { isSuccess } returns true
        }

        attachHost(isPreLogoutUpSync = false)
        viewModel.onAction(SyncInfoUiAction.LoginResultReceived(successResult))

        verify { syncOrchestrator.execute(OneTime.Events.restart()) }
    }

    @Test
    fun `should not trigger forceEventSync when login result is failure`() = runTest {
        val failureResult = mockk<LoginResult> {
            every { isSuccess } returns false
        }

        attachHost(isPreLogoutUpSync = false)
        viewModel.onAction(SyncInfoUiAction.LoginResultReceived(failureResult))

        verify(exactly = 0) { deviceStateDataTracker.refresh() }
        verify(exactly = 0) { syncOrchestrator.execute(OneTime.Events.restart(isDownSyncAllowed = true)) }
        verify(exactly = 0) { syncOrchestrator.execute(OneTime.Events.restart(isDownSyncAllowed = false)) }
    }

    // Sync button responsiveness optimization

    @Test
    fun `should immediately show event progress snapshot when forcing event sync`() = runTest {
        attachHost(isPreLogoutUpSync = false)

        syncInfo().test {
            // The data-driven info comes first (see awaitInitialSyncInfo)
            val initial = awaitItem()
            assertThat(initial).isEqualTo(createDefaultSyncInfo())
            assertThat(initial.syncInfoSectionRecords.isProgressVisible).isFalse()
            // AP-2: the snapshot is built from the current info, so the sections the click does not touch are kept as they were.
            assertThat(initial.syncInfoSectionImages).isNotEqualTo(SyncInfo().syncInfoSectionImages)

            viewModel.onAction(SyncInfoUiAction.SyncRecordsClicked)

            assertThat(awaitItem()).isEqualTo(
                initial.copy(
                    syncInfoSectionRecords = initial.syncInfoSectionRecords.copy(
                        counterTotalRecords = "",
                        counterRecordsToUpload = "",
                        counterRecordsToDownload = "",
                        recordSyncVisibleState = RecordSyncVisibleState.NOTHING,
                        isProgressVisible = true,
                        isSyncButtonEnabled = false,
                        footerLastSyncMinutesAgo = "",
                    ),
                ),
            )
            expectNoEvents()
        }
    }

    @Test
    fun `should not emit forced event progress when events already syncing`() = runTest {
        val mockInProgressEventSyncState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncInProgress() } returns true
        }
        syncStatusFlow.value = SyncStatus(eventSyncState = mockInProgressEventSyncState, imageSyncStatus = mockImageSyncStatus)
        attachHost(isPreLogoutUpSync = false)

        syncInfo().test {
            awaitInitialSyncInfo()
            viewModel.onAction(SyncInfoUiAction.SyncRecordsClicked)

            verify(exactly = 1) { syncOrchestrator.execute(OneTime.Events.restart(isDownSyncAllowed = true)) }
            expectNoEvents()
        }
    }

    @Test
    fun `should immediately show image progress snapshot when starting image sync`() = runTest {
        val mockNotSyncingImageStatus = mockk<ImageSyncStatus>(relaxed = true) {
            every { isSyncing } returns false
        }
        syncStatusFlow.value = SyncStatus(eventSyncState = mockEventSyncState, imageSyncStatus = mockNotSyncingImageStatus)
        attachHost(isPreLogoutUpSync = false)

        syncInfo().test {
            // The data-driven info comes first (see awaitInitialSyncInfo)
            val initial = awaitItem()
            assertThat(initial).isEqualTo(createDefaultSyncInfo())
            assertThat(initial.syncInfoSectionImages.isProgressVisible).isFalse()
            // AP-2: the snapshot is built from the current info, so the sections the click does not touch are kept as they were.
            assertThat(initial.syncInfoSectionRecords).isNotEqualTo(SyncInfo().syncInfoSectionRecords)

            viewModel.onAction(SyncInfoUiAction.SyncImagesClicked)

            assertThat(awaitItem()).isEqualTo(
                initial.copy(
                    syncInfoSectionImages = initial.syncInfoSectionImages.copy(
                        counterImagesToUpload = "",
                        isInstructionDefaultVisible = false,
                        isInstructionOfflineVisible = false,
                        isProgressVisible = true,
                        footerLastSyncMinutesAgo = "",
                    ),
                ),
            )
            expectNoEvents()
        }
    }

    @Test
    fun `should not emit forced image progress when stopping image sync`() = runTest {
        val mockSyncingImageStatus = mockk<ImageSyncStatus>(relaxed = true) {
            every { isSyncing } returns true
        }
        syncStatusFlow.value = SyncStatus(eventSyncState = mockEventSyncState, imageSyncStatus = mockSyncingImageStatus)
        attachHost(isPreLogoutUpSync = false)

        syncInfo().test {
            awaitInitialSyncInfo()
            viewModel.onAction(SyncInfoUiAction.SyncImagesClicked)

            verify(exactly = 1) { syncOrchestrator.execute(OneTime.Images.stop()) }
            expectNoEvents()
        }
    }

    @Test
    fun `should switch from forced to data-driven event sync progress once available`() = runTest {
        val base = createDefaultSyncInfo()
        val dataFlow = MutableStateFlow(base)
        every { observeSyncInfo(any()) } returns dataFlow
        attachHost(isPreLogoutUpSync = false)

        syncInfo().test {
            // The data-driven info comes first (see awaitInitialSyncInfo)
            val initial = awaitItem()
            assertThat(initial).isEqualTo(createDefaultSyncInfo())
            viewModel.onAction(SyncInfoUiAction.SyncRecordsClicked)
            val forced = awaitItem()
            dataFlow.value = base.copy(
                syncInfoSectionRecords = base.syncInfoSectionRecords.copy(
                    isProgressVisible = true,
                    counterTotalRecords = "123",
                ),
            )
            val dataDriven = awaitItem()

            assertThat(initial.syncInfoSectionRecords.isProgressVisible).isFalse()
            assertThat(forced.syncInfoSectionRecords.isProgressVisible).isTrue()
            assertThat(forced.syncInfoSectionRecords.counterTotalRecords).isEmpty()
            assertThat(dataDriven.syncInfoSectionRecords.isProgressVisible).isTrue()
            assertThat(dataDriven.syncInfoSectionRecords.counterTotalRecords).isEqualTo("123")
            expectNoEvents()
        }
    }

    @Test
    fun `should switch from forced to data-driven image sync progress once available`() = runTest {
        val base = createDefaultSyncInfo()
        val dataFlow = MutableStateFlow(base)
        every { observeSyncInfo(any()) } returns dataFlow
        attachHost(isPreLogoutUpSync = false)

        syncInfo().test {
            // The data-driven info comes first (see awaitInitialSyncInfo)
            val initial = awaitItem()
            assertThat(initial).isEqualTo(createDefaultSyncInfo())
            viewModel.onAction(SyncInfoUiAction.SyncImagesClicked)
            val forced = awaitItem()
            dataFlow.value = base.copy(
                syncInfoSectionImages = base.syncInfoSectionImages.copy(
                    isProgressVisible = true,
                    counterImagesToUpload = "123",
                ),
            )
            val dataDriven = awaitItem()

            assertThat(initial.syncInfoSectionImages.isProgressVisible).isFalse()
            assertThat(forced.syncInfoSectionImages.isProgressVisible).isTrue()
            assertThat(forced.syncInfoSectionImages.counterImagesToUpload).isEmpty()
            assertThat(dataDriven.syncInfoSectionImages.isProgressVisible).isTrue()
            assertThat(dataDriven.syncInfoSectionImages.counterImagesToUpload).isEqualTo("123")
            expectNoEvents()
        }
    }

    // Other/combined UX case tests

    @Test
    fun `should trigger initial sync when no previous sync history`() = runTest {
        val mockIdleEventSyncState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncRunning() } returns false
            every { lastSyncTime } returns null
        }
        syncStatusFlow.value = SyncStatus(eventSyncState = mockIdleEventSyncState, imageSyncStatus = mockImageSyncStatus)
        attachHost(isPreLogoutUpSync = false)

        syncInfo().test {
            awaitInitialSyncInfo()

            verify { syncOrchestrator.execute(OneTime.Events.restart()) }
            expectNoEvents()
        }
    }

    @Test
    fun `should trigger initial sync when last sync too old`() = runTest {
        val oldTimestamp = Timestamp(TEST_TIMESTAMP.ms - 600000) // 10 minutes ago, over threshold of 5
        val mockIdleEventSyncState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncRunning() } returns false
            every { lastSyncTime } returns oldTimestamp
        }
        syncStatusFlow.value = SyncStatus(eventSyncState = mockIdleEventSyncState, imageSyncStatus = mockImageSyncStatus)
        every { timeHelper.msBetweenNowAndTime(oldTimestamp) } returns 600000L // 10 minutes
        attachHost(isPreLogoutUpSync = false)

        syncInfo().test {
            awaitInitialSyncInfo()

            verify { syncOrchestrator.execute(OneTime.Events.restart()) }
            expectNoEvents()
        }
    }

    @Test
    fun `should not trigger initial sync when recently synced`() = runTest {
        val recentTimestamp = Timestamp(TEST_TIMESTAMP.ms - 60000) // 1 minute ago, under threshold of 5
        val mockIdleEventSyncState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncRunning() } returns false
            every { lastSyncTime } returns recentTimestamp
        }
        syncStatusFlow.value = SyncStatus(eventSyncState = mockIdleEventSyncState, imageSyncStatus = mockImageSyncStatus)
        every { timeHelper.msBetweenNowAndTime(recentTimestamp) } returns 60000L // 1 minute
        attachHost(isPreLogoutUpSync = false)

        syncInfo().test {
            awaitInitialSyncInfo()

            verify(exactly = 0) { syncOrchestrator.execute(OneTime.Events.restart(isDownSyncAllowed = true)) }
            verify(exactly = 0) { syncOrchestrator.execute(OneTime.Events.restart(isDownSyncAllowed = false)) }
            expectNoEvents()
        }
    }

    @Test
    fun `should not trigger initial sync when sync already running`() = runTest {
        val mockRunningSyncState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncRunning() } returns true
            every { lastSyncTime } returns null
        }
        syncStatusFlow.value = SyncStatus(eventSyncState = mockRunningSyncState, imageSyncStatus = mockImageSyncStatus)
        attachHost(isPreLogoutUpSync = false)

        syncInfo().test {
            awaitInitialSyncInfo()

            verify(exactly = 0) { syncOrchestrator.execute(OneTime.Events.restart(isDownSyncAllowed = true)) }
            verify(exactly = 0) { syncOrchestrator.execute(OneTime.Events.restart(isDownSyncAllowed = false)) }
            expectNoEvents()
        }
    }

    @Test
    fun `should trigger initial sync in pre-logout mode regardless of history`() = runTest {
        val recentTimestamp = Timestamp(TEST_TIMESTAMP.ms - 60000) // 1 minute ago, under threshold of 5
        val mockIdleEventSyncState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncRunning() } returns false
            every { lastSyncTime } returns recentTimestamp
        }
        every { timeHelper.msBetweenNowAndTime(recentTimestamp) } returns 60000L // 1 minute
        attachHost(isPreLogoutUpSync = true)
        syncStatusFlow.value = SyncStatus(eventSyncState = mockIdleEventSyncState, imageSyncStatus = mockImageSyncStatus)

        syncInfo().test {
            awaitInitialSyncInfo()

            verify { syncOrchestrator.execute(OneTime.Events.restart(isDownSyncAllowed = false)) }
            expectNoEvents()
        }
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
        attachHost(isPreLogoutUpSync = true)

        syncInfo().test {
            awaitInitialSyncInfo()

            verify(exactly = 1) { syncOrchestrator.execute(OneTime.Events.restart(isDownSyncAllowed = false)) }
            expectNoEvents()
        }
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
        attachHost(isPreLogoutUpSync = false)

        syncInfo().test {
            awaitInitialSyncInfo()

            verify(exactly = 0) { syncOrchestrator.execute(OneTime.Events.restart(isDownSyncAllowed = true)) }
            verify(exactly = 0) { syncOrchestrator.execute(OneTime.Events.restart(isDownSyncAllowed = false)) }
            expectNoEvents()
        }
    }

    // S-1
    @Test
    fun `pre-logout mode starts images once per events completed transition`() = runTest {
        attachHost(isPreLogoutUpSync = true)
        syncInfo().test {
            awaitInitialSyncInfo()
            verify(exactly = 0) { syncOrchestrator.execute(OneTime.Images.start()) }

            syncStatusFlow.value = SyncStatus(eventSyncState = completedEventSyncState(), imageSyncStatus = mockImageSyncStatus)
            verify(exactly = 1) { syncOrchestrator.execute(OneTime.Images.start()) }

            // Another status while events stay completed: not a new transition
            syncStatusFlow.value = SyncStatus(eventSyncState = completedEventSyncState(), imageSyncStatus = syncingImageStatus())
            verify(exactly = 1) { syncOrchestrator.execute(OneTime.Images.start()) }

            // Not completed, then completed again: a new transition
            syncStatusFlow.value = SyncStatus(eventSyncState = mockEventSyncState, imageSyncStatus = mockImageSyncStatus)
            syncStatusFlow.value = SyncStatus(eventSyncState = completedEventSyncState(), imageSyncStatus = mockImageSyncStatus)
            verify(exactly = 2) { syncOrchestrator.execute(OneTime.Images.start()) }
            expectNoEvents()
        }
    }

    // S-1
    @Test
    fun `normal mode does not start images when events complete`() = runTest {
        attachHost(isPreLogoutUpSync = false)
        syncInfo().test {
            awaitInitialSyncInfo()

            syncStatusFlow.value = SyncStatus(eventSyncState = completedEventSyncState(), imageSyncStatus = mockImageSyncStatus)

            verify(exactly = 0) { syncOrchestrator.execute(OneTime.Images.start()) }
            expectNoEvents()
        }
    }

    // S-3
    @Test
    fun `pre-logout mode set before the first subscription observes pre-logout sync info and restarts without down sync`() = runTest {
        attachHost(isPreLogoutUpSync = true)

        syncInfo().test {
            awaitInitialSyncInfo()

            verify(exactly = 1) { observeSyncInfo(true) }
            verify(exactly = 1) { syncOrchestrator.execute(OneTime.Events.restart(isDownSyncAllowed = false)) }
            verify(exactly = 0) { observeSyncInfo(false) }
            verify(exactly = 0) { syncOrchestrator.execute(OneTime.Events.restart(isDownSyncAllowed = true)) }
            expectNoEvents()
        }
    }

    // S-4
    @Test
    fun `module selection confirmed restarts event sync without forced progress`() = runTest {
        attachHost(isPreLogoutUpSync = false)
        syncInfo().test {
            awaitInitialSyncInfo()

            viewModel.onAction(SyncInfoUiAction.ModuleSelectionResultReceived(ModuleSelectorResult(isConfirmed = true)))

            verify(exactly = 1) { syncOrchestrator.execute(OneTime.Events.restart(isDownSyncAllowed = true)) }
            verify(exactly = 1) { deviceStateDataTracker.refresh() }
            expectNoEvents()
        }
    }

    // S-16
    @Test
    fun `initial sync waits until the event sync state is initialised`() = runTest {
        val uninitialisedState = mockk<EventSyncState>(relaxed = true) {
            every { isUninitialized() } returns true
            every { lastSyncTime } returns null
        }
        val idleWithoutHistoryState = mockk<EventSyncState>(relaxed = true) {
            every { isUninitialized() } returns false
            every { isSyncRunning() } returns false
            every { lastSyncTime } returns null
        }
        syncStatusFlow.value = SyncStatus(eventSyncState = uninitialisedState, imageSyncStatus = mockImageSyncStatus)
        attachHost(isPreLogoutUpSync = false)
        syncInfo().test {
            awaitInitialSyncInfo()
            verify(exactly = 0) { syncOrchestrator.execute(any<OneTime>()) }

            syncStatusFlow.value = SyncStatus(eventSyncState = idleWithoutHistoryState, imageSyncStatus = mockImageSyncStatus)

            verify(exactly = 1) { syncOrchestrator.execute(OneTime.Events.restart(isDownSyncAllowed = true)) }
            verify(exactly = 1) { syncOrchestrator.execute(any<OneTime>()) }
            expectNoEvents()
        }
    }

    // S-16
    @Test
    fun `should not trigger initial sync when last sync was exactly five minutes ago`() = runTest {
        val fiveMinutesAgo = Timestamp(TEST_TIMESTAMP.ms - 300000)
        val mockIdleEventSyncState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncRunning() } returns false
            every { lastSyncTime } returns fiveMinutesAgo
        }
        syncStatusFlow.value = SyncStatus(eventSyncState = mockIdleEventSyncState, imageSyncStatus = mockImageSyncStatus)
        every { timeHelper.msBetweenNowAndTime(fiveMinutesAgo) } returns 300000L
        attachHost(isPreLogoutUpSync = false)

        syncInfo().test {
            awaitInitialSyncInfo()

            verify(exactly = 1) { timeHelper.msBetweenNowAndTime(fiveMinutesAgo) }
            verify(exactly = 0) { syncOrchestrator.execute(any<OneTime>()) }
            expectNoEvents()
        }
    }

    // N-9
    @Test
    fun `two quick sync taps restart event sync twice`() = runTest {
        attachHost(isPreLogoutUpSync = false)

        viewModel.onAction(SyncInfoUiAction.SyncRecordsClicked)
        viewModel.onAction(SyncInfoUiAction.SyncRecordsClicked)

        verify(exactly = 2) { deviceStateDataTracker.refresh() }
        verify(exactly = 2) { syncOrchestrator.execute(OneTime.Events.restart(isDownSyncAllowed = true)) }
    }

    // S-6 (view model half): the confirmation check moved from the Fragment into the view model
    @Test
    fun `module selection not confirmed does nothing`() = runTest {
        attachHost(isPreLogoutUpSync = false)
        syncInfo().test {
            awaitInitialSyncInfo()

            viewModel.onAction(SyncInfoUiAction.ModuleSelectionResultReceived(ModuleSelectorResult(isConfirmed = false)))

            expectNoEvents()
            verify(exactly = 0) { deviceStateDataTracker.refresh() }
            verify(exactly = 0) { syncOrchestrator.execute(any<OneTime>()) }
        }
    }

    // S-13 (view model half), PC-47
    @Test
    fun `before the first data emission the state carries the default sync info`() = runTest {
        every { observeSyncInfo(any()) } returns MutableSharedFlow() // no data emission yet
        attachHost(isPreLogoutUpSync = false)

        syncInfo().test {
            assertThat(awaitItem()).isEqualTo(SyncInfo())
            verify(exactly = 1) { observeSyncInfo(false) }
            assertThat(viewModel.uiState.value).isEqualTo(SyncInfoUiState(isPreLogoutUpSync = false, syncInfo = SyncInfo()))
            expectNoEvents()
        }
    }

    // N-1
    @Test
    fun `sync info is not observed until the host is attached, then once with the attached mode`() = runTest {
        syncInfo().test {
            assertThat(awaitItem()).isEqualTo(SyncInfo())
            expectNoEvents()
            verify(exactly = 0) { observeSyncInfo(any()) }
            verify(exactly = 0) { syncOrchestrator.execute(any<OneTime>()) }

            attachHost(isPreLogoutUpSync = true)
            attachHost(isPreLogoutUpSync = false) // ignored: the first value wins

            assertThat(awaitItem()).isEqualTo(createDefaultSyncInfo())
            verify(exactly = 1) { observeSyncInfo(true) }
            verify(exactly = 1) { syncOrchestrator.execute(OneTime.Events.restart(isDownSyncAllowed = false)) }
            assertThat(viewModel.uiState.value.isPreLogoutUpSync).isTrue()
            expectNoEvents()
            verify(exactly = 0) { observeSyncInfo(false) }
            verify(exactly = 0) { syncOrchestrator.execute(OneTime.Events.restart(isDownSyncAllowed = true)) }
        }
    }

    // N-1
    @Test
    fun `auto logout waits for the host mode and then runs in pre-logout mode`() = runTest {
        syncStatusFlow.value = SyncStatus(eventSyncState = completedEventSyncState(), imageSyncStatus = mockImageSyncStatus)
        advanceTimeBy(4100L)
        verify(exactly = 0) { syncOrchestrator.execute(any<OneTime>()) }

        attachHost(isPreLogoutUpSync = true)
        // KI-4 fix: the image auto-start (from init) also waits for the host mode, then starts images for the completed events
        verify(exactly = 1) { syncOrchestrator.execute(OneTime.Images.start()) }

        advanceTimeBy(2900L)
        verify(exactly = 1) { syncOrchestrator.execute(any<OneTime>()) }
        advanceTimeBy(200L)
        verify(exactly = 1) { syncOrchestrator.execute(OneTime.Logout.start()) }
        verify(exactly = 2) { syncOrchestrator.execute(any<OneTime>()) }
    }

    @Test
    fun `after a collector gap of five seconds the last sync info comes first and the initial sync decision runs again`() = runTest {
        givenNoSyncHistory()
        every { observeSyncInfo(any()) } returns MutableStateFlow(createDefaultSyncInfo())
        attachHost(isPreLogoutUpSync = false)
        syncInfo().test {
            awaitInitialSyncInfo()
            verify(exactly = 1) { syncOrchestrator.execute(OneTime.Events.restart(isDownSyncAllowed = true)) }
        } // the only collector leaves here

        advanceTimeBy(5_001L)
        syncInfo().test {
            // The last value comes first, not the default placeholder
            assertThat(awaitItem()).isEqualTo(createDefaultSyncInfo())
            verify(exactly = 2) { syncOrchestrator.execute(OneTime.Events.restart(isDownSyncAllowed = true)) }
            expectNoEvents()
        }
    }

    @Test
    fun `after a collector gap shorter than five seconds the initial sync decision does not run again`() = runTest {
        givenNoSyncHistory()
        every { observeSyncInfo(any()) } returns MutableStateFlow(createDefaultSyncInfo())
        attachHost(isPreLogoutUpSync = false)
        syncInfo().test {
            awaitInitialSyncInfo()
            verify(exactly = 1) { syncOrchestrator.execute(OneTime.Events.restart(isDownSyncAllowed = true)) }
        } // the only collector leaves here

        advanceTimeBy(4_900L)
        syncInfo().test {
            assertThat(awaitItem()).isEqualTo(createDefaultSyncInfo())
            expectNoEvents()
            verify(exactly = 1) { syncOrchestrator.execute(OneTime.Events.restart(isDownSyncAllowed = true)) }
        }
    }

    @Test
    fun `after a stream restart in pre-logout mode one events completion starts images exactly once`() = runTest {
        attachHost(isPreLogoutUpSync = true)
        restartSyncInfoStream()

        syncInfo().test {
            assertThat(awaitItem()).isEqualTo(createDefaultSyncInfo())
            // The stream did restart: the pre-logout initial sync ran again for the idle event sync
            verify(exactly = 2) { syncOrchestrator.execute(OneTime.Events.restart(isDownSyncAllowed = false)) }
            verify(exactly = 0) { syncOrchestrator.execute(OneTime.Images.start()) }

            syncStatusFlow.value = SyncStatus(eventSyncState = completedEventSyncState(), imageSyncStatus = mockImageSyncStatus)

            verify(exactly = 1) { syncOrchestrator.execute(OneTime.Images.start()) }
            expectNoEvents()
        }
    }

    @Test
    fun `the first pre-logout stream start forces the event sync even while one is running`() = runTest {
        syncStatusFlow.value = SyncStatus(eventSyncState = runningEventSyncState(), imageSyncStatus = mockImageSyncStatus)
        attachHost(isPreLogoutUpSync = true)

        syncInfo().test {
            awaitInitialSyncInfo()
            verify(exactly = 1) { syncOrchestrator.execute(OneTime.Events.restart(isDownSyncAllowed = false)) }
        }
    }

    @Test
    fun `a pre-logout stream restart does not restart an event sync that is running`() = runTest {
        attachHost(isPreLogoutUpSync = true)
        restartSyncInfoStream(eventSyncStateBeforeRestart = runningEventSyncState())

        syncInfo().test {
            assertThat(awaitItem()).isEqualTo(createDefaultSyncInfo())
            // The stream did restart (sync info observed again) and the first start forced the sync once
            verify(exactly = 2) { observeSyncInfo(true) }
            verify(exactly = 1) { syncOrchestrator.execute(OneTime.Events.restart(isDownSyncAllowed = false)) }
            expectNoEvents()
        }
    }

    @Test
    fun `a pre-logout stream restart does not restart an event sync that has completed`() = runTest {
        attachHost(isPreLogoutUpSync = true)
        restartSyncInfoStream(eventSyncStateBeforeRestart = completedEventSyncState())

        syncInfo().test {
            assertThat(awaitItem()).isEqualTo(createDefaultSyncInfo())
            // The stream did restart (sync info observed again) and the first start forced the sync once
            verify(exactly = 2) { observeSyncInfo(true) }
            verify(exactly = 1) { syncOrchestrator.execute(OneTime.Events.restart(isDownSyncAllowed = false)) }
            expectNoEvents()
        }
    }

    @Test
    fun `a pre-logout stream restart restarts an event sync that has failed`() = runTest {
        attachHost(isPreLogoutUpSync = true)
        restartSyncInfoStream(eventSyncStateBeforeRestart = failedEventSyncState())

        syncInfo().test {
            assertThat(awaitItem()).isEqualTo(createDefaultSyncInfo())
            // A failed sync is neither running nor completed, so the restart forces it again
            verify(exactly = 2) { observeSyncInfo(true) }
            verify(exactly = 2) { syncOrchestrator.execute(OneTime.Events.restart(isDownSyncAllowed = false)) }
            expectNoEvents()
        }
    }

    // Starts the stream once (one collector that leaves), waits past the 5 s stop timeout, then optionally changes the event sync
    // state so the next collector restarts the stream with it.
    private suspend fun TestScope.restartSyncInfoStream(eventSyncStateBeforeRestart: EventSyncState? = null) {
        syncInfo().test {
            awaitInitialSyncInfo()
            verify(exactly = 1) { syncOrchestrator.execute(OneTime.Events.restart(isDownSyncAllowed = false)) }
        } // the only collector leaves here
        advanceTimeBy(5_001L)
        eventSyncStateBeforeRestart?.let { syncStatusFlow.value = SyncStatus(eventSyncState = it, imageSyncStatus = mockImageSyncStatus) }
    }

    private fun runningEventSyncState() = mockk<EventSyncState>(relaxed = true) {
        every { isSyncRunning() } returns true
        every { isSyncCompleted() } returns false
    }

    private fun failedEventSyncState() = mockk<EventSyncState>(relaxed = true) {
        every { isSyncRunning() } returns false
        every { isSyncCompleted() } returns false
        every { isSyncFailed() } returns true
    }

    private fun givenNoSyncHistory() {
        val idleWithoutHistoryState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncRunning() } returns false
            every { lastSyncTime } returns null
        }
        syncStatusFlow.value = SyncStatus(eventSyncState = idleWithoutHistoryState, imageSyncStatus = mockImageSyncStatus)
    }

    private fun attachHost(isPreLogoutUpSync: Boolean) {
        viewModel.onAction(SyncInfoUiAction.HostAttached(isPreLogoutUpSync))
    }

    // uiState only runs its streams while collected: collecting this with Turbine is the screen being started.
    private fun syncInfo() = viewModel.uiState
        .map { it.syncInfo }
        .distinctUntilChanged()

    // The first value after subscribing when the (mocked) data source already has a value. uiState is a conflated StateFlow, so the
    // collector gets the current data-driven info and not the SyncInfo() placeholder; the placeholder is only seen while no data has
    // arrived (pinned by `before the first data emission the state carries the default sync info` and the N-1 test).
    private suspend fun ReceiveTurbine<SyncInfo>.awaitInitialSyncInfo() {
        assertThat(awaitItem()).isEqualTo(createDefaultSyncInfo())
    }

    private fun completedEventSyncState() = mockk<EventSyncState>(relaxed = true) {
        every { isSyncCompleted() } returns true
    }

    private fun notSyncingImageStatus() = mockk<ImageSyncStatus>(relaxed = true) {
        every { isSyncing } returns false
        every { progress } returns null
        every { lastUpdateTimeMillis } returns 0
    }

    private fun syncingImageStatus() = mockk<ImageSyncStatus>(relaxed = true) {
        every { isSyncing } returns true
    }

    private companion object {
        const val TEST_PROJECT_ID = "test_project_id"
        const val TEST_USER_ID = "test_user_id"
        const val TEST_RECENT_USER_ID = "recent_user_id"
        val TEST_TIMESTAMP = Timestamp(1000L)
        val EXPECTED_LOGIN_PARAMS = LoginParams(projectId = TEST_PROJECT_ID, userId = TokenizableString.Raw(TEST_USER_ID))
    }
}
