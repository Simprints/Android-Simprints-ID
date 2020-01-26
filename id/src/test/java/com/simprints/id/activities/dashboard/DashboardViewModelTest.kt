package com.simprints.id.activities.dashboard

import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.id.activities.dashboard.DashboardViewModel.Companion.TIME_BETWEEN_TWO_SYNCS
import com.simprints.id.activities.dashboard.cards.sync.DashboardSyncCardState.*
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODULE_ID
import com.simprints.id.commontesttools.DefaultTestConstants.moduleSyncScope
import com.simprints.id.commontesttools.DefaultTestConstants.projectSyncScope
import com.simprints.id.data.db.people_sync.down.PeopleDownSyncScopeRepository
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncManager
import com.simprints.id.services.scheduledSync.people.master.internal.PeopleSyncCache
import com.simprints.id.services.scheduledSync.people.master.models.PeopleSyncState
import com.simprints.id.services.scheduledSync.people.master.models.PeopleSyncState.SyncWorkerInfo
import com.simprints.id.services.scheduledSync.people.master.models.PeopleSyncWorkerState.*
import com.simprints.id.services.scheduledSync.people.master.models.PeopleSyncWorkerType.DOWN_COUNTER
import com.simprints.id.services.scheduledSync.people.master.models.PeopleSyncWorkerType.UP_COUNTER
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.device.DeviceManager
import com.simprints.testtools.common.livedata.testObserver
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class DashboardViewModelTest {

    private lateinit var dashboardViewModel: DashboardViewModel
    @MockK lateinit var peopleSyncManager: PeopleSyncManager
    @MockK lateinit var deviceManager: DeviceManager
    @MockK lateinit var preferencesManager: PreferencesManager
    @MockK lateinit var syncScopeRepository: PeopleDownSyncScopeRepository
    @MockK lateinit var cacheSync: PeopleSyncCache
    @MockK lateinit var timeHelper: TimeHelper

    private lateinit var isConnectedUpdates: MutableLiveData<Boolean>
    private lateinit var syncStateLiveData: MutableLiveData<PeopleSyncState>
    private val syncId = UUID.randomUUID().toString()
    private val lastSyncTime = Date()

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        isConnectedUpdates = MutableLiveData()
        syncStateLiveData = MutableLiveData()
        every { deviceManager.isConnectedUpdates } returns isConnectedUpdates
        every { peopleSyncManager.getLastSyncState() } returns syncStateLiveData
        every { syncScopeRepository.getDownSyncScope() } returns projectSyncScope
        every { preferencesManager.selectedModules } returns emptySet()
        every { cacheSync.readLastSuccessfulSyncTime() } returns lastSyncTime

        isConnectedUpdates.value = true
        dashboardViewModel = createViewModel()
    }

    @Test
    fun deviceIsOffline_syncStateShouldBeSyncOffline() {
        isConnectedUpdates.value = false

        val tester = dashboardViewModel.syncCardState.testObserver()

        assertThat(tester.observedValues.last()).isEqualTo(SyncOffline(lastSyncTime))
    }

    @Test
    fun deviceIsOnline_syncStateShouldBeDefault() {
        isConnectedUpdates.value = true

        val tester = dashboardViewModel.syncCardState.testObserver()

        assertThat(tester.observedValues.last()).isEqualTo(SyncDefault(lastSyncTime))
    }

    @Test
    fun noModulesSelectedWithSyncByModule_syncStateShouldBeSelectModules() {
        every { preferencesManager.selectedModules } returns emptySet()
        every { syncScopeRepository.getDownSyncScope() } returns moduleSyncScope

        val tester = dashboardViewModel.syncCardState.testObserver()

        assertThat(tester.observedValues.last()).isEqualTo(SyncNoModules(lastSyncTime))
    }

    @Test
    fun modulesSelectedWithSyncByModule_syncStateShouldBeDefault() {
        every { preferencesManager.selectedModules } returns setOf(DEFAULT_MODULE_ID)
        every { syncScopeRepository.getDownSyncScope() } returns moduleSyncScope

        val tester = dashboardViewModel.syncCardState.testObserver()

        assertThat(tester.observedValues.last()).isEqualTo(SyncDefault(lastSyncTime))
    }

    @Test
    fun noModulesSelectedWithSyncByProject_syncStateShouldBeDefault() {
        every { preferencesManager.selectedModules } returns emptySet()
        every { syncScopeRepository.getDownSyncScope() } returns projectSyncScope

        val tester = dashboardViewModel.syncCardState.testObserver()

        assertThat(tester.observedValues.last()).isEqualTo(SyncDefault(lastSyncTime))
    }

    @Test
    fun syncWorkersCompleted_syncStateShouldBeCompleted() {
        syncStateLiveData.value = PeopleSyncState(syncId, 10, 10, listOf(SyncWorkerInfo(DOWN_COUNTER, Succeeded)), listOf(SyncWorkerInfo(UP_COUNTER, Succeeded)))

        val tester = dashboardViewModel.syncCardState.testObserver()

        assertThat(tester.observedValues.last()).isEqualTo(SyncComplete(lastSyncTime))
    }

    @Test
    fun syncWorkersEnqueued_syncStateShouldBeConnecting() {
        syncStateLiveData.value = PeopleSyncState(syncId, 0, null, listOf(SyncWorkerInfo(DOWN_COUNTER, Enqueued)), listOf(SyncWorkerInfo(UP_COUNTER, Succeeded)))

        val tester = dashboardViewModel.syncCardState.testObserver()

        assertThat(tester.observedValues.last()).isEqualTo(SyncConnecting(lastSyncTime, 0, null))
    }

    @Test
    fun syncWorkersFailedBecauseCloudIntegration_syncStateShouldBeTryAgain() {
        dashboardViewModel.hasSyncEverRun = true
        syncStateLiveData.value = PeopleSyncState(syncId, 0, null, listOf(SyncWorkerInfo(DOWN_COUNTER, Failed(false))), listOf(SyncWorkerInfo(UP_COUNTER, Succeeded)))

        val tester = dashboardViewModel.syncCardState.testObserver()

        assertThat(tester.observedValues.last()).isEqualTo(SyncTryAgain(lastSyncTime))
    }

    @Test
    fun noHistoryAboutSync_syncStateShouldBeDefault() {
        syncStateLiveData.value = PeopleSyncState(syncId, 0, null, listOf(SyncWorkerInfo(DOWN_COUNTER, Failed(false))), listOf(SyncWorkerInfo(UP_COUNTER, Succeeded)))

        val tester = dashboardViewModel.syncCardState.testObserver()

        assertThat(tester.observedValues).isEmpty()
        verify { peopleSyncManager.sync() }
    }

