package com.simprints.infra.eventsync

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.google.common.truth.Truth.assertThat
import com.simprints.infra.eventsync.status.down.EventDownSyncScopeRepository
import com.simprints.infra.eventsync.status.up.EventUpSyncScopeRepository
import com.simprints.infra.eventsync.sync.EventSyncStateProcessor
import com.simprints.infra.eventsync.sync.common.*
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EventSyncManagerImplTest {

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()


    private val ctx = mockk<Context>()
    private val workManager = mockk<WorkManager>(relaxed = true)
    private val eventSyncStateProcessor = mockk<EventSyncStateProcessor>(relaxed = true)
    private val eventUpSyncScopeRepository = mockk<EventUpSyncScopeRepository>(relaxed = true)
    private val eventDownSyncScopeRepository = mockk<EventDownSyncScopeRepository>(relaxed = true)
    private val eventSyncCache = mockk<EventSyncCache>(relaxed = true)
    private lateinit var eventSyncManagerImpl: EventSyncManagerImpl

    @Before
    fun setup() {
        mockkStatic(WorkManager::class)
        every { WorkManager.getInstance(ctx) } returns workManager

        eventSyncManagerImpl = EventSyncManagerImpl(
            ctx,
            eventSyncStateProcessor,
            eventDownSyncScopeRepository,
            eventUpSyncScopeRepository,
            eventSyncCache,
        )
    }

    @Test
    fun getLastSyncState_shouldUseProcessor() {
        eventSyncManagerImpl.getLastSyncState()
        verify { eventSyncStateProcessor.getLastSyncState() }
    }

    @Test
    fun sync_shouldEnqueueAOneTimeSyncMasterWorker() {
        eventSyncManagerImpl.sync()

        verify(exactly = 1) {
            workManager.beginUniqueWork(
                MASTER_SYNC_SCHEDULER_ONE_TIME,
                ExistingWorkPolicy.KEEP,
                match<OneTimeWorkRequest> { req ->
                    assertThat(req.tags.firstOrNull { it.contains(TAG_SCHEDULED_AT) }).isNotNull()
                    assertThat(req.tags).contains(MASTER_SYNC_SCHEDULER_ONE_TIME)
                    assertThat(req.tags).contains(MASTER_SYNC_SCHEDULERS)
                    true
                }
            )
        }
    }

    @Test
    fun sync_shouldEnqueuePeriodicSyncMasterWorker() {
        eventSyncManagerImpl.scheduleSync()

        verify(exactly = 1) {
            workManager.enqueueUniquePeriodicWork(
                MASTER_SYNC_SCHEDULER_PERIODIC_TIME,
                ExistingPeriodicWorkPolicy.KEEP,
                match { req ->
                    assertThat(req.tags.firstOrNull { it.contains(TAG_SCHEDULED_AT) }).isNotNull()
                    assertThat(req.tags).contains(MASTER_SYNC_SCHEDULER_PERIODIC_TIME)
                    assertThat(req.tags).contains(MASTER_SYNC_SCHEDULERS)
                    true
                }
            )
        }
    }

    @Test
    fun cancelScheduledSync_shouldClearPeriodicMasterWorkers() {
        eventSyncManagerImpl.cancelScheduledSync()

        verify(exactly = 1) { workManager.cancelAllWorkByTag(MASTER_SYNC_SCHEDULERS) }
        verify(exactly = 1) { workManager.cancelAllWorkByTag(TAG_SUBJECTS_SYNC_ALL_WORKERS) }
    }

    @Test
    fun stop_shouldStopAllWorkers() {
        eventSyncManagerImpl.stop()

        verify(exactly = 1) { workManager.cancelAllWorkByTag(TAG_SUBJECTS_SYNC_ALL_WORKERS) }
    }

    @Test
    fun deleteSyncInfo_shouldDeleteAnyInfoRelatedToSync() {
        runTest {
            eventSyncManagerImpl.deleteSyncInfo()

            coVerify(exactly = 1) { eventUpSyncScopeRepository.deleteAll() }
            coVerify(exactly = 1) { eventDownSyncScopeRepository.deleteAll() }
            coVerify(exactly = 1) { eventSyncCache.clearProgresses() }
            coVerify(exactly = 1) { eventSyncCache.storeLastSuccessfulSyncTime(null) }
            verify(exactly = 1) { workManager.pruneWork() }
        }
    }
}

