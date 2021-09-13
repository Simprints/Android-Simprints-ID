package com.simprints.id.services.sync.events.down

import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.coroutines.DispatcherProvider
import com.simprints.core.tools.time.TimeHelper
import com.simprints.eventsystem.event.domain.models.Event
import com.simprints.eventsystem.events_sync.down.EventDownSyncScopeRepository
import com.simprints.eventsystem.events_sync.down.domain.EventDownSyncOperation.DownSyncState.*
import com.simprints.eventsystem.sampledata.*
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_MODULE_ID
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_MODULE_ID_2
import com.simprints.id.data.db.subject.SubjectRepository
import com.simprints.id.data.db.subject.domain.SubjectAction.Creation
import com.simprints.id.data.db.subject.domain.SubjectAction.Deletion
import com.simprints.id.data.db.subject.domain.SubjectFactoryImpl
import com.simprints.id.data.prefs.IdPreferencesManager
import com.simprints.id.services.sync.events.down.EventDownSyncHelperImpl.Companion.EVENTS_BATCH_SIZE
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.unit.EncodingUtilsImplForTests
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class EventDownSyncHelperImplTest {

    private val projectOp = SampleDefaults.projectDownSyncScope.operations.first()
    private val moduleOp = SampleDefaults.modulesDownSyncScope.operations.first()

    private lateinit var downloadEventsChannel: Channel<Event>

    private lateinit var eventDownSyncHelper: EventDownSyncHelper
    @MockK private lateinit var subjectRepository: SubjectRepository
    @MockK private lateinit var eventRepository: com.simprints.eventsystem.event.EventRepository
    @MockK private lateinit var eventDownSyncScopeRepository: EventDownSyncScopeRepository
    @MockK private lateinit var timeHelper: TimeHelper
    @MockK private lateinit var preferencesManager: IdPreferencesManager
    private val subjectFactory = SubjectFactoryImpl(EncodingUtilsImplForTests)


    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private val testDispatcherProvider = object : DispatcherProvider {
        override fun main(): CoroutineDispatcher = testCoroutineRule.testCoroutineDispatcher

        override fun default(): CoroutineDispatcher = testCoroutineRule.testCoroutineDispatcher

        override fun io(): CoroutineDispatcher = testCoroutineRule.testCoroutineDispatcher

        override fun unconfined(): CoroutineDispatcher = testCoroutineRule.testCoroutineDispatcher
    }

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        eventDownSyncHelper = EventDownSyncHelperImpl(
            subjectRepository,
            eventRepository,
            eventDownSyncScopeRepository,
            subjectFactory,
            preferencesManager,
            timeHelper,
            testDispatcherProvider
        )

        runBlockingTest {
            mockProgressEmission(emptyList())
        }
    }

    @Test
    fun countForDownSync_shouldReturnRepoEventChannel() {
        runBlockingTest {
            eventDownSyncHelper.countForDownSync(projectOp)
            coVerify { eventRepository.countEventsToDownload(any()) }
        }
    }

    @Test
    fun downSync_shouldConsumeRepoEventChannel() {
        runBlockingTest {
            eventDownSyncHelper.downSync(this, projectOp)

            coVerify { eventRepository.downloadEvents(this@runBlockingTest, projectOp.queryEvent) }
        }
    }

    @Test
    fun downSync_shouldProgressEventsInBatches() {
        runBlocking {
            val eventsToDownload = mutableListOf<Event>()
            repeat(2 * EVENTS_BATCH_SIZE) { eventsToDownload += createPersonCreationEvent() }
            mockProgressEmission(eventsToDownload)

            val channel = eventDownSyncHelper.downSync(this, projectOp)

            val progress = channel.consumeAsFlow().toList()
            assertThat(progress.first().progress).isEqualTo(1)
            assertThat(progress.first().operation.state).isEqualTo(RUNNING)
            //Shifted by 1 since the first batch is immediately emitted with only 1 element
            assertThat(progress[1].progress).isEqualTo(EVENTS_BATCH_SIZE + 2)
            assertThat(progress[1].operation.state).isEqualTo(RUNNING)
            assertThat(progress[2].progress).isEqualTo(2 * EVENTS_BATCH_SIZE)
            assertThat(progress[2].operation.state).isEqualTo(RUNNING)
            assertThat(progress[3].operation.state).isEqualTo(COMPLETE)
            coVerify(exactly = 4) { eventDownSyncScopeRepository.insertOrUpdate(any()) }

        }
    }

    @Test
    fun downSync_shouldEmitAFailureIfDownloadFails() {
        runBlocking {
            coEvery { eventRepository.downloadEvents(any(), any()) } throws Throwable("IO Exception")

            val channel = eventDownSyncHelper.downSync(this, projectOp)

            val progress = channel.consumeAsFlow().toList()
            assertThat(progress.last().operation.state).isEqualTo(FAILED)
            coVerify(exactly = 2) { eventDownSyncScopeRepository.insertOrUpdate(any()) }
        }
    }

    @Test
    fun downSync_shouldProcessRecordCreationEvent() {
        runBlocking {
            val event = createEnrolmentRecordCreationEvent(EncodingUtilsImplForTests)
            mockProgressEmission(listOf(event))

            eventDownSyncHelper.downSync(this, projectOp).consumeAsFlow().toList()

            coVerify { subjectRepository.performActions(listOf(Creation(subjectFactory.buildSubjectFromCreationPayload(event.payload)))) }
        }
    }

    @Test
    fun downSync_shouldProcessRecordDeletionEvent() {
        runBlocking {
            val event = createEnrolmentRecordDeletionEvent()
            mockProgressEmission(listOf(event))

            eventDownSyncHelper.downSync(this, projectOp).consumeAsFlow().toList()

            coVerify { subjectRepository.performActions(listOf(Deletion(event.payload.subjectId))) }
        }
    }

    @Test
    fun moveSubjectFromModulesUnderSyncing_theOriginalModuleSyncShouldDoNothing() {
        runBlocking {
            val eventToMoveToModule2 = createEnrolmentRecordMoveEvent(EncodingUtilsImplForTests)
            mockProgressEmission(listOf(eventToMoveToModule2))
            every { preferencesManager.selectedModules } returns setOf(DEFAULT_MODULE_ID, DEFAULT_MODULE_ID_2)

            eventDownSyncHelper.downSync(this, moduleOp).consumeAsFlow().toList()

            coVerify {
                subjectRepository.performActions(emptyList())
            }
        }
    }

    @Test
    fun moveSubjectFromModulesUnderSyncing_theDestinationModuleSyncShouldPerformCreation() {
        runBlocking {
            val eventToMoveToModule2 = createEnrolmentRecordMoveEvent(EncodingUtilsImplForTests)
            mockProgressEmission(listOf(eventToMoveToModule2))
            every { preferencesManager.selectedModules } returns setOf(DEFAULT_MODULE_ID, DEFAULT_MODULE_ID_2)

            val syncByModule2 = moduleOp.copy(queryEvent = moduleOp.queryEvent.copy(moduleIds = listOf(DEFAULT_MODULE_ID_2)))
            eventDownSyncHelper.downSync(this, syncByModule2).consumeAsFlow().toList()

            coVerify {
                subjectRepository.performActions(listOf(
                    Creation(subjectFactory.buildSubjectFromMovePayload(eventToMoveToModule2.payload.enrolmentRecordCreation)))
                )
            }
        }
    }

    @Test
    fun moveSubjectToAModuleNotUnderSyncing_shouldPerformDeletionOnly() {
        runBlocking {
            val eventToMoveToModule2 = createEnrolmentRecordMoveEvent(EncodingUtilsImplForTests)
            mockProgressEmission(listOf(eventToMoveToModule2))
            every { preferencesManager.selectedModules } returns setOf(DEFAULT_MODULE_ID)

            eventDownSyncHelper.downSync(this, moduleOp).consumeAsFlow().toList()

            coVerify {
                subjectRepository.performActions(listOf(
                    Deletion(eventToMoveToModule2.payload.enrolmentRecordDeletion.subjectId))
                )
            }
        }
    }

    @Test
    fun moveSubjectToAModuleUnderSyncing_shouldPerformCreationOnly() {
        runBlocking {
            val eventToMoveToModule2 = createEnrolmentRecordMoveEvent(EncodingUtilsImplForTests)
            mockProgressEmission(listOf(eventToMoveToModule2))
            every { preferencesManager.selectedModules } returns setOf(DEFAULT_MODULE_ID_2)

            val syncByModule2 = moduleOp.copy(queryEvent = moduleOp.queryEvent.copy(moduleIds = listOf(DEFAULT_MODULE_ID_2)))
            eventDownSyncHelper.downSync(this, syncByModule2).consumeAsFlow().toList()

            coVerify {
                subjectRepository.performActions(listOf(
                    Creation(subjectFactory.buildSubjectFromMovePayload(eventToMoveToModule2.payload.enrolmentRecordCreation)))
                )
            }
        }
    }

    private suspend fun mockProgressEmission(progressEvents: List<Event>) {
        downloadEventsChannel = Channel(capacity = Channel.UNLIMITED)
        coEvery { eventRepository.downloadEvents(any(), any()) } returns downloadEventsChannel

        progressEvents.forEach {
            downloadEventsChannel.send(it)
        }
        downloadEventsChannel.close()
    }
}
