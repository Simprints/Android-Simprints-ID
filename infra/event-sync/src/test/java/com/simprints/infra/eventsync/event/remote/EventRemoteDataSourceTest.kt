package com.simprints.infra.eventsync.event.remote

import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.json.JsonHelper
import com.simprints.infra.events.event.domain.EventCount
import com.simprints.infra.events.event.domain.models.Event
import com.simprints.infra.events.event.domain.models.EventType.*
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordEvent
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordEventType
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_MODULE_ID
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_MODULE_ID_2
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_USER_ID
import com.simprints.infra.events.sampledata.SampleDefaults.GUID1
import com.simprints.infra.events.sampledata.SampleDefaults.GUID2
import com.simprints.infra.eventsync.event.remote.exceptions.TooManyRequestsException
import com.simprints.infra.eventsync.event.remote.models.ApiEventCount
import com.simprints.infra.eventsync.event.remote.models.ApiEventPayloadType.*
import com.simprints.infra.eventsync.event.remote.models.fromDomainToApi
import com.simprints.infra.eventsync.event.remote.models.subject.ApiEnrolmentRecordPayloadType
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.network.SimNetwork
import com.simprints.infra.network.exceptions.BackendMaintenanceException
import com.simprints.infra.network.exceptions.SyncCloudIntegrationException
import com.simprints.testtools.common.alias.InterfaceInvocation
import com.simprints.testtools.common.syntax.assertThrows
import io.kotest.assertions.throwables.shouldThrow
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class EventRemoteDataSourceTest {

    @MockK
    lateinit var authStore: AuthStore

    @MockK
    private lateinit var simApiClient: SimNetwork.SimApiClient<EventRemoteInterface>

    @MockK
    private lateinit var eventRemoteInterface: EventRemoteInterface

    private lateinit var eventRemoteDataSource: EventRemoteDataSource
    private val query = ApiRemoteEventQuery(
        projectId = DEFAULT_PROJECT_ID,
        userId = DEFAULT_USER_ID.value,
        moduleIds = listOf(DEFAULT_MODULE_ID.value, DEFAULT_MODULE_ID_2),
        subjectId = GUID1,
        lastEventId = GUID2,
        modes = listOf(ApiModes.FACE, ApiModes.FINGERPRINT),
    )

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        coEvery { simApiClient.executeCall<Int>(any()) } coAnswers {
            val args = this.args
            @Suppress("UNCHECKED_CAST")
            (args[0] as InterfaceInvocation<EventRemoteInterface, Int>).invoke(eventRemoteInterface)
        }

        coEvery { authStore.buildClient(EventRemoteInterface::class) } returns simApiClient
        eventRemoteDataSource = EventRemoteDataSource(authStore, JsonHelper)
    }

    @Test
    fun count_shouldMakeANetworkRequest() {
        runBlocking {
            coEvery {
                eventRemoteInterface.countEvents(
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any()
                )
            } returns listOf(ApiEventCount(ApiEnrolmentRecordPayloadType.EnrolmentRecordCreation, 1))

            val count = eventRemoteDataSource.count(query)

            assertThat(count).isEqualTo(listOf(EventCount(EnrolmentRecordEventType.EnrolmentRecordCreation, 1)))
            coVerify(exactly = 1) {
                eventRemoteInterface.countEvents(
                    projectId = DEFAULT_PROJECT_ID,
                    moduleIds = listOf(DEFAULT_MODULE_ID.value, DEFAULT_MODULE_ID_2),
                    attendantId = DEFAULT_USER_ID.value,
                    subjectId = GUID1,
                    modes = listOf(ApiModes.FACE, ApiModes.FINGERPRINT),
                    lastEventId = GUID2
                )
            }
        }
    }

    @Test
    fun errorForCountRequestFails_shouldThrowAnException() {
        runBlocking {
            coEvery {
                eventRemoteInterface.countEvents(
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any()
                )
            } throws Throwable("Request issue")

            shouldThrow<Throwable> {
                eventRemoteDataSource.count(query)
            }
        }
    }

    @Test
    fun downloadEvents_shouldParseStreamAndEmitBatches() {
        runBlocking {
            val responseStreamWith6Events =
                this.javaClass.classLoader?.getResourceAsStream("responses/down_sync_7events.json")!!
            val channel = mockk<ProducerScope<EnrolmentRecordEvent>>(relaxed = true)
            excludeRecords { channel.isClosedForSend }

            (eventRemoteDataSource as EventRemoteDataSource).parseStreamAndEmitEvents(
                responseStreamWith6Events,
                channel
            )

            verifySequenceOfEventsEmitted(
                channel,
                listOf(
                    EnrolmentRecordEventType.EnrolmentRecordCreation,
                    EnrolmentRecordEventType.EnrolmentRecordDeletion,
                    EnrolmentRecordEventType.EnrolmentRecordMove,
                    EnrolmentRecordEventType.EnrolmentRecordCreation,
                    EnrolmentRecordEventType.EnrolmentRecordDeletion,
                    EnrolmentRecordEventType.EnrolmentRecordMove,
                    EnrolmentRecordEventType.EnrolmentRecordMove
                )
            )
        }
    }

    private fun verifySequenceOfEventsEmitted(
        channel: ProducerScope<EnrolmentRecordEvent>,
        eventTypes: List<EnrolmentRecordEventType>
    ) {
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
    fun `Get events should throw the exception received when downloading events`() {
        runBlocking {
            val exception = BackendMaintenanceException(estimatedOutage = 100)
            coEvery {
                eventRemoteInterface.downloadEvents(
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any()
                )
            } throws exception

            val exceptionThrown = assertThrows<BackendMaintenanceException> {
                eventRemoteDataSource.getEvents(query, this)
            }
            assertThat(exceptionThrown).isEqualTo(exception)
        }
    }

    @Test
    fun `Get events should map a SyncCloudIntegrationException with the status 429 to a TooManyRequestException`() {
        runBlocking {
            val exception = SyncCloudIntegrationException(
                cause = HttpException(
                    Response.error<Event>(
                        429,
                        "".toResponseBody()
                    )
                )
            )
            coEvery {
                eventRemoteInterface.downloadEvents(
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any()
                )
            } throws exception

            assertThrows<TooManyRequestsException> {
                eventRemoteDataSource.getEvents(query, this)
            }
        }
    }

    @Test
    fun getEvents_shouldMakeTheRightRequest() {
        runBlocking {
            val mockedScope: CoroutineScope = mockk()
            mockkStatic("kotlinx.coroutines.channels.ProduceKt")
            every { mockedScope.produce<Event>(capacity = 2000, block = any()) } returns mockk()
            eventRemoteDataSource.getEvents(query, mockedScope)

            with(query) {
                coVerify {
                    eventRemoteInterface.downloadEvents(
                        projectId, moduleIds, userId, subjectId, modes, lastEventId
                    )
                }
            }
        }
    }

    @Test
    fun postEvent_shouldUploadEvents() {
        runBlocking {
            coEvery { eventRemoteInterface.uploadEvents(any(), any(), any()) } returns mockk()

            val events = listOf(com.simprints.infra.events.sampledata.createSessionCaptureEvent())
            eventRemoteDataSource.post(DEFAULT_PROJECT_ID, events)

            coVerify(exactly = 1) {
                eventRemoteInterface.uploadEvents(
                    DEFAULT_PROJECT_ID,
                    true,
                    match { body ->
                        assertThat(body.events).containsExactlyElementsIn(events.map { it.fromDomainToApi() })
                        true
                    }
                )
            }
        }
    }

    @Test
    fun postEventFails_shouldThrowAnException() {
        runBlocking {
            coEvery {
                eventRemoteInterface.uploadEvents(
                    any(),
                    any(),
                    any()
                )
            } throws Throwable("Request issue")

            shouldThrow<Throwable> {
                eventRemoteDataSource.post(DEFAULT_PROJECT_ID, listOf(com.simprints.infra.events.sampledata.createSessionCaptureEvent()))
            }
        }
    }

    @Test
    fun dumpInvalidEvents_shouldDumpEvents() = runTest {
        coEvery { eventRemoteInterface.dumpInvalidEvents(any(), any(), any()) } returns mockk()

        val events = listOf("anEventJson")
        eventRemoteDataSource.dumpInvalidEvents(DEFAULT_PROJECT_ID, events)

        coVerify(exactly = 1) {
            eventRemoteInterface.dumpInvalidEvents(DEFAULT_PROJECT_ID, events = events)
        }
    }
}
