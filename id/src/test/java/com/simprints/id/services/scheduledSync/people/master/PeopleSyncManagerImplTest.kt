package com.simprints.id.services.scheduledSync.people.master

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.*
import androidx.work.WorkInfo.State.CANCELLED
import androidx.work.WorkInfo.State.ENQUEUED
import com.google.common.truth.Truth.assertThat
import com.simprints.id.data.db.people_sync.down.PeopleDownSyncScopeRepository
import com.simprints.id.data.db.people_sync.up.PeopleUpSyncScopeRepository
import com.simprints.id.services.scheduledSync.people.common.TAG_PEOPLE_SYNC_ALL_WORKERS
import com.simprints.id.services.scheduledSync.people.common.TAG_SCHEDULED_AT
import com.simprints.id.services.scheduledSync.people.down.workers.PeopleDownSyncCountWorker
import com.simprints.id.services.scheduledSync.people.down.workers.PeopleDownSyncDownloaderWorker
import com.simprints.id.services.scheduledSync.people.master.internal.PeopleSyncCache
import com.simprints.id.services.scheduledSync.people.master.workers.PeopleSyncMasterWorker.Companion.MASTER_SYNC_SCHEDULERS
import com.simprints.id.services.scheduledSync.people.master.workers.PeopleSyncMasterWorker.Companion.MASTER_SYNC_SCHEDULER_ONE_TIME
import com.simprints.id.services.scheduledSync.people.master.workers.PeopleSyncMasterWorker.Companion.MASTER_SYNC_SCHEDULER_PERIODIC_TIME
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
class PeopleSyncManagerImplTest {

    private lateinit var peopleSyncManager: PeopleSyncManagerImpl
    @MockK lateinit var peopleSyncStateProcessor: PeopleSyncStateProcessor
    @MockK lateinit var peopleUpSyncScopeRepository: PeopleUpSyncScopeRepository
    @MockK lateinit var peopleDownSyncScopeRepository: PeopleDownSyncScopeRepository
    @MockK lateinit var peopleSyncCache: PeopleSyncCache

    private val ctx: Context = ApplicationProvider.getApplicationContext()
    private val wm: WorkManager
        get() = WorkManager.getInstance(ctx)

    private val masterSyncWorkers
        get() = wm.getWorkInfosByTag(MASTER_SYNC_SCHEDULERS).get()

    private val syncWorkers
        get() = wm.getWorkInfosByTag(TAG_PEOPLE_SYNC_ALL_WORKERS).get()

    @Before
    fun setUp() {
        UnitTestConfig(this)
            .setupWorkManager()
            .setupFirebase()
            .setupCrashlytics()

        MockKAnnotations.init(this, relaxed = true)
        peopleSyncManager = PeopleSyncManagerImpl(ctx, peopleSyncStateProcessor, peopleUpSyncScopeRepository, peopleDownSyncScopeRepository, peopleSyncCache)
    }

    @Test
    fun getLastSyncState_shouldUseProcessor() {
        peopleSyncManager.getLastSyncState()
        verify { peopleSyncStateProcessor.getLastSyncState() }
    }

    @Test
    fun sync_shouldEnqueueAOneTimeSyncMasterWorker() {
        peopleSyncManager.sync()

        assertThat(masterSyncWorkers).hasSize(1)
        masterSyncWorkers.first().verifyOneTimeMasterWorker()
    }

    @Test
    fun sync_shouldEnqueuePeriodicSyncMasterWorker() {
        peopleSyncManager.scheduleSync()

        assertThat(masterSyncWorkers).hasSize(1)
        masterSyncWorkers.first().verifyPeriodicMasterWorker()
    }

    @Test
    fun cancelScheduledSync_shouldClearPeriodicMasterWorkers() {
        peopleSyncManager.scheduleSync()
        assertThat(masterSyncWorkers.first().state).isEqualTo(ENQUEUED)

        peopleSyncManager.cancelScheduledSync()

        assertThat(masterSyncWorkers.first().state).isEqualTo(CANCELLED)
        assertThat(masterSyncWorkers).hasSize(1)
    }

    @Test
    fun stop_shouldStopAllWorkers() {
        enqueueDownSyncWorkers()

        peopleSyncManager.stop()

        syncWorkers.forEach {
            assertThat(it.state).isEqualTo(CANCELLED)
        }
    }

    @Test
    fun deleteSyncInfo_shouldDeleteAnyInfoRelatedToSync() = runBlockingTest {

        peopleSyncManager.deleteSyncInfo()

        coVerify { peopleUpSyncScopeRepository.deleteAll() }
        coVerify { peopleDownSyncScopeRepository.deleteAll() }
        verify { peopleSyncCache.clearProgresses() }
        verify { peopleSyncCache.storeLastSuccessfulSyncTime(null) }
    }

    private fun enqueueDownSyncWorkers() {
        val counterWorkerRequest =
            OneTimeWorkRequestBuilder<PeopleDownSyncCountWorker>()
                .setConstraints(constraintsForWorkers())
                .addTag(TAG_PEOPLE_SYNC_ALL_WORKERS)
                .build()

        val downSyncWorkerRequest =
            OneTimeWorkRequestBuilder<PeopleDownSyncDownloaderWorker>()
                .addTag(TAG_PEOPLE_SYNC_ALL_WORKERS)
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
