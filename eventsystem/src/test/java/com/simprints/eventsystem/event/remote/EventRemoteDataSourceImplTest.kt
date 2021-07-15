package com.simprints.eventsystem.event.remote

import com.google.common.truth.Truth.assertThat
import com.simprints.core.network.SimApiClient
import com.simprints.core.network.SimApiClientFactory
import com.simprints.core.tools.json.JsonHelper
import com.simprints.eventsystem.event.domain.EventCount
import com.simprints.eventsystem.event.domain.models.Event
import com.simprints.eventsystem.event.domain.models.EventType
import com.simprints.eventsystem.event.domain.models.EventType.ENROLMENT_RECORD_CREATION
import com.simprints.eventsystem.event.domain.models.EventType.ENROLMENT_RECORD_DELETION
import com.simprints.eventsystem.event.domain.models.EventType.ENROLMENT_RECORD_MOVE
import com.simprints.eventsystem.event.remote.models.ApiEventCount
import com.simprints.eventsystem.event.remote.models.ApiEventPayloadType.EnrolmentRecordCreation
import com.simprints.eventsystem.event.remote.models.ApiEventPayloadType.EnrolmentRecordDeletion
import com.simprints.eventsystem.event.remote.models.ApiEventPayloadType.EnrolmentRecordMove
import com.simprints.eventsystem.event.remote.models.fromDomainToApi
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_MODULE_ID
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_MODULE_ID_2
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_USER_ID
import com.simprints.eventsystem.sampledata.SampleDefaults.GUID1
import com.simprints.eventsystem.sampledata.SampleDefaults.GUID2
import com.simprints.eventsystem.sampledata.createSessionCaptureEvent
import io.kotest.assertions.throwables.shouldThrow
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifySequence
import io.mockk.excludeRecords
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

typealias CountInvocation<T, V> = suspend (T) -> V

@OptIn(ExperimentalCoroutinesApi::class)
class EventRemoteDataSourceImplTest {

    @MockK lateinit var simApiClientFactory: SimApiClientFactory
    @MockK lateinit var simApiClient: SimApiClient<EventRemoteInterface>
    @MockK lateinit var eventRemoteInterface: EventRemoteInterface

    private lateinit var eventRemoteDataSource: EventRemoteDataSource
    private val query = ApiRemoteEventQuery(
        projectId = DEFAULT_PROJECT_ID,
        userId = DEFAULT_USER_ID,
        moduleIds = listOf(DEFAULT_MODULE_ID, DEFAULT_MODULE_ID_2),
        subjectId = GUID1,
        lastEventId = GUID2,
        modes = listOf(ApiModes.FACE, ApiModes.FINGERPRINT),
        types = listOf(EnrolmentRecordCreation, EnrolmentRecordDeletion, EnrolmentRecordMove)
    )

    @Before
    @ExperimentalCoroutinesApi
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        coEvery { simApiClient.executeCall<Int>(any(), any()) } coAnswers {
            val args = this.args
            (args[1] as CountInvocation<EventRemoteInterface, Int>).invoke(eventRemoteInterface)
        }

        coEvery { simApiClientFactory.buildClient(EventRemoteInterface::class) } returns simApiClient
        eventRemoteDataSource = EventRemoteDataSourceImpl(simApiClientFactory, JsonHelper)
    }

    @Test
    fun count_shouldMakeANetworkRequest() {
        runBlocking {
            coEvery { eventRemoteInterface.countEvents(any(), any(), any(), any(), any(), any(), any()) } returns listOf(ApiEventCount(EnrolmentRecordCreation, 1))

            val count = eventRemoteDataSource.count(query)

            assertThat(count).isEqualTo(listOf(EventCount(ENROLMENT_RECORD_CREATION, 1)))
            coVerify(exactly = 1) {
                eventRemoteInterface.countEvents(
                    DEFAULT_PROJECT_ID,
                    listOf(DEFAULT_MODULE_ID, DEFAULT_MODULE_ID_2),
                    DEFAULT_USER_ID, GUID1,
                    listOf(ApiModes.FACE, ApiModes.FINGERPRINT),
                    GUID2,
                    listOf(EnrolmentRecordCreation, EnrolmentRecordDeletion, EnrolmentRecordMove)
                )
            }
        }
    }

    @Test
    fun errorForCountRequestFails_shouldThrowAnException() {
        runBlocking {
            coEvery { eventRemoteInterface.countEvents(any(), any(), any(), any(), any(), any(), any()) } throws Throwable("Request issue")

            shouldThrow<Throwable> {
                eventRemoteDataSource.count(query)
            }
        }
    }

    @Test
    fun downloadEvents_shouldParseStreamAndEmitBatches() {
        runBlocking {
            val responseStreamWith6Events = this.javaClass.classLoader?.getResourceAsStream("responses/down_sync_7events.json")!!
            val channel = mockk<ProducerScope<Event>>(relaxed = true)
            excludeRecords { channel.isClosedForSend }

            (eventRemoteDataSource as EventRemoteDataSourceImpl).parseStreamAndEmitEvents(responseStreamWith6Events, channel)

            verifySequenceOfEventsEmitted(channel,
                listOf(
                    ENROLMENT_RECORD_CREATION,
                    ENROLMENT_RECORD_DELETION,
                    ENROLMENT_RECORD_MOVE,
                    ENROLMENT_RECORD_CREATION,
                    ENROLMENT_RECORD_DELETION,
                    ENROLMENT_RECORD_MOVE,
                    ENROLMENT_RECORD_MOVE))
        }
    }

    private fun verifySequenceOfEventsEmitted(channel: ProducerScope<Event>, eventTypes: List<EventType>) {
        coVerifySequence {
            eventTypes.forEach { eventType ->
                channel.send(match {
                    it.type == eventType
                })
            }
            channel.close(null)
        }
    }

    @Test
    fun getEvents_shouldThrowAnException() {
        runBlocking {
            coEvery { eventRemoteInterface.downloadEvents(any(), any(), any(), any(), any(), any(), any()) } throws Throwable("Request issue")

            shouldThrow<Throwable> {
                eventRemoteDataSource.getEvents(query, this)
            }
        }
    }

    @Test
    fun getEvents_shouldMakeTheRightRequest() {
        runBlocking {
            shouldThrow<Throwable> {
                eventRemoteDataSource.getEvents(query, this)
            }

            with(query) {
                coVerify {
                    eventRemoteInterface.downloadEvents(
                        projectId, moduleIds, userId, subjectId, modes, lastEventId, types.map { it.name }
                    )
                }
            }
        }
    }

    @Test
    fun postEvent_shouldUploadEvents() {
        runBlocking {
            coEvery { eventRemoteInterface.uploadEvents(any(), any()) } returns mockk()

            val events = listOf(createSessionCaptureEvent())
            eventRemoteDataSource.post(DEFAULT_PROJECT_ID, events)

            coVerify(exactly = 1) {
                eventRemoteInterface.uploadEvents(
                    DEFAULT_PROJECT_ID,
                    match {
                        assertThat(it.events).containsExactlyElementsIn(events.map { it.fromDomainToApi() })
                        true
                    }
                )
            }
        }
    }

    @Test
    fun postEventFails_shouldThrowAnException() {
        runBlocking {
            coEvery { eventRemoteInterface.uploadEvents(any(), any()) } throws Throwable("Request issue")

            shouldThrow<Throwable> {
                eventRemoteDataSource.post(DEFAULT_PROJECT_ID, listOf(createSessionCaptureEvent()))
            }
        }
    }
}
