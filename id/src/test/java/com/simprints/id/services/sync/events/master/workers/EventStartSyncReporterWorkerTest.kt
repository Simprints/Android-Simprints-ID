package com.simprints.id.services.sync.events.master.workers

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Data
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import com.simprints.id.services.sync.events.common.TAG_MASTER_SYNC_ID
import com.simprints.id.services.sync.events.master.workers.EventStartSyncReporterWorker.Companion.SYNC_ID_STARTED
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
class EventStartSyncReporterWorkerTest {

    private val syncId = UUID.randomUUID().toString()
    private val tagForMasterSyncId = "$TAG_MASTER_SYNC_ID$syncId"

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()
    private val testDispatcherProvider = TestDispatcherProvider(testCoroutineRule)

    private val app = ApplicationProvider.getApplicationContext() as TestApplication

    private lateinit var startSyncReportWorker: EventStartSyncReporterWorker

    @Before
    fun setUp() {
        UnitTestConfig(this).setupWorkManager()
        app.component = mockk(relaxed = true)
    }


    @Test
    fun worker_shouldSucceed() = runBlocking {
        startSyncReportWorker = createWorker(workDataOf(SYNC_ID_STARTED to syncId))

        with(startSyncReportWorker) {
            doWork()
            verify { resultSetter.success(any()) }
        }
    }

    private fun createWorker(inputData: Data): EventStartSyncReporterWorker =
        (TestListenableWorkerBuilder<EventStartSyncReporterWorker>(app)
            .setTags(listOf(tagForMasterSyncId))
            .setInputData(inputData)
            .build() as EventStartSyncReporterWorker)
            .apply {
                resultSetter = mockk(relaxed = true)
                dispatcher = testDispatcherProvider
            }
}
