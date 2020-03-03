package com.simprints.id.services.scheduledSync.people.master.workers

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Data
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import com.simprints.id.services.scheduledSync.people.common.TAG_MASTER_SYNC_ID
import com.simprints.id.services.scheduledSync.people.master.workers.PeopleEndSyncReporterWorker.Companion.SYNC_ID_TO_MARK_AS_COMPLETED
import com.simprints.id.testtools.TestApplication
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.util.*

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class PeopleEndSyncReporterWorkerTest {

    private val syncId = UUID.randomUUID().toString()
    private val tagForMasterSyncId = "$TAG_MASTER_SYNC_ID$syncId"

    private val app = ApplicationProvider.getApplicationContext() as TestApplication

    private lateinit var endSyncReportWorker: PeopleEndSyncReporterWorker

    @Before
    fun setUp() {
        UnitTestConfig(this).setupWorkManager()

        endSyncReportWorker = createWorker(workDataOf(SYNC_ID_TO_MARK_AS_COMPLETED to syncId))
        app.component = mockk(relaxed = true)
    }

    @Test
    fun worker_withInvalidSyncIdAsInput_shouldFail() = runBlocking {
        endSyncReportWorker = createWorker(workDataOf(SYNC_ID_TO_MARK_AS_COMPLETED to ""))
        with(endSyncReportWorker) {
            doWork()
            verify { resultSetter.failure(any()) }
        }
    }

    @Test
    fun worker_withValidSyncIdAsInput_shouldReportLastSyncTime() = runBlocking {
        with(endSyncReportWorker) {
            doWork()
            verify { syncCache.storeLastSuccessfulSyncTime(any()) }
            verify { resultSetter.success() }
        }
    }

    private fun createWorker(inputData: Data): PeopleEndSyncReporterWorker =
        (TestListenableWorkerBuilder<PeopleEndSyncReporterWorker>(app)
            .setTags(listOf(tagForMasterSyncId))
            .setInputData(inputData)
            .build() as PeopleEndSyncReporterWorker)
            .apply {
                crashReportManager = mockk(relaxed = true)
                resultSetter = mockk(relaxed = true)
                syncCache = mockk(relaxed = true)

            }
}
