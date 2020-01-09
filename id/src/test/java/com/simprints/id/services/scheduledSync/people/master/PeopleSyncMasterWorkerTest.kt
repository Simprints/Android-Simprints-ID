package com.simprints.id.services.scheduledSync.people.master

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.*
import androidx.work.WorkInfo.State.ENQUEUED
import androidx.work.testing.TestListenableWorkerBuilder
import com.google.common.truth.Truth.assertThat
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.services.scheduledSync.people.down.controllers.PeopleDownSyncWorkersFactory.Companion.TAG_PEOPLE_DOWN_SYNC_ALL_WORKERS
import com.simprints.id.services.scheduledSync.people.down.workers.PeopleDownSyncDownloaderWorker
import com.simprints.id.services.scheduledSync.people.master.PeopleDownSyncTrigger.PERIODIC_BACKGROUND
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncMasterWorker.Companion.MASTER_SYNC_SCHEDULER_ONE_TIME
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncMasterWorker.Companion.MASTER_SYNC_SCHEDULER_PERIODIC_TIME
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncMasterWorker.Companion.OUTPUT_LAST_SYNC_ID
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncMasterWorker.Companion.TAG_MASTER_SYNC_ID
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncMasterWorker.Companion.TAG_PEOPLE_SYNC_ALL_WORKERS
import com.simprints.id.services.scheduledSync.people.up.controllers.PeopleUpSyncWorkersBuilder.Companion.TAG_PEOPLE_UP_SYNC_ALL_WORKERS
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
            downSyncWorkerFactory = mockk(relaxed = true)
            upSyncWorkerBuilder = mockk(relaxed = true)
            peopleSyncProgressCache = mockk(relaxed = true)
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
    fun masterPeriodicSyncWorker_syncNotGoingAndBackgroundOff_shouldEnqueueOnyUpWorkers() = runBlockingTest {
        with(masterWorker) {
            val uniqueSyncId = masterWorker.uniqueSyncId
            prepareSyncWorkers(uniqueSyncId)
            mockBackgroundTrigger(false)

            masterWorker.doWork()

            assertWorkerOutput(uniqueSyncId)
            coVerify(exactly = 0) { downSyncWorkerFactory.buildDownSyncWorkerChain(any()) }
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
        coEvery { masterWorker.downSyncWorkerFactory.buildDownSyncWorkerChain(any()) } throws Throwable("IO Error")

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
        val downWorkers = wm.getWorkInfosByTag("${TAG_MASTER_SYNC_ID}$uniqueSyncId").get()
        assertThat(downWorkers).isNotEmpty()
        assertThat(downWorkers.first().state).isEqualTo(ENQUEUED)
    }

    private fun assertOnlyUpSyncWorkersAreEnqueued(uniqueSyncId: String) {
        val syncWorkers = wm.getWorkInfosByTag("${TAG_MASTER_SYNC_ID}$uniqueSyncId").get()
        assertThat(syncWorkers).hasSize(1)

        val downWorkers = wm.getWorkInfosByTag(TAG_PEOPLE_DOWN_SYNC_ALL_WORKERS).get()
        assertThat(downWorkers).hasSize(0)
    }

    private fun constraintsForWorkers() =
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

    private fun assertSyncChainWasBuilt(nTimes: Int = 1) {
        coVerify(exactly = nTimes) { masterWorker.downSyncWorkerFactory.buildDownSyncWorkerChain(any()) }
        coVerify(exactly = nTimes) { masterWorker.downSyncWorkerFactory.buildDownSyncWorkerChain(any()) }
    }

    private fun prepareSyncWorkers(uniqueSyncId: String) {
        coEvery { masterWorker.downSyncWorkerFactory.buildDownSyncWorkerChain(any()) } returns buildDownSyncWorkers(uniqueSyncId)
        coEvery { masterWorker.upSyncWorkerBuilder.buildUpSyncWorkerChain(any()) } returns buildUpSyncWorkers(uniqueSyncId)
    }

    private fun buildDownSyncWorkers(uniqueSyncId: String): List<WorkRequest> =
        listOf(OneTimeWorkRequestBuilder<PeopleDownSyncDownloaderWorker>()
            .setConstraints(constraintsForWorkers())
            .addTag(TAG_PEOPLE_SYNC_ALL_WORKERS)
            .addTag(TAG_PEOPLE_DOWN_SYNC_ALL_WORKERS)
            .addTag("${TAG_MASTER_SYNC_ID}$uniqueSyncId")
            .build())

    private fun buildUpSyncWorkers(uniqueSyncId: String): List<WorkRequest> =
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
