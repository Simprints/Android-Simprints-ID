package com.simprints.id.services.scheduledSync.sync.peopleDownSync.workers.downsync

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.work.Data
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.data.db.people_sync.down.domain.PeopleDownSyncOperation
import com.simprints.id.domain.modality.Modes
import com.simprints.id.services.scheduledSync.sync.peopleDownSync.workers.downsync.DownSyncWorker.Companion.DOWN_SYNC_WORKER_INPUT
import com.simprints.id.testtools.TestApplication
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@SmallTest
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class DownSyncWorkerTest {

    private val app = ApplicationProvider.getApplicationContext() as TestApplication
    private lateinit var downSyncWorker: DownSyncWorker

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
        downSyncWorker = createWorker()
    }

    @Test
    fun downSyncWorker_shouldExecuteTheTask() = runBlockingTest {
        val correctInputData = JsonHelper.gson.toJson(projectSyncOp)
        downSyncWorker = createWorker(workDataOf(DOWN_SYNC_WORKER_INPUT to correctInputData))

        downSyncWorker.doWork()

        coVerify { downSyncWorker.downSyncTask.execute(any(), any()) }
        verify { downSyncWorker.resultSetter.success() }
    }


    @Test
    fun downSyncWorker_shouldParseInputDataCorrectly() = runBlockingTest {
        val correctInputData = JsonHelper.gson.toJson(projectSyncOp)
        downSyncWorker = createWorker(workDataOf(DOWN_SYNC_WORKER_INPUT to correctInputData))

        val data = downSyncWorker.jsonForOp

        assertThat(data).isEqualTo(correctInputData)
    }

    @Test
    fun downSyncWorker_wrongInput_shouldFail() = runBlockingTest {
        downSyncWorker.doWork()

        verify { downSyncWorker.resultSetter.failure() }
    }

    private fun createWorker(inputData: Data? = null) =
        (inputData?.let {
            TestListenableWorkerBuilder<DownSyncWorker>(app, inputData = it).build()
        } ?: TestListenableWorkerBuilder<DownSyncWorker>(app).build()).apply {
            crashReportManager = mockk(relaxed = true)
            resultSetter = mockk(relaxed = true)
            downSyncTask = mockk(relaxed = true)
        }
}

