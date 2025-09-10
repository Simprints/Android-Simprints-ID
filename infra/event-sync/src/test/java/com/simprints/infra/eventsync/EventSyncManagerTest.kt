package com.simprints.infra.eventsync

import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.common.Partitioning
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.core.tools.utils.ExtractCommCareCaseIdUseCase
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.EventCount
import com.simprints.infra.events.event.domain.models.EventType
import com.simprints.infra.events.event.domain.models.scope.EventScope
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_MODULE_ID
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_MODULE_ID_2
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.infra.eventsync.event.commcare.cache.CommCareSyncCache
import com.simprints.infra.eventsync.event.remote.EventRemoteDataSource
import com.simprints.infra.eventsync.status.down.EventDownSyncScopeRepository
import com.simprints.infra.eventsync.status.models.DownSyncCounts
import com.simprints.infra.eventsync.status.models.EventSyncState
import com.simprints.infra.eventsync.status.up.EventUpSyncScopeRepository
import com.simprints.infra.eventsync.sync.EventSyncStateProcessor
import com.simprints.infra.eventsync.sync.common.EventSyncCache
import com.simprints.infra.eventsync.sync.down.tasks.CommCareEventSyncTask
import com.simprints.infra.eventsync.sync.down.tasks.SimprintsEventDownSyncTask
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
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
    lateinit var timeHelper: TimeHelper

    @MockK
    lateinit var eventSyncStateProcessor: EventSyncStateProcessor

    @MockK
    lateinit var eventUpSyncScopeRepository: EventUpSyncScopeRepository

    @MockK
    lateinit var eventDownSyncScopeRepository: EventDownSyncScopeRepository

    @MockK
    lateinit var eventSyncCache: EventSyncCache

    @MockK
    lateinit var commCareSyncCache: CommCareSyncCache

    @MockK
    lateinit var eventRepository: EventRepository

    @MockK
    lateinit var simprintsDownSyncTask: SimprintsEventDownSyncTask

    @MockK
    lateinit var commCareDownSyncTask: CommCareEventSyncTask

    @MockK
    lateinit var eventRemoteDataSource: EventRemoteDataSource

    @MockK
    lateinit var configRepository: ConfigRepository

    @MockK
    lateinit var eventScope: EventScope

    @MockK
    lateinit var project: Project

    @MockK
    lateinit var extractCommCareCaseIdUseCase: ExtractCommCareCaseIdUseCase

    private lateinit var eventSyncManagerImpl: EventSyncManagerImpl

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        every { timeHelper.now() } returns Timestamp(1)
        coEvery { configRepository.getProjectConfiguration() } returns mockk {
            every { general.modalities } returns listOf()
            every {
                synchronization.down.simprints
                    ?.partitionType
                    ?.toDomain()
            } returns Partitioning.MODULE
        }

        eventSyncManagerImpl = EventSyncManagerImpl(
            timeHelper = timeHelper,
            eventSyncStateProcessor = eventSyncStateProcessor,
            downSyncScopeRepository = eventDownSyncScopeRepository,
            eventRepository = eventRepository,
            upSyncScopeRepo = eventUpSyncScopeRepository,
            eventSyncCache = eventSyncCache,
            commCareSyncCache = commCareSyncCache,
            simprintsDownSyncTask = simprintsDownSyncTask,
            commCareSyncTask = commCareDownSyncTask,
            eventRemoteDataSource = eventRemoteDataSource,
            configRepository = configRepository,
            extractCommCareCaseId = extractCommCareCaseIdUseCase,
            dispatcher = testCoroutineRule.testCoroutineDispatcher,
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
    fun `getLastSyncState with useDefaultValue true should return an immediate default value`() = runTest {
        every { eventSyncStateProcessor.getLastSyncState() } returns MutableLiveData(null)
        val defaultValue = EventSyncState(syncId = "", null, null, emptyList(), emptyList(), emptyList())

        val result = eventSyncManagerImpl.getLastSyncState(true).value

        assertThat(result).isEqualTo(defaultValue)
    }

    @Test
    fun `getLastSyncState with useDefaultValue false and no data emission should return null value`() = runTest {
        every { eventSyncStateProcessor.getLastSyncState() } returns MutableLiveData(null)

        val result = eventSyncManagerImpl.getLastSyncState(false).value

        assertThat(result).isEqualTo(null)
    }

    @Test
    fun `countEventsToUpload without types should call event repo`() = runTest {
        eventSyncManagerImpl.countEventsToUpload().toList()

        coVerify { eventRepository.observeEventCount(null) }
    }

    @Test
    fun `countEventsToUpload with types should call event repo per type`() = runTest {
        eventSyncManagerImpl.countEventsToUpload(listOf(EventType.ENROLMENT_V2, EventType.EVENT_UP_SYNC_REQUEST)).toList()

        coVerify(exactly = 2) { eventRepository.observeEventCount(any<EventType>()) }
    }

    @Test
    fun `countEventsToDownload correctly counts sync events`() = runTest {
        coEvery {
            eventDownSyncScopeRepository.getDownSyncScope(any(), any(), any())
        } returns SampleSyncScopes.modulesDownSyncScope

        coEvery { eventRemoteDataSource.count(any()) } returnsMany listOf(
            EventCount(8, false),
            EventCount(18, true),
        )
        coEvery { configRepository.getDeviceConfiguration() } returns mockk {
            every { selectedModules } returns listOf(DEFAULT_MODULE_ID, DEFAULT_MODULE_ID_2)
        }

        val result = eventSyncManagerImpl.countEventsToDownload()

        assertThat(result).isEqualTo(DownSyncCounts(26, isLowerBound = true))
    }

    @Test
    fun `downSyncSubject should call CommCare sync when CommCare config is present`() = runTest {
        val metadata = """{"caseId": "case123"}"""
        val expectedCaseId = "case123"

        // Mock CommCare configuration
        coEvery { configRepository.getProjectConfiguration() } returns mockk {
            every { general.modalities } returns listOf()
            every { synchronization.down.simprints } returns null
            every { synchronization.down.commCare } returns mockk()
        }
        every { extractCommCareCaseIdUseCase.invoke(metadata) } returns expectedCaseId
        coEvery { eventRepository.createEventScope(any()) } returns eventScope
        coEvery { commCareDownSyncTask.downSync(any(), any(), eventScope, any()) } returns emptyFlow()

        eventSyncManagerImpl.downSyncSubject(DEFAULT_PROJECT_ID, "subjectId", metadata)

        coVerify { extractCommCareCaseIdUseCase.invoke(metadata) }
        coVerify {
            commCareDownSyncTask.downSync(
                any(),
                match { operation ->
                    operation.queryEvent.externalIds == listOf(expectedCaseId)
                },
                eventScope,
                any(),
            )
        }
        coVerify { eventRepository.closeEventScope(eventScope, any()) }
        coVerify(exactly = 0) { simprintsDownSyncTask.downSync(any(), any(), any(), any()) }
    }

    @Test
    fun `downSyncSubject should call CommCare sync with null externalIds when caseId is null`() = runTest {
        val metadata = """{"otherField": "value"}"""

        // Mock CommCare configuration
        coEvery { configRepository.getProjectConfiguration() } returns mockk {
            every { general.modalities } returns listOf()
            every { synchronization.down.simprints } returns null
            every { synchronization.down.commCare } returns mockk()
        }
        every { extractCommCareCaseIdUseCase.invoke(metadata) } returns null
        coEvery { eventRepository.createEventScope(any()) } returns eventScope
        coEvery { commCareDownSyncTask.downSync(any(), any(), eventScope, any()) } returns emptyFlow()

        eventSyncManagerImpl.downSyncSubject(DEFAULT_PROJECT_ID, "subjectId", metadata)

        coVerify { extractCommCareCaseIdUseCase.invoke(metadata) }
        coVerify {
            commCareDownSyncTask.downSync(
                any(),
                match { operation ->
                    operation.queryEvent.externalIds == null
                },
                eventScope,
                any(),
            )
        }
        coVerify { eventRepository.closeEventScope(eventScope, any()) }
        coVerify(exactly = 0) { simprintsDownSyncTask.downSync(any(), any(), any(), any()) }
    }

    @Test
    fun `downSyncSubject should call Simprints sync when Simprints config is present`() = runTest {
        // Mock Simprints configuration (restore original config from setup)
        coEvery { configRepository.getProjectConfiguration() } returns mockk {
            every { general.modalities } returns listOf()
            every { synchronization.down.simprints } returns mockk {
                every { partitionType.toDomain() } returns Partitioning.MODULE
            }
            every { synchronization.down.commCare } returns null
        }
        coEvery { eventRepository.createEventScope(any()) } returns eventScope
        coEvery { simprintsDownSyncTask.downSync(any(), any(), eventScope, any()) } returns emptyFlow()

        eventSyncManagerImpl.downSyncSubject(DEFAULT_PROJECT_ID, "subjectId", "metadata")

        coVerify {
            simprintsDownSyncTask.downSync(
                any(),
                match { operation ->
                    operation.queryEvent.subjectId == "subjectId" &&
                        operation.queryEvent.projectId == DEFAULT_PROJECT_ID &&
                        operation.queryEvent.externalIds == null
                },
                eventScope,
                any(),
            )
        }
        coVerify { eventRepository.closeEventScope(eventScope, any()) }
        coVerify(exactly = 0) { commCareDownSyncTask.downSync(any(), any(), any(), any()) }
        coVerify(exactly = 0) { extractCommCareCaseIdUseCase.invoke(any()) }
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
        coVerify(exactly = 1) { commCareSyncCache.clearAllSyncedCases() }
    }

    @Test
    fun `resetDownSyncInfo should call sync scope repo`() = runTest {
        eventSyncManagerImpl.resetDownSyncInfo()

        coVerify(exactly = 1) { eventDownSyncScopeRepository.deleteAll() }
        coVerify(exactly = 1) { commCareSyncCache.clearAllSyncedCases() }
    }
}
