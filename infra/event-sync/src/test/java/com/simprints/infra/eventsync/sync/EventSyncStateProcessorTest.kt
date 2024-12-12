package com.simprints.infra.eventsync.sync

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.WorkInfo
import androidx.work.WorkInfo.State.FAILED
import androidx.work.WorkInfo.State.SUCCEEDED
import androidx.work.workDataOf
import com.simprints.infra.eventsync.status.models.EventSyncWorkerState
import com.simprints.infra.eventsync.status.models.EventSyncWorkerType.Companion.tagForType
import com.simprints.infra.eventsync.status.models.EventSyncWorkerType.START_SYNC_REPORTER
import com.simprints.infra.eventsync.sync.common.EventSyncCache
import com.simprints.infra.eventsync.sync.common.SyncWorkersLiveDataProvider
import com.simprints.infra.eventsync.sync.common.TAG_MASTER_SYNC_ID
import com.simprints.infra.eventsync.sync.common.TAG_SCHEDULED_AT
import com.simprints.infra.eventsync.sync.common.TAG_SUBJECTS_DOWN_SYNC_ALL_WORKERS
import com.simprints.infra.eventsync.sync.common.TAG_SUBJECTS_SYNC_ALL_WORKERS
import com.simprints.infra.eventsync.sync.master.EventStartSyncReporterWorker.Companion.SYNC_ID_STARTED
import com.simprints.testtools.common.livedata.getOrAwaitValue
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

@RunWith(AndroidJUnit4::class)
internal class EventSyncStateProcessorTest {
    companion object {
        const val UNIQUE_SYNC_ID = "UNIQUE_SYNC_ID"
        const val UNIQUE_DOWN_SYNC_ID = "UNIQUE_DOWN_SYNC_ID"
        const val UNIQUE_UP_SYNC_ID = "UNIQUE_UP_SYNC_ID"

        const val DOWNLOADED = 100
        const val TO_DOWNLOAD = 100
        const val UPLOADED = 10
        const val TO_UPLOAD = 10
    }

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private val successfulMasterWorkers: List<WorkInfo> =
        listOf(
            createStartSyncReporterWorker(uniqueMasterSyncId = "${UNIQUE_SYNC_ID}_older"),
            createStartSyncReporterWorker(uniqueMasterSyncId = UNIQUE_SYNC_ID),
        )

    private val failedMasterWorkers: List<WorkInfo> =
        listOf(createWorkInfo(FAILED))

    private var startSyncReporterWorker = MutableLiveData<List<WorkInfo>>()
    private var syncWorkersLiveData = MutableLiveData<List<WorkInfo>>()

    private lateinit var eventSyncStateProcessor: EventSyncStateProcessor

    @RelaxedMockK
    lateinit var syncWorkersLiveDataProvider: SyncWorkersLiveDataProvider

