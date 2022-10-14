package com.simprints.id.services.sync.events.master

import android.content.Context
import androidx.work.*
import com.google.common.truth.Truth.assertThat
import com.simprints.id.services.sync.events.common.TAG_MASTER_SYNC_ID
import com.simprints.id.services.sync.events.common.TAG_SUBJECTS_SYNC_ALL_WORKERS
import com.simprints.id.services.sync.events.down.EventDownSyncWorkersBuilder
import com.simprints.id.services.sync.events.master.internal.EventSyncCache
import com.simprints.id.services.sync.events.master.workers.EventSyncMasterWorker
import com.simprints.id.services.sync.events.master.workers.EventSyncMasterWorker.Companion.MASTER_SYNC_SCHEDULER_PERIODIC_TIME
import com.simprints.id.services.sync.events.master.workers.EventSyncSubMasterWorkersBuilder
import com.simprints.id.services.sync.events.up.EventUpSyncWorkersBuilder
import com.simprints.id.testtools.TestTimeHelperImpl
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.ProjectConfiguration
import com.simprints.infra.config.domain.models.SynchronizationConfiguration
import com.simprints.infra.config.domain.models.SynchronizationConfiguration.Frequency.ONLY_PERIODICALLY_UP_SYNC
import com.simprints.infra.config.domain.models.SynchronizationConfiguration.Frequency.PERIODICALLY
import com.simprints.infra.config.domain.models.UpSynchronizationConfiguration.SimprintsUpSynchronizationConfiguration
import com.simprints.infra.config.domain.models.UpSynchronizationConfiguration.UpSynchronizationKind.ALL
import com.simprints.infra.config.domain.models.UpSynchronizationConfiguration.UpSynchronizationKind.NONE
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.*
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

    private val ctx = mockk<Context>()
    private val workContinuation = mockk<WorkContinuation>()
    private val workManager = mockk<WorkManager>(relaxed = true) {
        every { beginWith(any<OneTimeWorkRequest>()) } returns workContinuation
    }
    private val startSyncReporterWorker = mockk<OneTimeWorkRequest>()
    private val endSyncReporterWorker = mockk<OneTimeWorkRequest>()
    private val upSyncWorker = mockk<OneTimeWorkRequest>()
    private val downSyncWorker = mockk<OneTimeWorkRequest>()

    private val downSyncWorkerBuilder = mockk<EventDownSyncWorkersBuilder> {
        coEvery { buildDownSyncWorkerChain(any()) } returns listOf(downSyncWorker)
    }
    private val upSyncWorkerBuilder = mockk<EventUpSyncWorkersBuilder> {
        coEvery { buildUpSyncWorkerChain(any()) } returns listOf(upSyncWorker)
    }
    private val eventSyncSubMasterWorkersBuilder = mockk<EventSyncSubMasterWorkersBuilder> {
        every { buildStartSyncReporterWorker(any()) } returns startSyncReporterWorker
        every { buildEndSyncReporterWorker(any()) } returns endSyncReporterWorker
    }
    private val eventSyncCache = mockk<EventSyncCache>(relaxed = true)
    private val bfsidUpSynchronizationConfiguration =
        mockk<SimprintsUpSynchronizationConfiguration>()
    private val synchronizationConfiguration = mockk<SynchronizationConfiguration> {
        every { up } returns mockk {
            every { simprints } returns bfsidUpSynchronizationConfiguration
        }
    }
    private val projectConfiguration = mockk<ProjectConfiguration> {
        every { synchronization } returns synchronizationConfiguration
    }
    private val configManager = mockk<ConfigManager>(relaxed = true) {
        coEvery { getProjectConfiguration() } returns projectConfiguration
    }
    private lateinit var masterWorker: EventSyncMasterWorker

    @Before
    fun setUp() {
        mockkStatic(WorkManager::class)
        every { WorkManager.getInstance(ctx) } returns workManager
        every { workContinuation.then(any<OneTimeWorkRequest>()) } returns workContinuation
        every { workContinuation.then(any<List<OneTimeWorkRequest>>()) } returns workContinuation

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
                    every { tags } returns setOf("${TAG_MASTER_SYNC_ID}${UNIQUE_SYNC_ID}")
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
