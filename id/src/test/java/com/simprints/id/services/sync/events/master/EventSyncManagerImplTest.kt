package com.simprints.id.services.sync.events.master

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.*
import androidx.work.WorkInfo.State.CANCELLED
import androidx.work.WorkInfo.State.ENQUEUED
import com.google.common.truth.Truth.assertThat
import com.simprints.id.data.db.subjects_sync.down.SubjectsDownSyncScopeRepository
import com.simprints.id.data.db.subjects_sync.up.SubjectsUpSyncScopeRepository
import com.simprints.id.services.sync.events.common.TAG_SCHEDULED_AT
import com.simprints.id.services.sync.events.common.TAG_SUBJECTS_SYNC_ALL_WORKERS
import com.simprints.id.services.sync.events.down.workers.EventDownSyncCountWorker
import com.simprints.id.services.sync.events.down.workers.EventDownSyncDownloaderWorker
import com.simprints.id.services.sync.events.master.internal.EventSyncCache
import com.simprints.id.services.sync.events.master.workers.SubjectsSyncMasterWorker.Companion.MASTER_SYNC_SCHEDULERS
import com.simprints.id.services.sync.events.master.workers.SubjectsSyncMasterWorker.Companion.MASTER_SYNC_SCHEDULER_ONE_TIME
import com.simprints.id.services.sync.events.master.workers.SubjectsSyncMasterWorker.Companion.MASTER_SYNC_SCHEDULER_PERIODIC_TIME
import com.simprints.id.testtools.UnitTestConfig
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EventSyncManagerImplTest {

    private lateinit var subjectsSyncManager: EventSyncManagerImpl
    @MockK lateinit var subjectsSyncStateProcessor: SubjectsSyncStateProcessor
    @MockK lateinit var subjectsUpSyncScopeRepository: SubjectsUpSyncScopeRepository
    @MockK lateinit var subjectsDownSyncScopeRepository: SubjectsDownSyncScopeRepository
    @MockK lateinit var eventSyncCache: EventSyncCache

    private val ctx: Context = ApplicationProvider.getApplicationContext()
    private val wm: WorkManager
        get() = WorkManager.getInstance(ctx)

    private val masterSyncWorkers
        get() = wm.getWorkInfosByTag(MASTER_SYNC_SCHEDULERS).get()

    private val syncWorkers
        get() = wm.getWorkInfosByTag(TAG_SUBJECTS_SYNC_ALL_WORKERS).get()

    @Before
    fun setUp() {
        UnitTestConfig(this)
            .setupWorkManager()
            .setupFirebase()

        MockKAnnotations.init(this, relaxed = true)
        subjectsSyncManager = EventSyncManagerImpl(ctx, subjectsSyncStateProcessor, subjectsUpSyncScopeRepository, subjectsDownSyncScopeRepository, eventSyncCache)
    }

    @Test
    fun getLastSyncState_shouldUseProcessor() {
        subjectsSyncManager.getLastSyncState()
        verify { subjectsSyncStateProcessor.getLastSyncState() }
    }

    @Test
    fun sync_shouldEnqueueAOneTimeSyncMasterWorker() {
        subjectsSyncManager.sync()

        assertThat(masterSyncWorkers).hasSize(1)
        masterSyncWorkers.first().verifyOneTimeMasterWorker()
    }

    @Test
    fun sync_shouldEnqueuePeriodicSyncMasterWorker() {
        subjectsSyncManager.scheduleSync()

        assertThat(masterSyncWorkers).hasSize(1)
        masterSyncWorkers.first().verifyPeriodicMasterWorker()
    }

    @Test
    fun cancelScheduledSync_shouldClearPeriodicMasterWorkers() {
        subjectsSyncManager.scheduleSync()
        assertThat(masterSyncWorkers.first().state).isEqualTo(ENQUEUED)

        subjectsSyncManager.cancelScheduledSync()

        assertThat(masterSyncWorkers.first().state).isEqualTo(CANCELLED)
        assertThat(masterSyncWorkers).hasSize(1)
    }

    @Test
    fun stop_shouldStopAllWorkers() {
        enqueueDownSyncWorkers()

        subjectsSyncManager.stop()

        syncWorkers.forEach {
            assertThat(it.state).isEqualTo(CANCELLED)
        }
    }

    @Test
    fun deleteSyncInfo_shouldDeleteAnyInfoRelatedToSync() = runBlockingTest {

        subjectsSyncManager.deleteSyncInfo()

        coVerify { subjectsUpSyncScopeRepository.deleteAll() }
        coVerify { subjectsDownSyncScopeRepository.deleteAll() }
        verify { eventSyncCache.clearProgresses() }
        verify { eventSyncCache.storeLastSuccessfulSyncTime(null) }
    }

    private fun enqueueDownSyncWorkers() {
        val counterWorkerRequest =
            OneTimeWorkRequestBuilder<EventDownSyncCountWorker>()
                .setConstraints(constraintsForWorkers())
                .addTag(TAG_SUBJECTS_SYNC_ALL_WORKERS)
                .build()

        val downSyncWorkerRequest =
            OneTimeWorkRequestBuilder<EventDownSyncDownloaderWorker>()
                .addTag(TAG_SUBJECTS_SYNC_ALL_WORKERS)
                .setConstraints(constraintsForWorkers())
                .build()

        wm.enqueue(counterWorkerRequest)
        wm.enqueue(downSyncWorkerRequest)
        assertThat(syncWorkers.first().state).isEqualTo(ENQUEUED)
        assertThat(syncWorkers[1].state).isEqualTo(ENQUEUED)
    }


    private fun WorkInfo.verifyPeriodicMasterWorker() {
        assertCommonMasterTag()
        assertPeriodicTimeMasterTag()
        assertScheduleAtTag()
    }

    private fun WorkInfo.verifyOneTimeMasterWorker() {
        assertCommonMasterTag()
        assertOneTimeMasterTag()
        assertScheduleAtTag()
    }

    private fun constraintsForWorkers() =
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
}

private fun WorkInfo.assertCommonMasterTag() =
    assertThat(tags).contains(MASTER_SYNC_SCHEDULERS)

private fun WorkInfo.assertOneTimeMasterTag() =
    assertThat(tags).contains(MASTER_SYNC_SCHEDULER_ONE_TIME)

private fun WorkInfo.assertPeriodicTimeMasterTag() =
    assertThat(tags).contains(MASTER_SYNC_SCHEDULER_PERIODIC_TIME)


private fun WorkInfo.assertScheduleAtTag() =
    assertThat(tags.firstOrNull { it.contains(TAG_SCHEDULED_AT) }).isNotNull()
