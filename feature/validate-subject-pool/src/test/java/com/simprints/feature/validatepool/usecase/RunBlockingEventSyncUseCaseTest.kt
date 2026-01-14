package com.simprints.feature.validatepool.usecase

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.simprints.infra.eventsync.status.models.EventSyncState
import com.simprints.infra.eventsync.status.models.EventSyncWorkerState
import com.simprints.infra.eventsync.status.models.EventSyncWorkerType
import com.simprints.infra.sync.ImageSyncStatus
import com.simprints.infra.sync.LegacySyncStates
import com.simprints.infra.sync.SyncCommand
import com.simprints.infra.sync.SyncOrchestrator
import com.simprints.infra.sync.SyncStatus
import com.simprints.infra.sync.usecase.SyncUseCase
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.MutableStateFlow
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
        every { sync.invoke(any(), any()) } returns syncFlow

        launch { usecase.invoke() }
        testScheduler.advanceUntilIdle()

        syncFlow.value = createSyncStatus("sync", EventSyncWorkerState.Succeeded)
        testScheduler.advanceUntilIdle()

        coVerify { syncOrchestrator.startEventSync(any()) }
        verify(exactly = 2) { sync.invoke(SyncCommand.OBSERVE_ONLY, SyncCommand.OBSERVE_ONLY) }
    }

    @Test
    fun `finishes execution when sync reporters have failed`() = runTest {
        val syncFlow = MutableStateFlow(createSyncStatus("oldSync", EventSyncWorkerState.Succeeded))
        every { sync.invoke(any(), any()) } returns syncFlow

        launch { usecase.invoke() }
        testScheduler.advanceUntilIdle()

        syncFlow.value = createSyncStatus("sync", EventSyncWorkerState.Failed())
        testScheduler.advanceUntilIdle()

        coVerify { syncOrchestrator.startEventSync(any()) }
        verify(exactly = 2) { sync.invoke(SyncCommand.OBSERVE_ONLY, SyncCommand.OBSERVE_ONLY) }
    }

    @Test
    fun `finishes execution when sync reporters have been cancelled`() = runTest {
        val syncFlow = MutableStateFlow(createSyncStatus("oldSync", EventSyncWorkerState.Succeeded))
        every { sync.invoke(any(), any()) } returns syncFlow

        launch { usecase.invoke() }
        testScheduler.advanceUntilIdle()

        syncFlow.value = createSyncStatus("sync", EventSyncWorkerState.Cancelled)
        testScheduler.advanceUntilIdle()

        coVerify { syncOrchestrator.startEventSync(any()) }
        verify(exactly = 2) { sync.invoke(SyncCommand.OBSERVE_ONLY, SyncCommand.OBSERVE_ONLY) }
    }

    private fun createSyncStatus(
        syncId: String,
        endReporterState: EventSyncWorkerState,
    ): SyncStatus {
        val eventSyncState = EventSyncState(
            syncId,
            0,
            0,
            emptyList(),
            emptyList(),
            listOf(
                EventSyncState.SyncWorkerInfo(
                    EventSyncWorkerType.END_SYNC_REPORTER,
                    endReporterState,
                ),
            ),
            null,
        )
        return SyncStatus(
            LegacySyncStates(
                eventSyncState = eventSyncState,
                imageSyncStatus = ImageSyncStatus(isSyncing = false, progress = null, lastUpdateTimeMillis = null),
            ),
        )
    }
}
