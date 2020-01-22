package com.simprints.id.services.scheduledSync.people.master

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.*
import androidx.work.WorkInfo.State.ENQUEUED
import androidx.work.testing.TestListenableWorkerBuilder
import com.google.common.truth.Truth.assertThat
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.services.scheduledSync.people.common.*
import com.simprints.id.services.scheduledSync.people.down.workers.PeopleDownSyncDownloaderWorker
import com.simprints.id.services.scheduledSync.people.master.models.PeopleDownSyncTrigger.PERIODIC_BACKGROUND
import com.simprints.id.services.scheduledSync.people.master.workers.PeopleSyncMasterWorker
import com.simprints.id.services.scheduledSync.people.master.workers.PeopleSyncMasterWorker.Companion.MASTER_SYNC_SCHEDULER_ONE_TIME
import com.simprints.id.services.scheduledSync.people.master.workers.PeopleSyncMasterWorker.Companion.MASTER_SYNC_SCHEDULER_PERIODIC_TIME
import com.simprints.id.services.scheduledSync.people.master.workers.PeopleSyncMasterWorker.Companion.OUTPUT_LAST_SYNC_ID
import com.simprints.id.services.scheduledSync.people.up.workers.PeopleUpSyncUploaderWorker
import com.simprints.id.testtools.TestApplication
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import io.mockk.*
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class PeopleSyncMasterWorkerTest {

    companion object {
        const val UNIQUE_SYNC_ID = "UNIQUE_SYNC_ID"
    }

    private val app = ApplicationProvider.getApplicationContext() as TestApplication

    private val wm: WorkManager
        get() = WorkManager.getInstance(ApplicationProvider.getApplicationContext())

    private lateinit var masterWorker: PeopleSyncMasterWorker

    @Before
    fun setUp() {
        UnitTestConfig(this).setupWorkManager()

        masterWorker =
            TestListenableWorkerBuilder<PeopleSyncMasterWorker>(app)
                .setTags(listOf(MASTER_SYNC_SCHEDULER_PERIODIC_TIME))
                .build() as PeopleSyncMasterWorker

        app.component = mockk(relaxed = true)
        mockDependencies()
    }

    private fun mockDependencies() {
        with(masterWorker) {
            crashReportManager = mockk(relaxed = true)
            resultSetter = mockk(relaxed = true)
            downSyncWorkerBuilder = mockk(relaxed = true)
            upSyncWorkerBuilder = mockk(relaxed = true)
            peopleSyncCache = mockk(relaxed = true)
        }
        mockBackgroundTrigger(true)
    }

    @Test
    fun doWork_syncNotGoing_shouldEnqueueANewUniqueSync() = runBlockingTest {
        val uniqueSyncId = masterWorker.uniqueSyncId
        prepareSyncWorkers(uniqueSyncId)

        masterWorker.doWork()

        assertWorkerOutput(uniqueSyncId)
        assertSyncChainWasBuilt()
        assertSyncWorkersAreEnqueued(uniqueSyncId)
    }

    @Test
    fun masterPeriodicSyncWorker_syncNotGoingAndBackgroundOff_shouldEnqueueOnlyUpWorkers() = runBlockingTest {
        with(masterWorker) {
            val uniqueSyncId = masterWorker.uniqueSyncId
            prepareSyncWorkers(uniqueSyncId)
            mockBackgroundTrigger(false)

            masterWorker.doWork()

            assertWorkerOutput(uniqueSyncId)
            coVerify(exactly = 0) { downSyncWorkerBuilder.buildDownSyncWorkerChain(any()) }
            coVerify(exactly = 1) { upSyncWorkerBuilder.buildUpSyncWorkerChain(any()) }
            assertOnlyUpSyncWorkersAreEnqueued(uniqueSyncId)
        }
    }

    @Test
    fun masterOneTimeSyncWorker_syncNotGoingAndBackgroundOff_shouldEnqueueAllWorkers() = runBlockingTest {
        buildOneTimeMasterWorker()
        val uniqueSyncId = masterWorker.uniqueSyncId
        prepareSyncWorkers(uniqueSyncId)
        mockBackgroundTrigger(false)

        masterWorker.doWork()

        assertWorkerOutput(uniqueSyncId)
        assertSyncChainWasBuilt()
        assertSyncWorkersAreEnqueued(uniqueSyncId)
    }

    @Test
    fun doWork_syncGoing_shouldReturnTheExistingUniqueSync() = runBlockingTest {
        enqueueASyncWorker(UNIQUE_SYNC_ID)

        masterWorker.doWork()

        assertWorkerOutput(UNIQUE_SYNC_ID)
        assertSyncChainWasBuilt(0)
        assertSyncWorkersAreEnqueued(UNIQUE_SYNC_ID)
    }

    @Test
    fun doWork_errorOccurs_shouldWorkerFail() = runBlockingTest {
        coEvery { masterWorker.downSyncWorkerBuilder.buildDownSyncWorkerChain(any()) } throws Throwable("IO Error")

        masterWorker.doWork()

        verify { masterWorker.resultSetter.failure(any()) }
    }

    private fun enqueueASyncWorker(existingSyncId: String) {
        wm.enqueue(OneTimeWorkRequestBuilder<PeopleDownSyncDownloaderWorker>()
            .setConstraints(constraintsForWorkers())
            .addTag(TAG_PEOPLE_SYNC_ALL_WORKERS)
            .addTag("${TAG_MASTER_SYNC_ID}$existingSyncId")
            .build())
    }

    private fun assertSyncWorkersAreEnqueued(uniqueSyncId: String) {
        val workers = wm.getWorkInfosByTag("${TAG_MASTER_SYNC_ID}$uniqueSyncId").get()
        val downWorkers = workers.filterByTags(TAG_PEOPLE_DOWN_SYNC_ALL_WORKERS)

        assertThat(downWorkers).isNotEmpty()
        assertThat(downWorkers.first().state).isEqualTo(ENQUEUED)
    }

    private fun assertOnlyUpSyncWorkersAreEnqueued(uniqueSyncId: String) {
        val syncWorkers = wm.getWorkInfosByTag("${TAG_MASTER_SYNC_ID}$uniqueSyncId").get()
        val lastSyncReporterWorkersCount = 1
        assertThat(syncWorkers).hasSize(1 + lastSyncReporterWorkersCount)

        val downWorkers = wm.getWorkInfosByTag(TAG_PEOPLE_DOWN_SYNC_ALL_WORKERS).get()
        assertThat(downWorkers).hasSize(0)
    }

    private fun constraintsForWorkers() =
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

    private fun assertSyncChainWasBuilt(nTimes: Int = 1) {
        coVerify(exactly = nTimes) { masterWorker.downSyncWorkerBuilder.buildDownSyncWorkerChain(any()) }
        coVerify(exactly = nTimes) { masterWorker.downSyncWorkerBuilder.buildDownSyncWorkerChain(any()) }
    }

    private fun prepareSyncWorkers(uniqueSyncId: String) {
        coEvery { masterWorker.downSyncWorkerBuilder.buildDownSyncWorkerChain(any()) } returns buildDownSyncWorkers(uniqueSyncId)
        coEvery { masterWorker.upSyncWorkerBuilder.buildUpSyncWorkerChain(any()) } returns buildUpSyncWorkers(uniqueSyncId)
    }

    private fun buildDownSyncWorkers(uniqueSyncId: String): List<OneTimeWorkRequest> =
        listOf(OneTimeWorkRequestBuilder<PeopleDownSyncDownloaderWorker>()
            .setConstraints(constraintsForWorkers())
            .addTag(TAG_PEOPLE_SYNC_ALL_WORKERS)
            .addTag(TAG_PEOPLE_DOWN_SYNC_ALL_WORKERS)
            .addTag("${TAG_MASTER_SYNC_ID}$uniqueSyncId")
            .build())

    private fun buildUpSyncWorkers(uniqueSyncId: String): List<OneTimeWorkRequest> =
        listOf(OneTimeWorkRequestBuilder<PeopleUpSyncUploaderWorker>()
            .setConstraints(constraintsForWorkers())
            .addTag(TAG_PEOPLE_SYNC_ALL_WORKERS)
            .addTag(TAG_PEOPLE_UP_SYNC_ALL_WORKERS)
            .addTag("${TAG_MASTER_SYNC_ID}$uniqueSyncId")
            .build())

    private fun assertWorkerOutput(uniqueSyncId: String) {
        verify { masterWorker.resultSetter.success(workDataOf(OUTPUT_LAST_SYNC_ID to uniqueSyncId)) }
    }

    private fun mockBackgroundTrigger(on: Boolean) {
        masterWorker.preferenceManager = mockk<PreferencesManager>(relaxed = true).apply {
            every { this@apply.peopleDownSyncTriggers } returns mapOf(PERIODIC_BACKGROUND to on)
        }
    }

    private fun buildOneTimeMasterWorker() {
        masterWorker = TestListenableWorkerBuilder<PeopleSyncMasterWorker>(app)
            .setTags(listOf(MASTER_SYNC_SCHEDULER_ONE_TIME))
            .build() as PeopleSyncMasterWorker
        mockDependencies()
    }
}
