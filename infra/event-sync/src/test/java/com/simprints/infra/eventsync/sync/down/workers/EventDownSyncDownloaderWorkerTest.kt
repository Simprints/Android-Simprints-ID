package com.simprints.infra.eventsync.sync.down.workers

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.WorkInfo
import androidx.work.WorkInfo.State.RUNNING
import androidx.work.WorkInfo.State.SUCCEEDED
import androidx.work.workDataOf
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.json.JsonHelper
import com.simprints.infra.eventsync.SampleSyncScopes.projectDownSyncScope
import com.simprints.infra.eventsync.event.remote.exceptions.TooManyRequestsException
import com.simprints.infra.eventsync.status.down.EventDownSyncScopeRepository
import com.simprints.infra.eventsync.sync.common.*
import com.simprints.infra.eventsync.sync.down.tasks.EventDownSyncTask
import com.simprints.infra.eventsync.sync.down.workers.EventDownSyncDownloaderWorker.Companion.INPUT_DOWN_SYNC_OPS
import com.simprints.infra.eventsync.sync.down.workers.EventDownSyncDownloaderWorker.Companion.OUTPUT_DOWN_SYNC
import com.simprints.infra.eventsync.sync.down.workers.EventDownSyncDownloaderWorker.Companion.PROGRESS_DOWN_SYNC
import com.simprints.infra.network.exceptions.BackendMaintenanceException
import com.simprints.infra.network.exceptions.SyncCloudIntegrationException
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
internal class EventDownSyncDownloaderWorkerTest {

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    lateinit var downSyncTask: EventDownSyncTask

    @MockK
    lateinit var eventDownSyncScopeRepository: EventDownSyncScopeRepository

    @MockK
    lateinit var syncCache: EventSyncCache

    private lateinit var eventDownSyncDownloaderWorker: EventDownSyncDownloaderWorker

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        eventDownSyncDownloaderWorker = EventDownSyncDownloaderWorker(
            mockk(relaxed = true),
            mockk(relaxed = true) {
                every { inputData } returns workDataOf(
                    INPUT_DOWN_SYNC_OPS to JsonHelper.toJson(projectDownSyncScope.operations.first())
                )
            },
            downSyncTask,
            eventDownSyncScopeRepository,
            syncCache,
            JsonHelper,
            testCoroutineRule.testCoroutineDispatcher,
        )
    }

    @Test
    fun worker_failForCloudIntegration_shouldFail() = runTest {
        coEvery {
            downSyncTask.downSync(any(), any())
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
            downSyncTask.downSync(any(), any())
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
            downSyncTask.downSync(any(), any())
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
            downSyncTask.downSync(any(), any())
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
            downSyncTask.downSync(any(), any())
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
            setOf(),
            workDataOf(),
            workDataOf(PROGRESS_DOWN_SYNC to progress),
            2,
            0
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
            setOf(),
            workDataOf(OUTPUT_DOWN_SYNC to progress),
            workDataOf(),
            2,
            1
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
            setOf(),
            workDataOf(),
            workDataOf(),
            2,
            1
        )
        assertThat(workInfo.extractDownSyncProgress(syncCacheMock)).isEqualTo(progress)
    }
}

