package com.simprints.infra.eventsync.event.remote

import com.google.common.truth.Truth.*
import com.simprints.infra.backendapi.BackendApiClient
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.events.event.domain.EventCount
import com.simprints.infra.events.event.domain.models.EnrolmentRecordEvent
import com.simprints.infra.events.event.domain.models.EnrolmentRecordEventType
import com.simprints.infra.events.event.domain.models.Event
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_MODULE_ID
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_USER_ID
import com.simprints.infra.events.sampledata.SampleDefaults.GUID1
import com.simprints.infra.events.sampledata.SampleDefaults.GUID2
import com.simprints.infra.events.sampledata.createAlertScreenEvent
import com.simprints.infra.events.sampledata.createSessionScope
import com.simprints.infra.eventsync.event.remote.exceptions.TooManyRequestsException
import com.simprints.infra.eventsync.event.remote.models.session.ApiEventScope
import com.simprints.infra.eventsync.event.usecases.MapDomainEventScopeToApiUseCase
import com.simprints.infra.network.SimNetwork
import com.simprints.infra.network.exceptions.BackendMaintenanceException
import com.simprints.infra.network.exceptions.SyncCloudIntegrationException
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import okhttp3.Headers.Companion.toHeaders
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import kotlin.test.assertEquals

class EventRemoteDataSourceTest {
    lateinit var backendApiClient: BackendApiClient

    @MockK
    private lateinit var eventRemoteInterface: EventRemoteInterface

    @MockK
    private lateinit var mapDomainEventScopeToApiUseCase: MapDomainEventScopeToApiUseCase

    @MockK
    private lateinit var project: Project

    @MockK
    private lateinit var apiEventScope: ApiEventScope

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

        // Faking is simpler than mocking in this case
        backendApiClient = BackendApiClient(
            simNetwork = mockk {
                coEvery {
                    getSimApiClient(EventRemoteInterface::class, any(), any(), any())
                } returns mockk<SimNetwork.SimApiClient<EventRemoteInterface>> {
                    coEvery { executeCall(any<suspend (EventRemoteInterface) -> Any>()) } coAnswers {
                        firstArg<suspend (EventRemoteInterface) -> Any>()(eventRemoteInterface)
                    }
                }
            },
            authStore = mockk { coEvery { getFirebaseToken() } returns "token" },
            deviceId = "deviceId",
            versionName = "versionName",
        )

