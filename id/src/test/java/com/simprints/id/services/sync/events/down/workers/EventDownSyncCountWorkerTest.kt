package com.simprints.id.services.sync.events.down.workers

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.common.truth.Truth.assertThat
import com.google.common.util.concurrent.ListenableFuture
import com.simprints.core.tools.json.JsonHelper
import com.simprints.eventsystem.event.domain.EventCount
import com.simprints.eventsystem.event.domain.models.subject.EnrolmentRecordEventType
import com.simprints.eventsystem.events_sync.down.EventDownSyncScopeRepository
import com.simprints.eventsystem.events_sync.models.EventSyncWorkerType
import com.simprints.eventsystem.events_sync.models.EventSyncWorkerType.Companion.tagForType
import com.simprints.eventsystem.sampledata.SampleDefaults.projectDownSyncScope
import com.simprints.id.services.sync.events.common.TAG_MASTER_SYNC_ID
import com.simprints.id.services.sync.events.down.EventDownSyncHelper
import com.simprints.id.services.sync.events.down.workers.EventDownSyncCountWorker.Companion.INPUT_COUNT_WORKER_DOWN
import com.simprints.id.services.sync.events.down.workers.EventDownSyncCountWorker.Companion.OUTPUT_COUNT_WORKER_DOWN
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class EventDownSyncCountWorkerTest {

    private val syncId = UUID.randomUUID().toString()
    private val tagForMasterSyncId = "${TAG_MASTER_SYNC_ID}$syncId"

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private val eventDownSyncHelper = mockk<EventDownSyncHelper>()
    private val eventDownSyncScopeRepository = mockk<EventDownSyncScopeRepository>()

    private val countWorker = EventDownSyncCountWorker(
        mockk(relaxed = true),
        mockk(relaxed = true) {
            every { inputData } returns workDataOf(
                INPUT_COUNT_WORKER_DOWN to JsonHelper.toJson(
                    projectDownSyncScope
                )
            )
            every { tags } returns setOf(tagForMasterSyncId)
        },
        eventDownSyncHelper,
        JsonHelper,
        eventDownSyncScopeRepository,
        testCoroutineRule.testCoroutineDispatcher
    )

    @Test
    fun countWorker_shouldExtractTheDownSyncScopeFromTheRepo() {
        runTest {
            countWorker.doWork()

            coVerify { eventDownSyncScopeRepository.refreshState(any()) }
        }
    }

    @Test
    fun countWorker_shouldExecuteTheTaskSuccessfully() {
        runTest {
            val counts = EventCount(EnrolmentRecordEventType.EnrolmentRecordMove, 1)
            mockDependenciesToSucceed(counts)

            val result = countWorker.doWork()

            val output = JsonHelper.toJson(listOf(counts))
            val expectedSuccessfulOutput = workDataOf(OUTPUT_COUNT_WORKER_DOWN to output)
            assertThat(result).isEqualTo(ListenableWorker.Result.success(expectedSuccessfulOutput))
        }
    }

    @Test
    fun countWorkerFailed_syncStillRunning_shouldRetry() {
        runTest {
            coEvery { eventDownSyncHelper.countForDownSync(any()) } throws Throwable("IO Error")
            coEvery {
                eventDownSyncScopeRepository.getDownSyncScope(
                    any(),
                    any(),
                    any()
                )
            } returns projectDownSyncScope
            mockDependenciesToHaveSyncStillRunning()

            val result = countWorker.doWork()

            assertThat(result).isEqualTo(ListenableWorker.Result.retry())
        }
    }

    @Test
    fun countWorkerFailed_syncIsNotRunning_shouldSucceed() {
        runTest {
            coEvery { eventDownSyncHelper.countForDownSync(any()) } throws Throwable("IO Error")
            coEvery {
                eventDownSyncScopeRepository.getDownSyncScope(
                    any(),
                    any(),
                    any()
                )
            } returns projectDownSyncScope
            mockDependenciesToHaveSyncNotRunning()

            val result = countWorker.doWork()

            assertThat(result).isEqualTo(ListenableWorker.Result.success())
        }
    }

    private fun mockDependenciesToSucceed(counts: EventCount) {
        coEvery { eventDownSyncHelper.countForDownSync(any()) } returns listOf(counts)
        coEvery {
            eventDownSyncScopeRepository.getDownSyncScope(
                any(),
                any(),
                any()
            )
        } returns projectDownSyncScope
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
