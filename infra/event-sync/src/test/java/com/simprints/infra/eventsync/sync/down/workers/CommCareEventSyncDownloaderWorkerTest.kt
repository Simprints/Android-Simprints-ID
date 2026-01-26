package com.simprints.infra.eventsync.sync.down.workers

import android.os.PowerManager
import androidx.test.ext.junit.runners.*
import androidx.work.ListenableWorker
import androidx.work.WorkInfo
import androidx.work.WorkInfo.State.RUNNING
import androidx.work.WorkInfo.State.SUCCEEDED
import androidx.work.workDataOf
import com.google.common.truth.Truth.*
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.enrolment.records.repository.local.migration.RealmToRoomMigrationFlagsStore
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.scope.EventScope
import com.simprints.infra.eventsync.SampleSyncScopes.projectDownSyncScope
import com.simprints.infra.eventsync.status.down.EventDownSyncScopeRepository
import com.simprints.infra.eventsync.sync.common.EventSyncCache
import com.simprints.infra.eventsync.sync.common.OUTPUT_FAILED_BECAUSE_COMMCARE_PERMISSION_MISSING
import com.simprints.infra.eventsync.sync.down.tasks.CommCareEventSyncTask
import com.simprints.infra.eventsync.sync.down.workers.BaseEventDownSyncDownloaderWorker.Companion.INPUT_DOWN_SYNC_OPS
import com.simprints.infra.eventsync.sync.down.workers.BaseEventDownSyncDownloaderWorker.Companion.INPUT_EVENT_DOWN_SYNC_SCOPE_ID
import com.simprints.infra.eventsync.sync.down.workers.BaseEventDownSyncDownloaderWorker.Companion.OUTPUT_DOWN_SYNC
import com.simprints.infra.eventsync.sync.down.workers.BaseEventDownSyncDownloaderWorker.Companion.PROGRESS_DOWN_SYNC
import com.simprints.infra.serialization.SimJson
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
internal class CommCareEventSyncDownloaderWorkerTest {
    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    lateinit var commCareSyncTask: CommCareEventSyncTask

    @MockK
    lateinit var eventDownSyncScopeRepository: EventDownSyncScopeRepository

    @MockK
    lateinit var syncCache: EventSyncCache

    @MockK
    lateinit var eventRepository: EventRepository

    @MockK
    lateinit var eventScope: EventScope

    @MockK
    lateinit var configRepository: ConfigRepository

    @MockK
    lateinit var migrationFlagsStore: RealmToRoomMigrationFlagsStore

