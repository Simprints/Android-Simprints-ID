package com.simprints.id.services.scheduledSync.people.down.workers

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Data
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.data.db.people_sync.down.domain.PeopleDownSyncOperation
import com.simprints.id.domain.modality.Modes
import com.simprints.id.services.scheduledSync.people.down.workers.PeopleDownSyncDownloaderWorker.Companion.INPUT_DOWN_SYNC_OPS
import com.simprints.id.services.scheduledSync.people.down.workers.PeopleDownSyncDownloaderWorker.Companion.OUTPUT_DOWN_SYNC
import com.simprints.id.testtools.TestApplication
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

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
        peopleDownSyncDownloaderWorker = createWorker()
    }

    @Test
    fun downSyncWorker_shouldExecuteTheTask() = runBlockingTest {
        val correctInputData = JsonHelper.gson.toJson(projectSyncOp)
        peopleDownSyncDownloaderWorker = createWorker(workDataOf(INPUT_DOWN_SYNC_OPS to correctInputData))
        coEvery { peopleDownSyncDownloaderWorker.peopleDownSyncDownloaderTask.execute(any(), any(), any()) } returns 0
        coEvery { peopleDownSyncDownloaderWorker.downSyncScopeRepository.refreshDownSyncOperationFromDb(any()) } returns null

        peopleDownSyncDownloaderWorker.doWork()

        coVerify { peopleDownSyncDownloaderWorker.peopleDownSyncDownloaderTask.execute(any(), any(), any()) }
        verify { peopleDownSyncDownloaderWorker.resultSetter.success(workDataOf(OUTPUT_DOWN_SYNC to 0)) }
    }


    @Test
    fun downSyncWorker_shouldParseInputDataCorrectly() = runBlockingTest {
        val correctInputData = JsonHelper.gson.toJson(projectSyncOp)
        peopleDownSyncDownloaderWorker = createWorker(workDataOf(INPUT_DOWN_SYNC_OPS to correctInputData))

        peopleDownSyncDownloaderWorker.doWork()

        coEvery { peopleDownSyncDownloaderWorker.peopleDownSyncDownloaderTask.execute(projectSyncOp, any(), any()) }
    }

    @Test
    fun downSyncWorker_wrongInput_shouldFail() = runBlockingTest {
        peopleDownSyncDownloaderWorker.doWork()

        verify { peopleDownSyncDownloaderWorker.resultSetter.failure() }
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

