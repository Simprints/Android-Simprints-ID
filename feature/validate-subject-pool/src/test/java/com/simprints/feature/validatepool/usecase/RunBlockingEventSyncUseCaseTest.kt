package com.simprints.feature.validatepool.usecase

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.simprints.infra.eventsync.status.models.EventSyncState
import com.simprints.infra.eventsync.status.models.EventSyncWorkerState
import com.simprints.infra.eventsync.status.models.EventSyncWorkerType
import com.simprints.infra.sync.ImageSyncStatus
import com.simprints.infra.sync.SyncCommands
import com.simprints.infra.sync.SyncOrchestrator
import com.simprints.infra.sync.SyncResponse
import com.simprints.infra.sync.SyncStatus
import com.simprints.infra.sync.usecase.SyncUseCase
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class RunBlockingEventSyncUseCaseTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var sync: SyncUseCase

    @MockK
    private lateinit var syncOrchestrator: SyncOrchestrator

    private lateinit var usecase: RunBlockingEventSyncUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        coJustRun { syncOrchestrator.startEventSync(any()) }

        usecase = RunBlockingEventSyncUseCase(
            sync,
            syncOrchestrator,
        )
    }

    @Test
    fun `finishes execution when sync reporters are finished`() = runTest {
        val syncFlow = MutableStateFlow(createSyncStatus("oldSync", EventSyncWorkerState.Succeeded))
        setUpSync(syncFlow)

        launch { usecase.invoke() }
        testScheduler.advanceUntilIdle()

        syncFlow.value = createSyncStatus("sync", EventSyncWorkerState.Succeeded)
        testScheduler.advanceUntilIdle()

        coVerify { syncOrchestrator.startEventSync(any()) }
        verify(exactly = 2) { sync.invoke(SyncCommands.ObserveOnly) }
    }

    @Test
    fun `finishes execution when sync reporters have failed`() = runTest {
        val syncFlow = MutableStateFlow(createSyncStatus("oldSync", EventSyncWorkerState.Succeeded))
        setUpSync(syncFlow)

        launch { usecase.invoke() }
        testScheduler.advanceUntilIdle()

        syncFlow.value = createSyncStatus("sync", EventSyncWorkerState.Failed())
        testScheduler.advanceUntilIdle()

        coVerify { syncOrchestrator.startEventSync(any()) }
        verify(exactly = 2) { sync.invoke(SyncCommands.ObserveOnly) }
    }

    @Test
    fun `finishes execution when sync reporters have been cancelled`() = runTest {
        val syncFlow = MutableStateFlow(createSyncStatus("oldSync", EventSyncWorkerState.Succeeded))
        setUpSync(syncFlow)

        launch { usecase.invoke() }
        testScheduler.advanceUntilIdle()

        syncFlow.value = createSyncStatus("sync", EventSyncWorkerState.Cancelled)
        testScheduler.advanceUntilIdle()

        coVerify { syncOrchestrator.startEventSync(any()) }
        verify(exactly = 2) { sync.invoke(SyncCommands.ObserveOnly) }
    }

    @Test
    fun `does not start sync early when initial default state is emitted before last completed sync`() = runTest {
        val syncFlow = MutableStateFlow(createPlaceholderSyncStatus())
        setUpSync(syncFlow)

        val job = launch { usecase.invoke() }
        testScheduler.advanceUntilIdle()

        coVerify(exactly = 0) { syncOrchestrator.startEventSync(any()) }

        syncFlow.value = createSyncStatus("sync", EventSyncWorkerState.Succeeded)
        testScheduler.advanceUntilIdle()

        coVerify(exactly = 1) { syncOrchestrator.startEventSync(any()) }
        job.cancel()
    }

    private fun createSyncStatus(
        syncId: String,
        endReporterState: EventSyncWorkerState?,
        progress: Int? = 0,
        total: Int? = 0,
    ): SyncStatus {
        val eventSyncState = EventSyncState(
            syncId,
            progress,
            total,
            emptyList(),
            emptyList(),
            listOfNotNull(
                endReporterState?.let {
                    EventSyncState.SyncWorkerInfo(EventSyncWorkerType.END_SYNC_REPORTER, it)
                },
            ),
            null,
        )
        return SyncStatus(
            eventSyncState = eventSyncState,
            imageSyncStatus = ImageSyncStatus(isSyncing = false, progress = null, lastUpdateTimeMillis = null),
        )
    }

    private fun setUpSync(syncFlow: StateFlow<SyncStatus>) {
        every { sync.invoke(SyncCommands.ObserveOnly) } returns SyncResponse(
            syncCommandJob = Job().apply { complete() },
            syncStatusFlow = syncFlow,
        )
    }

    private fun createPlaceholderSyncStatus(): SyncStatus = createSyncStatus("", null, null, null)
}
