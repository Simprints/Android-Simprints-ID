package com.simprints.id.activities.dashboard.cards.sync

import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.id.activities.dashboard.cards.sync.DashboardSyncCardState.*
import com.simprints.id.activities.dashboard.cards.sync.DashboardSyncCardStateRepositoryImpl.Companion.MAX_TIME_BEFORE_SYNC_AGAIN
import com.simprints.id.commontesttools.DefaultTestConstants
import com.simprints.id.commontesttools.TestTimeHelperImpl
import com.simprints.id.data.db.events_sync.down.EventDownSyncScopeRepository
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.services.sync.events.master.EventSyncManager
import com.simprints.id.services.sync.events.master.internal.EventSyncCache
import com.simprints.id.services.sync.events.master.models.EventDownSyncSetting
import com.simprints.id.services.sync.events.master.models.EventSyncState
import com.simprints.id.services.sync.events.master.models.EventSyncState.SyncWorkerInfo
import com.simprints.id.services.sync.events.master.models.EventSyncWorkerState.*
import com.simprints.id.services.sync.events.master.models.EventSyncWorkerType.DOWN_COUNTER
import com.simprints.id.services.sync.events.master.models.EventSyncWorkerType.UP_COUNTER
import com.simprints.id.testtools.TestApplication
import com.simprints.id.tools.device.DeviceManager
import com.simprints.id.tools.time.TimeHelper
import com.simprints.testtools.common.livedata.testObserver
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.util.*

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class DashboardSyncCardStateRepositoryImplTest {

    private lateinit var dashboardSyncCardStateRepository: DashboardSyncCardStateRepositoryImpl
    private val syncCardTestLiveData
        get() = dashboardSyncCardStateRepository.syncCardStateLiveData

    @MockK lateinit var eventSyncManager: EventSyncManager
    @MockK lateinit var deviceManager: DeviceManager
    @MockK lateinit var preferencesManager: PreferencesManager
    @MockK lateinit var downSyncScopeRepository: EventDownSyncScopeRepository
    @MockK lateinit var cacheSync: EventSyncCache
    @MockK lateinit var timeHelper: TimeHelper

    private lateinit var isConnectedUpdates: MutableLiveData<Boolean>
    private lateinit var syncStateLiveData: MutableLiveData<EventSyncState>
    private val syncId = UUID.randomUUID().toString()
    private val lastSyncTime = Date()

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        isConnectedUpdates = MutableLiveData()
        isConnectedUpdates.value = true
        syncStateLiveData = MutableLiveData()
        every { deviceManager.isConnectedLiveData } returns isConnectedUpdates
        every { eventSyncManager.getLastSyncState() } returns syncStateLiveData
        coEvery { downSyncScopeRepository.getDownSyncScope() } returns DefaultTestConstants.projectDownSyncScope
        every { preferencesManager.selectedModules } returns emptySet()
        every { cacheSync.readLastSuccessfulSyncTime() } returns lastSyncTime
        every { eventSyncManager.hasSyncEverRunBefore() } returns true

        isConnectedUpdates.value = true
        dashboardSyncCardStateRepository = createRepository()
    }

    @Test
    fun deviceIsOffline_syncStateShouldBeSyncOffline() = runBlockingTest {
        isConnectedUpdates.value = false

        dashboardSyncCardStateRepository.syncIfRequired()
        val tester = syncCardTestLiveData.testObserver()

        assertThat(tester.observedValues.last()).isEqualTo(SyncOffline(lastSyncTime))
    }

    @Test
    fun deviceIsOnline_syncStateShouldBeConnecting() = runBlockingTest {
        isConnectedUpdates.value = true

        dashboardSyncCardStateRepository.syncIfRequired()
        val tester = syncCardTestLiveData.testObserver()

        assertThat(tester.observedValues.last()).isEqualTo(SyncConnecting(lastSyncTime, 0, null))
    }

    @Test
    fun downSyncSettingIsOnAndModulesEmpty_syncStateShouldBeSelectModules() = runBlockingTest {
        every { preferencesManager.selectedModules } returns emptySet()
        coEvery { downSyncScopeRepository.getDownSyncScope() } returns DefaultTestConstants.modulesDownSyncScope
        every { preferencesManager.eventDownSyncSetting } returns EventDownSyncSetting.ON

        dashboardSyncCardStateRepository.syncIfRequired()
        val tester = syncCardTestLiveData.testObserver()

        assertThat(tester.observedValues.last()).isEqualTo(SyncNoModules(lastSyncTime))
    }

    @Test
    fun downSyncSettingIsExtraAndModulesEmpty_syncStateShouldBeSelectModules() = runBlockingTest {
        every { preferencesManager.selectedModules } returns emptySet()
        coEvery { downSyncScopeRepository.getDownSyncScope() } returns DefaultTestConstants.modulesDownSyncScope
        every { preferencesManager.eventDownSyncSetting } returns EventDownSyncSetting.EXTRA

        dashboardSyncCardStateRepository.syncIfRequired()
        val tester = syncCardTestLiveData.testObserver()

        assertThat(tester.observedValues.last()).isEqualTo(SyncNoModules(lastSyncTime))
    }

    @Test
    fun downSyncSettingIsOffAndModulesEmpty_syncStateShouldBeConnecting() = runBlockingTest {
        every { preferencesManager.selectedModules } returns emptySet()
        coEvery { downSyncScopeRepository.getDownSyncScope() } returns DefaultTestConstants.modulesDownSyncScope
        every { preferencesManager.eventDownSyncSetting } returns EventDownSyncSetting.OFF

        dashboardSyncCardStateRepository.syncIfRequired()
        val tester = syncCardTestLiveData.testObserver()

        assertThat(tester.observedValues.last()).isEqualTo(SyncConnecting(lastSyncTime, 0, null))
    }

    @Test
    fun modulesSelectedWithSyncByModule_syncStateShouldBeConnecting() = runBlockingTest {
        every { preferencesManager.selectedModules } returns setOf(DefaultTestConstants.DEFAULT_MODULE_ID)
        coEvery { downSyncScopeRepository.getDownSyncScope() } returns DefaultTestConstants.modulesDownSyncScope

        dashboardSyncCardStateRepository.syncIfRequired()
        val tester = syncCardTestLiveData.testObserver()

        assertThat(tester.observedValues.last()).isEqualTo(SyncConnecting(lastSyncTime, 0, null))
    }

    @Test
    fun noModulesSelectedWithSyncByProject_syncStateShouldBeConnecting() = runBlockingTest {
        every { preferencesManager.selectedModules } returns emptySet()
        coEvery { downSyncScopeRepository.getDownSyncScope() } returns DefaultTestConstants.projectDownSyncScope

        dashboardSyncCardStateRepository.syncIfRequired()
        val tester = syncCardTestLiveData.testObserver()

        assertThat(tester.observedValues.last()).isEqualTo(SyncConnecting(lastSyncTime, 0, null))
    }

    @Test
    fun syncWorkersCompleted_syncStateShouldBeCompleted() = runBlockingTest {
        syncStateLiveData.value = EventSyncState(syncId, 10, 10,
            listOf(SyncWorkerInfo(DOWN_COUNTER, Succeeded)), listOf(SyncWorkerInfo(UP_COUNTER, Succeeded)))

        dashboardSyncCardStateRepository.syncIfRequired()
        val tester = syncCardTestLiveData.testObserver()

        assertThat(tester.observedValues.last()).isEqualTo(SyncComplete(lastSyncTime))
    }

    @Test
    fun syncWorkersEnqueued_syncStateShouldBeConnecting() = runBlockingTest {
        syncStateLiveData.value = EventSyncState(syncId, 0, null,
            listOf(SyncWorkerInfo(DOWN_COUNTER, Enqueued)), listOf(SyncWorkerInfo(UP_COUNTER, Succeeded)))

        dashboardSyncCardStateRepository.syncIfRequired()
        val tester = syncCardTestLiveData.testObserver()

        assertThat(tester.observedValues.last()).isEqualTo(SyncConnecting(lastSyncTime, 0, null))
    }

    @Test
    fun syncWorkersFailedForNetworkIssues_syncStateShouldBeTryAgain() = runBlockingTest {
        syncStateLiveData.value = EventSyncState(syncId, 0, null,
            listOf(SyncWorkerInfo(DOWN_COUNTER, Failed(false))), listOf(SyncWorkerInfo(UP_COUNTER, Succeeded)))

        dashboardSyncCardStateRepository.syncIfRequired()
        val tester = syncCardTestLiveData.testObserver()

        assertThat(tester.observedValues.last()).isEqualTo(SyncTryAgain(lastSyncTime))
    }

    @Test
    fun syncWorkersFailedBecauseCloudIntegration_syncStateShouldBeFailed() = runBlockingTest {
        syncStateLiveData.value = EventSyncState(syncId, 0, null,
            listOf(SyncWorkerInfo(DOWN_COUNTER, Failed(true))), listOf(SyncWorkerInfo(UP_COUNTER, Succeeded)))

        dashboardSyncCardStateRepository.syncIfRequired()
        val tester = syncCardTestLiveData.testObserver()

        assertThat(tester.observedValues.last()).isEqualTo(SyncFailed(lastSyncTime))
    }

    @Test
    fun noHistoryAboutSync_syncStateShouldBeDefault() = runBlockingTest {
        syncStateLiveData.value =
            EventSyncState(syncId, 0, null, emptyList(), emptyList())

        dashboardSyncCardStateRepository.syncIfRequired()
        val tester = syncCardTestLiveData.testObserver()

        assertThat(tester.observedValues.last()).isEqualTo(SyncDefault(lastSyncTime))
    }

    @Test
    fun syncSucceedInBackgroundLongTimeAgo_syncShouldBeTriggered() = runBlockingTest {
        dashboardSyncCardStateRepository = createRepository(TestTimeHelperImpl())
        syncStateLiveData.value = EventSyncState(syncId, 10, 10,
            listOf(SyncWorkerInfo(DOWN_COUNTER, Succeeded)), listOf(SyncWorkerInfo(UP_COUNTER, Succeeded)))
        every { cacheSync.readLastSuccessfulSyncTime() } returns Date(System.currentTimeMillis() - MAX_TIME_BEFORE_SYNC_AGAIN - 1)

        dashboardSyncCardStateRepository.syncIfRequired()
        syncCardTestLiveData.testObserver()

        verify { eventSyncManager.sync() }
    }

    @Test
    fun syncSucceedInBackgroundNotTooLongAgo_syncShouldNotBeTriggered() = runBlockingTest {
        dashboardSyncCardStateRepository = createRepository(timeHelper)
        syncStateLiveData.value = EventSyncState(syncId, 10, 10,
            listOf(SyncWorkerInfo(DOWN_COUNTER, Succeeded)), listOf(SyncWorkerInfo(UP_COUNTER, Succeeded)))
        every { cacheSync.readLastSuccessfulSyncTime() } returns Date()

        dashboardSyncCardStateRepository.syncIfRequired()
        syncCardTestLiveData.testObserver()

        verify(exactly = 0) { eventSyncManager.sync() }
    }

    @Test
    fun syncFinishedButOnlyRecently_syncIfRequired_shouldNotLaunchTheSync() = runBlockingTest {
        every { timeHelper.msBetweenNowAndTime(any()) } returns MAX_TIME_BEFORE_SYNC_AGAIN - 1
        syncStateLiveData.value = EventSyncState(syncId, 0, null,
            listOf(SyncWorkerInfo(DOWN_COUNTER, Succeeded)), listOf(SyncWorkerInfo(UP_COUNTER, Succeeded)))

        dashboardSyncCardStateRepository.syncIfRequired()
        dashboardSyncCardStateRepository.syncIfRequired()

        verify(exactly = 0) { eventSyncManager.sync() }
    }

    @Test
    fun noHistoryAboutSync_syncIfRequired_shouldNotLaunchTheSync() = runBlockingTest {
        dashboardSyncCardStateRepository.syncIfRequired()

        verify(exactly = 0) { eventSyncManager.sync() }
    }

    @Test
    fun syncWorkersFailedDueToTimeout_syncStateShouldBeFailed() = runBlockingTest {
        syncStateLiveData.value = EventSyncState(syncId, 0, null,
            listOf(SyncWorkerInfo(DOWN_COUNTER, Failed(true))), listOf(SyncWorkerInfo(UP_COUNTER, Succeeded)))

        dashboardSyncCardStateRepository.syncIfRequired()
        val tester = syncCardTestLiveData.testObserver()

        assertThat(tester.observedValues.last()).isEqualTo(SyncFailed(lastSyncTime))
    }

    @Test
    fun syncWorkersAreRunning_syncStateShouldBeInProgress() = runBlockingTest {
        val progress = 10
        val total = 100
        syncStateLiveData.value = EventSyncState(syncId, progress, total,
            listOf(SyncWorkerInfo(DOWN_COUNTER, Running)), listOf(SyncWorkerInfo(UP_COUNTER, Running)))

        dashboardSyncCardStateRepository.syncIfRequired()
        val tester = syncCardTestLiveData.testObserver()

        assertThat(tester.observedValues.last()).isEqualTo(SyncProgress(lastSyncTime, progress, total))
    }


    private fun createRepository(specificTimeHelper: TimeHelper = timeHelper) =
        DashboardSyncCardStateRepositoryImpl(eventSyncManager, deviceManager, preferencesManager, downSyncScopeRepository, cacheSync, specificTimeHelper)

}

