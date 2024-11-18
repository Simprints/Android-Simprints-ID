package com.simprints.infra.eventsync.sync.master

import android.content.Context
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Configuration
import androidx.work.ListenableWorker
import androidx.work.ListenableWorker.Result.Success
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkContinuation
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import androidx.work.workDataOf
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.time.TimeHelper
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.ProjectState
import com.simprints.infra.config.store.models.SynchronizationConfiguration
import com.simprints.infra.config.store.models.SynchronizationConfiguration.Frequency.ONLY_PERIODICALLY_UP_SYNC
import com.simprints.infra.config.store.models.SynchronizationConfiguration.Frequency.PERIODICALLY
import com.simprints.infra.config.store.models.UpSynchronizationConfiguration.SimprintsUpSynchronizationConfiguration
import com.simprints.infra.config.store.models.UpSynchronizationConfiguration.UpSynchronizationKind.ALL
import com.simprints.infra.config.store.models.UpSynchronizationConfiguration.UpSynchronizationKind.NONE
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.scope.EventScopeType
import com.simprints.infra.eventsync.sync.common.EventSyncCache
import com.simprints.infra.eventsync.sync.common.MASTER_SYNC_SCHEDULER_PERIODIC_TIME
import com.simprints.infra.eventsync.sync.common.TAG_MASTER_SYNC_ID
import com.simprints.infra.eventsync.sync.common.TAG_SUBJECTS_SYNC_ALL_WORKERS
import com.simprints.infra.eventsync.sync.down.EventDownSyncWorkersBuilder
import com.simprints.infra.eventsync.sync.up.EventUpSyncWorkersBuilder
import com.simprints.infra.security.SecurityManager
import com.simprints.infra.security.exceptions.RootedDeviceException
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class EventSyncMasterWorkerTest {

    companion object {

        private const val UNIQUE_SYNC_ID = "uniqueId"
    }

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    val ctx: Context = getApplicationContext()

    @MockK
    lateinit var workContinuation: WorkContinuation

    @MockK
    lateinit var workManager: WorkManager

    @MockK
    lateinit var startSyncReporterWorker: OneTimeWorkRequest

    @MockK
    lateinit var endSyncReporterWorker: OneTimeWorkRequest

    @MockK
    lateinit var upSyncWorker: OneTimeWorkRequest

    @MockK
    lateinit var downSyncWorker: OneTimeWorkRequest

    @MockK
    lateinit var downSyncWorkerBuilder: EventDownSyncWorkersBuilder

    @MockK
    lateinit var upSyncWorkerBuilder: EventUpSyncWorkersBuilder

    @MockK
    lateinit var eventSyncSubMasterWorkersBuilder: EventSyncSubMasterWorkersBuilder

    @MockK
    lateinit var eventSyncCache: EventSyncCache

    @MockK
    lateinit var bfsidUpSynchronizationConfiguration: SimprintsUpSynchronizationConfiguration

    @MockK
    lateinit var synchronizationConfiguration: SynchronizationConfiguration

    @MockK
    lateinit var projectConfiguration: ProjectConfiguration

    @MockK
    lateinit var configManager: ConfigManager

    @MockK
    lateinit var timeHelper: TimeHelper

    @MockK
    lateinit var eventRepository: EventRepository

    @RelaxedMockK
    private lateinit var securityManager: SecurityManager

    private lateinit var masterWorker: EventSyncMasterWorker

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        val config = Configuration.Builder().build()
        WorkManagerTestInitHelper.initializeTestWorkManager(ctx, config)

        workManager = spyk( WorkManager.getInstance(ctx))
        mockkObject(WorkManager.Companion)

        every { workContinuation.then(any<OneTimeWorkRequest>()) } returns workContinuation
        every { workContinuation.then(any<List<OneTimeWorkRequest>>()) } returns workContinuation
        every { workManager.beginWith(any<OneTimeWorkRequest>()) } returns workContinuation
        every { WorkManager.getInstance(ctx) } returns workManager

        coEvery { downSyncWorkerBuilder.buildDownSyncWorkerChain(any(), any()) } returns listOf(
            downSyncWorker
        )
        coEvery { upSyncWorkerBuilder.buildUpSyncWorkerChain(any(), any()) } returns listOf(upSyncWorker)
        every { eventSyncSubMasterWorkersBuilder.buildStartSyncReporterWorker(any()) } returns startSyncReporterWorker
        every { eventSyncSubMasterWorkersBuilder.buildEndSyncReporterWorker(any(), any(), any()) } returns endSyncReporterWorker

        every { synchronizationConfiguration.up.simprints } returns bfsidUpSynchronizationConfiguration
        every { projectConfiguration.projectId } returns "projectId"
        every { projectConfiguration.synchronization } returns synchronizationConfiguration
        coEvery { configManager.getProjectConfiguration() } returns projectConfiguration

        masterWorker = EventSyncMasterWorker(
            appContext = ctx,
            params = mockk(relaxed = true) {
                every { tags } returns setOf(MASTER_SYNC_SCHEDULER_PERIODIC_TIME)
            },
            downSyncWorkerBuilder = downSyncWorkerBuilder,
            upSyncWorkerBuilder = upSyncWorkerBuilder,
            configManager = configManager,
            eventSyncCache = eventSyncCache,
            eventSyncSubMasterWorkersBuilder = eventSyncSubMasterWorkersBuilder,
            timeHelper = timeHelper,
            dispatcher = testCoroutineRule.testCoroutineDispatcher,
            securityManager = securityManager,
            eventRepository = eventRepository,
        )
    }

    @Test
    fun `doWork should fail if device is rooted`() = runTest {
        coEvery { securityManager.checkIfDeviceIsRooted() } throws RootedDeviceException()
        val result = masterWorker.doWork()
        assertThat(result).isEqualTo(ListenableWorker.Result.failure())
    }

    @Test
    fun `doWork should do nothing if it can't up or down sync to BFSID`() = runTest {
        canDownSync(false)
        canUpSync(false)

        val uniqueSyncId = masterWorker.uniqueSyncId
        val result = masterWorker.doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())
        coVerify(exactly = 0) {
            eventSyncCache.clearProgresses()
            eventRepository.createEventScope(any(), any())
        }
        assertUpSyncWorkerPresence(false, uniqueSyncId)
        assertDownSyncWorkerPresence(false, uniqueSyncId)
        assertWorkerChainBuild(false, uniqueSyncId)
    }

    @Test
    fun `doWork should only enqueue the up sync worker it can't down sync to BFSID`() = runTest {
        shouldSyncRun(false)
        canDownSync(false)
        canUpSync(true)

        val uniqueSyncId = masterWorker.uniqueSyncId
        val result = masterWorker.doWork()

        assertThat(result).isEqualTo(
            ListenableWorker.Result.success(
                workDataOf(
                    EventSyncMasterWorker.OUTPUT_LAST_SYNC_ID to uniqueSyncId
                )
            )
        )

        coVerify(exactly = 1) {
            eventSyncCache.clearProgresses()
            eventRepository.createEventScope(EventScopeType.UP_SYNC, any())
        }
        assertUpSyncWorkerPresence(true, uniqueSyncId)
        assertDownSyncWorkerPresence(false, uniqueSyncId)
        assertWorkerChainBuild(true, uniqueSyncId)
    }

    @Test
    fun `doWork should only enqueue the down sync worker it can't up sync to BFSID`() = runTest {
        shouldSyncRun(false)
        canDownSync(true)
        canUpSync(false)

        val uniqueSyncId = masterWorker.uniqueSyncId
        val result = masterWorker.doWork()

        assertThat(result).isEqualTo(
            ListenableWorker.Result.success(
                workDataOf(
                    EventSyncMasterWorker.OUTPUT_LAST_SYNC_ID to uniqueSyncId
                )
            )
        )

        coVerify(exactly = 1) {
            eventSyncCache.clearProgresses()
            eventRepository.createEventScope(EventScopeType.DOWN_SYNC, any())
        }
        assertUpSyncWorkerPresence(false, uniqueSyncId)
        assertDownSyncWorkerPresence(true, uniqueSyncId)
        assertWorkerChainBuild(true, uniqueSyncId)
    }

    @Test
    fun `doWork should enqueue the down and up sync worker it can sync to BFSID`() = runTest {
        shouldSyncRun(false)
        canDownSync(true)
        canUpSync(true)

        val uniqueSyncId = masterWorker.uniqueSyncId
        val result = masterWorker.doWork()

        assertThat(result).isEqualTo(
            ListenableWorker.Result.success(
                workDataOf(
                    EventSyncMasterWorker.OUTPUT_LAST_SYNC_ID to uniqueSyncId
                )
            )
        )

        coVerify(exactly = 1) { eventSyncCache.clearProgresses() }
        coVerify(exactly = 2) { eventRepository.createEventScope(any(), any()) }
        assertUpSyncWorkerPresence(true, uniqueSyncId)
        assertDownSyncWorkerPresence(true, uniqueSyncId)
        assertWorkerChainBuild(true, uniqueSyncId)
    }

    @Test
    fun `doWork should not enqueue the workers is one is already running`() = runTest {
        shouldSyncRun(true)
        canDownSync(true)
        canUpSync(false)

        val uniqueSyncId = masterWorker.uniqueSyncId
        val result = masterWorker.doWork()

        assertThat(result).isEqualTo(
            ListenableWorker.Result.success(
                workDataOf(
                    EventSyncMasterWorker.OUTPUT_LAST_SYNC_ID to UNIQUE_SYNC_ID
                )
            )
        )

        coVerify(exactly = 0) { eventSyncCache.clearProgresses() }
        assertUpSyncWorkerPresence(false, uniqueSyncId)
        assertDownSyncWorkerPresence(false, uniqueSyncId)
        assertWorkerChainBuild(false, uniqueSyncId)
    }

    @Test
    fun `doWork should fail if there is an exception`() = runTest {
        coEvery { configManager.getProjectConfiguration() } throws Throwable()
        val result = masterWorker.doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.failure())
    }

    @Test
    fun `event down sync should be disabled when project state is paused`() = runTest {
        val result = getIsEventDownSyncAllowedResult(
            projectState = ProjectState.PROJECT_PAUSED,
            syncConfig = PERIODICALLY
        )
        assertThat(result).isInstanceOf(Success::class.java)
        verify(exactly = 0) { timeHelper.now() }
    }

    @Test
    fun `event down sync should be disabled when sync config is ONLY_PERIODICALLY_UP_SYNC`() =
        runTest {
            val result = getIsEventDownSyncAllowedResult(
                projectState = ProjectState.RUNNING,
                syncConfig = ONLY_PERIODICALLY_UP_SYNC
            )
            assertThat(result).isInstanceOf(Success::class.java)
            verify(exactly = 0) { timeHelper.now() }
        }

    private suspend fun getIsEventDownSyncAllowedResult(
        projectState: ProjectState,
        syncConfig: SynchronizationConfiguration.Frequency,
    ): ListenableWorker.Result {
        coEvery { configManager.getProject(any()).state } returns projectState
        coEvery { configManager.getProjectConfiguration() } returns mockk {
            every { projectId } returns "projectId"
            every { synchronization } returns mockk {
                every { frequency } returns syncConfig
                every { up.simprints.kind } returns NONE
            }
        }
        return masterWorker.doWork()
    }

    private fun shouldSyncRun(should: Boolean) {
        val workerState = if (should) WorkInfo.State.RUNNING else WorkInfo.State.SUCCEEDED
        every { workManager.getWorkInfosByTag(TAG_SUBJECTS_SYNC_ALL_WORKERS) } returns mockk {
            every { get() } returns listOf(
                mockk {
                    every { state } returns workerState
                    every { tags } returns setOf("$TAG_MASTER_SYNC_ID$UNIQUE_SYNC_ID")
                }
            )
        }
    }

    private fun canDownSync(should: Boolean) {
        every { synchronizationConfiguration.frequency } returns if (should) PERIODICALLY else ONLY_PERIODICALLY_UP_SYNC
    }

    private fun canUpSync(should: Boolean) {
        every { bfsidUpSynchronizationConfiguration.kind } returns if (should) ALL else NONE
    }

    private fun assertWorkerChainBuild(isBuilt: Boolean, uniqueSyncId: String) {
        val times = if (isBuilt) 1 else 0
        verify(exactly = times) {
            eventSyncSubMasterWorkersBuilder.buildStartSyncReporterWorker(
                uniqueSyncId
            )
        }
        verify(exactly = times) {
            eventSyncSubMasterWorkersBuilder.buildEndSyncReporterWorker(
                uniqueSyncId,
                any(),
                any()
            )
        }
        verify(exactly = times) { workManager.beginWith(any<OneTimeWorkRequest>()) }
    }

    private fun assertUpSyncWorkerPresence(isPresent: Boolean, uniqueSyncId: String) {
        val times = if (isPresent) 1 else 0
        coVerify(exactly = times) { upSyncWorkerBuilder.buildUpSyncWorkerChain(uniqueSyncId, any()) }
        verify(exactly = times) {
            workContinuation.then(match<List<OneTimeWorkRequest>> {
                it.contains(upSyncWorker)
            })
        }
    }

    private fun assertDownSyncWorkerPresence(isPresent: Boolean, uniqueSyncId: String) {
        val times = if (isPresent) 1 else 0
        coVerify(exactly = times) { downSyncWorkerBuilder.buildDownSyncWorkerChain(uniqueSyncId, any()) }
        verify(exactly = times) {
            workContinuation.then(match<List<OneTimeWorkRequest>> {
                it.contains(downSyncWorker)
            })
        }
    }
}
