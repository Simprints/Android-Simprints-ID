package com.simprints.id.activities.dashboard.cards.sync

import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.id.activities.dashboard.cards.sync.DashboardSyncCardState.*
import com.simprints.id.activities.dashboard.cards.sync.DashboardSyncCardStateRepositoryImpl.Companion.MAX_TIME_BEFORE_SYNC_AGAIN
import com.simprints.id.commontesttools.DefaultTestConstants
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
import com.simprints.id.tools.TimeHelperImpl
import com.simprints.id.tools.device.DeviceManager
import com.simprints.testtools.common.livedata.testObserver
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class DashboardSyncCardStateRepositoryImplTest {

    private lateinit var dashboardSyncCardStateRepository: DashboardSyncCardStateRepositoryImpl
    private val syncCardTestLiveData
        get() = dashboardSyncCardStateRepository.syncCardStateLiveData

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
        isConnectedUpdates.value = true
        syncStateLiveData = MutableLiveData()
        every { deviceManager.isConnectedLiveData } returns isConnectedUpdates
        every { peopleSyncManager.getLastSyncState() } returns syncStateLiveData
        every { syncScopeRepository.getDownSyncScope() } returns DefaultTestConstants.projectSyncScope
        every { preferencesManager.selectedModules } returns emptySet()
        every { cacheSync.readLastSuccessfulSyncTime() } returns lastSyncTime
        every { peopleSyncManager.hasSyncEverRunBefore() } returns true

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
    fun noModulesSelectedWithSyncByModule_syncStateShouldBeSelectModules() = runBlockingTest {
        every { preferencesManager.selectedModules } returns emptySet()
        every { syncScopeRepository.getDownSyncScope() } returns DefaultTestConstants.moduleSyncScope

        dashboardSyncCardStateRepository.syncIfRequired()
        val tester = syncCardTestLiveData.testObserver()

        assertThat(tester.observedValues.last()).isEqualTo(SyncNoModules(lastSyncTime))
    }

    @Test
    fun modulesSelectedWithSyncByModule_syncStateShouldBeConnecting() = runBlockingTest {
        every { preferencesManager.selectedModules } returns setOf(DefaultTestConstants.DEFAULT_MODULE_ID)
        every { syncScopeRepository.getDownSyncScope() } returns DefaultTestConstants.moduleSyncScope

        dashboardSyncCardStateRepository.syncIfRequired()
        val tester = syncCardTestLiveData.testObserver()

        assertThat(tester.observedValues.last()).isEqualTo(SyncConnecting(lastSyncTime, 0, null))
    }

    @Test
    fun noModulesSelectedWithSyncByProject_syncStateShouldBeConnecting() = runBlockingTest {
        every { preferencesManager.selectedModules } returns emptySet()
        every { syncScopeRepository.getDownSyncScope() } returns DefaultTestConstants.projectSyncScope

        dashboardSyncCardStateRepository.syncIfRequired()
        val tester = syncCardTestLiveData.testObserver()

        assertThat(tester.observedValues.last()).isEqualTo(SyncConnecting(lastSyncTime, 0, null))
    }

    @Test
    fun syncWorkersCompleted_syncStateShouldBeCompleted() = runBlockingTest {
        syncStateLiveData.value = PeopleSyncState(syncId, 10, 10,
            listOf(SyncWorkerInfo(DOWN_COUNTER, Succeeded)), listOf(SyncWorkerInfo(UP_COUNTER, Succeeded)))

        dashboardSyncCardStateRepository.syncIfRequired()
        val tester = syncCardTestLiveData.testObserver()

        assertThat(tester.observedValues.last()).isEqualTo(SyncComplete(lastSyncTime))
    }

    @Test
    fun syncWorkersEnqueued_syncStateShouldBeConnecting() = runBlockingTest {
        syncStateLiveData.value = PeopleSyncState(syncId, 0, null,
            listOf(SyncWorkerInfo(DOWN_COUNTER, Enqueued)), listOf(SyncWorkerInfo(UP_COUNTER, Succeeded)))

        dashboardSyncCardStateRepository.syncIfRequired()
        val tester = syncCardTestLiveData.testObserver()

        assertThat(tester.observedValues.last()).isEqualTo(SyncConnecting(lastSyncTime, 0, null))
    }

    @Test
    fun syncWorkersFailedForNetworkIssues_syncStateShouldBeTryAgain() = runBlockingTest {
        syncStateLiveData.value = PeopleSyncState(syncId, 0, null,
            listOf(SyncWorkerInfo(DOWN_COUNTER, Failed(false))), listOf(SyncWorkerInfo(UP_COUNTER, Succeeded)))

        dashboardSyncCardStateRepository.syncIfRequired()
        val tester = syncCardTestLiveData.testObserver()

        assertThat(tester.observedValues.last()).isEqualTo(SyncTryAgain(lastSyncTime))
    }

    @Test
    fun syncWorkersFailedBecauseCloudIntegration_syncStateShouldBeFailed() = runBlockingTest {
        syncStateLiveData.value = PeopleSyncState(syncId, 0, null,
            listOf(SyncWorkerInfo(DOWN_COUNTER, Failed(true))), listOf(SyncWorkerInfo(UP_COUNTER, Succeeded)))

        dashboardSyncCardStateRepository.syncIfRequired()
        val tester = syncCardTestLiveData.testObserver()

        assertThat(tester.observedValues.last()).isEqualTo(SyncFailed(lastSyncTime))
    }

    @Test
    fun noHistoryAboutSync_syncStateShouldBeDefault() = runBlockingTest {
        syncStateLiveData.value =
            PeopleSyncState(syncId, 0, null, emptyList(), emptyList())

        dashboardSyncCardStateRepository.syncIfRequired()
        val tester = syncCardTestLiveData.testObserver()

        assertThat(tester.observedValues.last()).isEqualTo(SyncDefault(lastSyncTime))
    }

    @Test
    fun syncSucceedInBackgroundLongTimeAgo_syncShouldBeTriggered() = runBlockingTest {
        dashboardSyncCardStateRepository = createRepository(TimeHelperImpl())
        syncStateLiveData.value = PeopleSyncState(syncId, 10, 10,
            listOf(SyncWorkerInfo(DOWN_COUNTER, Succeeded)), listOf(SyncWorkerInfo(UP_COUNTER, Succeeded)))
        every { cacheSync.readLastSuccessfulSyncTime() } returns Date(System.currentTimeMillis() - MAX_TIME_BEFORE_SYNC_AGAIN - 1)

        dashboardSyncCardStateRepository.syncIfRequired()
        syncCardTestLiveData.testObserver()

        verify { peopleSyncManager.sync() }
    }

    @Test
    fun syncSucceedInBackgroundNotTooLongAgo_syncShouldNotBeTriggered() = runBlockingTest {
        dashboardSyncCardStateRepository = createRepository(TimeHelperImpl())
        syncStateLiveData.value = PeopleSyncState(syncId, 10, 10,
            listOf(SyncWorkerInfo(DOWN_COUNTER, Succeeded)), listOf(SyncWorkerInfo(UP_COUNTER, Succeeded)))
        every { cacheSync.readLastSuccessfulSyncTime() } returns Date()

        dashboardSyncCardStateRepository.syncIfRequired()
        syncCardTestLiveData.testObserver()

        verify(exactly = 0) { peopleSyncManager.sync() }
    }

    @Test
    fun syncFinishedButOnlyRecently_syncIfRequired_shouldNotLaunchTheSync() = runBlockingTest {
        every { timeHelper.msBetweenNowAndTime(any()) } returns MAX_TIME_BEFORE_SYNC_AGAIN - 1
        syncStateLiveData.value = PeopleSyncState(syncId, 0, null,
            listOf(SyncWorkerInfo(DOWN_COUNTER, Succeeded)), listOf(SyncWorkerInfo(UP_COUNTER, Succeeded)))

        dashboardSyncCardStateRepository.syncIfRequired()
        dashboardSyncCardStateRepository.syncIfRequired()

        verify(exactly = 0) { peopleSyncManager.sync() }
    }

    @Test
    fun noHistoryAboutSync_syncIfRequired_shouldNotLaunchTheSync() = runBlockingTest {
        dashboardSyncCardStateRepository.syncIfRequired()

        verify(exactly = 0) { peopleSyncManager.sync() }
    }

    @Test
    fun syncWorkersFailedDueToTimeout_syncStateShouldBeFailed() = runBlockingTest {
        syncStateLiveData.value = PeopleSyncState(syncId, 0, null,
            listOf(SyncWorkerInfo(DOWN_COUNTER, Failed(true))), listOf(SyncWorkerInfo(UP_COUNTER, Succeeded)))

        dashboardSyncCardStateRepository.syncIfRequired()
        val tester = syncCardTestLiveData.testObserver()

        assertThat(tester.observedValues.last()).isEqualTo(SyncFailed(lastSyncTime))
    }

    @Test
    fun syncWorkersAreRunning_syncStateShouldBeInProgress() = runBlockingTest {
        val progress = 10
        val total = 100
        syncStateLiveData.value = PeopleSyncState(syncId, progress, total,
            listOf(SyncWorkerInfo(DOWN_COUNTER, Running)), listOf(SyncWorkerInfo(UP_COUNTER, Running)))

        dashboardSyncCardStateRepository.syncIfRequired()
        val tester = syncCardTestLiveData.testObserver()

        assertThat(tester.observedValues.last()).isEqualTo(SyncProgress(lastSyncTime, progress, total))
    }


    private fun createRepository(specificTimeHelper: TimeHelper = timeHelper) =
        DashboardSyncCardStateRepositoryImpl(peopleSyncManager, deviceManager, preferencesManager, syncScopeRepository, cacheSync, specificTimeHelper)

}

