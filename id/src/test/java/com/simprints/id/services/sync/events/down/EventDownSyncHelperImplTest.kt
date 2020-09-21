package com.simprints.id.services.sync.events.down

import com.google.common.truth.Truth.assertThat
import com.simprints.id.commontesttools.DefaultTestConstants
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
import com.simprints.id.data.db.subject.domain.SubjectFactoryImpl
import com.simprints.id.services.sync.events.down.EventDownSyncHelperImpl.Companion.EVENTS_BATCH_SIZE
import com.simprints.id.tools.time.TimeHelper
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test

class EventDownSyncHelperImplTest {

    private val op = DefaultTestConstants.projectDownSyncScope.operations.first()
    private lateinit var downloadEventsChannel: Channel<Event>

    private lateinit var eventDownSyncHelper: EventDownSyncHelper
    @MockK private lateinit var subjectRepository: SubjectRepository
    @MockK private lateinit var eventRepository: EventRepository
    @MockK private lateinit var eventDownSyncScopeRepository: EventDownSyncScopeRepository
    @MockK private lateinit var timeHelper: TimeHelper

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        eventDownSyncHelper = EventDownSyncHelperImpl(
            subjectRepository,
            eventRepository,
            eventDownSyncScopeRepository,
            SubjectFactoryImpl(encodingUtilsForTests),
            timeHelper
        )

        runBlockingTest {
            mockProgressEmission(emptyList())
        }
    }

    @Test
    fun countForDownSync_shouldReturnEventRepoChannel() {
        runBlockingTest {
            eventDownSyncHelper.countForDownSync(op)
            coVerify { eventRepository.countEventsToDownload(any()) }
        }
    }

    @Test
    fun downSync_shouldConsumeEventRepoChannel() {
        runBlockingTest {
            eventDownSyncHelper.downSync(this, op)

            coVerify { eventRepository.downloadEvents(this@runBlockingTest, op.queryEvent) }
        }
    }

    @Test
    fun downSync_shouldProgressEventsInBatches() {
        runBlocking {
            val eventsToDownload = mutableListOf<Event>()
            repeat(2 * EVENTS_BATCH_SIZE) { eventsToDownload += createPersonCreationEvent() }
            mockProgressEmission(eventsToDownload)

            val channel = eventDownSyncHelper.downSync(this, op)

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

            val channel = eventDownSyncHelper.downSync(this, op)

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

            eventDownSyncHelper.downSync(this, op).consumeAsFlow().toList()

            coVerify { subjectRepository.performActions((eventDownSyncHelper as EventDownSyncHelperImpl).handleSubjectCreationEvent(event)) }
        }
    }

    @Test
    fun downSync_shouldProcessRecordDeletionEvent() {
        runBlocking {
            val event = createEnrolmentRecordDeletionEvent()
            mockProgressEmission(listOf(event))

            eventDownSyncHelper.downSync(this, op).consumeAsFlow().toList()

            coVerify { subjectRepository.performActions((eventDownSyncHelper as EventDownSyncHelperImpl).handleSubjectDeletionEvent(event)) }
        }
    }

    @Test
    fun downSync_shouldProcessRecordMoveEvent() {
        runBlocking {
            val event = createEnrolmentRecordMoveEvent()
            mockProgressEmission(listOf(event))

            eventDownSyncHelper.downSync(this, op).consumeAsFlow().toList()

            coVerify { subjectRepository.performActions((eventDownSyncHelper as EventDownSyncHelperImpl).handleSubjectMoveEvent(event)) }
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
