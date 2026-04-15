package com.simprints.feature.validatepool.usecase

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.ProjectState
import com.simprints.infra.config.store.models.isCommCareEventDownSyncAllowed
import com.simprints.infra.config.store.models.isSimprintsEventDownSyncAllowed
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

    @MockK
    private lateinit var configRepository: ConfigRepository

    private lateinit var usecase: RunBlockingEventSyncUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        mockkStatic("com.simprints.infra.config.store.models.ProjectConfigurationKt")
        usecase = RunBlockingEventSyncUseCase(syncOrchestrator, configRepository)
    }

    @Test
    fun `finishes execution when both up and down sync complete`() = runTest {
        val upFlow = MutableStateFlow(createUpSyncState("oldSync"))
        val downFlow = MutableStateFlow(createDownSyncState("oldSync"))
        setUpSync(upFlow, downFlow, downSyncAllowed = true)

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
        setUpSync(upFlow, downFlow, downSyncAllowed = true)

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
        setUpSync(upFlow, downFlow, downSyncAllowed = true)

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
        setUpSync(upFlow, downFlow, downSyncAllowed = true)

        val job = launch { usecase.invoke() }
        testScheduler.advanceUntilIdle()

        verify(exactly = 0) { syncOrchestrator.execute(OneTime.UpSync.start()) }

        upFlow.value = createUpSyncState("newSync", EventSyncWorkerState.Succeeded)
        downFlow.value = createDownSyncState("newSync", EventSyncWorkerState.Succeeded)
        testScheduler.advanceUntilIdle()

        verify(exactly = 1) { syncOrchestrator.execute(OneTime.UpSync.start()) }
        job.cancel()
    }

    @Test
    fun `skips down sync when project is ending`() = runTest {
        val upFlow = MutableStateFlow(createUpSyncState("oldSync"))
        setUpSync(upFlow, downFlow = null, downSyncAllowed = false, projectState = ProjectState.PROJECT_ENDING)

        launch { usecase.invoke() }
        testScheduler.advanceUntilIdle()

        upFlow.value = createUpSyncState("newSync", EventSyncWorkerState.Succeeded)
        testScheduler.advanceUntilIdle()

        verify(exactly = 1) { syncOrchestrator.execute(OneTime.UpSync.start()) }
        verify(exactly = 0) { syncOrchestrator.execute(OneTime.DownSync.start()) }
        verify(exactly = 0) { syncOrchestrator.observeDownSyncState() }
    }

    @Test
    fun `skips down sync when project is paused`() = runTest {
        val upFlow = MutableStateFlow(createUpSyncState("oldSync"))
        setUpSync(upFlow, downFlow = null, downSyncAllowed = false, projectState = ProjectState.PROJECT_PAUSED)

        launch { usecase.invoke() }
        testScheduler.advanceUntilIdle()

        upFlow.value = createUpSyncState("newSync", EventSyncWorkerState.Succeeded)
        testScheduler.advanceUntilIdle()

        verify(exactly = 1) { syncOrchestrator.execute(OneTime.UpSync.start()) }
        verify(exactly = 0) { syncOrchestrator.execute(OneTime.DownSync.start()) }
        verify(exactly = 0) { syncOrchestrator.observeDownSyncState() }
    }

    @Test
    fun `skips down sync when down sync is disabled in config`() = runTest {
        val upFlow = MutableStateFlow(createUpSyncState("oldSync"))
        setUpSync(upFlow, downFlow = null, downSyncAllowed = false, projectState = ProjectState.RUNNING)

        launch { usecase.invoke() }
        testScheduler.advanceUntilIdle()

        upFlow.value = createUpSyncState("newSync", EventSyncWorkerState.Succeeded)
        testScheduler.advanceUntilIdle()

        verify(exactly = 1) { syncOrchestrator.execute(OneTime.UpSync.start()) }
        verify(exactly = 0) { syncOrchestrator.execute(OneTime.DownSync.start()) }
        verify(exactly = 0) { syncOrchestrator.observeDownSyncState() }
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
        downFlow: MutableStateFlow<DownSyncState>?,
        downSyncAllowed: Boolean,
        projectState: ProjectState = ProjectState.RUNNING,
    ) {
        val mockProject = mockk<Project> { every { state } returns projectState }
        coEvery { configRepository.getProject() } returns mockProject
        val mockConfig = mockk<com.simprints.infra.config.store.models.ProjectConfiguration>(relaxed = true)
        coEvery { configRepository.getProjectConfiguration() } returns mockConfig
        every { mockConfig.isSimprintsEventDownSyncAllowed() } returns downSyncAllowed
        every { mockConfig.isCommCareEventDownSyncAllowed() } returns false

        every { syncOrchestrator.observeUpSyncState() } returns upFlow
        if (downFlow != null) {
            every { syncOrchestrator.observeDownSyncState() } returns downFlow
        }
        every { syncOrchestrator.execute(OneTime.UpSync.start()) } returns Job().apply { complete() }
        if (downSyncAllowed) {
            every { syncOrchestrator.execute(OneTime.DownSync.start()) } returns Job().apply { complete() }
        }
    }
}
