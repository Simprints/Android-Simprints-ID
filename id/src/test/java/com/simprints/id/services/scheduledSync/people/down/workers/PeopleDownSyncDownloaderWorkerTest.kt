package com.simprints.id.services.scheduledSync.people.down.workers

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Data
import androidx.work.WorkInfo
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.data.db.people_sync.down.domain.PeopleDownSyncOperation
import com.simprints.id.domain.modality.Modes
import com.simprints.id.exceptions.safe.sync.SyncCloudIntegrationException
import com.simprints.id.services.scheduledSync.people.down.workers.PeopleDownSyncDownloaderWorker.Companion.INPUT_DOWN_SYNC_OPS
import com.simprints.id.services.scheduledSync.people.down.workers.PeopleDownSyncDownloaderWorker.Companion.OUTPUT_DOWN_SYNC
import com.simprints.id.services.scheduledSync.people.down.workers.PeopleDownSyncDownloaderWorker.Companion.PROGRESS_DOWN_SYNC
import com.simprints.id.services.scheduledSync.people.master.internal.OUTPUT_FAILED_BECAUSE_CLOUD_INTEGRATION
import com.simprints.id.services.scheduledSync.people.master.internal.PeopleSyncCache
import com.simprints.id.testtools.TestApplication
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import io.mockk.*
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.util.*

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class PeopleDownSyncDownloaderWorkerTest {

    private val app = ApplicationProvider.getApplicationContext() as TestApplication
    private lateinit var peopleDownSyncDownloaderWorker: PeopleDownSyncDownloaderWorker

    private val projectSyncOp = PeopleDownSyncOperation(
        DEFAULT_PROJECT_ID,
        null,
        null,
        listOf(Modes.FINGERPRINT),
        null
    )


    @Before
    fun setUp() {
        UnitTestConfig(this).setupWorkManager()
        app.component = mockk(relaxed = true)
        val correctInputData = JsonHelper.gson.toJson(projectSyncOp)
        peopleDownSyncDownloaderWorker = createWorker(workDataOf(INPUT_DOWN_SYNC_OPS to correctInputData))
        coEvery { peopleDownSyncDownloaderWorker.downSyncScopeRepository.refreshDownSyncOperationFromDb(any()) } returns null
    }

    @Test
    fun worker_shouldParseInputDataCorrectly() = runBlockingTest {
        with(peopleDownSyncDownloaderWorker) {
            doWork()
            coEvery { peopleDownSyncDownloaderTask.execute(projectSyncOp, any(), any()) }
        }
    }

    @Test
    fun worker_shouldExecuteTheTask() = runBlockingTest {
        with(peopleDownSyncDownloaderWorker) {
            coEvery { peopleDownSyncDownloaderTask.execute(any(), any(), any()) } returns 0

            doWork()

            coVerify { peopleDownSyncDownloaderTask.execute(any(), any(), any()) }
            verify { resultSetter.success(workDataOf(OUTPUT_DOWN_SYNC to 0)) }
        }
    }


    @Test
    fun worker_failForCloudIntegration_shouldFail() = runBlockingTest {
        with(peopleDownSyncDownloaderWorker) {
            coEvery { peopleDownSyncDownloaderTask.execute(any(), any(), any()) } throws SyncCloudIntegrationException("Cloud integration", Throwable())

            doWork()

            verify { resultSetter.failure(workDataOf(OUTPUT_FAILED_BECAUSE_CLOUD_INTEGRATION to true)) }
        }
    }

    @Test
    fun worker_failForNetworkIssue_shouldRetry() = runBlockingTest {
        with(peopleDownSyncDownloaderWorker) {
            coEvery { peopleDownSyncDownloaderTask.execute(any(), any(), any()) } throws Throwable("Network Exception")

            doWork()

            verify { resultSetter.retry() }
        }
    }

    @Test
    fun worker_inputDataIsWrong_shouldFail() = runBlockingTest {
        peopleDownSyncDownloaderWorker = createWorker(workDataOf(INPUT_DOWN_SYNC_OPS to ""))
        with(peopleDownSyncDownloaderWorker) {

            doWork()

            verify { resultSetter.failure(any()) }
        }
    }

    @Test
    fun worker_progressCountInProgressData_shouldExtractTheProgressCountCorrectly() = runBlockingTest {
        val progress = 2
        val syncCacheMock = mockk<PeopleSyncCache>()
        every { syncCacheMock.readProgress(any()) } returns 1

        val workInfo = WorkInfo(UUID.randomUUID(), WorkInfo.State.RUNNING, workDataOf(), listOf(), workDataOf(PROGRESS_DOWN_SYNC to progress), 2)
        assertThat(workInfo.extractDownSyncProgress(syncCacheMock)).isEqualTo(progress)
    }

    @Test
    fun worker_SyncDown_shouldExtractTheFinalProgressCountCorrectly() = runBlockingTest {
        val progress = 2
        val syncCacheMock = mockk<PeopleSyncCache>()
        every { syncCacheMock.readProgress(any()) } returns 1

        val workInfo = WorkInfo(UUID.randomUUID(), WorkInfo.State.SUCCEEDED, workDataOf(OUTPUT_DOWN_SYNC to progress), listOf(), workDataOf(), 2)
        assertThat(workInfo.extractDownSyncProgress(syncCacheMock)).isEqualTo(progress)
    }

    @Test
    fun workerResumed_progressCountInCache_shouldExtractTheProgressCountCorrectly() = runBlockingTest {
        val progress = 2
        val syncCacheMock = mockk<PeopleSyncCache>()
        every { syncCacheMock.readProgress(any()) } returns progress

        val workInfo = WorkInfo(UUID.randomUUID(), WorkInfo.State.RUNNING, workDataOf(), listOf(), workDataOf(), 2)
        assertThat(workInfo.extractDownSyncProgress(syncCacheMock)).isEqualTo(progress)
    }

    private fun createWorker(inputData: Data? = null) =
        (inputData?.let {
            TestListenableWorkerBuilder<PeopleDownSyncDownloaderWorker>(app, inputData = it).build()
        } ?: TestListenableWorkerBuilder<PeopleDownSyncDownloaderWorker>(app).build()).apply {
            crashReportManager = mockk(relaxed = true)
            resultSetter = mockk(relaxed = true)
            peopleDownSyncDownloaderTask = mockk(relaxed = true)
            downSyncScopeRepository = mockk(relaxed = true)
        }
}

