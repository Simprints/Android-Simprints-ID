package com.simprints.id.activities.dashboard.cards.sync

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.time.TimeHelper
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_MODULE_ID
import com.simprints.id.activities.dashboard.cards.sync.DashboardSyncCardState.*
import com.simprints.id.activities.dashboard.cards.sync.DashboardSyncCardStateRepositoryImpl.Companion.MAX_TIME_BEFORE_SYNC_AGAIN
import com.simprints.id.services.sync.events.master.EventSyncManager
import com.simprints.id.services.sync.events.master.internal.EventSyncCache
import com.simprints.id.services.sync.events.master.models.EventSyncState
import com.simprints.id.services.sync.events.master.models.EventSyncState.SyncWorkerInfo
import com.simprints.id.services.sync.events.master.models.EventSyncWorkerState.*
import com.simprints.id.services.sync.events.master.models.EventSyncWorkerType.DOWN_COUNTER
import com.simprints.id.services.sync.events.master.models.EventSyncWorkerType.UP_COUNTER
import com.simprints.id.testtools.TestApplication
import com.simprints.id.testtools.TestTimeHelperImpl
import com.simprints.id.tools.device.DeviceManager
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.DeviceConfiguration
import com.simprints.infra.config.domain.models.DownSynchronizationConfiguration
import com.simprints.infra.config.domain.models.SynchronizationConfiguration
import com.simprints.testtools.common.livedata.getOrAwaitValue
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.util.*

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class DashboardSyncCardStateRepositoryImplTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private lateinit var dashboardSyncCardStateRepository: DashboardSyncCardStateRepositoryImpl

    private val syncCardTestLiveData
        get() = dashboardSyncCardStateRepository.syncCardStateLiveData

    @MockK
    lateinit var eventSyncManager: EventSyncManager

    @MockK
    lateinit var deviceManager: DeviceManager

    @MockK
    lateinit var configManager: ConfigManager

    @MockK
    lateinit var cacheSync: EventSyncCache

    @MockK
    lateinit var timeHelper: TimeHelper

    private lateinit var isConnectedUpdates: MutableLiveData<Boolean>
    private lateinit var syncStateLiveData: MutableLiveData<EventSyncState>
    private val syncId = UUID.randomUUID().toString()
    private val lastSyncTime = Date()
    private val synchronizationConfiguration = mockk<SynchronizationConfiguration>()

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        isConnectedUpdates = MutableLiveData()
        isConnectedUpdates.value = true
        syncStateLiveData = MutableLiveData()
        every { deviceManager.isConnectedLiveData } returns isConnectedUpdates
        every { eventSyncManager.getLastSyncState() } returns syncStateLiveData
        coEvery { configManager.getProjectConfiguration() } returns mockk {
            every { synchronization } returns synchronizationConfiguration
        }
        every { cacheSync.readLastSuccessfulSyncTime() } returns lastSyncTime
        every { eventSyncManager.hasSyncEverRunBefore() } returns true

        isConnectedUpdates.value = true
        dashboardSyncCardStateRepository = createRepository()
    }

    @Test
    fun deviceIsOffline_syncStateShouldBeSyncOffline() = runTest(UnconfinedTestDispatcher()) {
        isConnectedUpdates.value = false

        dashboardSyncCardStateRepository.syncIfRequired()
        val tester = syncCardTestLiveData.getOrAwaitValue()

        assertThat(tester).isEqualTo(SyncOffline(lastSyncTime))
    }

    @Test
    fun deviceIsOnline_syncStateShouldBeConnecting() = runTest(UnconfinedTestDispatcher()) {
        isConnectedUpdates.value = true

        dashboardSyncCardStateRepository.syncIfRequired()
        val tester = syncCardTestLiveData.getOrAwaitValue()

        assertThat(tester).isEqualTo(SyncConnecting(lastSyncTime, 0, null))
    }

    @Test
    fun frequencyIsPeriodicallyAndModulesEmpty_syncStateShouldBeSelectModules() = runTest(
        UnconfinedTestDispatcher()
    ) {
        coEvery { configManager.getDeviceConfiguration() } returns DeviceConfiguration(
            "",
            listOf(),
            listOf(),
            ""
        )
        every { synchronizationConfiguration.frequency } returns SynchronizationConfiguration.Frequency.PERIODICALLY
        every { synchronizationConfiguration.down } returns mockk {
            every { partitionType } returns DownSynchronizationConfiguration.PartitionType.MODULE
        }

        dashboardSyncCardStateRepository.syncIfRequired()
        val tester = syncCardTestLiveData.getOrAwaitValue()

        assertThat(tester).isEqualTo(SyncHasNoModules(lastSyncTime))
    }

    @Test
    fun frequencyIsPeriodicallyAndOnSessionStartAndModulesEmpty_syncStateShouldBeSelectModules() =
        runTest(
            UnconfinedTestDispatcher()
        ) {
            coEvery { configManager.getDeviceConfiguration() } returns DeviceConfiguration(
                "",
                listOf(),
                listOf(),
                ""
            )
            every { synchronizationConfiguration.frequency } returns SynchronizationConfiguration.Frequency.PERIODICALLY_AND_ON_SESSION_START
            every { synchronizationConfiguration.down } returns mockk {
                every { partitionType } returns DownSynchronizationConfiguration.PartitionType.MODULE
            }

            dashboardSyncCardStateRepository.syncIfRequired()
            val tester = syncCardTestLiveData.getOrAwaitValue()

            assertThat(tester).isEqualTo(SyncHasNoModules(lastSyncTime))
        }

    @Test
    fun frequencyIsOnlyPeriodicallyUpSyncAndModulesEmpty_syncStateShouldBeConnecting() = runTest(
        UnconfinedTestDispatcher()
    ) {
        coEvery { configManager.getDeviceConfiguration() } returns DeviceConfiguration(
            "",
            listOf(),
            listOf(),
            ""
        )
        every { synchronizationConfiguration.frequency } returns SynchronizationConfiguration.Frequency.ONLY_PERIODICALLY_UP_SYNC
        every { synchronizationConfiguration.down } returns mockk {
            every { partitionType } returns DownSynchronizationConfiguration.PartitionType.MODULE
        }

        dashboardSyncCardStateRepository.syncIfRequired()
        val tester = syncCardTestLiveData.getOrAwaitValue()

        assertThat(tester).isEqualTo(SyncConnecting(lastSyncTime, 0, null))
    }

    @Test
    fun modulesSelectedWithSyncByModule_syncStateShouldBeConnecting() = runTest(
        UnconfinedTestDispatcher()
    ) {
        coEvery { configManager.getDeviceConfiguration() } returns DeviceConfiguration(
            "",
            listOf(DEFAULT_MODULE_ID),
            listOf(),
            ""
        )
        every { synchronizationConfiguration.frequency } returns SynchronizationConfiguration.Frequency.PERIODICALLY
        every { synchronizationConfiguration.down } returns mockk {
            every { partitionType } returns DownSynchronizationConfiguration.PartitionType.MODULE
        }

        dashboardSyncCardStateRepository.syncIfRequired()
        val tester = syncCardTestLiveData.getOrAwaitValue()

        assertThat(tester).isEqualTo(SyncConnecting(lastSyncTime, 0, null))
    }

    @Test
    fun noModulesSelectedWithSyncByProject_syncStateShouldBeConnecting() = runTest(
        UnconfinedTestDispatcher()
    ) {
        coEvery { configManager.getDeviceConfiguration() } returns DeviceConfiguration(
            "",
            listOf(),
            listOf(),
            ""
        )
        every { synchronizationConfiguration.frequency } returns SynchronizationConfiguration.Frequency.PERIODICALLY
        every { synchronizationConfiguration.down } returns mockk {
            every { partitionType } returns DownSynchronizationConfiguration.PartitionType.PROJECT
        }

        dashboardSyncCardStateRepository.syncIfRequired()
        val tester = syncCardTestLiveData.getOrAwaitValue()

        assertThat(tester).isEqualTo(SyncConnecting(lastSyncTime, 0, null))
    }

    @Test
    fun syncWorkersCompleted_syncStateShouldBeCompleted() = runTest(UnconfinedTestDispatcher()) {
        syncStateLiveData.value = EventSyncState(
            syncId,
            10,
            10,
            listOf(SyncWorkerInfo(DOWN_COUNTER, Succeeded)),
            listOf(SyncWorkerInfo(UP_COUNTER, Succeeded))
        )

        dashboardSyncCardStateRepository.syncIfRequired()
        val tester = syncCardTestLiveData.getOrAwaitValue()

        assertThat(tester).isEqualTo(SyncComplete(lastSyncTime))
    }

    @Test
    fun syncWorkersEnqueued_syncStateShouldBeConnecting() = runTest(UnconfinedTestDispatcher()) {
        syncStateLiveData.value = EventSyncState(
            syncId,
            0,
            null,
            listOf(SyncWorkerInfo(DOWN_COUNTER, Enqueued)),
            listOf(SyncWorkerInfo(UP_COUNTER, Succeeded))
        )

        dashboardSyncCardStateRepository.syncIfRequired()
        val tester = syncCardTestLiveData.getOrAwaitValue()

        assertThat(tester).isEqualTo(SyncConnecting(lastSyncTime, 0, null))
    }

    @Test
    fun syncWorkersFailedForNetworkIssues_syncStateShouldBeTryAgain() = runTest(
        UnconfinedTestDispatcher()
    ) {
        syncStateLiveData.value = EventSyncState(
            syncId,
            0,
            null,
            listOf(SyncWorkerInfo(DOWN_COUNTER, Failed(false))),
            listOf(SyncWorkerInfo(UP_COUNTER, Succeeded))
        )

        dashboardSyncCardStateRepository.syncIfRequired()
        val tester = syncCardTestLiveData.getOrAwaitValue()

        assertThat(tester).isEqualTo(SyncTryAgain(lastSyncTime))
    }

    @Test
    fun syncWorkersFailedBecauseCloudIntegration_syncStateShouldBeFailed() = runTest(
        UnconfinedTestDispatcher()
    ) {
        syncStateLiveData.value = EventSyncState(
            syncId,
            0,
            null,
            listOf(SyncWorkerInfo(DOWN_COUNTER, Failed(true))),
            listOf(SyncWorkerInfo(UP_COUNTER, Succeeded))
        )

        dashboardSyncCardStateRepository.syncIfRequired()
        val tester = syncCardTestLiveData.getOrAwaitValue()

        assertThat(tester).isEqualTo(SyncFailed(lastSyncTime))
    }

    @Test
    fun syncWorkersFailedBecauseTooManyRequests_syncStateShouldBeFailedBecauseTooManyRequests() =
        runTest(
            UnconfinedTestDispatcher()
        ) {
            syncStateLiveData.value = EventSyncState(
                syncId,
                0,
                null,
                listOf(SyncWorkerInfo(DOWN_COUNTER, Failed(failedBecauseTooManyRequest = true))),
                listOf(SyncWorkerInfo(UP_COUNTER, Succeeded))
            )

            dashboardSyncCardStateRepository.syncIfRequired()
            val tester = syncCardTestLiveData.getOrAwaitValue()

            assertThat(tester).isEqualTo(SyncTooManyRequests(lastSyncTime))
        }

    @Test
    fun syncWorkersFailedBecauseBackendMaintenance_syncStateShouldBeFailedBecauseBackendMaintenance() =
        runTest(
            UnconfinedTestDispatcher()
        ) {
            syncStateLiveData.value = EventSyncState(
                syncId,
                0,
                null,
                listOf(
                    SyncWorkerInfo(
                        DOWN_COUNTER,
                        Failed(failedBecauseBackendMaintenance = true)
                    )
                ),
                listOf(SyncWorkerInfo(UP_COUNTER, Succeeded))
            )

            dashboardSyncCardStateRepository.syncIfRequired()
            val state = syncCardTestLiveData.getOrAwaitValue()

            assertThat(state).isEqualTo(SyncFailedBackendMaintenance(lastSyncTime))
        }

    @Test
    fun noHistoryAboutSync_syncStateShouldBeDefault() = runTest(UnconfinedTestDispatcher()) {
        syncStateLiveData.value =
            EventSyncState(syncId, 0, null, emptyList(), emptyList())

        dashboardSyncCardStateRepository.syncIfRequired()
        val tester = syncCardTestLiveData.getOrAwaitValue()

        assertThat(tester).isEqualTo(SyncDefault(lastSyncTime))
    }

    @Test
    fun syncSucceedInBackgroundLongTimeAgo_syncShouldBeTriggered() =
        runTest(UnconfinedTestDispatcher()) {
            dashboardSyncCardStateRepository = createRepository(TestTimeHelperImpl())
            syncStateLiveData.value = EventSyncState(
                syncId,
                10,
                10,
                listOf(SyncWorkerInfo(DOWN_COUNTER, Succeeded)),
                listOf(SyncWorkerInfo(UP_COUNTER, Succeeded))
            )
            every { cacheSync.readLastSuccessfulSyncTime() } returns Date(System.currentTimeMillis() - MAX_TIME_BEFORE_SYNC_AGAIN - 1)

            dashboardSyncCardStateRepository.syncIfRequired()
            syncCardTestLiveData.getOrAwaitValue()

            verify { eventSyncManager.sync() }
        }

    @Test
    fun syncSucceedInBackgroundNotTooLongAgo_syncShouldNotBeTriggered() = runTest(
        UnconfinedTestDispatcher()
    ) {
        dashboardSyncCardStateRepository = createRepository(timeHelper)
        syncStateLiveData.value = EventSyncState(
            syncId,
            10,
            10,
            listOf(SyncWorkerInfo(DOWN_COUNTER, Succeeded)),
            listOf(SyncWorkerInfo(UP_COUNTER, Succeeded))
        )
        every { cacheSync.readLastSuccessfulSyncTime() } returns Date()

        dashboardSyncCardStateRepository.syncIfRequired()
        syncCardTestLiveData.getOrAwaitValue()

        verify(exactly = 0) { eventSyncManager.sync() }
    }

    @Test
    fun syncFinishedButOnlyRecently_syncIfRequired_shouldNotLaunchTheSync() = runTest(
        UnconfinedTestDispatcher()
    ) {
        every { timeHelper.msBetweenNowAndTime(any()) } returns MAX_TIME_BEFORE_SYNC_AGAIN - 1
        syncStateLiveData.value = EventSyncState(
            syncId,
            0,
            null,
            listOf(SyncWorkerInfo(DOWN_COUNTER, Succeeded)),
            listOf(SyncWorkerInfo(UP_COUNTER, Succeeded))
        )

        dashboardSyncCardStateRepository.syncIfRequired()
        dashboardSyncCardStateRepository.syncIfRequired()

        verify(exactly = 0) { eventSyncManager.sync() }
    }

    @Test
    fun noHistoryAboutSync_syncIfRequired_shouldNotLaunchTheSync() =
        runTest(UnconfinedTestDispatcher()) {
            dashboardSyncCardStateRepository.syncIfRequired()

            verify(exactly = 0) { eventSyncManager.sync() }
        }

    @Test
    fun syncWorkersFailedDueToTimeout_syncStateShouldBeFailed() =
        runTest(UnconfinedTestDispatcher()) {
            syncStateLiveData.value = EventSyncState(
                syncId,
                0,
                null,
                listOf(SyncWorkerInfo(DOWN_COUNTER, Failed(true))),
                listOf(SyncWorkerInfo(UP_COUNTER, Succeeded))
            )

            dashboardSyncCardStateRepository.syncIfRequired()
            val tester = syncCardTestLiveData.getOrAwaitValue()

            assertThat(tester).isEqualTo(SyncFailed(lastSyncTime))
        }

    @Test
    fun syncWorkersAreRunning_syncStateShouldBeInProgress() = runTest(UnconfinedTestDispatcher()) {
        val progress = 10
        val total = 100
        syncStateLiveData.value = EventSyncState(
            syncId,
            progress,
            total,
            listOf(SyncWorkerInfo(DOWN_COUNTER, Running)),
            listOf(SyncWorkerInfo(UP_COUNTER, Running))
        )

        dashboardSyncCardStateRepository.syncIfRequired()
        val tester = syncCardTestLiveData.getOrAwaitValue()

        assertThat(tester).isEqualTo(
            SyncProgress(
                lastSyncTime,
                progress,
                total
            )
        )
    }


    private fun createRepository(specificTimeHelper: TimeHelper = timeHelper) =
        DashboardSyncCardStateRepositoryImpl(
            eventSyncManager,
            deviceManager,
            configManager,
            cacheSync,
            specificTimeHelper
        )

}

