package com.simprints.infra.eventsync.sync.master

import android.content.Context
import androidx.work.*
import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.ProjectConfiguration
import com.simprints.infra.config.domain.models.SynchronizationConfiguration
import com.simprints.infra.config.domain.models.SynchronizationConfiguration.Frequency.ONLY_PERIODICALLY_UP_SYNC
import com.simprints.infra.config.domain.models.SynchronizationConfiguration.Frequency.PERIODICALLY
import com.simprints.infra.config.domain.models.UpSynchronizationConfiguration.SimprintsUpSynchronizationConfiguration
import com.simprints.infra.config.domain.models.UpSynchronizationConfiguration.UpSynchronizationKind.ALL
import com.simprints.infra.config.domain.models.UpSynchronizationConfiguration.UpSynchronizationKind.NONE
import com.simprints.infra.eventsync.TestTimeHelperImpl
import com.simprints.infra.eventsync.sync.common.EventSyncCache
import com.simprints.infra.eventsync.sync.common.MASTER_SYNC_SCHEDULER_PERIODIC_TIME
import com.simprints.infra.eventsync.sync.common.TAG_MASTER_SYNC_ID
import com.simprints.infra.eventsync.sync.common.TAG_SUBJECTS_SYNC_ALL_WORKERS
import com.simprints.infra.eventsync.sync.down.EventDownSyncWorkersBuilder
import com.simprints.infra.eventsync.sync.up.EventUpSyncWorkersBuilder
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class EventSyncMasterWorkerTest {

    companion object {
        private const val UNIQUE_SYNC_ID = "uniqueId"
    }

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    lateinit var ctx: Context

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


    private lateinit var masterWorker: EventSyncMasterWorker

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        mockkStatic(WorkManager::class)

        every { workContinuation.then(any<OneTimeWorkRequest>()) } returns workContinuation
        every { workContinuation.then(any<List<OneTimeWorkRequest>>()) } returns workContinuation
        every { workManager.beginWith(any<OneTimeWorkRequest>()) } returns workContinuation
        every { WorkManager.getInstance(ctx) } returns workManager

        coEvery { downSyncWorkerBuilder.buildDownSyncWorkerChain(any()) } returns listOf(downSyncWorker)
        coEvery { upSyncWorkerBuilder.buildUpSyncWorkerChain(any()) } returns listOf(upSyncWorker)
        every { eventSyncSubMasterWorkersBuilder.buildStartSyncReporterWorker(any()) } returns startSyncReporterWorker
        every { eventSyncSubMasterWorkersBuilder.buildEndSyncReporterWorker(any()) } returns endSyncReporterWorker

        every { synchronizationConfiguration.up.simprints } returns bfsidUpSynchronizationConfiguration
        every { projectConfiguration.synchronization } returns synchronizationConfiguration
        coEvery { configManager.getProjectConfiguration() } returns projectConfiguration

        masterWorker = EventSyncMasterWorker(
            ctx,
            mockk(relaxed = true) {
                every { tags } returns setOf(MASTER_SYNC_SCHEDULER_PERIODIC_TIME)
            },
            downSyncWorkerBuilder,
            upSyncWorkerBuilder,
            configManager,
            eventSyncCache,
            eventSyncSubMasterWorkersBuilder,
            TestTimeHelperImpl(),
            testCoroutineRule.testCoroutineDispatcher
        )
    }

    @Test
    fun `doWork should do nothing if it can't up or down sync to BFSID`() = runTest {
        canDownSync(false)
        canUpSync(false)

        val uniqueSyncId = masterWorker.uniqueSyncId
        val result = masterWorker.doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())
        coVerify(exactly = 0) { eventSyncCache.clearProgresses() }
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

        coVerify(exactly = 1) { eventSyncCache.clearProgresses() }
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

        coVerify(exactly = 1) { eventSyncCache.clearProgresses() }
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
                uniqueSyncId
            )
        }
        verify(exactly = times) { workManager.beginWith(any<OneTimeWorkRequest>()) }
    }

    private fun assertUpSyncWorkerPresence(isPresent: Boolean, uniqueSyncId: String) {
        val times = if (isPresent) 1 else 0
        coVerify(exactly = times) { upSyncWorkerBuilder.buildUpSyncWorkerChain(uniqueSyncId) }
        verify(exactly = times) {
            workContinuation.then(match<List<OneTimeWorkRequest>> {
                it.contains(upSyncWorker)
            })
        }
    }

    private fun assertDownSyncWorkerPresence(isPresent: Boolean, uniqueSyncId: String) {
        val times = if (isPresent) 1 else 0
        coVerify(exactly = times) { downSyncWorkerBuilder.buildDownSyncWorkerChain(uniqueSyncId) }
        verify(exactly = times) {
            workContinuation.then(match<List<OneTimeWorkRequest>> {
                it.contains(downSyncWorker)
            })
        }
    }
}
