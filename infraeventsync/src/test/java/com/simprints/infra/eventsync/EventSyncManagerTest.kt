package com.simprints.infra.eventsync

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.common.GROUP
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.EventCount
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordEventType
import com.simprints.infra.events.sampledata.SampleDefaults
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_MODULE_ID
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.infra.eventsync.event.remote.EventRemoteDataSource
import com.simprints.infra.eventsync.status.down.EventDownSyncScopeRepository
import com.simprints.infra.eventsync.status.up.EventUpSyncScopeRepository
import com.simprints.infra.eventsync.sync.EventSyncStateProcessor
import com.simprints.infra.eventsync.sync.common.*
import com.simprints.infra.eventsync.sync.down.tasks.EventDownSyncTask
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class EventSyncManagerTest {

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    lateinit var ctx: Context

    @MockK
    lateinit var workManager: WorkManager

    @MockK
    lateinit var eventSyncStateProcessor: EventSyncStateProcessor

    @MockK
    lateinit var eventUpSyncScopeRepository: EventUpSyncScopeRepository

    @MockK
    lateinit var eventDownSyncScopeRepository: EventDownSyncScopeRepository

    @MockK
    lateinit var eventSyncCache: EventSyncCache

    @MockK
    lateinit var eventRepository: EventRepository

    @MockK
    lateinit var downSyncTask: EventDownSyncTask

    @MockK
    lateinit var eventRemoteDataSource: EventRemoteDataSource

    @MockK
    lateinit var configManager: ConfigManager

    private lateinit var eventSyncManagerImpl: EventSyncManagerImpl

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        mockkStatic(WorkManager::class)
        every { WorkManager.getInstance(ctx) } returns workManager

        coEvery { configManager.getProjectConfiguration() } returns mockk {
            every { general.modalities } returns listOf()
            every { synchronization.down.partitionType.toGroup() } returns GROUP.MODULE
        }

        eventSyncManagerImpl = EventSyncManagerImpl(
            ctx,
            eventSyncStateProcessor,
            eventDownSyncScopeRepository,
            eventRepository,
            eventUpSyncScopeRepository,
            eventSyncCache,
            downSyncTask,
            eventRemoteDataSource,
            configManager,
            testCoroutineRule.testCoroutineDispatcher,
        )
    }

    @Test
    fun `getLastSyncTime should call sync cache`() = runTest {
        eventSyncManagerImpl.getLastSyncTime()
        coVerify { eventSyncCache.readLastSuccessfulSyncTime() }
    }

    @Test
    fun `getLastSyncState should call sync processor`() = runTest {
        eventSyncManagerImpl.getLastSyncState()
        verify { eventSyncStateProcessor.getLastSyncState() }
    }

    @Test
    fun `sync should enqueue a one time sync master worker`() = runTest {
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
    fun `sync should enqueue periodic sync master worker`() = runTest {
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
    fun `cancelScheduledSync should clear periodic master workers`() = runTest {
        eventSyncManagerImpl.cancelScheduledSync()

        verify(exactly = 1) { workManager.cancelAllWorkByTag(MASTER_SYNC_SCHEDULERS) }
        verify(exactly = 1) { workManager.cancelAllWorkByTag(TAG_SUBJECTS_SYNC_ALL_WORKERS) }
    }

    @Test
    fun `stop should stop all workers`() = runTest {
        eventSyncManagerImpl.stop()

        verify(exactly = 1) { workManager.cancelAllWorkByTag(TAG_SUBJECTS_SYNC_ALL_WORKERS) }
    }

    @Test
    fun `countEventsToUpload should call event repo`() = runTest {
        eventSyncManagerImpl.countEventsToUpload("projectId", null).toList()

        coVerify { eventRepository.observeEventCount(any(), any()) }
    }

    @Test
    fun `getDownSyncCounts correctly counts sync events`() = runTest {
        coEvery {
            eventDownSyncScopeRepository.getDownSyncScope(any(), any(), any())
        } returns SampleSyncScopes.modulesDownSyncScope

        coEvery { eventRemoteDataSource.count(any()) } returnsMany listOf(
            listOf(
                EventCount(EnrolmentRecordEventType.EnrolmentRecordCreation, 3),
                EventCount(EnrolmentRecordEventType.EnrolmentRecordDeletion, 5),
            ),
            listOf(
                EventCount(EnrolmentRecordEventType.EnrolmentRecordCreation, 7),
                EventCount(EnrolmentRecordEventType.EnrolmentRecordDeletion, 11),
            )
        )
        coEvery { configManager.getDeviceConfiguration() } returns mockk {
            every { selectedModules } returns listOf(DEFAULT_MODULE_ID, SampleDefaults.DEFAULT_MODULE_ID_2)
        }

        val result = eventSyncManagerImpl.countEventsToDownload()

        assertThat(result.toCreate).isEqualTo(10)
        assertThat(result.toDelete).isEqualTo(16)
    }

    @Test
    fun `getDownSyncCounts does not count record move`() = runTest {
        coEvery {
            eventDownSyncScopeRepository.getDownSyncScope(any(), any(), any())
        } returns SampleSyncScopes.modulesDownSyncScope

        coEvery { eventRemoteDataSource.count(any()) } returnsMany listOf(
            listOf(
                EventCount(EnrolmentRecordEventType.EnrolmentRecordCreation, 3),
            ),
            listOf(
                EventCount(EnrolmentRecordEventType.EnrolmentRecordMove, 7),
                EventCount(EnrolmentRecordEventType.EnrolmentRecordDeletion, 5),
            )
        )
        coEvery { configManager.getDeviceConfiguration() } returns mockk {
            every { selectedModules } returns listOf(DEFAULT_MODULE_ID, SampleDefaults.DEFAULT_MODULE_ID_2)
        }

        val result = eventSyncManagerImpl.countEventsToDownload()

        assertThat(result.toCreate).isEqualTo(3)
        assertThat(result.toDelete).isEqualTo(5)
    }

    @Test
    fun `downSync should call down sync helper`() = runTest {
        coEvery { downSyncTask.downSync(any(), any()) } returns emptyFlow()

        eventSyncManagerImpl.downSyncSubject(DEFAULT_PROJECT_ID, "subjectId")

        coVerify { downSyncTask.downSync(any(), any()) }
    }

    @Test
    fun `deleteModules should call sync scope repo`() = runTest {
        eventSyncManagerImpl.deleteModules(emptyList())

        coVerify { eventDownSyncScopeRepository.deleteOperations(any(), any()) }
    }

    @Test
    fun `deleteSyncInfo should delete any info related to sync`() = runTest {
        eventSyncManagerImpl.deleteSyncInfo()

        coVerify(exactly = 1) { eventUpSyncScopeRepository.deleteAll() }
        coVerify(exactly = 1) { eventDownSyncScopeRepository.deleteAll() }
        coVerify(exactly = 1) { eventSyncCache.clearProgresses() }
        coVerify(exactly = 1) { eventSyncCache.storeLastSuccessfulSyncTime(null) }
        verify(exactly = 1) { workManager.pruneWork() }
    }

    @Test
    fun `resetDownSyncInfo should call sync scope repo`() = runTest {
        eventSyncManagerImpl.resetDownSyncInfo()

        coVerify { eventDownSyncScopeRepository.deleteAll() }
    }
}
