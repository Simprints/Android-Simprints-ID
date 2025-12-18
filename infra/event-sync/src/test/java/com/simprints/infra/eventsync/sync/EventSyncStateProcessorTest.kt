package com.simprints.infra.eventsync.sync

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.*
import androidx.work.WorkInfo
import androidx.work.WorkInfo.State.FAILED
import androidx.work.WorkInfo.State.SUCCEEDED
import androidx.work.workDataOf
import com.simprints.infra.eventsync.status.models.EventSyncWorkerState
import com.simprints.infra.eventsync.status.models.EventSyncWorkerType.Companion.tagForType
import com.simprints.infra.eventsync.status.models.EventSyncWorkerType.START_SYNC_REPORTER
import com.simprints.infra.eventsync.sync.common.EventSyncCache
import com.simprints.infra.eventsync.sync.common.SyncWorkersInfoProvider
import com.simprints.infra.eventsync.sync.common.TAG_MASTER_SYNC_ID
import com.simprints.infra.eventsync.sync.common.TAG_SCHEDULED_AT
import com.simprints.infra.eventsync.sync.common.TAG_SUBJECTS_DOWN_SYNC_ALL_WORKERS
import com.simprints.infra.eventsync.sync.common.TAG_SUBJECTS_SYNC_ALL_WORKERS
import com.simprints.infra.eventsync.sync.master.EventStartSyncReporterWorker.Companion.SYNC_ID_STARTED
import io.mockk.*
import io.mockk.impl.annotations.*
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.timeout
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date
import kotlin.time.Duration.Companion.seconds

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

    private var startSyncReporterWorker = MutableSharedFlow<List<WorkInfo>>(replay = 1)
    private var syncWorkersFlow = MutableSharedFlow<List<WorkInfo>>(replay = 1)

    private lateinit var eventSyncStateProcessor: EventSyncStateProcessor

    @RelaxedMockK
    lateinit var syncWorkersInfoProvider: SyncWorkersInfoProvider

    @RelaxedMockK
    lateinit var eventSyncCache: EventSyncCache

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        eventSyncStateProcessor = EventSyncStateProcessor(eventSyncCache, syncWorkersInfoProvider)
        mockDependencies()
    }

    @Test
    fun processor_masterWorkerCompletes_shouldExtractTheUniqueSyncId() = runTest {
        startSyncReporterWorker.emit(successfulMasterWorkers)
        syncWorkersFlow.emit(createWorkInfosHistoryForSuccessfulSync())

        eventSyncStateProcessor.getLastSyncState().first()

        verify { syncWorkersInfoProvider.getSyncWorkerInfos(UNIQUE_SYNC_ID) }
    }

    @Test(expected = TimeoutCancellationException::class)
    fun processor_masterWorkerFails_shouldNotExtractTheUniqueSyncId() = runTest {
        startSyncReporterWorker.emit(failedMasterWorkers)

        eventSyncStateProcessor.getLastSyncState().timeout(1.seconds).firstOrNull()

        // flow will never complete since it the worker flow is not executed
    }

    @Test
    fun processor_allWorkersSucceed_shouldSyncStateBeSuccess() = runTest {
        startSyncReporterWorker.emit(successfulMasterWorkers)
        syncWorkersFlow.emit(createWorkInfosHistoryForSuccessfulSync())

        val syncStates = eventSyncStateProcessor.getLastSyncState().first()

        syncStates.assertSuccessfulSyncState()
    }

    @Test
    fun processor_oneWorkerStillRunning_shouldSyncStateBeRunning() = runTest {
        startSyncReporterWorker.emit(successfulMasterWorkers)
        syncWorkersFlow.emit(createWorkInfosHistoryForRunningSync())

        val syncStates = eventSyncStateProcessor.getLastSyncState().first()
        syncStates.assertRunningSyncState()
    }

    @Test
    fun processor_oneWorkerRunning_shouldIgnoreCount() = runTest {
        coEvery { eventSyncCache.shouldIgnoreMax() } returns true

        startSyncReporterWorker.emit(successfulMasterWorkers)
        syncWorkersFlow.emit(createWorkInfosHistoryForRunningSync())

        val syncStates = eventSyncStateProcessor.getLastSyncState().first()
        syncStates.assertRunningSyncStateWithoutProgress()
    }

    @Test
    fun processor_oneWorkerFailed_shouldSyncStateBeFail() = runTest {
        startSyncReporterWorker.emit(successfulMasterWorkers)
        syncWorkersFlow.emit(createWorkInfosHistoryForFailingSync())

        val syncStates = eventSyncStateProcessor.getLastSyncState().first()

        syncStates.assertFailingSyncState()
    }

    @Test
    fun processor_oneWorkerEnqueued_shouldSyncStateBeConnecting() = runTest {
        startSyncReporterWorker.emit(successfulMasterWorkers)
        syncWorkersFlow.emit(createWorkInfosHistoryForConnectingSync())

        val syncStates = eventSyncStateProcessor.getLastSyncState().first()

        syncStates.assertConnectingSyncState()
    }

    @Test
    fun getLastSyncState_shouldMapCorrectlyTheBackendMaintenanceFailed() = runTest {
        startSyncReporterWorker.emit(successfulMasterWorkers)
        syncWorkersFlow.emit(createWorkInfosHistoryForFailingSyncDueBackendMaintenanceError())

        val syncStates = eventSyncStateProcessor.getLastSyncState().first()

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
    fun getLastSyncState_shouldMapCorrectlyTheTooManyRequestsFailed() = runTest {
        startSyncReporterWorker.emit(successfulMasterWorkers)
        syncWorkersFlow.emit(createWorkInfosHistoryForFailingSyncDueTooManyRequestsError())

        val syncStates = eventSyncStateProcessor.getLastSyncState().first()

        val expectedState = EventSyncWorkerState.Failed(failedBecauseTooManyRequest = true)
        syncStates.downSyncWorkersInfo
            .first()
            .state
            .assertEqualToFailedState(expectedState)
    }

    @Test
    fun getLastSyncState_shouldMapCorrectlyTheCloudIntegrationFailed() = runTest {
        startSyncReporterWorker.emit(successfulMasterWorkers)
        syncWorkersFlow.emit(createWorkInfosHistoryForFailingSyncDueCloudIntegrationError())

        val syncStates = eventSyncStateProcessor.getLastSyncState().first()

        val expectedState = EventSyncWorkerState.Failed(failedBecauseCloudIntegration = true)
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
        every { syncWorkersInfoProvider.getStartSyncReporters() } returns startSyncReporterWorker
        every { syncWorkersInfoProvider.getSyncWorkerInfos(any()) } returns syncWorkersFlow
        coEvery { eventSyncCache.readProgress(any()) } returns 0
    }
}
