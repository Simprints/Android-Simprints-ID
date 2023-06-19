package com.simprints.feature.dashboard.main.sync

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.time.TimeHelper
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
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.DeviceConfiguration
import com.simprints.infra.config.domain.models.DownSynchronizationConfiguration
import com.simprints.infra.config.domain.models.SynchronizationConfiguration
import com.simprints.infra.config.domain.models.UpSynchronizationConfiguration.SimprintsUpSynchronizationConfiguration
import com.simprints.infra.config.domain.models.UpSynchronizationConfiguration.UpSynchronizationKind.ALL
import com.simprints.infra.eventsync.EventSyncManager
import com.simprints.infra.eventsync.status.models.EventSyncState
import com.simprints.infra.eventsync.status.models.EventSyncWorkerState
import com.simprints.infra.eventsync.status.models.EventSyncWorkerType
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.network.ConnectivityTracker
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.livedata.getOrAwaitValue
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SyncViewModelTest {

    companion object {
        private const val DATE = "2022-10-10"
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
    lateinit var connectivityTracker: ConnectivityTracker

    @MockK
    lateinit var configManager: ConfigManager

    @MockK
    lateinit var timeHelper: TimeHelper

    @MockK
    lateinit var authStore: AuthStore

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        every { eventSyncManager.getLastSyncState() } returns syncState
        every { connectivityTracker.observeIsConnected() } returns isConnected
        coEvery { configManager.getProjectConfiguration().synchronization } returns mockk {
            every { up.simprints } returns SimprintsUpSynchronizationConfiguration(kind = ALL)
            every { frequency } returns SynchronizationConfiguration.Frequency.PERIODICALLY_AND_ON_SESSION_START
            every { down.partitionType } returns DownSynchronizationConfiguration.PartitionType.MODULE
        }
        every { timeHelper.readableBetweenNowAndTime(any()) } returns DATE
        every { authStore.signedInProjectId } returns "projectId"
    }

    @Test
    fun `should initialize the live data syncToBFSIDAllowed correctly`() {
        syncState.postValue(EventSyncState("", 0, 0, listOf(), listOf()))
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

        verify(exactly = 1) { eventSyncManager.sync() }
        assertThat(viewModel.syncCardLiveData.value).isEqualTo(SyncConnecting(null, 0, null))
    }

    @Test
    fun `should trigger an initial sync if the sync is not running and there is a last sync that is longer than 5 minutes ago`() {
        every { timeHelper.msBetweenNowAndTime(any()) } returns 6 * 60_0000
        syncState.value = null
        isConnected.value = true

        val viewModel = initViewModel()

        verify(exactly = 1) { eventSyncManager.sync() }
        assertThat(viewModel.syncCardLiveData.value).isEqualTo(SyncConnecting(null, 0, null))
    }

    @Test
    fun `should not trigger an initial sync if the sync is running`() {
        syncState.value = EventSyncState(
            "", 0, 0, listOf(
            EventSyncState.SyncWorkerInfo(
                EventSyncWorkerType.DOWNLOADER,
                EventSyncWorkerState.Running
            )
        ), listOf()
        )
        isConnected.value = true

        initViewModel()

        verify(exactly = 0) { eventSyncManager.sync() }
    }

    @Test
    fun `should post a SyncHasNoModules card state if the module selection is empty`() {
        coEvery { configManager.getDeviceConfiguration() } returns DeviceConfiguration(
            "",
            listOf(),
            ""
        )
        isConnected.value = true
        val syncCardLiveData = initViewModel().syncCardLiveData.getOrAwaitValue()

        assertThat(syncCardLiveData).isEqualTo(SyncHasNoModules(DATE))
    }

    @Test
    fun `should post a SyncOffline card state if the device is not connected`() {
        coEvery { configManager.getDeviceConfiguration() } returns DeviceConfiguration(
            "",
            listOf("module 1"),
            ""
        )
        isConnected.value = false
        val syncCardLiveData = initViewModel().syncCardLiveData.getOrAwaitValue()

        assertThat(syncCardLiveData).isEqualTo(SyncOffline(DATE))
    }

    @Test
    fun `should post a SyncConnecting card state if the sync is running but not info are available`() {
        coEvery { configManager.getDeviceConfiguration() } returns DeviceConfiguration(
            "",
            listOf("module 1"),
            ""
        )
        isConnected.value = true
        syncState.value = null
        val syncCardLiveData = initViewModel().syncCardLiveData.getOrAwaitValue()

        assertThat(syncCardLiveData).isEqualTo(SyncConnecting(DATE, 0, null))
    }

    @Test
    fun `should post a SyncDefault card state if there is no sync history`() {
        coEvery { configManager.getDeviceConfiguration() } returns DeviceConfiguration(
            "",
            listOf("module 1"),
            ""
        )
        isConnected.value = true
        syncState.value = EventSyncState("", 0, 0, listOf(), listOf())
        val syncCardLiveData = initViewModel().syncCardLiveData.getOrAwaitValue()

        assertThat(syncCardLiveData).isEqualTo(SyncDefault(DATE))
    }

    @Test
    fun `should post a SyncComplete card state if the sync is completed`() {
        coEvery { configManager.getDeviceConfiguration() } returns DeviceConfiguration(
            "",
            listOf("module 1"),
            ""
        )
        isConnected.value = true
        syncState.value = EventSyncState(
            "", 0, 0, listOf(),
            listOf(
                EventSyncState.SyncWorkerInfo(
                    EventSyncWorkerType.DOWNLOADER,
                    EventSyncWorkerState.Succeeded
                )
            )
        )
        val syncCardLiveData = initViewModel().syncCardLiveData.getOrAwaitValue()

        assertThat(syncCardLiveData).isEqualTo(SyncComplete(DATE))
    }

    @Test
    fun `should post a SyncPendingUpload card state if there are records to upload`() {
        coEvery { configManager.getDeviceConfiguration() } returns DeviceConfiguration(
            "",
            listOf("module 1"),
            ""
        )
        coEvery { eventSyncManager.countEventsToUpload(any(), any()) }.returns(flowOf(2))

        isConnected.value = true
        syncState.value = EventSyncState(
            "", 0, 0, listOf(), listOf(
            EventSyncState.SyncWorkerInfo(
                EventSyncWorkerType.DOWNLOADER,
                EventSyncWorkerState.Succeeded
            )
        )
        )
        val syncCardLiveData = initViewModel().syncCardLiveData.getOrAwaitValue()

        assertThat(syncCardLiveData).isEqualTo(SyncPendingUpload(DATE, 2))
    }

    @Test
    fun `should post a SyncProgress card state if the sync is in progress`() {
        coEvery { configManager.getDeviceConfiguration() } returns DeviceConfiguration(
            "",
            listOf("module 1"),
            ""
        )
        isConnected.value = true
        syncState.value = EventSyncState(
            "", 10, 40, listOf(),
            listOf(
                EventSyncState.SyncWorkerInfo(
                    EventSyncWorkerType.DOWNLOADER,
                    EventSyncWorkerState.Running
                )
            )
        )
        val syncCardLiveData = initViewModel().syncCardLiveData.getOrAwaitValue()

        assertThat(syncCardLiveData).isEqualTo(SyncProgress(DATE, 10, 40))
    }

    @Test
    fun `should post a SyncConnecting card state if the sync is enqueued`() {
        coEvery { configManager.getDeviceConfiguration() } returns DeviceConfiguration(
            "",
            listOf("module 1"),
            ""
        )
        isConnected.value = true
        syncState.value = EventSyncState(
            "", 10, 40, listOf(),
            listOf(
                EventSyncState.SyncWorkerInfo(
                    EventSyncWorkerType.DOWNLOADER,
                    EventSyncWorkerState.Enqueued
                )
            )
        )
        val syncCardLiveData = initViewModel().syncCardLiveData.getOrAwaitValue()

        assertThat(syncCardLiveData).isEqualTo(SyncConnecting(DATE, 10, 40))
    }

    @Test
    fun `should post a SyncTooManyRequests card state if there are too many sync requests`() {
        coEvery { configManager.getDeviceConfiguration() } returns DeviceConfiguration(
            "",
            listOf("module 1"),
            ""
        )
        isConnected.value = true
        syncState.value = EventSyncState(
            "", 10, 40, listOf(), listOf(
            EventSyncState.SyncWorkerInfo(
                EventSyncWorkerType.DOWNLOADER,
                EventSyncWorkerState.Failed(failedBecauseTooManyRequest = true)
            )
        )
        )
        val syncCardLiveData = initViewModel().syncCardLiveData.getOrAwaitValue()

        assertThat(syncCardLiveData).isEqualTo(SyncTooManyRequests(DATE))
    }

    @Test
    fun `should post a SyncFailed card state if the sync fails because of cloud integration`() {
        coEvery { configManager.getDeviceConfiguration() } returns DeviceConfiguration(
            "",
            listOf("module 1"),
            ""
        )
        isConnected.value = true
        syncState.value = EventSyncState(
            "", 10, 40, listOf(), listOf(
            EventSyncState.SyncWorkerInfo(
                EventSyncWorkerType.DOWNLOADER,
                EventSyncWorkerState.Failed(failedBecauseCloudIntegration = true)
            )
        )
        )
        val syncCardLiveData = initViewModel().syncCardLiveData.getOrAwaitValue()

        assertThat(syncCardLiveData).isEqualTo(SyncFailed(DATE))
    }

    @Test
    fun `should post a SyncFailedBackendMaintenance card state if the sync fails because of cloud maintenance`() {
        coEvery { configManager.getDeviceConfiguration() } returns DeviceConfiguration(
            "",
            listOf("module 1"),
            ""
        )
        isConnected.value = true
        syncState.value = EventSyncState(
            "", 10, 40, listOf(),
            listOf(
                EventSyncState.SyncWorkerInfo(
                    EventSyncWorkerType.DOWNLOADER,
                    EventSyncWorkerState.Failed(failedBecauseBackendMaintenance = true)
                )
            )
        )
        val syncCardLiveData = initViewModel().syncCardLiveData.getOrAwaitValue()

        assertThat(syncCardLiveData).isEqualTo(SyncFailedBackendMaintenance(DATE))
    }

    @Test
    fun `should post a SyncFailedBackendMaintenance with estimated outage card state if the sync fails because of cloud maintenance with outage`() {
        coEvery { configManager.getDeviceConfiguration() } returns DeviceConfiguration(
            "",
            listOf("module 1"),
            ""
        )
        isConnected.value = true
        syncState.value = EventSyncState(
            "", 10, 40, listOf(), listOf(
            EventSyncState.SyncWorkerInfo(
                EventSyncWorkerType.DOWNLOADER,
                EventSyncWorkerState.Failed(
                    failedBecauseBackendMaintenance = true,
                    estimatedOutage = 30
                )
            )
        )
        )
        val syncCardLiveData = initViewModel().syncCardLiveData.getOrAwaitValue()

        assertThat(syncCardLiveData).isEqualTo(SyncFailedBackendMaintenance(DATE, 30))
    }

    @Test
    fun `should post a SyncTryAgain card state if the sync fails because of another thing`() {
        coEvery { configManager.getDeviceConfiguration() } returns DeviceConfiguration(
            "",
            listOf("module 1"),
            ""
        )
        isConnected.value = true
        syncState.value = EventSyncState(
            "", 10, 40, listOf(),
            listOf(
                EventSyncState.SyncWorkerInfo(
                    EventSyncWorkerType.DOWNLOADER,
                    EventSyncWorkerState.Failed()
                )
            )
        )
        val syncCardLiveData = initViewModel().syncCardLiveData.getOrAwaitValue()

        assertThat(syncCardLiveData).isEqualTo(SyncTryAgain(DATE))
    }

    private fun initViewModel(): SyncViewModel = SyncViewModel(
        eventSyncManager,
        connectivityTracker,
        configManager,
        timeHelper,
        authStore,
    )
}