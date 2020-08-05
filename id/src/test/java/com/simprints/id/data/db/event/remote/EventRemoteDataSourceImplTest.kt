package com.simprints.id.data.db.event.remote

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODULE_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODULE_ID_2
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_USER_ID
import com.simprints.id.commontesttools.DefaultTestConstants.GUID1
import com.simprints.id.commontesttools.DefaultTestConstants.GUID2
import com.simprints.id.commontesttools.events.createSessionCaptureEvent
import com.simprints.id.data.db.common.models.EventCount
import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.EventType
import com.simprints.id.data.db.event.domain.models.EventType.*
import com.simprints.id.data.db.event.remote.models.ApiEventCount
import com.simprints.id.data.db.event.remote.models.ApiEventPayloadType.*
import com.simprints.id.data.db.event.remote.models.fromDomainToApi
import com.simprints.id.network.SimApiClient
import com.simprints.id.network.SimApiClientFactory
import com.simprints.id.testtools.UnitTestConfig
import io.kotlintest.shouldThrow
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

typealias CountInvocation<T, V> = suspend (T) -> V

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
class EventRemoteDataSourceImplTest {

    @MockK lateinit var simApiClientFactory: SimApiClientFactory
    @MockK lateinit var simApiClient: SimApiClient<EventRemoteInterface>
    @MockK lateinit var eventRemoteInterface: EventRemoteInterface

    private lateinit var eventRemoteDataSource: EventRemoteDataSource
    private val query = ApiEventQuery(
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
        UnitTestConfig(this).setupFirebase()

        coEvery { simApiClient.executeCall<Int>(any(), any()) } coAnswers {
            val args = this.args
            (args[1] as CountInvocation<EventRemoteInterface, Int>).invoke(eventRemoteInterface)
        }

        coEvery { simApiClientFactory.buildClient(EventRemoteInterface::class) } returns simApiClient
        eventRemoteDataSource = EventRemoteDataSourceImpl(simApiClientFactory)
    }

    @Test
    fun count_shouldMakeANetworkRequest() {
        runBlocking {
            coEvery { eventRemoteInterface.countEvents(any(), any(), any(), any(), any(), any(), any()) } returns listOf(ApiEventCount(EnrolmentRecordCreation, 1))

            val count = eventRemoteDataSource.count(query)

            assertThat(count).isEqualTo(listOf(EventCount(EventType.ENROLMENT_RECORD_CREATION, 1)))
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
            val channel = mockk<ProducerScope<List<Event>>>(relaxed = true)
            excludeRecords { channel.isClosedForSend }

            (eventRemoteDataSource as EventRemoteDataSourceImpl).parseStreamAndEmitEvents(responseStreamWith6Events, channel, 2)

            coVerifySequence {
                channel.send(match {
                    it[0].type == ENROLMENT_RECORD_CREATION && it[1].type == ENROLMENT_RECORD_DELETION
                })
                channel.send(match {
                    it[0].type == ENROLMENT_RECORD_MOVE && it[1].type == ENROLMENT_RECORD_CREATION
                })
                channel.send(match {
                    it[0].type == ENROLMENT_RECORD_DELETION && it[1].type == ENROLMENT_RECORD_MOVE
                })
                channel.send(match {
                    it[0].type == ENROLMENT_RECORD_MOVE
                })
            }

            coVerify(exactly = 0) { channel.close(any()) }
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
                        projectId, moduleIds, userId, subjectId, modes, lastEventId, types.map { it.key }
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
