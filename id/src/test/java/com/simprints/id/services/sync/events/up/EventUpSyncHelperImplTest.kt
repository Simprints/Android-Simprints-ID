package com.simprints.id.services.sync.events.up

import com.google.common.truth.Truth.assertThat
import com.simprints.id.sampledata.DefaultTestConstants
import com.simprints.id.commontesttools.events.createPersonCreationEvent
import com.simprints.id.data.db.event.EventRepository
import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.events_sync.up.EventUpSyncScopeRepository
import com.simprints.id.data.db.events_sync.up.domain.EventUpSyncOperation.UpSyncState.*
import com.simprints.id.tools.time.TimeHelper
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test

class EventUpSyncHelperImplTest {

    private val op = DefaultTestConstants.projectUpSyncScope.operation
    private lateinit var uploadEventsChannel: Channel<Event>

    private lateinit var eventUpSyncHelper: EventUpSyncHelper
    @MockK private lateinit var eventRepository: EventRepository
    @MockK private lateinit var eventUpSyncScopeRepository: EventUpSyncScopeRepository
    @MockK private lateinit var timeHelper: TimeHelper

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        eventUpSyncHelper = EventUpSyncHelperImpl(
            eventRepository,
            eventUpSyncScopeRepository,
            timeHelper
        )

        runBlockingTest {
            mockProgressEmission(emptyFlow())
        }
    }

    @Test
    fun countForUpSync_shouldInvokeTheEventRepo() {
        runBlockingTest {
            eventUpSyncHelper.countForUpSync(op)

            coVerify { eventRepository.localCount(op.projectId) }
        }
    }

    @Test
    fun upSync_shouldConsumeRepoEventChannel() {
        runBlocking {
            eventUpSyncHelper.upSync(this, op).consumeAsFlow().toList()

            coVerify { eventRepository.uploadEvents(op.projectId) }
        }
    }

    @Test
    fun upSync_shouldProgressEventsInBatches() {
        runBlocking {
            val eventsToUpload = mutableListOf<Event>()
            repeat(3) { eventsToUpload += createPersonCreationEvent() }
            val sequenceOfProgress = flowOf(1, 1, 1)
            mockProgressEmission(sequenceOfProgress)

            val channel = eventUpSyncHelper.upSync(this, op)

            val progress = channel.consumeAsFlow().toList()
            sequenceOfProgress.onEach {
                assertThat(progress[it].progress).isEqualTo(it)
                assertThat(progress[it].operation.lastState).isEqualTo(RUNNING)
            }

            assertThat(progress.last().operation.lastState).isEqualTo(COMPLETE)
            coVerify(exactly = 4) { eventUpSyncScopeRepository.insertOrUpdate(any()) }
        }
    }

    @Test
    fun upSync_shouldEmitAFailureIfUploadFails() {
        runBlocking {
            coEvery { eventRepository.uploadEvents(any()) } throws Throwable("IO Exception")

            val channel = eventUpSyncHelper.upSync(this, op)

            val progress = channel.consumeAsFlow().toList()
            assertThat(progress.first().operation.lastState).isEqualTo(FAILED)
            coVerify(exactly = 1) { eventUpSyncScopeRepository.insertOrUpdate(any()) }
        }
    }

    private suspend fun mockProgressEmission(sequence: Flow<Int>) {
        uploadEventsChannel = Channel(capacity = Channel.UNLIMITED)
        coEvery { eventRepository.uploadEvents(any()) } returns sequence
    }
}
