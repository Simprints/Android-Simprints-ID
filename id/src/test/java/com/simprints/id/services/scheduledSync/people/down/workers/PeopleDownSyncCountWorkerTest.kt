package com.simprints.id.services.scheduledSync.people.down.workers

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import com.google.common.util.concurrent.ListenableFuture
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.data.db.common.models.PeopleCount
import com.simprints.id.data.db.people_sync.down.domain.ProjectSyncScope
import com.simprints.id.domain.modality.Modes
import com.simprints.id.services.scheduledSync.people.common.TAG_MASTER_SYNC_ID
import com.simprints.id.services.scheduledSync.people.master.models.PeopleSyncWorkerType
import com.simprints.id.services.scheduledSync.people.master.models.PeopleSyncWorkerType.Companion.tagForType
import com.simprints.id.testtools.TestApplication
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import io.mockk.coEvery
import io.mockk.every
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
class PeopleDownSyncCountWorkerTest {

    private val syncId = UUID.randomUUID().toString()
    private val tagForMasterSyncId = "${TAG_MASTER_SYNC_ID}$syncId"
    private val app = ApplicationProvider.getApplicationContext() as TestApplication
    private lateinit var countWorker: PeopleDownSyncCountWorker

    @Before
    fun setUp() {
        UnitTestConfig(this).fullSetup()
        countWorker = TestListenableWorkerBuilder<PeopleDownSyncCountWorker>(app)
            .setTags(listOf(tagForMasterSyncId))
            .build() as PeopleDownSyncCountWorker

        app.component = mockk(relaxed = true)
        with(countWorker) {
            downSyncScopeRepository = mockk(relaxed = true)
            crashReportManager = mockk(relaxed = true)
            personRepository = mockk(relaxed = true)
            resultSetter = mockk(relaxed = true)
        }
    }

    @Test
    fun countWorker_shouldExtractTheDownSyncScopeFromTheRepo() = runBlocking {
        countWorker.doWork()

        verify { countWorker.downSyncScopeRepository.getDownSyncScope() }
    }

    @Test
    fun countWorker_shouldExecuteTheTaskSuccessfully() = runBlocking {
        val counts = listOf(PeopleCount(1, 1, 1))
        mockDependenciesToSucceed(counts)

        countWorker.doWork()

        val output = JsonHelper.gson.toJson(counts)
        val expectedSuccessfulOutput = workDataOf(PeopleDownSyncCountWorker.OUTPUT_COUNT_WORKER_DOWN to output)
        verify { countWorker.resultSetter.success(expectedSuccessfulOutput) }
    }

    @Test
    fun countWorker_anUnexpectedErrorOccurs_shouldFail() = runBlocking {
        coEvery { countWorker.downSyncScopeRepository.getDownSyncScope() } throws Throwable("Impossible to extract downSyncScope")

        countWorker.doWork()

        verify { countWorker.resultSetter.failure() }
    }

    @Test
    fun countWorkerFailed_syncStillRunning_shouldRetry() = runBlocking {
        coEvery { countWorker.personRepository.countToDownSync(any()) } throws Throwable("IO Error")
        coEvery { countWorker.downSyncScopeRepository.getDownSyncScope() } returns ProjectSyncScope(DEFAULT_PROJECT_ID, listOf(Modes.FINGERPRINT))
        mockDependenciesToHaveSyncStillRunning()

        countWorker.doWork()

        verify { countWorker.resultSetter.retry() }
    }

    @Test
    fun countWorkerFailed_syncIsNotRunning_shouldSucceed() = runBlocking {
        coEvery { countWorker.personRepository.countToDownSync(any()) } throws Throwable("IO Error")
        coEvery { countWorker.downSyncScopeRepository.getDownSyncScope() } returns ProjectSyncScope(DEFAULT_PROJECT_ID, listOf(Modes.FINGERPRINT))
        mockDependenciesToHaveSyncNotRunning()

        countWorker.doWork()

        verify { countWorker.resultSetter.success() }
    }

    private fun mockDependenciesToSucceed(counts: List<PeopleCount>) {
        coEvery { countWorker.personRepository.countToDownSync(any()) } returns counts
        coEvery { countWorker.downSyncScopeRepository.getDownSyncScope() } returns ProjectSyncScope(DEFAULT_PROJECT_ID, listOf(Modes.FINGERPRINT))
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
        every { mockWorkInfo.get() } returns listOf(WorkInfo(UUID.randomUUID(), state, workDataOf(), listOf(tagForMasterSyncId, tagForType(PeopleSyncWorkerType.DOWNLOADER)), workDataOf(), 2))
        every { mockWm.getWorkInfosByTag(any()) } returns mockWorkInfo
        countWorker.wm = mockWm
    }

}
