package com.simprints.infra.eventsync.sync.up.workers

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.common.truth.Truth.assertThat
import com.google.common.util.concurrent.ListenableFuture
import com.simprints.core.tools.json.JsonHelper
import com.simprints.infra.events.EventRepository
import com.simprints.infra.eventsync.SampleSyncScopes.projectUpSyncScope
import com.simprints.infra.eventsync.status.models.EventSyncWorkerType
import com.simprints.infra.eventsync.status.models.EventSyncWorkerType.Companion.tagForType
import com.simprints.infra.eventsync.sync.common.TAG_MASTER_SYNC_ID
import com.simprints.infra.eventsync.sync.up.workers.EventUpSyncCountWorker.Companion.INPUT_COUNT_WORKER_UP
import com.simprints.infra.eventsync.sync.up.workers.EventUpSyncCountWorker.Companion.OUTPUT_COUNT_WORKER_UP
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
internal class EventUpSyncCountWorkerTest {

    private val syncId = UUID.randomUUID().toString()
    private val tagForMasterSyncId = "$TAG_MASTER_SYNC_ID$syncId"

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    lateinit var eventRepository: EventRepository

    lateinit var countWorker: EventUpSyncCountWorker

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        countWorker = EventUpSyncCountWorker(
            mockk(relaxed = true),
            mockk(relaxed = true) {
                every { inputData } returns workDataOf(
                    INPUT_COUNT_WORKER_UP to JsonHelper.toJson(projectUpSyncScope)
                )
                every { tags } returns setOf(tagForMasterSyncId)
            },
            eventRepository,
            JsonHelper,
            testCoroutineRule.testCoroutineDispatcher
        )
    }

    @Test
    fun countWorker_shouldExtractTheUpSyncScopeFromTheRepo() = runTest {
        countWorker.doWork()

        coVerify { eventRepository.observeEventCount(any(), any()) }
    }

    @Test
    fun countWorker_shouldExecuteTheTaskSuccessfully() = runTest {
        coEvery { eventRepository.observeEventCount(any(), any()) } returns flowOf(1)

        val result = countWorker.doWork()

        val expectedSuccessfulOutput = workDataOf(OUTPUT_COUNT_WORKER_UP to 1)
        assertThat(result).isEqualTo(ListenableWorker.Result.success(expectedSuccessfulOutput))
    }

    @Test
    fun countWorkerFailed_shouldFail() = runTest {
        coEvery { eventRepository.observeEventCount(any(), any()) } throws Throwable("IO Error")
        mockWorkManagerToReturnDownloaderWorkInfo(WorkInfo.State.RUNNING)

        val result = countWorker.doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.failure())
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
    }
}
