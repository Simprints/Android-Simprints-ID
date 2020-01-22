package com.simprints.id.services.scheduledSync.people.master.workers

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Data
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import com.simprints.id.services.scheduledSync.people.common.TAG_MASTER_SYNC_ID
import com.simprints.id.services.scheduledSync.people.master.workers.PeopleLastSyncReporterWorker.Companion.SYNC_ID_TO_MARK_AS_COMPLETED
import com.simprints.id.testtools.TestApplication
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.util.*

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class PeopleLastSyncReporterWorkerTest {

    private val syncId = UUID.randomUUID().toString()
    private val tagForMasterSyncId = "$TAG_MASTER_SYNC_ID$syncId"

    private val app = ApplicationProvider.getApplicationContext() as TestApplication

    private lateinit var lastSyncReportWorker: PeopleLastSyncReporterWorker

    @Before
    fun setUp() {
        UnitTestConfig(this).setupWorkManager()

        lastSyncReportWorker = createWorker(workDataOf(SYNC_ID_TO_MARK_AS_COMPLETED to syncId))
        app.component = mockk(relaxed = true)
    }

    @Test
    fun worker_withInvalidSyncIdAsInput_shouldFail() = runBlockingTest {
        lastSyncReportWorker = createWorker(workDataOf(SYNC_ID_TO_MARK_AS_COMPLETED to ""))
        with(lastSyncReportWorker) {
            doWork()
            verify { resultSetter.failure(any()) }
        }
    }

    @Test
    fun worker_withValidSyncIdAsInput_shouldReportLastSyncTime() = runBlockingTest {
        with(lastSyncReportWorker) {
            doWork()
            verify { syncCache.storeLastSuccessfulSyncTime(any()) }
            verify { resultSetter.success() }
        }
    }

    private fun createWorker(inputData: Data): PeopleLastSyncReporterWorker =
        (TestListenableWorkerBuilder<PeopleLastSyncReporterWorker>(app)
            .setTags(listOf(tagForMasterSyncId))
            .setInputData(inputData)
            .build() as PeopleLastSyncReporterWorker)
            .apply {
                crashReportManager = mockk(relaxed = true)
                resultSetter = mockk(relaxed = true)
                syncCache = mockk(relaxed = true)

            }
}
