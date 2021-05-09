package com.simprints.id.services.sync.events.down

import com.google.common.truth.Truth.assertThat
import com.simprints.id.sampledata.DefaultTestConstants
import com.simprints.id.sampledata.DefaultTestConstants.DEFAULT_MODULE_ID
import com.simprints.id.sampledata.DefaultTestConstants.DEFAULT_MODULE_ID_2
import com.simprints.id.commontesttools.encodingUtilsForTests
import com.simprints.id.commontesttools.events.createEnrolmentRecordCreationEvent
import com.simprints.id.commontesttools.events.createEnrolmentRecordDeletionEvent
import com.simprints.id.commontesttools.events.createEnrolmentRecordMoveEvent
import com.simprints.id.commontesttools.events.createPersonCreationEvent
import com.simprints.id.data.db.event.EventRepository
import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.events_sync.down.EventDownSyncScopeRepository
import com.simprints.id.data.db.events_sync.down.domain.EventDownSyncOperation.DownSyncState.*
import com.simprints.id.data.db.subject.SubjectRepository
import com.simprints.id.data.db.subject.domain.SubjectAction.Creation
import com.simprints.id.data.db.subject.domain.SubjectAction.Deletion
import com.simprints.id.data.db.subject.domain.SubjectFactoryImpl
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.services.sync.events.down.EventDownSyncHelperImpl.Companion.EVENTS_BATCH_SIZE
import com.simprints.id.tools.time.TimeHelper
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test

class EventDownSyncHelperImplTest {

    private val projectOp = DefaultTestConstants.projectDownSyncScope.operations.first()
    private val moduleOp = DefaultTestConstants.modulesDownSyncScope.operations.first()

    private lateinit var downloadEventsChannel: Channel<Event>

    private lateinit var eventDownSyncHelper: EventDownSyncHelper
    @MockK private lateinit var subjectRepository: SubjectRepository
    @MockK private lateinit var eventRepository: EventRepository
    @MockK private lateinit var eventDownSyncScopeRepository: EventDownSyncScopeRepository
    @MockK private lateinit var timeHelper: TimeHelper
    @MockK private lateinit var preferencesManager: PreferencesManager
    private val subjectFactory = SubjectFactoryImpl(encodingUtilsForTests)

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        eventDownSyncHelper = EventDownSyncHelperImpl(
            subjectRepository,
            eventRepository,
            eventDownSyncScopeRepository,
            subjectFactory,
            preferencesManager,
            timeHelper
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
            val event = createEnrolmentRecordCreationEvent()
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
            val eventToMoveToModule2 = createEnrolmentRecordMoveEvent()
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
            val eventToMoveToModule2 = createEnrolmentRecordMoveEvent()
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
            val eventToMoveToModule2 = createEnrolmentRecordMoveEvent()
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
            val eventToMoveToModule2 = createEnrolmentRecordMoveEvent()
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