//    @Test
//    fun syncFailedAndNeverRunInTheDashboard_shouldNotEmitStateAndLaunchTheSync() {
//        syncStateLiveData.value = PeopleSyncState(syncId, 0, null, listOf(), listOf())
//
//        val tester = dashboardViewModel.syncCardState.testObserver()
//
//        assertThat(tester.observedValues.last()).isEqualTo(SyncDefault(lastSyncTime))
//    }

    @Test
    fun syncCompletedButLongTimeAgo_shouldNotEmitAStateAndLaunchTheSync() {
        every { timeHelper.msBetweenNowAndTime(any()) } returns TIME_BETWEEN_TWO_SYNCS + 1
        syncStateLiveData.value = PeopleSyncState(syncId, 0, null, listOf(SyncWorkerInfo(DOWN_COUNTER, Succeeded)), listOf(SyncWorkerInfo(UP_COUNTER, Succeeded)))

        val tester = dashboardViewModel.syncCardState.testObserver()

        assertThat(tester.observedValues).isEmpty()
        verify { peopleSyncManager.sync() }
    }

    @Test
    fun syncFinishedButLongTimeAgo_syncIfRequired_shouldLaunchTheSync() {
        every { timeHelper.msBetweenNowAndTime(any()) } returns TIME_BETWEEN_TWO_SYNCS + 1
        syncStateLiveData.value = PeopleSyncState(syncId, 0, null, listOf(SyncWorkerInfo(DOWN_COUNTER, Succeeded)), listOf(SyncWorkerInfo(UP_COUNTER, Succeeded)))

        dashboardViewModel.syncIfRequired()

        verify(exactly = 0) { peopleSyncManager.sync() }
    }


    @Test
    fun syncFinishedButOnlyRecently_syncIfRequired_shouldNotLaunchTheSync() {
        every { timeHelper.msBetweenNowAndTime(any()) } returns TIME_BETWEEN_TWO_SYNCS - 1
        syncStateLiveData.value = PeopleSyncState(syncId, 0, null, listOf(SyncWorkerInfo(DOWN_COUNTER, Succeeded)), listOf(SyncWorkerInfo(UP_COUNTER, Succeeded)))

        dashboardViewModel.syncIfRequired()

        verify(exactly = 0) { peopleSyncManager.sync() }
    }

    @Test
    fun noHistoryAboutSync_syncIfRequired_shouldNotLaunchTheSync() {
        dashboardViewModel.syncIfRequired()

        verify(exactly = 0) { peopleSyncManager.sync() }
    }

    @Test
    fun syncWorkersFailedDueToTimeout_syncStateShouldBeFailed() {
        dashboardViewModel.hasSyncEverRun = true
        syncStateLiveData.value = PeopleSyncState(syncId, 0, null, listOf(SyncWorkerInfo(DOWN_COUNTER, Failed(true))), listOf(SyncWorkerInfo(UP_COUNTER, Succeeded)))

        val tester = dashboardViewModel.syncCardState.testObserver()

        assertThat(tester.observedValues.last()).isEqualTo(SyncFailed(lastSyncTime))
    }

    @Test
    fun syncWorkersAreRunning_syncStateShouldBeInProgress() {
        val progress = 10
        val total = 100
        syncStateLiveData.value = PeopleSyncState(syncId, progress, total, listOf(SyncWorkerInfo(DOWN_COUNTER, Running)), listOf(SyncWorkerInfo(UP_COUNTER, Running)))

        val tester = dashboardViewModel.syncCardState.testObserver()

        assertThat(tester.observedValues.last()).isEqualTo(SyncProgress(lastSyncTime, progress, total))
    }


    private fun createViewModel() =
        DashboardViewModel(peopleSyncManager, deviceManager, preferencesManager, syncScopeRepository, cacheSync, timeHelper)

}
