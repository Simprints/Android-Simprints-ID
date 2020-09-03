package com.simprints.id.services.sync.subjects.down.workers

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import com.google.common.util.concurrent.ListenableFuture
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.commontesttools.DefaultTestConstants.projectDownSyncScope
import com.simprints.id.data.db.event.domain.EventCount
import com.simprints.id.data.db.event.domain.models.EventType.SESSION_CAPTURE
import com.simprints.id.data.db.events_sync.down.domain.EventDownSyncOperation
import com.simprints.id.services.sync.events.common.TAG_MASTER_SYNC_ID
import com.simprints.id.services.sync.events.down.workers.EventDownSyncCountWorker
import com.simprints.id.services.sync.events.down.workers.EventDownSyncCountWorker.Companion.INPUT_COUNT_WORKER_DOWN
import com.simprints.id.services.sync.events.down.workers.EventDownSyncCountWorker.Companion.OUTPUT_COUNT_WORKER_DOWN
import com.simprints.id.services.sync.events.master.models.EventSyncWorkerType
import com.simprints.id.services.sync.events.master.models.EventSyncWorkerType.Companion.tagForType
import com.simprints.id.testtools.TestApplication
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.util.*

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class EventDownSyncCountWorkerTest {

    private val syncId = UUID.randomUUID().toString()
    private val tagForMasterSyncId = "${TAG_MASTER_SYNC_ID}$syncId"
    private val app = ApplicationProvider.getApplicationContext() as TestApplication
    private lateinit var countWorker: EventDownSyncCountWorker

    @Before
    fun setUp() {
        UnitTestConfig(this).fullSetup()
        countWorker = TestListenableWorkerBuilder<EventDownSyncCountWorker>(app)
            .setTags(listOf(tagForMasterSyncId))
            .setInputData(workDataOf(INPUT_COUNT_WORKER_DOWN to JsonHelper().toJson(projectDownSyncScope)))
            .build() as EventDownSyncCountWorker

        app.component = mockk(relaxed = true)
        with(countWorker) {
            crashReportManager = mockk(relaxed = true)
            resultSetter = mockk(relaxed = true)
            eventDownSyncHelper = mockk(relaxed = true)
            jsonHelper = JsonHelper()
            eventDownSyncScopeRepository = mockk(relaxed = true)
        }

        coEvery { countWorker.eventDownSyncScopeRepository.refreshState(any()) } coAnswers { args.first() as EventDownSyncOperation }
    }

    @Test
    fun countWorker_shouldExtractTheDownSyncScopeFromTheRepo() {
        runBlocking {
            countWorker.doWork()

            coVerify { countWorker.eventDownSyncScopeRepository.refreshState(any()) }
        }
    }

    @Test
    fun countWorker_shouldExecuteTheTaskSuccessfully() {
        runBlocking {
            val counts = EventCount(SESSION_CAPTURE, 1)
            mockDependenciesToSucceed(counts)

            countWorker.doWork()

            val output = JsonHelper().toJson(listOf(counts))
            val expectedSuccessfulOutput = workDataOf(OUTPUT_COUNT_WORKER_DOWN to output)
            verify { countWorker.resultSetter.success(expectedSuccessfulOutput) }
        }
    }

    @Test
    fun countWorker_anUnexpectedErrorOccurs_shouldFail() {
        runBlocking {
            coEvery { countWorker.eventDownSyncScopeRepository.getDownSyncScope() } throws Throwable("Impossible to extract downSyncScope")

            countWorker.doWork()

            verify { countWorker.resultSetter.failure() }
        }
    }

    @Test
    fun countWorkerFailed_syncStillRunning_shouldRetry() {
        runBlocking {
            coEvery { countWorker.eventDownSyncHelper.countForDownSync(any()) } throws Throwable("IO Error")
            coEvery { countWorker.eventDownSyncScopeRepository.getDownSyncScope() } returns projectDownSyncScope
            mockDependenciesToHaveSyncStillRunning()

            countWorker.doWork()

            verify { countWorker.resultSetter.retry() }
        }
    }

    @Test
    fun countWorkerFailed_syncIsNotRunning_shouldSucceed() {
        runBlocking {
            coEvery { countWorker.eventDownSyncHelper.countForDownSync(any()) } throws Throwable("IO Error")
            coEvery { countWorker.eventDownSyncScopeRepository.getDownSyncScope() } returns projectDownSyncScope
            mockDependenciesToHaveSyncNotRunning()

            countWorker.doWork()

            verify { countWorker.resultSetter.success() }
        }
    }

    private fun mockDependenciesToSucceed(counts: EventCount) {
        coEvery { countWorker.eventDownSyncHelper.countForDownSync(any()) } returns listOf(counts)
        coEvery { countWorker.eventDownSyncScopeRepository.getDownSyncScope() } returns projectDownSyncScope
    }

    private fun mockDependenciesToHaveSyncStillRunning() {
        mockWorkManagerToReturnDownloaderWorkInfo(WorkInfo.State.RUNNING)
    }

    private fun mockDependenciesToHaveSyncNotRunning() {
        mockWorkManagerToReturnDownloaderWorkInfo(WorkInfo.State.CANCELLED)
    }

    private fun mockWorkManagerToReturnDownloaderWorkInfo(state: WorkInfo.State) {
        val mockWm = mockk<WorkManager>(relaxed = true)
        val mockWorkInfo = mockk<ListenableFuture<List<WorkInfo>>>()
        every { mockWorkInfo.get() } returns listOf(WorkInfo(UUID.randomUUID(), state, workDataOf(), listOf(tagForMasterSyncId, tagForType(EventSyncWorkerType.DOWNLOADER)), workDataOf(), 2))
        every { mockWm.getWorkInfosByTag(any()) } returns mockWorkInfo
        countWorker.wm = mockWm
    }

}
