package com.simprints.id.services.sync.events.master.workers

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Data
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import com.simprints.id.services.sync.events.common.TAG_MASTER_SYNC_ID
import com.simprints.id.services.sync.events.master.workers.EventEndSyncReporterWorker.Companion.SYNC_ID_TO_MARK_AS_COMPLETED
import com.simprints.id.testtools.TestApplication
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.coroutines.TestDispatcherProvider
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.util.*

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class EventEndSyncReporterWorkerTest {

    private val syncId = UUID.randomUUID().toString()
    private val tagForMasterSyncId = "$TAG_MASTER_SYNC_ID$syncId"

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()
    private val testDispatcherProvider = TestDispatcherProvider(testCoroutineRule)

    private val app = ApplicationProvider.getApplicationContext() as TestApplication

    private lateinit var endSyncReportWorker: EventEndSyncReporterWorker

    @Before
    fun setUp() {
        UnitTestConfig().setupWorkManager()

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

    private fun createWorker(inputData: Data): EventEndSyncReporterWorker =
        (TestListenableWorkerBuilder<EventEndSyncReporterWorker>(app)
            .setTags(listOf(tagForMasterSyncId))
            .setInputData(inputData)
            .build())
            .apply {
                resultSetter = mockk(relaxed = true)
                syncCache = mockk(relaxed = true)
                dispatcher = testDispatcherProvider
            }
}
