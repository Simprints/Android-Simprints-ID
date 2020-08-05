package com.simprints.id.services.sync.subjects.down.workers

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Data
import androidx.work.WorkInfo
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.data.db.subjects_sync.down.domain.EventsDownSyncOperation
import com.simprints.id.domain.modality.Modes
import com.simprints.id.exceptions.safe.sync.SyncCloudIntegrationException
import com.simprints.id.services.sync.subjects.down.workers.SubjectsDownSyncDownloaderWorker.Companion.INPUT_DOWN_SYNC_OPS
import com.simprints.id.services.sync.subjects.down.workers.SubjectsDownSyncDownloaderWorker.Companion.OUTPUT_DOWN_SYNC
import com.simprints.id.services.sync.subjects.down.workers.SubjectsDownSyncDownloaderWorker.Companion.PROGRESS_DOWN_SYNC
import com.simprints.id.services.sync.subjects.master.internal.OUTPUT_FAILED_BECAUSE_CLOUD_INTEGRATION
import com.simprints.id.services.sync.subjects.master.internal.SubjectsSyncCache
import com.simprints.id.testtools.TestApplication
import com.simprints.id.testtools.UnitTestConfig
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
class SubjectsDownSyncDownloaderWorkerTest {

    private val app = ApplicationProvider.getApplicationContext() as TestApplication
    private lateinit var subjectsDownSyncDownloaderWorker: SubjectsDownSyncDownloaderWorker

    private val projectSyncOp = EventsDownSyncOperation(
        DEFAULT_PROJECT_ID,
        null,
        null,
        listOf(Modes.FINGERPRINT),
        null
    )


    @Before
    fun setUp() {
        UnitTestConfig(this).setupWorkManager().setupFirebase()
        app.component = mockk(relaxed = true)
        val correctInputData = JsonHelper().toJson(projectSyncOp)
        subjectsDownSyncDownloaderWorker = createWorker(workDataOf(INPUT_DOWN_SYNC_OPS to correctInputData))

        coEvery { subjectsDownSyncDownloaderWorker.downSyncScopeRepository.refreshDownSyncOperationFromDb(any()) } returns null
        subjectsDownSyncDownloaderWorker.subjectsDownSyncDownloaderTask = mockk(relaxed = true)
        subjectsDownSyncDownloaderWorker.jsonHelper = JsonHelper()
    }

    @Test
    fun worker_shouldParseInputDataCorrectly() = runBlocking<Unit> {
        with(subjectsDownSyncDownloaderWorker) {
            doWork()
        }
    }

    @Test
    fun worker_shouldExecuteTheTask() {
        runBlocking {
            with(subjectsDownSyncDownloaderWorker) {
                coEvery { subjectsDownSyncDownloaderTask.execute(any(), any(), any(), any(), any(), any()) } returns 0

                doWork()

                verify { resultSetter.success(workDataOf(OUTPUT_DOWN_SYNC to 0)) }
            }
        }
    }

    @Test
    fun worker_failForCloudIntegration_shouldFail() = runBlocking<Unit> {
        with(subjectsDownSyncDownloaderWorker) {
            coEvery { subjectsDownSyncDownloaderTask.execute(any(), any(), any(), any(), any(), any()) } throws SyncCloudIntegrationException("Cloud integration", Throwable())

            doWork()

            verify { resultSetter.failure(workDataOf(OUTPUT_FAILED_BECAUSE_CLOUD_INTEGRATION to true)) }
        }
    }

    @Test
    fun worker_failForNetworkIssue_shouldRetry() = runBlocking<Unit> {
        with(subjectsDownSyncDownloaderWorker) {
            coEvery { subjectsDownSyncDownloaderTask.execute(any(), any(), any(), any(), any(), any()) } throws Throwable("Network Exception")

            doWork()

            verify { resultSetter.retry() }
        }
    }

    @Test
    fun worker_inputDataIsWrong_shouldFail() = runBlocking {
        subjectsDownSyncDownloaderWorker = createWorker(workDataOf(INPUT_DOWN_SYNC_OPS to "error"))
        with(subjectsDownSyncDownloaderWorker) {

            doWork()

            verify { resultSetter.failure(any()) }
        }
    }

    @Test
    fun worker_progressCountInProgressData_shouldExtractTheProgressCountCorrectly() = runBlocking {
        val progress = 2
        val syncCacheMock = mockk<SubjectsSyncCache>()
        every { syncCacheMock.readProgress(any()) } returns 1

        val workInfo = WorkInfo(UUID.randomUUID(), WorkInfo.State.RUNNING, workDataOf(), listOf(), workDataOf(PROGRESS_DOWN_SYNC to progress), 2)
        assertThat(workInfo.extractDownSyncProgress(syncCacheMock)).isEqualTo(progress)
    }

    @Test
    fun worker_SyncDown_shouldExtractTheFinalProgressCountCorrectly() = runBlocking {
        val progress = 2
        val syncCacheMock = mockk<SubjectsSyncCache>()
        every { syncCacheMock.readProgress(any()) } returns 1

        val workInfo = WorkInfo(UUID.randomUUID(), WorkInfo.State.SUCCEEDED, workDataOf(OUTPUT_DOWN_SYNC to progress), listOf(), workDataOf(), 2)
        assertThat(workInfo.extractDownSyncProgress(syncCacheMock)).isEqualTo(progress)
    }

    @Test
    fun workerResumed_progressCountInCache_shouldExtractTheProgressCountCorrectly() = runBlocking {
        val progress = 2
        val syncCacheMock = mockk<SubjectsSyncCache>()
        every { syncCacheMock.readProgress(any()) } returns progress

        val workInfo = WorkInfo(UUID.randomUUID(), WorkInfo.State.RUNNING, workDataOf(), listOf(), workDataOf(), 2)
        assertThat(workInfo.extractDownSyncProgress(syncCacheMock)).isEqualTo(progress)
    }

    private fun createWorker(inputData: Data? = null) =
        (inputData?.let {
            TestListenableWorkerBuilder<SubjectsDownSyncDownloaderWorker>(app, inputData = it).build()
        } ?: TestListenableWorkerBuilder<SubjectsDownSyncDownloaderWorker>(app).build()).apply {
            crashReportManager = mockk(relaxed = true)
            resultSetter = mockk(relaxed = true)
            downSyncScopeRepository = mockk(relaxed = true)
            subjectRepository = mockk(relaxed = true)
            subjectsSyncCache = mockk(relaxed = true)
        }
}

