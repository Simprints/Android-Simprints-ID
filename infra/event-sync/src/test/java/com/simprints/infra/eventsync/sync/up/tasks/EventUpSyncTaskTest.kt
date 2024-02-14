package com.simprints.infra.eventsync.sync.up.tasks

import com.fasterxml.jackson.core.JsonParseException
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.json.JsonHelper
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.core.tools.utils.randomUUID
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.SynchronizationConfiguration
import com.simprints.infra.config.store.models.UpSynchronizationConfiguration
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.scope.EventScope
import com.simprints.infra.events.event.domain.models.scope.EventScopeType
import com.simprints.infra.events.event.domain.models.upsync.EventUpSyncRequestEvent
import com.simprints.infra.events.sampledata.*
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.infra.events.sampledata.SampleDefaults.GUID1
import com.simprints.infra.events.sampledata.SampleDefaults.GUID2
import com.simprints.infra.events.sampledata.SampleDefaults.GUID3
import com.simprints.infra.eventsync.SampleSyncScopes
import com.simprints.infra.eventsync.event.remote.EventRemoteDataSource
import com.simprints.infra.eventsync.exceptions.TryToUploadEventsForNotSignedProject
import com.simprints.infra.eventsync.status.up.EventUpSyncScopeRepository
import com.simprints.infra.eventsync.status.up.domain.EventUpSyncOperation
import com.simprints.infra.eventsync.status.up.domain.EventUpSyncOperation.UpSyncState
import com.simprints.infra.network.exceptions.NetworkConnectionException
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

internal class EventUpSyncTaskTest {

    private val operation = SampleSyncScopes.projectUpSyncScope.operation

    private lateinit var eventUpSyncTask: EventUpSyncTask

    @MockK
    private lateinit var eventUpSyncScopeRepository: EventUpSyncScopeRepository

    @MockK
    lateinit var authStore: AuthStore

    @MockK
    lateinit var eventRepo: EventRepository

    @MockK
    lateinit var eventRemoteDataSource: EventRemoteDataSource

    @MockK
    private lateinit var timeHelper: TimeHelper

    @MockK
    private lateinit var synchronizationConfiguration: SynchronizationConfiguration

    @MockK
    private lateinit var projectConfiguration: ProjectConfiguration

    @MockK
    private lateinit var configRepository: ConfigRepository

    @MockK
    private lateinit var eventScope: EventScope

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        every { timeHelper.now() } returns NOW
        every { authStore.signedInProjectId } returns DEFAULT_PROJECT_ID

        every { projectConfiguration.synchronization } returns synchronizationConfiguration
        coEvery { configRepository.getProjectConfiguration() } returns projectConfiguration

