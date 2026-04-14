package com.simprints.feature.validatepool.usecase

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.simprints.infra.eventsync.status.models.DownSyncState
import com.simprints.infra.eventsync.status.models.EventSyncState
import com.simprints.infra.eventsync.status.models.EventSyncWorkerState
import com.simprints.infra.eventsync.status.models.EventSyncWorkerType
import com.simprints.infra.eventsync.status.models.UpSyncState
import com.simprints.infra.sync.OneTime
import com.simprints.infra.sync.SyncOrchestrator
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Job
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
    private lateinit var syncOrchestrator: SyncOrchestrator

    private lateinit var usecase: RunBlockingEventSyncUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        usecase = RunBlockingEventSyncUseCase(syncOrchestrator)
    }

    @Test
    fun `finishes execution when both up and down sync complete`() = runTest {
        val upFlow = MutableStateFlow(createUpSyncState("oldSync"))
        val downFlow = MutableStateFlow(createDownSyncState("oldSync"))
        setUpSync(upFlow, downFlow)

        launch { usecase.invoke() }
        testScheduler.advanceUntilIdle()

        upFlow.value = createUpSyncState("newSync", EventSyncWorkerState.Succeeded)
        downFlow.value = createDownSyncState("newSync", EventSyncWorkerState.Succeeded)
        testScheduler.advanceUntilIdle()

        verify(exactly = 1) { syncOrchestrator.execute(OneTime.UpSync.start()) }
        verify(exactly = 1) { syncOrchestrator.execute(OneTime.DownSync.start()) }
    }

    @Test
    fun `finishes execution when sync has failed`() = runTest {
        val upFlow = MutableStateFlow(createUpSyncState("oldSync"))
        val downFlow = MutableStateFlow(createDownSyncState("oldSync"))
        setUpSync(upFlow, downFlow)

        launch { usecase.invoke() }
        testScheduler.advanceUntilIdle()

        upFlow.value = createUpSyncState("newSync", EventSyncWorkerState.Failed())
        downFlow.value = createDownSyncState("newSync", EventSyncWorkerState.Failed())
        testScheduler.advanceUntilIdle()

        verify(exactly = 1) { syncOrchestrator.execute(OneTime.UpSync.start()) }
        verify(exactly = 1) { syncOrchestrator.execute(OneTime.DownSync.start()) }
    }

    @Test
    fun `finishes execution when sync has been cancelled`() = runTest {
        val upFlow = MutableStateFlow(createUpSyncState("oldSync"))
        val downFlow = MutableStateFlow(createDownSyncState("oldSync"))
        setUpSync(upFlow, downFlow)

        launch { usecase.invoke() }
        testScheduler.advanceUntilIdle()

        upFlow.value = createUpSyncState("newSync", EventSyncWorkerState.Cancelled)
        downFlow.value = createDownSyncState("newSync", EventSyncWorkerState.Cancelled)
        testScheduler.advanceUntilIdle()

        verify(exactly = 1) { syncOrchestrator.execute(OneTime.UpSync.start()) }
        verify(exactly = 1) { syncOrchestrator.execute(OneTime.DownSync.start()) }
    }

    @Test
    fun `does not finish early when initial state has no sync history`() = runTest {
        val upFlow = MutableStateFlow(createUpSyncState(""))
        val downFlow = MutableStateFlow(createDownSyncState(""))
        setUpSync(upFlow, downFlow)

        val job = launch { usecase.invoke() }
        testScheduler.advanceUntilIdle()

        verify(exactly = 0) { syncOrchestrator.execute(OneTime.UpSync.start()) }

        upFlow.value = createUpSyncState("newSync", EventSyncWorkerState.Succeeded)
        downFlow.value = createDownSyncState("newSync", EventSyncWorkerState.Succeeded)
        testScheduler.advanceUntilIdle()

        verify(exactly = 1) { syncOrchestrator.execute(OneTime.UpSync.start()) }
        job.cancel()
    }

    private fun createUpSyncState(
        syncId: String,
        workerState: EventSyncWorkerState? = null,
    ) = UpSyncState(
        syncId = syncId,
        workersInfo = listOfNotNull(
            workerState?.let { EventSyncState.SyncWorkerInfo(EventSyncWorkerType.UPLOADER, it) },
        ),
        progress = null,
        total = null,
        lastSyncTime = null,
    )

    private fun createDownSyncState(
        syncId: String,
        workerState: EventSyncWorkerState? = null,
    ) = DownSyncState(
        syncId = syncId,
        workersInfo = listOfNotNull(
            workerState?.let { EventSyncState.SyncWorkerInfo(EventSyncWorkerType.DOWNLOADER, it) },
        ),
        progress = null,
        total = null,
        lastSyncTime = null,
    )

    private fun setUpSync(
        upFlow: MutableStateFlow<UpSyncState>,
        downFlow: MutableStateFlow<DownSyncState>,
    ) {
        every { syncOrchestrator.observeUpSyncState() } returns upFlow
        every { syncOrchestrator.observeDownSyncState() } returns downFlow
        every { syncOrchestrator.execute(OneTime.UpSync.start()) } returns Job().apply { complete() }
        every { syncOrchestrator.execute(OneTime.DownSync.start()) } returns Job().apply { complete() }
    }
}