    private lateinit var eventDownSyncDownloaderWorker: CommCareEventSyncDownloaderWorker

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        eventDownSyncDownloaderWorker = CommCareEventSyncDownloaderWorker(
            mockk(relaxed = true) {
                every { getSystemService<PowerManager>(any()) } returns mockk {
                    every { isIgnoringBatteryOptimizations(any()) } returns true
                }
            },
            mockk(relaxed = true) {
                every { inputData } returns workDataOf(
                    INPUT_DOWN_SYNC_OPS to SimJson.encodeToString(projectDownSyncScope.operations.first()),
                    INPUT_EVENT_DOWN_SYNC_SCOPE_ID to "eventScopeId",
                )
            },
            eventDownSyncScopeRepository,
            syncCache,
            eventRepository,
            configRepository,
            testCoroutineRule.testCoroutineDispatcher,
            commCareSyncTask,
            migrationFlagsStore,
        )
    }

    @Test
    fun `worker when migration is in progress should retry and not set down sync in progress`() = runTest {
        coEvery { migrationFlagsStore.isMigrationInProgress() } returns true

        val result = eventDownSyncDownloaderWorker.doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.retry())
        coVerify(exactly = 0) { migrationFlagsStore.setDownSyncInProgress(true) }
        coVerify(exactly = 0) { migrationFlagsStore.setDownSyncInProgress(false) }
    }

    @Test
    fun `worker when migration is not in progress should set down sync in progress to true then false`() = runTest {
        coEvery { migrationFlagsStore.isMigrationInProgress() } returns false

        eventDownSyncDownloaderWorker.doWork()

        coVerify(exactly = 1) { migrationFlagsStore.setDownSyncInProgress(true) }
        coVerify(exactly = 1) { migrationFlagsStore.setDownSyncInProgress(false) }
    }

    @Test
    fun `worker when migration is not in progress and exception occurs should set down sync in progress to true then false`() = runTest {
        coEvery { migrationFlagsStore.isMigrationInProgress() } returns false
        coEvery { commCareSyncTask.downSync(any(), any(), any(), any()) } throws RuntimeException("Test exception")

        eventDownSyncDownloaderWorker.doWork()

        coVerify(exactly = 1) { migrationFlagsStore.setDownSyncInProgress(true) }
        coVerify(exactly = 1) { migrationFlagsStore.setDownSyncInProgress(false) }
    }

    @Test
    fun `worker with no event scope should fail`() = runTest {
        coEvery { eventRepository.getEventScope(any()) } returns null

        val result = eventDownSyncDownloaderWorker.doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.failure())
    }

    @Test
    fun `worker should fail if task throws IllegalArgumentException`() = runTest {
        coEvery { eventRepository.getEventScope(any()) } returns eventScope
        coEvery {
            commCareSyncTask.downSync(any(), any(), any(), any())
        } throws IllegalArgumentException("Invalid argument")

        val result = eventDownSyncDownloaderWorker.doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.failure())
    }

    @Test
    fun `worker should fail if task throws SecurityException`() = runTest {
        coEvery { eventRepository.getEventScope(any()) } returns eventScope
        coEvery {
            commCareSyncTask.downSync(any(), any(), any(), any())
        } throws SecurityException("Permission denied")

        val result = eventDownSyncDownloaderWorker.doWork()

        assertThat(result).isEqualTo(
            ListenableWorker.Result.failure(
                workDataOf(
                    OUTPUT_FAILED_BECAUSE_COMMCARE_PERMISSION_MISSING to true,
                ),
            ),
        )
    }

    @Test
    fun `worker should extract the progress count correctly`() = runTest {
        coEvery { eventRepository.getEventScope(any()) } returns eventScope
        val progress = 2
        val syncCacheMock = mockk<EventSyncCache>()
        coEvery { syncCacheMock.readProgress(any()) } returns 1

        val workInfo = WorkInfo(
            UUID.randomUUID(),
            RUNNING,
            setOf(),
            workDataOf(),
            workDataOf(PROGRESS_DOWN_SYNC to progress),
            2,
            0,
        )
        assertThat(workInfo.extractDownSyncProgress(syncCacheMock)).isEqualTo(progress)
    }

    @Test
    fun `worker should extract the final progress count correctly`() = runTest {
        coEvery { eventRepository.getEventScope(any()) } returns eventScope
        val progress = 2
        val syncCacheMock = mockk<EventSyncCache>()
        coEvery { syncCacheMock.readProgress(any()) } returns 1

        val workInfo = WorkInfo(
            UUID.randomUUID(),
            SUCCEEDED,
            setOf(),
            workDataOf(OUTPUT_DOWN_SYNC to progress),
            workDataOf(),
            2,
            1,
        )
        assertThat(workInfo.extractDownSyncProgress(syncCacheMock)).isEqualTo(progress)
    }

    @Test
    fun `when worker is resumed progress count in cache should be extracted correctly`() = runTest {
        coEvery { eventRepository.getEventScope(any()) } returns eventScope
        val progress = 2
        val syncCacheMock = mockk<EventSyncCache>()
        coEvery { syncCacheMock.readProgress(any()) } returns progress

        val workInfo = WorkInfo(
            UUID.randomUUID(),
            RUNNING,
            setOf(),
            workDataOf(),
            workDataOf(),
            2,
            1,
        )
        assertThat(workInfo.extractDownSyncProgress(syncCacheMock)).isEqualTo(progress)
    }
}
