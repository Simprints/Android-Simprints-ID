package com.simprints.infra.eventsync.event.remote

import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.json.JsonHelper
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.events.event.domain.EventCount
import com.simprints.infra.events.event.domain.models.Event
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordEvent
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordEventType
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_MODULE_ID
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_USER_ID
import com.simprints.infra.events.sampledata.SampleDefaults.GUID1
import com.simprints.infra.events.sampledata.SampleDefaults.GUID2
import com.simprints.infra.events.sampledata.createAlertScreenEvent
import com.simprints.infra.events.sampledata.createSessionScope
import com.simprints.infra.eventsync.event.remote.exceptions.TooManyRequestsException
import com.simprints.infra.eventsync.event.remote.models.session.ApiEventScope
import com.simprints.infra.network.SimNetwork
import com.simprints.infra.network.exceptions.BackendMaintenanceException
import com.simprints.infra.network.exceptions.SyncCloudIntegrationException
import com.simprints.testtools.common.alias.InterfaceInvocation
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifySequence
import io.mockk.every
import io.mockk.excludeRecords
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.test.runTest
import okhttp3.Headers.Companion.toHeaders
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
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
        moduleId = DEFAULT_MODULE_ID.value,
        subjectId = GUID1,
        lastEventId = GUID2,
    )

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        mockkStatic("kotlinx.coroutines.channels.ProduceKt")

        coEvery { simApiClient.executeCall<Int>(any()) } coAnswers {
            val args = this.args
            @Suppress("UNCHECKED_CAST")
            (args[0] as InterfaceInvocation<EventRemoteInterface, Int>).invoke(eventRemoteInterface)
        }

        coEvery { authStore.buildClient(EventRemoteInterface::class) } returns simApiClient
        eventRemoteDataSource = EventRemoteDataSource(authStore, JsonHelper)
    }

    @After
    fun tearDown() {
        unmockkStatic("kotlinx.coroutines.channels.ProduceKt")
    }

    @Test
    fun count_shouldMakeANetworkRequest() = runTest {
        coEvery {
            eventRemoteInterface.countEvents(any(), any(), any(), any(), any())
        } returns Response.success(
            null,
            mapOf("x-event-count" to "6", "x-event-count-is-lower-bound" to "true").toHeaders()
        )

        val count = eventRemoteDataSource.count(query)

        assertThat(count).isEqualTo(EventCount(6, true))
        coVerify(exactly = 1) {
            eventRemoteInterface.countEvents(
                projectId = DEFAULT_PROJECT_ID,
                moduleId = DEFAULT_MODULE_ID.value,
                attendantId = DEFAULT_USER_ID.value,
                subjectId = GUID1,
                lastEventId = GUID2
            )
        }
    }

    @Test
    fun errorForCountRequestFails_shouldThrowAnException() = runTest {
        coEvery {
            eventRemoteInterface.countEvents(
                any(),
                any(),
                any(),
                any(),
                any(),
            )
        } throws Throwable("Request issue")

        assertThrows<Throwable> {
            eventRemoteDataSource.count(query)
        }
    }

    @Test
    fun downloadEvents_shouldParseStreamAndEmitBatches() = runTest {
        val responseStreamWith6Events =
            this.javaClass.classLoader?.getResourceAsStream("responses/down_sync_7events.json")!!
        val channel = mockk<ProducerScope<EnrolmentRecordEvent>>(relaxed = true)
        excludeRecords { channel.isClosedForSend }

        eventRemoteDataSource.parseStreamAndEmitEvents(
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

    private fun verifySequenceOfEventsEmitted(
        channel: ProducerScope<EnrolmentRecordEvent>,
        eventTypes: List<EnrolmentRecordEventType>,
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
    fun `Get events should throw the exception received when downloading events`() = runTest {
        val exception = BackendMaintenanceException(estimatedOutage = 100)
        coEvery {
            eventRemoteInterface.downloadEvents(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
            )
        } throws exception

        val exceptionThrown = assertThrows<BackendMaintenanceException> {
            eventRemoteDataSource.getEvents(GUID1, query, this)
        }
        assertThat(exceptionThrown).isEqualTo(exception)
    }

    @Test
    fun `Get events should map a SyncCloudIntegrationException with the status 429 to a TooManyRequestException`() =
        runTest {
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
                    any(),
                )
            } throws exception

            assertThrows<TooManyRequestsException> {
                eventRemoteDataSource.getEvents(GUID1, query, this)
            }

        }

    @Test
    fun getEvents_shouldMakeTheRightRequest() = runTest {
        coEvery {
            eventRemoteInterface.downloadEvents(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
            )
        } returns Response.success("".toResponseBody())

        val mockedScope: CoroutineScope = mockk()
        every { mockedScope.produce<Event>(capacity = 2000, block = any()) } returns mockk()
        eventRemoteDataSource.getEvents(GUID1, query, mockedScope)

        with(query) {
            coVerify {
                eventRemoteInterface.downloadEvents(
                    GUID1, projectId, moduleId, userId, subjectId, lastEventId
                )
            }
        }
    }

    @Test
    fun getEvents_shouldReturnCorrectTotalHeader() = runTest {
        coEvery {
            eventRemoteInterface.downloadEvents(any(), any(), any(), any(), any(), any())
        } returns Response.success(
            "".toResponseBody(),
            mapOf("x-event-count" to "22").toHeaders()
        )

        val mockedScope: CoroutineScope = mockk()

        every { mockedScope.produce<Event>(capacity = 2000, block = any()) } returns mockk()
        assertThat(eventRemoteDataSource.getEvents(GUID1, query, mockedScope).totalCount).isEqualTo(
            22
        )
    }

    @Test
    fun getEvents_shouldReturnCorrectStatus() = runTest {
        coEvery {
            eventRemoteInterface.downloadEvents(any(), any(), any(), any(), any(), any())
        } returns Response.success(205, "".toResponseBody())

        val mockedScope: CoroutineScope = mockk()

        every { mockedScope.produce<Event>(capacity = 2000, block = any()) } returns mockk()
        assertThat(eventRemoteDataSource.getEvents(GUID1, query, mockedScope).status).isEqualTo(205)
    }

    @Test
    fun getEvents_shouldNotReturnTotalHeaderWhenLowerBound() = runTest {
        coEvery {
            eventRemoteInterface.downloadEvents(any(), any(), any(), any(), any(), any())
        } returns Response.success(
            "".toResponseBody(),
            mapOf("x-event-count" to "22", "x-event-count-is-lower-bound" to "true").toHeaders()
        )

        val mockedScope: CoroutineScope = mockk()

        every { mockedScope.produce<Event>(capacity = 2000, block = any()) } returns mockk()
        assertThat(eventRemoteDataSource.getEvents(GUID1, query, mockedScope).totalCount).isNull()
    }

    @Test
    fun postEvent_shouldUploadEvents() = runTest {
        coEvery {
            eventRemoteInterface.uploadEvents(
                any(),
                any(),
                any(),
                any()
            )
        } returns Response.success(
            "".toResponseBody(),
            mapOf("x-request-id" to "requestId").toHeaders()
        )

        val events = listOf(createAlertScreenEvent())
        val scope = createSessionScope()
        eventRemoteDataSource.post(
            GUID1,
            DEFAULT_PROJECT_ID,
            ApiUploadEventsBody(
                sessions = listOf(ApiEventScope.fromDomain(scope, events))
            )
        )

        coVerify(exactly = 1) {
            eventRemoteInterface.uploadEvents(
                GUID1,
                DEFAULT_PROJECT_ID,
                true,
                match { body ->
                    assertThat(body.sessions).hasSize(1)
                    assertThat(body.sessions.firstOrNull())
                        .isEqualTo(ApiEventScope.fromDomain(scope, events))
                    true
                }
            )
        }
    }

    @Test
    fun postEventFails_shouldThrowAnException() =
        runTest {
            coEvery {
                eventRemoteInterface.uploadEvents(
                    any(),
                    any(),
                    any(),
                    any()
                )
            } throws Throwable("Request issue")

            assertThrows<Throwable> {
                eventRemoteDataSource.post(
                    GUID1,
                    DEFAULT_PROJECT_ID,
                    ApiUploadEventsBody()
                )
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