        every { mapDomainEventScopeToApiUseCase(any(), any(), any()) } returns apiEventScope
        eventRemoteDataSource = EventRemoteDataSource(backendApiClient)
    }

    @After
    fun tearDown() {
        unmockkStatic("kotlinx.coroutines.channels.ProduceKt")
    }

    @Test
    fun `count should make a network request`() = runTest {
        coEvery {
            eventRemoteInterface.countEvents(any(), any(), any(), any(), any())
        } returns Response.success(
            null,
            mapOf("x-event-count" to "6", "x-event-count-is-lower-bound" to "true").toHeaders(),
        )

        val count = eventRemoteDataSource.count(query)

        assertThat(count).isEqualTo(EventCount(6, true))
        coVerify(exactly = 1) {
            eventRemoteInterface.countEvents(
                projectId = DEFAULT_PROJECT_ID,
                moduleId = DEFAULT_MODULE_ID.value,
                attendantId = DEFAULT_USER_ID.value,
                subjectId = GUID1,
                lastEventId = GUID2,
            )
        }
    }

    @Test
    fun `count request fails should throw an exception`() = runTest {
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
    fun `parseStreamAndEmitEvents should parse stream and emit events incrementally`() = runTest {
        val stream =
            javaClass.classLoader!!
                .getResourceAsStream("responses/down_sync_8events.json")

        val channel = Channel<EnrolmentRecordEvent>(Channel.RENDEZVOUS)
        val received = mutableListOf<EnrolmentRecordEvent>()

        val collector = launch {
            for (event in channel) {
                received += event
            }
        }
        eventRemoteDataSource.parseStreamAndEmitEvents(stream, channel)
        collector.join()
        assertEquals(
            listOf(
                EnrolmentRecordEventType.EnrolmentRecordCreation,
                EnrolmentRecordEventType.EnrolmentRecordDeletion,
                EnrolmentRecordEventType.EnrolmentRecordMove,
                EnrolmentRecordEventType.EnrolmentRecordCreation,
                EnrolmentRecordEventType.EnrolmentRecordDeletion,
                EnrolmentRecordEventType.EnrolmentRecordMove,
                EnrolmentRecordEventType.EnrolmentRecordMove,
                EnrolmentRecordEventType.EnrolmentRecordUpdate,
            ),
            received.map { it.type },
        )
    }

    @Test
    fun `getEvents should throw the exception received when downloading events`() = runTest {
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
    fun `getEvents should map a SyncCloudIntegrationException with the status 429 to a TooManyRequestException`() = runTest {
        val exception = SyncCloudIntegrationException(
            cause = HttpException(
                Response.error<Event>(
                    429,
                    "".toResponseBody(),
                ),
            ),
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
    fun `getEvents should make the right request`() = runTest {
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
                    GUID1,
                    projectId,
                    moduleId,
                    userId,
                    subjectId,
                    lastEventId,
                )
            }
        }
    }

    @Test
    fun `getEvents should return correct total header`() = runTest {
        coEvery {
            eventRemoteInterface.downloadEvents(any(), any(), any(), any(), any(), any())
        } returns Response.success(
            "".toResponseBody(),
            mapOf("x-event-count" to "22").toHeaders(),
        )

        val mockedScope: CoroutineScope = mockk()

        every { mockedScope.produce<Event>(capacity = 2000, block = any()) } returns mockk()
        assertThat(eventRemoteDataSource.getEvents(GUID1, query, mockedScope).totalCount).isEqualTo(
            22,
        )
    }

    @Test
    fun `getEvents should return correct status`() = runTest {
        coEvery {
            eventRemoteInterface.downloadEvents(any(), any(), any(), any(), any(), any())
        } returns Response.success(205, "".toResponseBody())

        val mockedScope: CoroutineScope = mockk()

        every { mockedScope.produce<Event>(capacity = 2000, block = any()) } returns mockk()
        assertThat(eventRemoteDataSource.getEvents(GUID1, query, mockedScope).status).isEqualTo(205)
    }

    @Test
    fun `getEvents should not return total header when lower bound`() = runTest {
        coEvery {
            eventRemoteInterface.downloadEvents(any(), any(), any(), any(), any(), any())
        } returns Response.success(
            "".toResponseBody(),
            mapOf("x-event-count" to "22", "x-event-count-is-lower-bound" to "true").toHeaders(),
        )

        val mockedScope: CoroutineScope = mockk()

        every { mockedScope.produce<Event>(capacity = 2000, block = any()) } returns mockk()
        assertThat(eventRemoteDataSource.getEvents(GUID1, query, mockedScope).totalCount).isNull()
    }

    @Test
    fun `post should upload events`() = runTest {
        coEvery {
            eventRemoteInterface.uploadEvents(
                any(),
                any(),
                any(),
                any(),
            )
        } returns Response.success(
            "".toResponseBody(),
            mapOf("x-request-id" to "requestId").toHeaders(),
        )

        val event = createAlertScreenEvent()
        val events = listOf(event)
        val scope = createSessionScope()
        eventRemoteDataSource.post(
            GUID1,
            DEFAULT_PROJECT_ID,
            ApiUploadEventsBody(
                sessions = listOf(mapDomainEventScopeToApiUseCase(scope, events, project)),
            ),
        )

        coVerify(exactly = 1) {
            eventRemoteInterface.uploadEvents(
                GUID1,
                DEFAULT_PROJECT_ID,
                true,
                match { body ->
                    assertThat(body.sessions).hasSize(1)
                    assertThat(body.sessions.firstOrNull())
                        .isEqualTo(apiEventScope)
                    true
                },
            )
        }
    }

    @Test
    fun `post fails should throw an exception`() = runTest {
        coEvery {
            eventRemoteInterface.uploadEvents(
                any(),
                any(),
                any(),
                any(),
            )
        } throws Throwable("Request issue")

        assertThrows<Throwable> {
            eventRemoteDataSource.post(
                GUID1,
                DEFAULT_PROJECT_ID,
                ApiUploadEventsBody(),
            )
        }
    }

    @Test
    fun `dumpInvalidEvents should dump events`() = runTest {
        coEvery { eventRemoteInterface.dumpInvalidEvents(any(), any(), any()) } returns mockk()

        val events = listOf("anEventJson")
        eventRemoteDataSource.dumpInvalidEvents(DEFAULT_PROJECT_ID, events)

        coVerify(exactly = 1) {
            eventRemoteInterface.dumpInvalidEvents(DEFAULT_PROJECT_ID, events = events)
        }
    }

    @Test
    fun `getEvents should throw an exception received on failure response`() = runTest {
        coEvery {
            eventRemoteInterface.downloadEvents(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
            )
        } returns Response.error(426, "".toResponseBody())

        assertThrows<SyncCloudIntegrationException> {
            eventRemoteDataSource.getEvents(GUID1, query, this)
        }
    }

    @Test
    fun `getEvents should throw an exception received on too many requests response`() = runTest {
        coEvery {
            eventRemoteInterface.downloadEvents(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
            )
        } returns Response.error(429, "".toResponseBody())

        assertThrows<TooManyRequestsException> {
            eventRemoteDataSource.getEvents(GUID1, query, this)
        }
    }

    @Test
    fun `post failure response should throw an exception`() = runTest {
        coEvery {
            eventRemoteInterface.uploadEvents(
                any(),
                any(),
                any(),
                any(),
            )
        } returns Response.error(426, "".toResponseBody())

        assertThrows<SyncCloudIntegrationException> {
            eventRemoteDataSource.post(
                GUID1,
                DEFAULT_PROJECT_ID,
                ApiUploadEventsBody(),
            )
        }
    }
}