    @RelaxedMockK
    lateinit var eventSyncCache: EventSyncCache

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        eventSyncStateProcessor =
            EventSyncStateProcessor(eventSyncCache, syncWorkersLiveDataProvider)
        mockDependencies()
    }

    @Test
    fun processor_masterWorkerCompletes_shouldExtractTheUniqueSyncId() = runTest(
        UnconfinedTestDispatcher(),
    ) {
        startSyncReporterWorker.value = successfulMasterWorkers
        syncWorkersLiveData.value = createWorkInfosHistoryForSuccessfulSync()

        eventSyncStateProcessor.getLastSyncState().getOrAwaitValue()

        verify {
            syncWorkersLiveDataProvider.getSyncWorkersLiveData(UNIQUE_SYNC_ID)
        }
    }

    @Test
    fun processor_masterWorkerFails_shouldNotExtractTheUniqueSyncId() = runTest(
        UnconfinedTestDispatcher(),
    ) {
        startSyncReporterWorker.value = failedMasterWorkers

        eventSyncStateProcessor.getLastSyncState()

        verify(exactly = 0) { syncWorkersLiveDataProvider.getSyncWorkersLiveData(UNIQUE_SYNC_ID) }
    }

    @Test
    fun processor_allWorkersSucceed_shouldSyncStateBeSuccess() = runTest(UnconfinedTestDispatcher()) {
        startSyncReporterWorker.value = successfulMasterWorkers
        syncWorkersLiveData.value = createWorkInfosHistoryForSuccessfulSync()

        val syncStates = eventSyncStateProcessor.getLastSyncState().getOrAwaitValue()

        syncStates.assertSuccessfulSyncState()
    }

    @Test
    fun processor_oneWorkerStillRunning_shouldSyncStateBeRunning() = runTest(UnconfinedTestDispatcher()) {
        startSyncReporterWorker.value = successfulMasterWorkers
        syncWorkersLiveData.value = createWorkInfosHistoryForRunningSync()

        val syncStates =
            eventSyncStateProcessor.getLastSyncState().getOrAwaitValue()
        syncStates.assertRunningSyncState()
    }

    @Test
    fun processor_oneWorkerRunning_shouldIgnoreCount() = runTest(UnconfinedTestDispatcher()) {
        coEvery { eventSyncCache.shouldIgnoreMax() } returns true

        startSyncReporterWorker.value = successfulMasterWorkers
        syncWorkersLiveData.value = createWorkInfosHistoryForRunningSync()

        val syncStates = eventSyncStateProcessor.getLastSyncState().getOrAwaitValue()
        syncStates.assertRunningSyncStateWithoutProgress()
    }

    @Test
    fun processor_oneWorkerFailed_shouldSyncStateBeFail() = runTest(UnconfinedTestDispatcher()) {
        startSyncReporterWorker.value = successfulMasterWorkers
        syncWorkersLiveData.value = createWorkInfosHistoryForFailingSync()

        val syncStates = eventSyncStateProcessor.getLastSyncState().getOrAwaitValue()

        syncStates.assertFailingSyncState()
    }

    @Test
    fun processor_oneWorkerEnqueued_shouldSyncStateBeConnecting() = runTest(UnconfinedTestDispatcher()) {
        startSyncReporterWorker.value = successfulMasterWorkers
        syncWorkersLiveData.value = createWorkInfosHistoryForConnectingSync()

        val syncStates =
            eventSyncStateProcessor.getLastSyncState().getOrAwaitValue()

        syncStates.assertConnectingSyncState()
    }

    @Test
    fun getLastSyncState_shouldMapCorrectlyTheBackendMaintenanceFailed() = runTest(UnconfinedTestDispatcher()) {
        startSyncReporterWorker.value = successfulMasterWorkers
        syncWorkersLiveData.value =
            createWorkInfosHistoryForFailingSyncDueBackendMaintenanceError()

        val syncStates = eventSyncStateProcessor.getLastSyncState().getOrAwaitValue()

        val expectedState = EventSyncWorkerState.Failed(
            failedBecauseBackendMaintenance = true,
            estimatedOutage = 6,
        )
        syncStates.downSyncWorkersInfo
            .first()
            .state
            .assertEqualToFailedState(expectedState)
    }

    @Test
    fun getLastSyncState_shouldMapCorrectlyTheTooManyRequestsFailed() = runTest(UnconfinedTestDispatcher()) {
        startSyncReporterWorker.value = successfulMasterWorkers
        syncWorkersLiveData.value =
            createWorkInfosHistoryForFailingSyncDueTooManyRequestsError()

        val syncStates =
            eventSyncStateProcessor.getLastSyncState().getOrAwaitValue()

        val expectedState = EventSyncWorkerState.Failed(
            failedBecauseTooManyRequest = true,
        )
        syncStates.downSyncWorkersInfo
            .first()
            .state
            .assertEqualToFailedState(expectedState)
    }

    @Test
    fun getLastSyncState_shouldMapCorrectlyTheCloudIntegrationFailed() = runTest(UnconfinedTestDispatcher()) {
        startSyncReporterWorker.value = successfulMasterWorkers
        syncWorkersLiveData.value =
            createWorkInfosHistoryForFailingSyncDueCloudIntegrationError()

        val syncStates =
            eventSyncStateProcessor.getLastSyncState().getOrAwaitValue()

        val expectedState = EventSyncWorkerState.Failed(
            failedBecauseCloudIntegration = true,
        )
        syncStates.downSyncWorkersInfo
            .first()
            .state
            .assertEqualToFailedState(expectedState)
    }

    private fun createStartSyncReporterWorker(
        state: WorkInfo.State = SUCCEEDED,
        uniqueMasterSyncId: String,
    ) = createWorkInfo(
        state,
        workDataOf(SYNC_ID_STARTED to uniqueMasterSyncId),
        setOf(
            "$TAG_SCHEDULED_AT${Date().time}",
            TAG_SUBJECTS_DOWN_SYNC_ALL_WORKERS,
            TAG_SUBJECTS_SYNC_ALL_WORKERS,
            "$TAG_MASTER_SYNC_ID$uniqueMasterSyncId",
        ) + listOf(tagForType(START_SYNC_REPORTER)),
    )

    private fun mockDependencies() {
        every { syncWorkersLiveDataProvider.getStartSyncReportersLiveData() } returns startSyncReporterWorker
        every { syncWorkersLiveDataProvider.getSyncWorkersLiveData(any()) } returns syncWorkersLiveData
        coEvery { eventSyncCache.readProgress(any()) } returns 0
    }
}
