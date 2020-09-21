package com.simprints.id.services.sync.subjects.down.workers

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Data
import androidx.work.WorkInfo
import androidx.work.WorkInfo.State
import androidx.work.WorkInfo.State.RUNNING
import androidx.work.WorkInfo.State.SUCCEEDED
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.commontesttools.DefaultTestConstants.projectDownSyncScope
import com.simprints.id.data.db.events_sync.down.domain.EventDownSyncOperation
import com.simprints.id.exceptions.safe.sync.SyncCloudIntegrationException
import com.simprints.id.services.sync.events.down.workers.EventDownSyncDownloaderWorker
import com.simprints.id.services.sync.events.down.workers.EventDownSyncDownloaderWorker.Companion.INPUT_DOWN_SYNC_OPS
import com.simprints.id.services.sync.events.down.workers.EventDownSyncDownloaderWorker.Companion.OUTPUT_DOWN_SYNC
import com.simprints.id.services.sync.events.down.workers.EventDownSyncDownloaderWorker.Companion.PROGRESS_DOWN_SYNC
import com.simprints.id.services.sync.events.down.workers.extractDownSyncProgress
import com.simprints.id.services.sync.events.master.internal.EventSyncCache
import com.simprints.id.services.sync.events.master.internal.OUTPUT_FAILED_BECAUSE_CLOUD_INTEGRATION
import com.simprints.id.testtools.TestApplication
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.util.*

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class EventDownSyncDownloaderWorkerTest {

    private val app = ApplicationProvider.getApplicationContext() as TestApplication
    private lateinit var eventDownSyncDownloaderWorker: EventDownSyncDownloaderWorker

    @Before
    fun setUp() {
        app.component = mockk(relaxed = true)
        val correctInputData = JsonHelper().toJson(projectDownSyncScope.operations.first())
        eventDownSyncDownloaderWorker = createWorker(workDataOf(INPUT_DOWN_SYNC_OPS to correctInputData))
        eventDownSyncDownloaderWorker.firebasePerformanceTraceFactory = mockk(relaxed = true)
        eventDownSyncDownloaderWorker.crashReportManager = mockk(relaxed = true)
    }

    @Test
    fun worker_shouldParseInputDataCorrectly() = runBlocking<Unit> {
        with(eventDownSyncDownloaderWorker) {
            doWork()
        }
    }

    @Test
    fun worker_shouldExecuteTheTask() {
        runBlocking {
            with(eventDownSyncDownloaderWorker) {
                coEvery { eventDownSyncDownloaderTask.execute(any(), any(), any(), any(), any(), any()) } returns 0

                doWork()

                verify { resultSetter.success(workDataOf(OUTPUT_DOWN_SYNC to 0)) }
            }
        }
    }

    @Test
    fun worker_failForCloudIntegration_shouldFail() = runBlocking<Unit> {
        with(eventDownSyncDownloaderWorker) {
            coEvery { eventDownSyncDownloaderTask.execute(any(), any(), any(), any(), any(), any()) } throws SyncCloudIntegrationException("Cloud integration", Throwable())

            doWork()

            verify { resultSetter.failure(workDataOf(OUTPUT_FAILED_BECAUSE_CLOUD_INTEGRATION to true)) }
        }
    }

    @Test
    fun worker_failForNetworkIssue_shouldRetry() = runBlocking<Unit> {
        with(eventDownSyncDownloaderWorker) {
            coEvery { eventDownSyncDownloaderTask.execute(any(), any(), any(), any(), any(), any()) } throws Throwable("Network Exception")

            doWork()

            verify { resultSetter.retry() }
        }
    }

    @Test
    fun worker_progressCountInProgressData_shouldExtractTheProgressCountCorrectly() = runBlocking {
        val progress = 2
        val syncCacheMock = mockk<EventSyncCache>()
        every { syncCacheMock.readProgress(any()) } returns 1

        val workInfo = WorkInfo(UUID.randomUUID(), State.RUNNING, workDataOf(), listOf(), workDataOf(PROGRESS_DOWN_SYNC to progress), 2)
        assertThat(workInfo.extractDownSyncProgress(syncCacheMock)).isEqualTo(progress)
    }

    @Test
    fun worker_SyncDown_shouldExtractTheFinalProgressCountCorrectly() = runBlocking {
        val progress = 2
        val syncCacheMock = mockk<EventSyncCache>()
        every { syncCacheMock.readProgress(any()) } returns 1

        val workInfo = WorkInfo(UUID.randomUUID(), SUCCEEDED, workDataOf(OUTPUT_DOWN_SYNC to progress), listOf(), workDataOf(), 2)
        assertThat(workInfo.extractDownSyncProgress(syncCacheMock)).isEqualTo(progress)
    }

    @Test
    fun workerResumed_progressCountInCache_shouldExtractTheProgressCountCorrectly() = runBlocking {
        val progress = 2
        val syncCacheMock = mockk<EventSyncCache>()
        every { syncCacheMock.readProgress(any()) } returns progress

        val workInfo = WorkInfo(UUID.randomUUID(), RUNNING, workDataOf(), listOf(), workDataOf(), 2)
        assertThat(workInfo.extractDownSyncProgress(syncCacheMock)).isEqualTo(progress)
    }

    private fun createWorker(inputData: Data? = null) =
        (inputData?.let {
            TestListenableWorkerBuilder<EventDownSyncDownloaderWorker>(app, inputData = it).build()
        } ?: TestListenableWorkerBuilder<EventDownSyncDownloaderWorker>(app).build()).apply {
            crashReportManager = mockk(relaxed = true)
            resultSetter = mockk(relaxed = true)
            eventDownSyncScopeRepository = mockk(relaxed = true)
            coEvery { eventDownSyncScopeRepository.refreshState(any()) } answers { this.args.first() as EventDownSyncOperation }
            syncCache = mockk(relaxed = true)
            jsonHelper = JsonHelper()
            eventDownSyncDownloaderTask = mockk(relaxed = true)
            downSyncHelper = mockk(relaxed = true)
        }
}

