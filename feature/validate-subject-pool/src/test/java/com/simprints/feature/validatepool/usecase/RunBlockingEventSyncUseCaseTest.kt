package com.simprints.feature.validatepool.usecase

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.simprints.infra.eventsync.EventSyncManager
import com.simprints.infra.eventsync.status.models.EventSyncState
import com.simprints.infra.eventsync.status.models.EventSyncWorkerState
import com.simprints.infra.eventsync.status.models.EventSyncWorkerType
import com.simprints.infra.sync.SyncOrchestrator
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.justRun
import io.mockk.verify
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
    private lateinit var syncManager: EventSyncManager

    @MockK
    private lateinit var syncOrchestrator: SyncOrchestrator

    private lateinit var usecase: RunBlockingEventSyncUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        justRun { syncOrchestrator.startEventSync() }

        usecase = RunBlockingEventSyncUseCase(
            syncManager,
            syncOrchestrator,
        )
    }

    @Test
    fun `finishes execution when sync reporters are finished`() = runTest {
        val liveData = MutableLiveData<EventSyncState>()
        every { syncManager.getLastSyncState() } returns liveData
        liveData.postValue(createSyncState("oldSync", EventSyncWorkerState.Succeeded))

        launch { usecase.invoke() }
        testScheduler.advanceUntilIdle()

        liveData.postValue(createSyncState("sync", EventSyncWorkerState.Succeeded))
        testScheduler.advanceUntilIdle()

        verify { syncOrchestrator.startEventSync() }
        verify { syncManager.getLastSyncState() }
    }

    @Test
    fun `finishes execution when sync reporters have failed`() = runTest {
        val liveData = MutableLiveData<EventSyncState>()
        every { syncManager.getLastSyncState() } returns liveData
        liveData.postValue(createSyncState("oldSync", EventSyncWorkerState.Succeeded))

        launch { usecase.invoke() }
        testScheduler.advanceUntilIdle()

        liveData.postValue(createSyncState("sync", EventSyncWorkerState.Failed()))
        testScheduler.advanceUntilIdle()

        verify { syncOrchestrator.startEventSync() }
        verify { syncManager.getLastSyncState() }
    }

    @Test
    fun `finishes execution when sync reporters have been cancelled`() = runTest {
        val liveData = MutableLiveData<EventSyncState>()
        every { syncManager.getLastSyncState() } returns liveData
        liveData.postValue(createSyncState("oldSync", EventSyncWorkerState.Succeeded))

        launch { usecase.invoke() }
        testScheduler.advanceUntilIdle()

        liveData.postValue(createSyncState("sync", EventSyncWorkerState.Cancelled))
        testScheduler.advanceUntilIdle()

        verify { syncOrchestrator.startEventSync() }
        verify { syncManager.getLastSyncState() }
    }

    private fun createSyncState(
        syncId: String,
        endReporterState: EventSyncWorkerState,
    ) = EventSyncState(
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
    )
}