        eventUpSyncTask = EventUpSyncTask(
            authStore = authStore,
            eventUpSyncScopeRepo = eventUpSyncScopeRepository,
            eventRepository = eventRepo,
            eventRemoteDataSource = eventRemoteDataSource,
            timeHelper = timeHelper,
            configRepository = configRepository,
            jsonHelper = JsonHelper,
        )
    }

    @Test
    fun `upload fetches events for all provided closed sessions`() = runTest {
        setUpSyncKind(UpSynchronizationConfiguration.UpSynchronizationKind.NONE)

        coEvery { eventRepo.getClosedEventScopes(any()) } returns emptyList()
        coEvery { eventRepo.getClosedEventScopes(EventScopeType.SESSION) } returns listOf(
            createSessionScope(GUID1),
            createSessionScope(GUID2)
        )
        coEvery {
            eventRepo.getEventsFromScope(GUID1)
        } returns listOf(createEventWithSessionId(GUID1, GUID1))
        coEvery {
            eventRepo.getEventsFromScope(GUID2)
        } returns listOf(createEventWithSessionId(GUID2, GUID2))

        eventUpSyncTask.upSync(operation, eventScope).toList()

        coVerify(exactly = 2) { eventRepo.getEventsFromScope(any()) }
    }

    @Test
    fun `upload should not filter any session events on upload`() = runTest {
        setUpSyncKind(UpSynchronizationConfiguration.UpSynchronizationKind.ALL)

        coEvery { eventRepo.getClosedEventScopes(any()) } returns emptyList()
        coEvery { eventRepo.getClosedEventScopes(EventScopeType.SESSION) } returns listOf(
            createSessionScope(GUID1)
        )
        coEvery { eventRepo.getEventsFromScope(any()) } returns listOf(
            createAuthenticationEvent(),
            createEnrolmentEventV2(),
        )

        eventUpSyncTask.upSync(operation, eventScope).toList()

        coVerify {
            eventRemoteDataSource.post(
                any(),
                withArg {
                    assertThat(it.sessions.first().id).isEqualTo(GUID1)
                    assertThat(it.sessions.first().events).hasSize(2)
                },
                any()
            )
        }
    }

    @Test
    fun `upload should filter biometric session events on upload`() = runTest {
        setUpSyncKind(UpSynchronizationConfiguration.UpSynchronizationKind.ONLY_BIOMETRICS)

        coEvery { eventRepo.getClosedEventScopes(any()) } returns emptyList()
        coEvery { eventRepo.getClosedEventScopes(EventScopeType.SESSION) } returns listOf(
            createSessionScope(GUID1)
        )
        coEvery { eventRepo.getEventsFromScope(any()) } returns listOf(
            createAuthenticationEvent(),
            createAlertScreenEvent(),
            // only following should be uploaded
            createEnrolmentEventV2(),
            createPersonCreationEvent(),
            createFingerprintCaptureBiometricsEvent(),
            createFaceCaptureBiometricsEvent(),
        )

        eventUpSyncTask.upSync(operation, eventScope).toList()

        coVerify {
            eventRemoteDataSource.post(
                any(),
                withArg {
                    assertThat(it.sessions.first().id).isEqualTo(GUID1)
                    assertThat(it.sessions.first().events).hasSize(4)
                },
                any()
            )
        }
    }

    @Test
    fun `upload should filter analytics session events on upload`() = runTest {
        setUpSyncKind(UpSynchronizationConfiguration.UpSynchronizationKind.ONLY_ANALYTICS)

        coEvery { eventRepo.getClosedEventScopes(any()) } returns emptyList()
        coEvery { eventRepo.getClosedEventScopes(EventScopeType.SESSION) } returns listOf(
            createSessionScope(GUID1)
        )
        coEvery { eventRepo.getEventsFromScope(any()) } returns listOf(
            createFingerprintCaptureBiometricsEvent(),
            createFaceCaptureBiometricsEvent(),
            // only following should be uploaded
            createPersonCreationEvent(),
            createEnrolmentEventV2(),
            createAlertScreenEvent(),
        )

        eventUpSyncTask.upSync(operation, eventScope).toList()

        coVerify {
            eventRemoteDataSource.post(
                any(),
                withArg {
                    assertThat(it.sessions.first().id).isEqualTo(GUID1)
                    assertThat(it.sessions.first().events).hasSize(3)
                },
                any()
            )
        }
    }

    @Test
    fun `should not upload sessions for not signed project`() = runTest {
        assertThrows<TryToUploadEventsForNotSignedProject> {
            eventUpSyncTask.upSync(EventUpSyncOperation(randomUUID()), eventScope).toList()
        }
    }

    @Test
    fun `when upload succeeds it should delete session events`() = runTest {
        setUpSyncKind(UpSynchronizationConfiguration.UpSynchronizationKind.ALL)

        coEvery { eventRepo.getClosedEventScopes(any()) } returns listOf(
            createSessionScope(GUID1),
            createSessionScope(GUID2)
        )
        coEvery {
            eventRepo.getEventsFromScope(GUID1)
        } returns listOf(createEventWithSessionId(GUID1, GUID1))
        coEvery {
            eventRepo.getEventsFromScope(GUID2)
        } returns listOf(createEventWithSessionId(GUID2, GUID2))

        eventUpSyncTask.upSync(operation, eventScope).toList()

        coVerify {
            eventRepo.deleteEventScope(GUID1)
            eventRepo.deleteEventScope(GUID2)
        }
    }

    @Test
    fun `upload in progress should emit progress`() = runTest {
        setUpSyncKind(UpSynchronizationConfiguration.UpSynchronizationKind.NONE)

        coEvery { eventRepo.getClosedEventScopes(any()) } returns emptyList()
        coEvery { eventRepo.getClosedEventScopes(EventScopeType.SESSION) } returns listOf(
            createSessionScope(GUID1),
            createSessionScope(GUID2)
        )
        coEvery { eventRepo.getEventsFromScope(GUID1) } returns listOf(
            createEventWithSessionId(GUID1, GUID1),
        )
        coEvery { eventRepo.getEventsFromScope(GUID2) } returns listOf(
            createEnrolmentEventV2(),
            createAlertScreenEvent(),
        )

        val progress = eventUpSyncTask.upSync(operation, eventScope).toList()

        assertThat(progress[0].operation.lastState).isEqualTo(UpSyncState.RUNNING)
        assertThat(progress[1].operation.lastState).isEqualTo(UpSyncState.RUNNING)
        assertThat(progress[2].operation.lastState).isEqualTo(UpSyncState.COMPLETE)
    }

    @Test
    fun `when upload fails due to generic error should not delete session events`() = runTest {
        setUpSyncKind(UpSynchronizationConfiguration.UpSynchronizationKind.NONE)

        coEvery { eventRepo.getClosedEventScopes(any()) } returns listOf(createSessionScope(GUID1))
        coEvery {
            eventRepo.getEventsFromScope(GUID1)
        } returns listOf(createEventWithSessionId(GUID1, GUID1))

        coEvery { eventRemoteDataSource.post(any(), any()) } throws Throwable("")

        eventUpSyncTask.upSync(operation, eventScope).toList()

        coVerify(exactly = 0) { eventRepo.deleteEventScope(any()) }
    }

    @Test
    fun `when upload fails due to network issue should not delete session events`() = runTest {
        setUpSyncKind(UpSynchronizationConfiguration.UpSynchronizationKind.NONE)

        coEvery { eventRepo.getClosedEventScopes(any()) } returns emptyList()
        coEvery { eventRepo.getClosedEventScopes(EventScopeType.SESSION) } returns listOf(
            createSessionScope(GUID1)
        )
        coEvery {
            eventRepo.getEventsFromScope(GUID1)
        } returns listOf(createEventWithSessionId(GUID1, GUID1))

        coEvery { eventRemoteDataSource.post(any(), any()) } throws NetworkConnectionException(
            cause = Exception()
        )

        eventUpSyncTask.upSync(operation, eventScope).toList()

        coVerify(exactly = 0) { eventRepo.deleteEventScope(any()) }
    }

    @Test
    fun `upload should not dump events when fetch fails`() = runTest {
        setUpSyncKind(UpSynchronizationConfiguration.UpSynchronizationKind.ALL)

        coEvery { eventRepo.getClosedEventScopes(any()) } returns listOf(createSessionScope(GUID1))
        coEvery { eventRepo.getEventsFromScope(GUID1) } throws IllegalStateException()

        eventUpSyncTask.upSync(operation, eventScope).toList()

        coVerify(exactly = 0) {
            eventRemoteDataSource.post(any(), any())
            eventRemoteDataSource.dumpInvalidEvents(any(), any())
            eventRepo.deleteEventScope(GUID1)
        }
    }

    @Test
    fun `upload should dump invalid events and delete the events`() = runTest {
        setUpSyncKind(UpSynchronizationConfiguration.UpSynchronizationKind.ALL)

        coEvery { eventRepo.getClosedEventScopes(any()) } returns listOf(createSessionScope(GUID1))
        coEvery {
            eventRepo.getEventsFromScope(GUID1)
        } throws JsonParseException(mockk(relaxed = true), "")

        coEvery { eventRepo.getEventsJsonFromScope(GUID1) } returns listOf("{}")

        eventUpSyncTask.upSync(operation, eventScope).toList()

        coVerify(exactly = 0) { eventRemoteDataSource.post(any(), any()) }
        coVerify {
            eventRepo.getEventsJsonFromScope(any())
            eventRemoteDataSource.dumpInvalidEvents(any(), any())
            eventRepo.deleteEventScope(GUID1)
        }
    }

    @Test
    fun `fail dump of invalid events should not delete the events`() = runTest {
        setUpSyncKind(UpSynchronizationConfiguration.UpSynchronizationKind.ALL)

        coEvery { eventRepo.getClosedEventScopes(any()) } returns listOf(createSessionScope(GUID1))
        coEvery {
            eventRepo.getEventsFromScope(GUID1)
        } throws JsonParseException(mockk(relaxed = true), "")
        coEvery { eventRepo.getEventsJsonFromScope(GUID1) } returns listOf("{}")
        coEvery { eventRemoteDataSource.dumpInvalidEvents(any(), any()) } throws HttpException(
            Response.error<String>(503, "".toResponseBody(null))
        )

        eventUpSyncTask.upSync(operation, eventScope).toList()

        coVerify(exactly = 0) {
            eventRemoteDataSource.post(any(), any())
            eventRepo.deleteEventScope(GUID1)
        }

        coVerify {
            eventRepo.getEventsJsonFromScope(any())
            eventRemoteDataSource.dumpInvalidEvents(any(), any())
        }
    }

    @Test
    fun `upSync should emit a failure if data fetch fails`() = runTest {
        coEvery { eventRepo.getClosedEventScopes(any()) } returns emptyList()
        coEvery { eventRepo.getClosedEventScopes(EventScopeType.SESSION) } throws IllegalStateException()

        val progress = eventUpSyncTask.upSync(operation, eventScope).toList()

        assertThat(progress.first().operation.lastState).isEqualTo(UpSyncState.FAILED)
        coVerify(exactly = 1) { eventUpSyncScopeRepository.insertOrUpdate(any()) }
    }

    @Test
    fun `upSync should log network failures and continue execution`() = runTest {
        coEvery { eventRepo.getClosedEventScopes(any()) } returns emptyList()
        coEvery { eventRepo.getClosedEventScopes(EventScopeType.SESSION) } returns listOf(
            createSessionScope(GUID1),
        )
        coEvery { eventRemoteDataSource.post(any(), any()) } throws HttpException(
            Response.error<ResponseBody>(427, "".toResponseBody(null))
        )

        val progress = eventUpSyncTask.upSync(operation, eventScope).toList()

        assertThat(progress.first().operation.lastState).isEqualTo(UpSyncState.RUNNING)
        assertThat(progress.last().operation.lastState).isEqualTo(UpSyncState.COMPLETE)
        coVerify(exactly = 1) {
            eventRepo.addOrUpdateEvent(any(), match {
                it is EventUpSyncRequestEvent && !it.payload.errorType.isNullOrEmpty()
            })
        }
    }

    @Test
    fun `upload should save request event for each upload request`() = runTest {
        setUpSyncKind(UpSynchronizationConfiguration.UpSynchronizationKind.NONE)

        coEvery { eventRepo.getClosedEventScopes(any()) } returns listOf(
            createSessionScope(GUID1),
            createSessionScope(GUID2)
        )
        coEvery {
            eventRepo.getEventsFromScope(GUID1)
        } returns listOf(createEventWithSessionId(GUID1, GUID1))
        coEvery {
            eventRepo.getEventsFromScope(GUID2)
        } returns listOf(createEventWithSessionId(GUID2, GUID2))

        eventUpSyncTask.upSync(operation, eventScope).toList()

        coVerify(exactly = 1) {
            eventRepo.addOrUpdateEvent(any(), match {
                it is EventUpSyncRequestEvent && it.payload.content.sessionCount == 2
            })
        }
    }

    @Test
    fun `upload fetches events for all non-session scopes`() = runTest {
        setUpSyncKind(UpSynchronizationConfiguration.UpSynchronizationKind.NONE)

        coEvery { eventRepo.getClosedEventScopes(EventScopeType.SESSION) } returns emptyList()
        coEvery { eventRepo.getClosedEventScopes(EventScopeType.UP_SYNC) } returns listOf(
            createSessionScope(GUID1),
        )
        coEvery { eventRepo.getClosedEventScopes(EventScopeType.DOWN_SYNC) } returns listOf(
            createSessionScope(GUID2),
        )

        coEvery {
            eventRepo.getEventsFromScope(GUID1)
        } returns listOf(createEventWithSessionId(GUID1, GUID1))
        coEvery {
            eventRepo.getEventsFromScope(GUID2)
        } returns listOf(createEventWithSessionId(GUID2, GUID2))

        eventUpSyncTask.upSync(operation, eventScope).toList()

        coVerify(exactly = 2) { eventRepo.getEventsFromScope(any()) }
    }

    @Test
    fun `upload reports separate events for session and out-of-session scopes`() = runTest {
        setUpSyncKind(UpSynchronizationConfiguration.UpSynchronizationKind.NONE)

        coEvery { eventRepo.getClosedEventScopes(EventScopeType.SESSION) } returns listOf(
            createSessionScope(GUID1),
        )
        coEvery { eventRepo.getClosedEventScopes(EventScopeType.UP_SYNC) } returns listOf(
            createSessionScope(GUID2),
        )
        coEvery { eventRepo.getClosedEventScopes(EventScopeType.DOWN_SYNC) } returns listOf(
            createSessionScope(GUID3),
        )
        coEvery {
            eventRepo.getEventsFromScope(any())
        } returns listOf(createEventWithSessionId(GUID1, GUID1))

        eventUpSyncTask.upSync(operation, eventScope).toList()

        coVerify(exactly = 1) {
            eventRepo.addOrUpdateEvent(any(), match {
                it is EventUpSyncRequestEvent &&
                    it.payload.content == EventUpSyncRequestEvent.UpSyncContent(1, 0, 0)
            })
            eventRepo.addOrUpdateEvent(any(), match {
                it is EventUpSyncRequestEvent &&
                    it.payload.content == EventUpSyncRequestEvent.UpSyncContent(0, 1, 1)
            })
        }
    }

    private fun setUpSyncKind(kind: UpSynchronizationConfiguration.UpSynchronizationKind) {
        every { synchronizationConfiguration.up.simprints.kind } returns kind
    }

    companion object {

        private val NOW = Timestamp(1000L)
    }
}
