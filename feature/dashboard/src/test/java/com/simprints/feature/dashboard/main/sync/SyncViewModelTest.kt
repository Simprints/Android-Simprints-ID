package com.simprints.feature.dashboard.main.sync

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.core.tools.time.TimeHelper
import com.simprints.feature.dashboard.logout.usecase.LogoutUseCase
import com.simprints.feature.dashboard.views.SyncCardState
import com.simprints.feature.dashboard.views.SyncCardState.SyncComplete
import com.simprints.feature.dashboard.views.SyncCardState.SyncConnecting
import com.simprints.feature.dashboard.views.SyncCardState.SyncDefault
import com.simprints.feature.dashboard.views.SyncCardState.SyncFailed
import com.simprints.feature.dashboard.views.SyncCardState.SyncFailedBackendMaintenance
import com.simprints.feature.dashboard.views.SyncCardState.SyncHasNoModules
import com.simprints.feature.dashboard.views.SyncCardState.SyncOffline
import com.simprints.feature.dashboard.views.SyncCardState.SyncPendingUpload
import com.simprints.feature.dashboard.views.SyncCardState.SyncProgress
import com.simprints.feature.dashboard.views.SyncCardState.SyncTooManyRequests
import com.simprints.feature.dashboard.views.SyncCardState.SyncTryAgain
import com.simprints.feature.login.LoginResult
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.models.DeviceConfiguration
import com.simprints.infra.config.store.models.DownSynchronizationConfiguration
import com.simprints.infra.config.store.models.ProjectState
import com.simprints.infra.config.store.models.SynchronizationConfiguration
import com.simprints.infra.config.store.models.UpSynchronizationConfiguration
import com.simprints.infra.config.store.models.UpSynchronizationConfiguration.SimprintsUpSynchronizationConfiguration
import com.simprints.infra.config.store.models.UpSynchronizationConfiguration.UpSynchronizationKind.ALL
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.eventsync.EventSyncManager
import com.simprints.infra.eventsync.status.models.EventSyncState
import com.simprints.infra.eventsync.status.models.EventSyncWorkerState
import com.simprints.infra.eventsync.status.models.EventSyncWorkerType
import com.simprints.infra.network.ConnectivityTracker
import com.simprints.infra.recent.user.activity.RecentUserActivityManager
import com.simprints.infra.sync.SyncOrchestrator
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.livedata.getOrAwaitValue
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test

internal class SyncViewModelTest {
    companion object {
        private const val DATE = "2022-10-10"
        private val deviceConfiguration = DeviceConfiguration(
            language = "",
            selectedModules = listOf("module 1".asTokenizableEncrypted()),
            lastInstructionId = "",
        )
    }

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private val isConnected = MutableLiveData<Boolean>()
    private val syncState = MutableLiveData<EventSyncState>()

    @MockK
    lateinit var eventSyncManager: EventSyncManager

    @MockK
    lateinit var syncOrchestrator: SyncOrchestrator

    @MockK
    lateinit var connectivityTracker: ConnectivityTracker

    @MockK
    lateinit var configManager: ConfigManager

    @MockK
    lateinit var timeHelper: TimeHelper

    @MockK
    lateinit var authStore: AuthStore

    @MockK
    lateinit var logoutUseCase: LogoutUseCase

