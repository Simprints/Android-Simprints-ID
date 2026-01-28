package com.simprints.feature.validatepool.usecase

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.simprints.infra.eventsync.status.models.EventSyncState
import com.simprints.infra.eventsync.status.models.EventSyncWorkerState
import com.simprints.infra.eventsync.status.models.EventSyncWorkerType
import com.simprints.infra.sync.ImageSyncStatus
import com.simprints.infra.sync.SyncCommands
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

    private lateinit var usecase: RunBlockingEventSyncUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        usecase = RunBlockingEventSyncUseCase(sync)
    }

    @Test
    fun `finishes execution when sync reporters are finished`() = runTest {
        val syncFlow = MutableStateFlow(createSyncStatus("oldSync", EventSyncWorkerState.Succeeded))
        setUpSync(syncFlow)

        launch { usecase.invoke() }
        testScheduler.advanceUntilIdle()

        syncFlow.value = createSyncStatus("sync", EventSyncWorkerState.Succeeded)
        testScheduler.advanceUntilIdle()

        verify(exactly = 1) { sync(SyncCommands.ObserveOnly) }
        verify(exactly = 1) { sync.invoke(SyncCommands.OneTime.Events.start()) }
    }

    @Test
    fun `finishes execution when sync reporters have failed`() = runTest {
        val syncFlow = MutableStateFlow(createSyncStatus("oldSync", EventSyncWorkerState.Succeeded))
        setUpSync(syncFlow)

        launch { usecase.invoke() }
        testScheduler.advanceUntilIdle()

        syncFlow.value = createSyncStatus("sync", EventSyncWorkerState.Failed())
        testScheduler.advanceUntilIdle()

        verify(exactly = 1) { sync(SyncCommands.ObserveOnly) }
        verify(exactly = 1) { sync.invoke(SyncCommands.OneTime.Events.start()) }
    }

    @Test
    fun `finishes execution when sync reporters have been cancelled`() = runTest {
        val syncFlow = MutableStateFlow(createSyncStatus("oldSync", EventSyncWorkerState.Succeeded))
        setUpSync(syncFlow)

        launch { usecase.invoke() }
        testScheduler.advanceUntilIdle()

        syncFlow.value = createSyncStatus("sync", EventSyncWorkerState.Cancelled)
        testScheduler.advanceUntilIdle()

        verify(exactly = 1) { sync(SyncCommands.ObserveOnly) }
        verify(exactly = 1) { sync.invoke(SyncCommands.OneTime.Events.start()) }
    }

    @Test
    fun `does not start sync early when initial default state is emitted before last completed sync`() = runTest {
        val syncFlow = MutableStateFlow(createPlaceholderSyncStatus())
        setUpSync(syncFlow)

        val job = launch { usecase.invoke() }
        testScheduler.advanceUntilIdle()

        verify(exactly = 0) { sync(SyncCommands.OneTime.Events.start()) }

        syncFlow.value = createSyncStatus("sync", EventSyncWorkerState.Succeeded)
        testScheduler.advanceUntilIdle()

        verify(exactly = 1) { sync(SyncCommands.OneTime.Events.start()) }
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
        val syncResponse = SyncResponse(
            syncCommandJob = Job().apply { complete() },
            syncStatusFlow = syncFlow,
        )
        every { sync.invoke(SyncCommands.ObserveOnly) } returns syncResponse
        every { sync.invoke(SyncCommands.OneTime.Events.start()) } returns syncResponse
    }

    private fun createPlaceholderSyncStatus(): SyncStatus = createSyncStatus("", null, null, null)
}
