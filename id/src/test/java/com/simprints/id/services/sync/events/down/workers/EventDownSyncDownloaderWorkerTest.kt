package com.simprints.id.services.sync.events.down.workers

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.WorkInfo
import androidx.work.WorkInfo.State.RUNNING
import androidx.work.WorkInfo.State.SUCCEEDED
import androidx.work.workDataOf
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.json.JsonHelper
import com.simprints.eventsystem.event.remote.exceptions.TooManyRequestsException
import com.simprints.eventsystem.events_sync.down.EventDownSyncScopeRepository
import com.simprints.eventsystem.sampledata.SampleDefaults.projectDownSyncScope
import com.simprints.id.services.sync.events.down.EventDownSyncHelper
import com.simprints.id.services.sync.events.down.workers.EventDownSyncDownloaderWorker.Companion.INPUT_DOWN_SYNC_OPS
import com.simprints.id.services.sync.events.down.workers.EventDownSyncDownloaderWorker.Companion.OUTPUT_DOWN_SYNC
import com.simprints.id.services.sync.events.down.workers.EventDownSyncDownloaderWorker.Companion.PROGRESS_DOWN_SYNC
import com.simprints.id.services.sync.events.master.internal.*
import com.simprints.infra.network.exceptions.BackendMaintenanceException
import com.simprints.infra.network.exceptions.SyncCloudIntegrationException
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class EventDownSyncDownloaderWorkerTest {

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private val downSyncHelper = mockk<EventDownSyncHelper>()
    private val eventDownSyncScopeRepository = mockk<EventDownSyncScopeRepository>()
    private val syncCache = mockk<EventSyncCache>()
    private val eventDownSyncDownloaderTask = mockk<EventDownSyncDownloaderTask>()
    private val eventDownSyncDownloaderWorker = EventDownSyncDownloaderWorker(
        mockk(relaxed = true),
        mockk(relaxed = true) {
            every { inputData } returns workDataOf(
                INPUT_DOWN_SYNC_OPS to JsonHelper.toJson(
                    projectDownSyncScope.operations.first()
                )
            )
        },
        downSyncHelper,
        eventDownSyncScopeRepository,
        syncCache,
        eventDownSyncDownloaderTask,
        JsonHelper,
        testCoroutineRule.testCoroutineDispatcher,
    )

    @Test
    fun worker_shouldExecuteTheTask() {
        runTest {
            coEvery {
                eventDownSyncDownloaderTask.execute(
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any()
                )
            } returns 0

            val result = eventDownSyncDownloaderWorker.doWork()

            assertThat(result).isEqualTo(ListenableWorker.Result.success(workDataOf(OUTPUT_DOWN_SYNC to 0)))
        }
    }

    @Test
    fun worker_failForCloudIntegration_shouldFail() = runTest {
        coEvery {
            eventDownSyncDownloaderTask.execute(
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } throws SyncCloudIntegrationException("Cloud integration", Throwable())

        val result = eventDownSyncDownloaderWorker.doWork()

        assertThat(result).isEqualTo(
            ListenableWorker.Result.failure(
                workDataOf(
                    OUTPUT_FAILED_BECAUSE_CLOUD_INTEGRATION to true
                )
            )
        )
    }

    @Test
    fun worker_failForBackendMaintenanceError_shouldFail() = runTest {
        coEvery {
            eventDownSyncDownloaderTask.execute(
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } throws BackendMaintenanceException(estimatedOutage = null)

        val result = eventDownSyncDownloaderWorker.doWork()

        assertThat(result).isEqualTo(
            ListenableWorker.Result.failure(
                workDataOf(
                    OUTPUT_FAILED_BECAUSE_BACKEND_MAINTENANCE to true,
                    OUTPUT_ESTIMATED_MAINTENANCE_TIME to null
                )
            )
        )
    }

    @Test
    fun worker_failForTimedBackendMaintenanceError_shouldFail() = runTest {
        coEvery {
            eventDownSyncDownloaderTask.execute(
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } throws BackendMaintenanceException(estimatedOutage = 600)

        val result = eventDownSyncDownloaderWorker.doWork()

        assertThat(result).isEqualTo(
            ListenableWorker.Result.failure(
                workDataOf(
                    OUTPUT_FAILED_BECAUSE_BACKEND_MAINTENANCE to true,
                    OUTPUT_ESTIMATED_MAINTENANCE_TIME to 600L
                )
            )
        )
    }

    @Test
    fun worker_failForTooManyRequestsError_shouldFail() = runTest {
        coEvery {
            eventDownSyncDownloaderTask.execute(
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } throws TooManyRequestsException()

        val result = eventDownSyncDownloaderWorker.doWork()

        assertThat(result).isEqualTo(
            ListenableWorker.Result.failure(
                workDataOf(
                    OUTPUT_FAILED_BECAUSE_TOO_MANY_REQUESTS to true
                )
            )
        )
    }

    @Test
    fun worker_failForNetworkIssue_shouldRetry() = runTest {
        coEvery {
            eventDownSyncDownloaderTask.execute(
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } throws Throwable("Network Exception")

        val result = eventDownSyncDownloaderWorker.doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.retry())
    }

    @Test
    fun worker_progressCountInProgressData_shouldExtractTheProgressCountCorrectly() = runTest {
        val progress = 2
        val syncCacheMock = mockk<EventSyncCache>()
        coEvery { syncCacheMock.readProgress(any()) } returns 1

        val workInfo = WorkInfo(
            UUID.randomUUID(),
            RUNNING,
            workDataOf(),
            listOf(),
            workDataOf(PROGRESS_DOWN_SYNC to progress),
            2
        )
        assertThat(workInfo.extractDownSyncProgress(syncCacheMock)).isEqualTo(progress)
    }

    @Test
    fun worker_SyncDown_shouldExtractTheFinalProgressCountCorrectly() = runTest {
        val progress = 2
        val syncCacheMock = mockk<EventSyncCache>()
        coEvery { syncCacheMock.readProgress(any()) } returns 1

        val workInfo = WorkInfo(
            UUID.randomUUID(),
            SUCCEEDED,
            workDataOf(OUTPUT_DOWN_SYNC to progress),
            listOf(),
            workDataOf(),
            2
        )
        assertThat(workInfo.extractDownSyncProgress(syncCacheMock)).isEqualTo(progress)
    }

    @Test
    fun workerResumed_progressCountInCache_shouldExtractTheProgressCountCorrectly() = runTest {
        val progress = 2
        val syncCacheMock = mockk<EventSyncCache>()
        coEvery { syncCacheMock.readProgress(any()) } returns progress

        val workInfo = WorkInfo(
            UUID.randomUUID(),
            RUNNING,
            workDataOf(),
            listOf(),
            workDataOf(),
            2
        )
        assertThat(workInfo.extractDownSyncProgress(syncCacheMock)).isEqualTo(progress)
    }
}