    @MockK
    lateinit var recentUserActivityManager: RecentUserActivityManager

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        every { eventSyncManager.getLastSyncState() } returns syncState
        every { connectivityTracker.observeIsConnected() } returns isConnected
        coEvery { configManager.getProjectConfiguration().synchronization } returns mockk {
            every { up.simprints } returns SimprintsUpSynchronizationConfiguration(
                kind = ALL,
                batchSizes = UpSynchronizationConfiguration.UpSyncBatchSizes.default(),
                false,
            )
            every { frequency } returns SynchronizationConfiguration.Frequency.PERIODICALLY_AND_ON_SESSION_START
            every { down.partitionType } returns DownSynchronizationConfiguration.PartitionType.MODULE
        }
        every { timeHelper.readableBetweenNowAndTime(any()) } returns DATE
        every { authStore.signedInProjectId } returns "projectId"
    }

    @Test
    fun `should initialize the live data syncToBFSIDAllowed correctly`() {
        syncState.postValue(EventSyncState("", 0, 0, listOf(), listOf(), listOf()))
        isConnected.postValue(true)

        val viewModel = initViewModel()

        assertThat(viewModel.syncToBFSIDAllowed.value).isEqualTo(true)
    }

    @Test
    fun `should trigger an initial sync if the sync is not running and there is no last sync`() {
        coEvery { eventSyncManager.getLastSyncTime() } returns null
        syncState.value = null
        isConnected.value = true

        val viewModel = initViewModel()

        verify(exactly = 1) { syncOrchestrator.startEventSync() }
        assertThat(viewModel.syncCardLiveData.value).isEqualTo(SyncConnecting(null, 0, null))
    }

    @Test
    fun `should trigger an initial sync if the sync is not running and there is a last sync that is longer than 5 minutes ago`() {
        every { timeHelper.msBetweenNowAndTime(any()) } returns 6 * 60_0000
        syncState.value = null
        isConnected.value = true

        val viewModel = initViewModel()

        verify(exactly = 1) { syncOrchestrator.startEventSync() }
        assertThat(viewModel.syncCardLiveData.value).isEqualTo(SyncConnecting(null, 0, null))
    }

    @Test
    fun `should not trigger an initial sync if the sync is running`() {
        syncState.value = EventSyncState(
            "",
            0,
            0,
            listOf(
                EventSyncState.SyncWorkerInfo(
                    EventSyncWorkerType.DOWNLOADER,
                    EventSyncWorkerState.Running,
                ),
            ),
            listOf(),
            listOf(),
        )
        isConnected.value = true

        initViewModel()

        verify(exactly = 0) { syncOrchestrator.startEventSync() }
    }

    @Test
    fun `should post a SyncHasNoModules card state if the module selection is empty`() {
        coEvery { configManager.getDeviceConfiguration() } returns DeviceConfiguration(
            "",
            listOf(),
            "",
        )
        isConnected.value = true
        val syncCardLiveData = initViewModel().syncCardLiveData.getOrAwaitValue()

        assertThat(syncCardLiveData).isEqualTo(SyncHasNoModules(DATE))
    }

    @Test
    fun `should post a SyncOffline card state if the device is not connected`() {
        coEvery { configManager.getDeviceConfiguration() } returns deviceConfiguration
        isConnected.value = false
        val syncCardLiveData = initViewModel().syncCardLiveData.getOrAwaitValue()

        assertThat(syncCardLiveData).isEqualTo(SyncOffline(DATE))
    }

    @Test
    fun `should post a SyncConnecting card state if the sync is running but not info are available`() {
        coEvery { configManager.getDeviceConfiguration() } returns deviceConfiguration
        isConnected.value = true
        syncState.value = null
        val syncCardLiveData = initViewModel().syncCardLiveData.getOrAwaitValue()

        assertThat(syncCardLiveData).isEqualTo(SyncConnecting(DATE, 0, null))
    }

    @Test
    fun `should post a SyncDefault card state if there is no sync history`() {
        coEvery { configManager.getDeviceConfiguration() } returns deviceConfiguration
        isConnected.value = true
        syncState.value = EventSyncState("", 0, 0, listOf(), listOf(), listOf())
        val syncCardLiveData = initViewModel().syncCardLiveData.getOrAwaitValue()

        assertThat(syncCardLiveData).isEqualTo(SyncDefault(DATE))
    }

    @Test
    fun `should post a SyncComplete card state if the sync is completed`() {
        coEvery { configManager.getDeviceConfiguration() } returns deviceConfiguration
        isConnected.value = true
        syncState.value = EventSyncState(
            "",
            0,
            0,
            listOf(),
            listOf(
                EventSyncState.SyncWorkerInfo(
                    EventSyncWorkerType.DOWNLOADER,
                    EventSyncWorkerState.Succeeded,
                ),
            ),
            listOf(),
        )
        val syncCardLiveData = initViewModel().syncCardLiveData.getOrAwaitValue()

        assertThat(syncCardLiveData).isEqualTo(SyncComplete(DATE))
    }

    @Test
    fun `should post a SyncPendingUpload card state if there are records to upload`() {
        coEvery { configManager.getDeviceConfiguration() } returns deviceConfiguration
        coEvery { eventSyncManager.countEventsToUpload(any()) }.returns(flowOf(2))

        isConnected.value = true
        syncState.value = EventSyncState(
            "",
            0,
            0,
            listOf(),
            listOf(
                EventSyncState.SyncWorkerInfo(
                    EventSyncWorkerType.DOWNLOADER,
                    EventSyncWorkerState.Succeeded,
                ),
            ),
            listOf(),
        )
        val syncCardLiveData = initViewModel().syncCardLiveData.getOrAwaitValue()

        assertThat(syncCardLiveData).isEqualTo(SyncPendingUpload(DATE, 2))
    }

    @Test
    fun `should post a SyncProgress card state if the sync is in progress`() {
        coEvery { configManager.getDeviceConfiguration() } returns deviceConfiguration
        isConnected.value = true
        syncState.value = EventSyncState(
            "",
            10,
            40,
            listOf(),
            listOf(
                EventSyncState.SyncWorkerInfo(
                    EventSyncWorkerType.DOWNLOADER,
                    EventSyncWorkerState.Running,
                ),
            ),
            listOf(),
        )
        val syncCardLiveData = initViewModel().syncCardLiveData.getOrAwaitValue()

        assertThat(syncCardLiveData).isEqualTo(SyncProgress(DATE, 10, 40))
    }

    @Test
    fun `should post a SyncConnecting card state if the sync is enqueued`() {
        coEvery { configManager.getDeviceConfiguration() } returns deviceConfiguration
        isConnected.value = true
        syncState.value = EventSyncState(
            "",
            10,
            40,
            listOf(),
            listOf(
                EventSyncState.SyncWorkerInfo(
                    EventSyncWorkerType.DOWNLOADER,
                    EventSyncWorkerState.Enqueued,
                ),
            ),
            listOf(),
        )
        val syncCardLiveData = initViewModel().syncCardLiveData.getOrAwaitValue()

        assertThat(syncCardLiveData).isEqualTo(SyncConnecting(DATE, 10, 40))
    }

    @Test
    fun `should post a SyncTooManyRequests card state if there are too many sync requests`() {
        coEvery { configManager.getDeviceConfiguration() } returns deviceConfiguration
        isConnected.value = true
        syncState.value = EventSyncState(
            "",
            10,
            40,
            listOf(),
            listOf(
                EventSyncState.SyncWorkerInfo(
                    EventSyncWorkerType.DOWNLOADER,
                    EventSyncWorkerState.Failed(failedBecauseTooManyRequest = true),
                ),
            ),
            listOf(),
        )
        val syncCardLiveData = initViewModel().syncCardLiveData.getOrAwaitValue()

        assertThat(syncCardLiveData).isEqualTo(SyncTooManyRequests(DATE))
    }

    @Test
    fun `should post a SyncFailed card state if the sync fails because of cloud integration`() {
        coEvery { configManager.getDeviceConfiguration() } returns deviceConfiguration
        isConnected.value = true
        syncState.value = EventSyncState(
            "",
            10,
            40,
            listOf(),
            listOf(
                EventSyncState.SyncWorkerInfo(
                    EventSyncWorkerType.DOWNLOADER,
                    EventSyncWorkerState.Failed(failedBecauseCloudIntegration = true),
                ),
            ),
            listOf(),
        )
        val syncCardLiveData = initViewModel().syncCardLiveData.getOrAwaitValue()

        assertThat(syncCardLiveData).isEqualTo(SyncFailed(DATE))
    }

    @Test
    fun `should post a ReloginRequired card state if the sync fails with such problem`() {
        coEvery { configManager.getDeviceConfiguration() } returns deviceConfiguration
        isConnected.value = true
        syncState.value = EventSyncState(
            "",
            10,
            40,
            listOf(),
            listOf(
                EventSyncState.SyncWorkerInfo(
                    EventSyncWorkerType.DOWNLOADER,
                    EventSyncWorkerState.Failed(failedBecauseReloginRequired = true),
                ),
            ),
            listOf(),
        )
        val syncCardLiveData = initViewModel().syncCardLiveData.getOrAwaitValue()

        assertThat(syncCardLiveData).isEqualTo(SyncCardState.SyncFailedReloginRequired(DATE))
    }

    @Test
    fun `calling login() sends respective event to the view`() {
        val viewModel = initViewModel()

        viewModel.login()

        val loginRequestedEvent = viewModel.loginRequestedEventLiveData.getOrAwaitValue()
        assertThat(loginRequestedEvent).isNotNull()
    }

    @Test
    fun `calling handleLoginResult() triggers sync if result is success`() {
        val viewModel = initViewModel()

        viewModel.handleLoginResult(LoginResult(true))

        verify(exactly = 1) { syncOrchestrator.startEventSync() }
    }

    @Test
    fun `calling handleLoginResult() does not trigger sync if result is not success`() {
        val viewModel = initViewModel()

        viewModel.handleLoginResult(LoginResult(false))

        verify(exactly = 0) { syncOrchestrator.startEventSync() }
    }

    @Test
    fun `should post a SyncFailedBackendMaintenance card state if the sync fails because of cloud maintenance`() {
        coEvery { configManager.getDeviceConfiguration() } returns deviceConfiguration
        isConnected.value = true
        syncState.value = EventSyncState(
            "",
            10,
            40,
            listOf(),
            listOf(
                EventSyncState.SyncWorkerInfo(
                    EventSyncWorkerType.DOWNLOADER,
                    EventSyncWorkerState.Failed(failedBecauseBackendMaintenance = true),
                ),
            ),
            listOf(),
        )
        val syncCardLiveData = initViewModel().syncCardLiveData.getOrAwaitValue()

        assertThat(syncCardLiveData).isEqualTo(SyncFailedBackendMaintenance(DATE))
    }

    @Test
    fun `should post a SyncFailedBackendMaintenance with estimated outage card state if the sync fails because of cloud maintenance with outage`() {
        coEvery { configManager.getDeviceConfiguration() } returns deviceConfiguration
        isConnected.value = true
        syncState.value = EventSyncState(
            "",
            10,
            40,
            listOf(),
            listOf(
                EventSyncState.SyncWorkerInfo(
                    EventSyncWorkerType.DOWNLOADER,
                    EventSyncWorkerState.Failed(
                        failedBecauseBackendMaintenance = true,
                        estimatedOutage = 30,
                    ),
                ),
            ),
            listOf(),
        )
        val syncCardLiveData = initViewModel().syncCardLiveData.getOrAwaitValue()

        assertThat(syncCardLiveData).isEqualTo(SyncFailedBackendMaintenance(DATE, 30))
    }

    @Test
    fun `should post a SyncTryAgain card state if the sync fails because of another thing`() {
        coEvery { configManager.getDeviceConfiguration() } returns deviceConfiguration
        isConnected.value = true
        syncState.value = EventSyncState(
            "",
            10,
            40,
            listOf(),
            listOf(
                EventSyncState.SyncWorkerInfo(
                    EventSyncWorkerType.DOWNLOADER,
                    EventSyncWorkerState.Failed(),
                ),
            ),
            listOf(),
        )
        val syncCardLiveData = initViewModel().syncCardLiveData.getOrAwaitValue()

        assertThat(syncCardLiveData).isEqualTo(SyncTryAgain(DATE))
    }

    @Test
    fun `should logout when project is ending and sync is complete`() {
        coEvery { configManager.getDeviceConfiguration() } returns deviceConfiguration
        coEvery { configManager.getProject(any()).state } returns ProjectState.PROJECT_ENDING
        isConnected.value = true
        syncState.value = EventSyncState(
            "",
            0,
            0,
            listOf(),
            listOf(
                EventSyncState.SyncWorkerInfo(
                    EventSyncWorkerType.DOWNLOADER,
                    EventSyncWorkerState.Succeeded,
                ),
            ),
            listOf(),
        )
        val viewModel = initViewModel()
        viewModel.syncCardLiveData.getOrAwaitValue()
        val signOutEvent = viewModel.signOutEventLiveData.getOrAwaitValue()

        assertThat(signOutEvent).isNotNull()
        coVerify(exactly = 1) { logoutUseCase.invoke() }
    }

    private fun initViewModel(): SyncViewModel = SyncViewModel(
        eventSyncManager = eventSyncManager,
        syncOrchestrator = syncOrchestrator,
        connectivityTracker = connectivityTracker,
        configManager = configManager,
        timeHelper = timeHelper,
        authStore = authStore,
        logout = logoutUseCase,
        recentUserActivityManager = recentUserActivityManager,
    )
}
