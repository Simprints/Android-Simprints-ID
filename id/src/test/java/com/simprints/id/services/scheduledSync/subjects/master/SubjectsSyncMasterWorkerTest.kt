package com.simprints.id.services.scheduledSync.subjects.master

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.*
import androidx.work.WorkInfo.State.BLOCKED
import androidx.work.WorkInfo.State.ENQUEUED
import androidx.work.testing.TestListenableWorkerBuilder
import com.google.common.truth.Truth.assertThat
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.services.scheduledSync.subjects.common.*
import com.simprints.id.services.scheduledSync.subjects.down.workers.SubjectsDownSyncCountWorker
import com.simprints.id.services.scheduledSync.subjects.down.workers.SubjectsDownSyncDownloaderWorker
import com.simprints.id.services.scheduledSync.subjects.master.models.SubjectsDownSyncSetting
import com.simprints.id.services.scheduledSync.subjects.master.models.SubjectsDownSyncSetting.OFF
import com.simprints.id.services.scheduledSync.subjects.master.models.SubjectsDownSyncSetting.ON
import com.simprints.id.services.scheduledSync.subjects.master.models.SubjectsSyncWorkerType
import com.simprints.id.services.scheduledSync.subjects.master.models.SubjectsSyncWorkerType.*
import com.simprints.id.services.scheduledSync.subjects.master.models.SubjectsSyncWorkerType.Companion.tagForType
import com.simprints.id.services.scheduledSync.subjects.master.workers.SubjectsEndSyncReporterWorker
import com.simprints.id.services.scheduledSync.subjects.master.workers.SubjectsStartSyncReporterWorker
import com.simprints.id.services.scheduledSync.subjects.master.workers.SubjectsSyncMasterWorker
import com.simprints.id.services.scheduledSync.subjects.master.workers.SubjectsSyncMasterWorker.Companion.MASTER_SYNC_SCHEDULER_ONE_TIME
import com.simprints.id.services.scheduledSync.subjects.master.workers.SubjectsSyncMasterWorker.Companion.MASTER_SYNC_SCHEDULER_PERIODIC_TIME
import com.simprints.id.services.scheduledSync.subjects.master.workers.SubjectsSyncMasterWorker.Companion.OUTPUT_LAST_SYNC_ID
import com.simprints.id.services.scheduledSync.subjects.up.workers.SubjectsUpSyncCountWorker
import com.simprints.id.services.scheduledSync.subjects.up.workers.SubjectsUpSyncUploaderWorker
import com.simprints.id.testtools.TestApplication
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class SubjectsSyncMasterWorkerTest {

    companion object {
        const val UNIQUE_SYNC_ID = "UNIQUE_SYNC_ID"
    }

    private val app = ApplicationProvider.getApplicationContext() as TestApplication

    private val wm: WorkManager
        get() = WorkManager.getInstance(ApplicationProvider.getApplicationContext())

    private lateinit var masterWorker: SubjectsSyncMasterWorker

    @Before
    fun setUp() {
        UnitTestConfig(this).setupWorkManager()

        masterWorker =
            TestListenableWorkerBuilder<SubjectsSyncMasterWorker>(app)
                .setTags(listOf(MASTER_SYNC_SCHEDULER_PERIODIC_TIME))
                .build() as SubjectsSyncMasterWorker

        app.component = mockk(relaxed = true)
        mockDependencies()
    }

    private fun mockDependencies() {
        with(masterWorker) {
            crashReportManager = mockk(relaxed = true)
            resultSetter = mockk(relaxed = true)
            downSyncWorkerBuilder = mockk(relaxed = true)
            upSyncWorkerBuilder = mockk(relaxed = true)
            subjectsSyncCache = mockk(relaxed = true)
            subjectsSyncSubMasterWorkersBuilder = mockk(relaxed = true)
        }
        mockPeopleDownSyncSetting(ON)
    }

    @Test
    fun doWork_syncNotGoing_shouldEnqueueANewUniqueSync() = runBlocking {
        val uniqueSyncId = masterWorker.uniqueSyncId
        prepareSyncWorkers(uniqueSyncId)

        masterWorker.doWork()

        assertWorkerOutput(uniqueSyncId)
        assertSyncChainWasBuilt()
        assertAllWorkersAreEnqueued(uniqueSyncId)
    }

    @Test
    fun doWork_syncNotGoingAndBackgroundOff_shouldEnqueueOnlyUpSyncWorkers() = runBlocking {
        with(masterWorker) {
            val uniqueSyncId = masterWorker.uniqueSyncId
            prepareSyncWorkers(uniqueSyncId)
            mockPeopleDownSyncSetting(OFF)

            masterWorker.doWork()

            assertWorkerOutput(uniqueSyncId)
            coVerify(exactly = 0) { downSyncWorkerBuilder.buildDownSyncWorkerChain(any()) }
            coVerify(exactly = 1) { upSyncWorkerBuilder.buildUpSyncWorkerChain(any()) }

            assertSyncWorkersState(uniqueSyncId, ENQUEUED, START_SYNC_REPORTER)
            assertSyncWorkersState(uniqueSyncId, BLOCKED, UP_COUNTER)
            assertSyncWorkersState(uniqueSyncId, BLOCKED, UPLOADER)
            assertSyncWorkersState(uniqueSyncId, BLOCKED, END_SYNC_REPORTER)
            assertTotalNumberOfWorkers(uniqueSyncId, 4)
        }
    }

    @Test
    fun doWork_syncNotGoingAndBackgroundOn_shouldEnqueueAllWorkers() = runBlocking {
        val uniqueSyncId = masterWorker.uniqueSyncId
        prepareSyncWorkers(uniqueSyncId)
        mockPeopleDownSyncSetting(ON)

        masterWorker.doWork()

        assertWorkerOutput(uniqueSyncId)
        assertSyncChainWasBuilt()
        assertAllWorkersAreEnqueued(uniqueSyncId)
    }

    @Test
    fun doWorkAsOneTimeSync_shouldEnqueueAllWorkers() = runBlocking {
        buildOneTimeMasterWorker()
        mockPeopleDownSyncSetting(ON)
        val uniqueSyncId = masterWorker.uniqueSyncId
        prepareSyncWorkers(uniqueSyncId)

        masterWorker.doWork()

        assertWorkerOutput(uniqueSyncId)
        assertSyncChainWasBuilt()
        assertAllWorkersAreEnqueued(uniqueSyncId)
    }

    @Test
    fun doWork_syncGoing_shouldReturnTheExistingUniqueSync() = runBlocking {
        val existingSyncId = enqueueASyncWorker()

        masterWorker.doWork()

        assertWorkerOutput(existingSyncId)
        assertSyncChainWasNotBuild()
    }

    @Test
    fun doWork_errorOccurs_shouldWorkerFail() = runBlocking {
        coEvery { masterWorker.downSyncWorkerBuilder.buildDownSyncWorkerChain(any()) } throws Throwable("IO Error")

        masterWorker.doWork()

        verify { masterWorker.resultSetter.failure(any()) }
    }

    private fun enqueueASyncWorker(): String {
        wm.enqueue(OneTimeWorkRequestBuilder<SubjectsDownSyncDownloaderWorker>()
            .setConstraints(constraintsForWorkers())
            .addTag(TAG_PEOPLE_SYNC_ALL_WORKERS)
            .addTag(TAG_PEOPLE_DOWN_SYNC_ALL_WORKERS)
            .addTag("${TAG_MASTER_SYNC_ID}$UNIQUE_SYNC_ID")
            .build())
        return UNIQUE_SYNC_ID
    }

    private fun assertAllWorkersAreEnqueued(uniqueSyncId: String) {
        assertSyncWorkersState(uniqueSyncId, ENQUEUED, START_SYNC_REPORTER)
        assertSyncWorkersState(uniqueSyncId, BLOCKED, UP_COUNTER)
        assertSyncWorkersState(uniqueSyncId, BLOCKED, UPLOADER)
        assertSyncWorkersState(uniqueSyncId, BLOCKED, DOWNLOADER)
        assertSyncWorkersState(uniqueSyncId, BLOCKED, DOWN_COUNTER)
        assertSyncWorkersState(uniqueSyncId, BLOCKED, END_SYNC_REPORTER)
        assertTotalNumberOfWorkers(uniqueSyncId, 6)
    }

    private fun assertTotalNumberOfWorkers(uniqueSyncId: String, total: Int) {
        val allWorkers = wm.getWorkInfosByTag("${TAG_MASTER_SYNC_ID}$uniqueSyncId").get()
        assertThat(allWorkers.size).isEqualTo(total)
    }

    private fun assertSyncWorkersState(uniqueSyncId: String,
                                       state: WorkInfo.State,
                                       specificType: SubjectsSyncWorkerType? = null) {

        val allWorkers = wm.getWorkInfosByTag("${TAG_MASTER_SYNC_ID}$uniqueSyncId").get()
        val specificWorkers = specificType?.let { allWorkers.filterByTags(tagForType(specificType)) } ?: allWorkers

        assertThat(specificWorkers.size).isEqualTo(1)
        assertThat(specificWorkers.all { it.state == state }).isTrue()
    }

    private fun constraintsForWorkers() =
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

    private fun assertSyncChainWasNotBuild() = assertSyncChainWasBuilt(0)
    private fun assertSyncChainWasBuilt(nTimes: Int = 1) {
        coVerify(exactly = nTimes) { masterWorker.downSyncWorkerBuilder.buildDownSyncWorkerChain(any()) }
        coVerify(exactly = nTimes) { masterWorker.downSyncWorkerBuilder.buildDownSyncWorkerChain(any()) }
    }

    private fun prepareSyncWorkers(uniqueSyncId: String) {
        coEvery { masterWorker.downSyncWorkerBuilder.buildDownSyncWorkerChain(any()) } returns buildDownSyncWorkers(uniqueSyncId)
        coEvery { masterWorker.upSyncWorkerBuilder.buildUpSyncWorkerChain(any()) } returns buildUpSyncWorkers(uniqueSyncId)
        coEvery { masterWorker.subjectsSyncSubMasterWorkersBuilder.buildStartSyncReporterWorker(any()) } returns buildStartSyncReporterWorker(uniqueSyncId)
        coEvery { masterWorker.subjectsSyncSubMasterWorkersBuilder.buildEndSyncReporterWorker(any()) } returns buildEndSyncReporterWorker(uniqueSyncId)

    }

    private fun buildEndSyncReporterWorker(uniqueSyncId: String): OneTimeWorkRequest =
        OneTimeWorkRequest.Builder(SubjectsEndSyncReporterWorker::class.java)
            .addTagForMasterSyncId(uniqueSyncId)
            .addTagForScheduledAtNow()
            .addCommonTagForAllSyncWorkers()
            .addTagForEndSyncReporter()
            .setInputData(workDataOf(SubjectsEndSyncReporterWorker.SYNC_ID_TO_MARK_AS_COMPLETED to uniqueSyncId))
            .setConstraints(constraintsForWorkers())
            .build() as OneTimeWorkRequest

    private fun buildStartSyncReporterWorker(uniqueSyncId: String): OneTimeWorkRequest =
        OneTimeWorkRequest.Builder(SubjectsStartSyncReporterWorker::class.java)
            .addTagForMasterSyncId(uniqueSyncId)
            .addTagForScheduledAtNow()
            .addCommonTagForAllSyncWorkers()
            .addTagForStartSyncReporter()
            .setInputData(workDataOf(SubjectsStartSyncReporterWorker.SYNC_ID_STARTED to uniqueSyncId))
            .setConstraints(constraintsForWorkers())
            .build() as OneTimeWorkRequest

    private fun buildDownSyncWorkers(uniqueSyncId: String): List<OneTimeWorkRequest> =
        listOf(
            OneTimeWorkRequestBuilder<SubjectsDownSyncDownloaderWorker>()
                .setConstraints(constraintsForWorkers())
                .addTag("${TAG_MASTER_SYNC_ID}$uniqueSyncId")
                .addTag(TAG_PEOPLE_SYNC_ALL_WORKERS)
                .addTag(TAG_PEOPLE_DOWN_SYNC_ALL_WORKERS)
                .addTag(tagForType(DOWNLOADER))
                .build(),
            OneTimeWorkRequestBuilder<SubjectsDownSyncCountWorker>()
                .setConstraints(constraintsForWorkers())
                .addTag("${TAG_MASTER_SYNC_ID}$uniqueSyncId")
                .addTag(TAG_PEOPLE_SYNC_ALL_WORKERS)
                .addTag(TAG_PEOPLE_DOWN_SYNC_ALL_WORKERS)
                .addTag(tagForType(DOWN_COUNTER))
                .build()
        )

    private fun buildUpSyncWorkers(uniqueSyncId: String): List<OneTimeWorkRequest> =
        listOf(
            OneTimeWorkRequestBuilder<SubjectsUpSyncUploaderWorker>()
                .setConstraints(constraintsForWorkers())
                .addTag("${TAG_MASTER_SYNC_ID}$uniqueSyncId")
                .addTag(TAG_PEOPLE_SYNC_ALL_WORKERS)
                .addTag(TAG_PEOPLE_UP_SYNC_ALL_WORKERS)
                .addTag(tagForType(UPLOADER))
                .build(),
            OneTimeWorkRequestBuilder<SubjectsUpSyncCountWorker>()
                .setConstraints(constraintsForWorkers())
                .addTag("${TAG_MASTER_SYNC_ID}$uniqueSyncId")
                .addTag(TAG_PEOPLE_SYNC_ALL_WORKERS)
                .addTag(TAG_PEOPLE_DOWN_SYNC_ALL_WORKERS)
                .addTag(tagForType(UP_COUNTER))
                .build()
        )

    private fun assertWorkerOutput(uniqueSyncId: String) {
        verify { masterWorker.resultSetter.success(workDataOf(OUTPUT_LAST_SYNC_ID to uniqueSyncId)) }
    }

    private fun mockPeopleDownSyncSetting(subjectsDownSyncSetting: SubjectsDownSyncSetting) {
        masterWorker.preferenceManager = mockk<PreferencesManager>(relaxed = true).apply {
            every { this@apply.subjectsDownSyncSetting } returns subjectsDownSyncSetting
        }
    }

    private fun buildOneTimeMasterWorker() {
        masterWorker = TestListenableWorkerBuilder<SubjectsSyncMasterWorker>(app)
            .setTags(listOf(MASTER_SYNC_SCHEDULER_ONE_TIME))
            .build() as SubjectsSyncMasterWorker
        mockDependencies()
    }
}
