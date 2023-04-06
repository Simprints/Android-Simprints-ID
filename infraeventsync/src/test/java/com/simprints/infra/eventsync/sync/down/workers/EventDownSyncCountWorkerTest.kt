package com.simprints.infra.eventsync.sync.down.workers

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.common.truth.Truth.assertThat
import com.google.common.util.concurrent.ListenableFuture
import com.simprints.core.tools.json.JsonHelper
import com.simprints.infra.events.event.domain.EventCount
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordEventType
import com.simprints.infra.eventsync.SampleSyncScopes.projectDownSyncScope
import com.simprints.infra.eventsync.status.models.EventSyncWorkerType
import com.simprints.infra.eventsync.status.models.EventSyncWorkerType.Companion.tagForType
import com.simprints.infra.eventsync.sync.common.TAG_MASTER_SYNC_ID
import com.simprints.infra.eventsync.sync.down.tasks.EventDownSyncCountTask
import com.simprints.infra.eventsync.sync.down.workers.EventDownSyncCountWorker.Companion.INPUT_COUNT_WORKER_DOWN
import com.simprints.infra.eventsync.sync.down.workers.EventDownSyncCountWorker.Companion.OUTPUT_COUNT_WORKER_DOWN
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
internal class EventDownSyncCountWorkerTest {

    private val syncId = UUID.randomUUID().toString()
    private val tagForMasterSyncId = "$TAG_MASTER_SYNC_ID$syncId"

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    lateinit var eventDownSyncCountTask: EventDownSyncCountTask


    private lateinit var countWorker: EventDownSyncCountWorker

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        countWorker = EventDownSyncCountWorker(
            mockk(relaxed = true),
            mockk(relaxed = true) {
                every { inputData } returns workDataOf(
                    INPUT_COUNT_WORKER_DOWN to JsonHelper.toJson(
                        projectDownSyncScope
                    )
                )
                every { tags } returns setOf(tagForMasterSyncId)
            },
            JsonHelper,
            eventDownSyncCountTask,
            testCoroutineRule.testCoroutineDispatcher
        )
    }

    @Test
    fun countWorker_shouldExtractTheDownSyncScopeFromTheRepo() {
        runTest {
            countWorker.doWork()

            coVerify { eventDownSyncCountTask.getCount(any()) }
        }
    }

    @Test
    fun countWorker_shouldExecuteTheTaskSuccessfully() {
        runTest {
            val counts = EventCount(EnrolmentRecordEventType.EnrolmentRecordMove, 1)
            coEvery { eventDownSyncCountTask.getCount(any()) }  returns listOf(counts)

            val result = countWorker.doWork()

            val output = JsonHelper.toJson(listOf(counts))
            val expectedSuccessfulOutput = workDataOf(OUTPUT_COUNT_WORKER_DOWN to output)
            assertThat(result).isEqualTo(ListenableWorker.Result.success(expectedSuccessfulOutput))
        }
    }

    @Test
    fun countWorkerFailed_syncStillRunning_shouldRetry() {
        runTest {
            coEvery { eventDownSyncCountTask.getCount(any()) } throws Throwable("IO Error")
            mockDependenciesToHaveSyncStillRunning()

            val result = countWorker.doWork()

            assertThat(result).isEqualTo(ListenableWorker.Result.retry())
        }
    }

    @Test
    fun countWorkerFailed_syncIsNotRunning_shouldSucceed() {
        runTest {
            coEvery { eventDownSyncCountTask.getCount(any()) } throws Throwable("IO Error")
            mockDependenciesToHaveSyncNotRunning()

            val result = countWorker.doWork()

            assertThat(result).isEqualTo(ListenableWorker.Result.success())
        }
    }

    private fun mockDependenciesToHaveSyncStillRunning() {
        mockWorkManagerToReturnDownloaderWorkInfo(WorkInfo.State.RUNNING)
    }

    private fun mockDependenciesToHaveSyncNotRunning() {
        mockWorkManagerToReturnDownloaderWorkInfo(WorkInfo.State.CANCELLED)
    }

    private fun mockWorkManagerToReturnDownloaderWorkInfo(state: WorkInfo.State) {
        val mockWm = mockk<WorkManager>(relaxed = true)
        val mockWorkInfo = mockk<ListenableFuture<List<WorkInfo>>>()
        every { mockWorkInfo.get() } returns listOf(
            WorkInfo(
                UUID.randomUUID(),
                state,
                workDataOf(),
                listOf(tagForMasterSyncId, tagForType(EventSyncWorkerType.DOWNLOADER)),
                workDataOf(),
                2,
                0
            )
        )
        every { mockWm.getWorkInfosByTag(any()) } returns mockWorkInfo
        countWorker.wm = mockWm
    }

}
