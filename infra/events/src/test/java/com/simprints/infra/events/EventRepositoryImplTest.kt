package com.simprints.infra.events

import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.time.TimeHelper
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.config.store.models.GeneralConfiguration.Modality
import com.simprints.infra.events.EventRepositoryImpl.Companion.PROJECT_ID_FOR_NOT_SIGNED_IN
import com.simprints.infra.events.domain.validators.EventValidator
import com.simprints.infra.events.domain.validators.SessionEventValidatorsFactory
import com.simprints.infra.events.event.domain.models.ArtificialTerminationEvent.ArtificialTerminationPayload.Reason.NEW_SESSION
import com.simprints.infra.events.event.domain.models.EventLabels
import com.simprints.infra.events.event.domain.models.EventType
import com.simprints.infra.events.event.domain.models.EventType.CALLBACK_ENROLMENT
import com.simprints.infra.events.event.domain.models.EventType.SESSION_CAPTURE
import com.simprints.infra.events.event.domain.models.session.SessionCaptureEvent
import com.simprints.infra.events.event.local.EventLocalDataSource
import com.simprints.infra.events.event.local.SessionDataCache
import com.simprints.infra.events.exceptions.validator.DuplicateGuidSelectEventValidatorException
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.infra.events.sampledata.SampleDefaults.GUID1
import com.simprints.infra.events.sampledata.createAlertScreenEvent
import com.simprints.infra.authstore.AuthStore
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

internal class EventRepositoryImplTest {

    private lateinit var eventRepo: EventRepository

    @MockK
    lateinit var authStore: AuthStore

    @MockK
    lateinit var eventLocalDataSource: EventLocalDataSource

    @MockK
    lateinit var timeHelper: TimeHelper

    @MockK
    lateinit var sessionEventValidatorsFactory: SessionEventValidatorsFactory

    @MockK
    lateinit var eventValidator: EventValidator

    @MockK
    lateinit var sessionDataCache: SessionDataCache

    @MockK
    lateinit var configManager: ConfigManager

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        every { timeHelper.now() } returns NOW
        every { authStore.signedInProjectId } returns DEFAULT_PROJECT_ID
        every { sessionDataCache.eventCache } returns mutableMapOf()
        every { sessionEventValidatorsFactory.build() } returns arrayOf(eventValidator)
        coEvery { configManager.getProjectConfiguration() } returns mockk {
            every { general } returns mockk {
                every { modalities } returns listOf(Modality.FINGERPRINT, Modality.FACE)
            }
        }
        coEvery { configManager.getDeviceConfiguration() } returns mockk {
            every { language } returns LANGUAGE
        }

        eventRepo = EventRepositoryImpl(
            DEVICE_ID,
            APP_VERSION_NAME,
            LIB_VERSION_NAME,
            authStore,
            eventLocalDataSource,
            timeHelper,
            sessionEventValidatorsFactory,
            sessionDataCache,
            configManager,
        )
    }

    @Test
    fun `create session should have the right session count`() {
        runBlocking {
            mockDbToHaveOneOpenSession()
            coEvery { eventLocalDataSource.count(SESSION_CAPTURE) } returns N_SESSIONS_DB

            eventRepo.createSession()

            coVerify {
                eventLocalDataSource.insertOrUpdate(match<SessionCaptureEvent> {
                    it.payload.databaseInfo.sessionCount == N_SESSIONS_DB
                })
            }
        }
    }

    @Test
    fun `create session for empty project id`() {
        runBlocking {
            every { authStore.signedInProjectId } returns ""
            coEvery { eventLocalDataSource.count(SESSION_CAPTURE) } returns N_SESSIONS_DB

            val session = eventRepo.createSession()

            assertThat(session.payload.projectId).isEqualTo(PROJECT_ID_FOR_NOT_SIGNED_IN)

        }
    }

    @Test(expected = DuplicateGuidSelectEventValidatorException::class)
    fun `create session report duplicate GUID select EventValidatorExceptionException`() {
        runBlocking {
            coEvery { eventLocalDataSource.count(SESSION_CAPTURE) } returns N_SESSIONS_DB
            coEvery {
                eventLocalDataSource.insertOrUpdate(any())
            } throws DuplicateGuidSelectEventValidatorException("oops...")
            eventRepo.createSession()
        }
    }

    @Test
    fun `create session should close open session events`() {
        runBlocking {
            val openSession = mockDbToHaveOneOpenSession()

            eventRepo.createSession()

            coVerify {
                eventLocalDataSource.insertOrUpdate(match {
                    assertThatArtificialTerminationEventWasAdded(it, openSession.id)
                })
            }

            coVerify {
                eventLocalDataSource.insertOrUpdate(match {
                    assertThatSessionCaptureEventWasClosed(it)
                })
            }
        }
    }

    @Test
    fun `create session should add a new session event`() {
        runBlocking {
            mockDbToBeEmpty()

            eventRepo.createSession()

            coVerify {
                eventLocalDataSource.insertOrUpdate(match {
                    assertANewSessionCaptureWasAdded(it)
                })
            }
        }
    }

    @Test
    fun `add event into a session should store it into the DB with right labels`() {
        runBlocking {
            mockDbToHaveOneOpenSession()
            val newEvent = createAlertScreenEvent()

            eventRepo.addOrUpdateEvent(newEvent)

            coVerify {
                eventLocalDataSource.insertOrUpdate(
                    newEvent.copy(
                        labels = EventLabels(
                            sessionId = GUID1,
                            deviceId = DEVICE_ID,
                            projectId = DEFAULT_PROJECT_ID
                        )
                    )
                )
            }
        }
    }

    @Test
    fun `add event should store it into the DB with right labels`() {
        runBlocking {
            mockDbToHaveOneOpenSession()
            val newEvent = createAlertScreenEvent()
            newEvent.labels = EventLabels()

            eventRepo.addOrUpdateEvent(newEvent)

            coVerify {
                eventLocalDataSource.insertOrUpdate(
                    newEvent.copy(
                        labels = EventLabels(
                            deviceId = DEVICE_ID,
                            projectId = DEFAULT_PROJECT_ID,
                            sessionId = GUID1
                        )
                    )
                )
            }
        }
    }

    @Test
    fun `add event to current session should add event related to current session into DB`() {
        runBlocking {
            mockDbToHaveOneOpenSession(GUID1)
            val newEvent = createAlertScreenEvent()

            eventRepo.addOrUpdateEvent(newEvent)

            coVerify {
                eventLocalDataSource.insertOrUpdate(
                    newEvent.copy(
                        labels = EventLabels(
                            sessionId = GUID1,
                            deviceId = DEVICE_ID,
                            projectId = DEFAULT_PROJECT_ID
                        )
                    )
                )
            }
        }
    }

    @Test
    fun `createSession should add artificial termination event to the previous one`() {
        runBlocking {
            mockDbToHaveOneOpenSession(GUID1)

            eventRepo.createSession()

            coVerify { eventLocalDataSource.loadOpenedSessions() }
            verifyArtificialEventWasAdded(GUID1, NEW_SESSION)
        }
    }

    @Test
    fun `test getCurrentCaptureSessionEvent from cached events`() = runTest {
        //Given
        val closedSessionCaptureEvent = mockk<SessionCaptureEvent> {
            every { payload.sessionIsClosed } returns true
        }
        val oldOpenedSessionCaptureEvent = mockk<SessionCaptureEvent> {
            every { payload.sessionIsClosed } returns false
            every { payload.createdAt } returns 1
        }
        val recentOpenedSessionCaptureEvent = mockk<SessionCaptureEvent> {
            every { payload.sessionIsClosed } returns false
            every { payload.createdAt } returns 2
        }
        every { sessionDataCache.eventCache } returns mutableMapOf(
            "SessionCaptureEvent1" to closedSessionCaptureEvent,
            "SessionCaptureEvent2" to oldOpenedSessionCaptureEvent,
            "SessionCaptureEvent3" to recentOpenedSessionCaptureEvent
        )
        //When
        val loadedSession = eventRepo.getCurrentCaptureSessionEvent()
        //Then
        assertThat(loadedSession).isEqualTo(recentOpenedSessionCaptureEvent)
    }

    @Test
    fun `test getCurrentCaptureSessionEvent from db`() = runTest {

        val oldOpenedSessionCaptureEvent = mockk<SessionCaptureEvent> {
            every { payload.sessionIsClosed } returns false
            every { payload.createdAt } returns 1
        }
        val recentOpenedSessionCaptureEvent = mockk<SessionCaptureEvent> {
            every { payload.sessionIsClosed } returns false
            every { payload.createdAt } returns 2
        }
        coEvery { eventLocalDataSource.loadOpenedSessions() } returns flowOf(
            recentOpenedSessionCaptureEvent,
            oldOpenedSessionCaptureEvent
        )
        val loadedSession = eventRepo.getCurrentCaptureSessionEvent()
        assertThat(loadedSession).isEqualTo(recentOpenedSessionCaptureEvent)
    }


    @Test
    fun `test getCurrentCaptureSessionEvent should create new CaptureSessionEvent is not exist`() =
        runTest {
            //Given
            coEvery { eventLocalDataSource.count(SESSION_CAPTURE) } returns N_SESSIONS_DB
            every { authStore.signedInProjectId } returns "projectId"
            //When
            val loadedSession = eventRepo.getCurrentCaptureSessionEvent()
            //Then
            assertThat(loadedSession.labels.projectId).isEqualTo("projectId")
        }

    @Test
    fun `events loaded to cache on request from session`() = runTest {
        mockDbToLoadOpenSession(GUID1)

        eventRepo.observeEventsFromSession(GUID1).toList()

        coVerify { eventLocalDataSource.loadAllFromSession(any()) }
        assertThat(sessionDataCache.eventCache).isNotEmpty()
    }

    @Test
    fun `insert event into current open session`() {
        runBlocking {
            mockSignedId()
            val session = mockDbToHaveOneOpenSession(GUID1)
            val eventInSession = createAlertScreenEvent().removeLabels()

            eventRepo.addOrUpdateEvent(eventInSession)

            coVerify { eventLocalDataSource.loadOpenedSessions() }
            coVerify {
                eventLocalDataSource.insertOrUpdate(
                    eventInSession.copy(
                        labels = EventLabels(
                            deviceId = DEVICE_ID,
                            sessionId = session.id,
                            projectId = DEFAULT_PROJECT_ID
                        )
                    )
                )
            }
        }
    }

    @Test
    fun `insert event into current open session should invoke validators`() {
        runBlocking {
            mockSignedId()
            val session = mockDbToHaveOneOpenSession(GUID1)
            val eventInSession = createAlertScreenEvent().removeLabels()
            coEvery { eventLocalDataSource.loadAllFromSession(sessionId = session.id) } returns listOf(
                session,
                eventInSession
            )
            val newEvent = createAlertScreenEvent().removeLabels()

            eventRepo.addOrUpdateEvent(newEvent)

            verify { eventValidator.validate(listOf(eventInSession), newEvent) }
        }
    }

    @Test
    fun `should close current session correctly`() = runTest(StandardTestDispatcher()) {
        val session = mockDbToHaveOneOpenSession(GUID1)
        eventRepo.closeCurrentSession(null)

        assertThatSessionCaptureEventWasClosed(session)
        coVerify(exactly = 0) {
            eventLocalDataSource.insertOrUpdate(match { it.type == EventType.ARTIFICIAL_TERMINATION })
        }
    }

    @Test
    fun `test removeLocationDataFromCurrentSession does nothing if location is null`() = runTest {
        // Given
        val sessionCaptureEvent = mockk<SessionCaptureEvent> {
            every { payload.location } returns null
        }
        every { sessionDataCache.eventCache } returns mutableMapOf(("SessionCaptureEvent" to sessionCaptureEvent))
        //When
        eventRepo.removeLocationDataFromCurrentSession()
        //Then
        coVerify(exactly = 0) { eventLocalDataSource.insertOrUpdate(sessionCaptureEvent) }
    }

    @Test
    fun `test removeLocationDataFromCurrentSession remove location if location exist`() = runTest {
        // Given
        val sessionCaptureEvent = mockk<SessionCaptureEvent> {
            every { payload.location } returns mockk()
        }
        every { sessionDataCache.eventCache } returns mutableMapOf(("SessionCaptureEvent" to sessionCaptureEvent))
        //When
        eventRepo.removeLocationDataFromCurrentSession()
        //Then
        coVerify { eventLocalDataSource.insertOrUpdate(sessionCaptureEvent) }
    }

    @Test
    fun `when observeEventCount called with null type return all events`() = runTest {
        coEvery { eventLocalDataSource.observeCount(any()) } returns flowOf(7)

        assertThat(eventRepo.observeEventCount("test", null).firstOrNull()).isEqualTo(7)

        coVerify(exactly = 1) { eventLocalDataSource.observeCount(any()) }
        coVerify(exactly = 0) { eventLocalDataSource.observeCount(any(), any()) }
    }

    @Test
    fun `when observeEventCount called with type return events of type`() = runTest {
        coEvery { eventLocalDataSource.observeCount(any(), any()) } returns flowOf(7)

        assertThat(eventRepo.observeEventCount("test", CALLBACK_ENROLMENT)
            .firstOrNull()).isEqualTo(7)

        coVerify(exactly = 0) { eventLocalDataSource.observeCount(any()) }
        coVerify(exactly = 1) { eventLocalDataSource.observeCount(any(), any()) }
    }

    @Test
    fun `deleteSessionEvents should call local store`() = runTest {
        eventRepo.deleteSessionEvents("test")

        coVerify { eventLocalDataSource.deleteAllFromSession(eq("test")) }
    }

    @Test
    fun `getAllClosedSessionIds should call local store`() = runTest {
        coEvery { eventLocalDataSource.loadAllClosedSessionIds(any()) } returns emptyList()

        eventRepo.getAllClosedSessionIds("test")

        coVerify { eventLocalDataSource.loadAllClosedSessionIds(eq("test")) }
    }

    @Test
    fun `getEventsFromSession should call local store`() = runTest {
        coEvery { eventLocalDataSource.loadAllFromSession(any()) } returns emptyList()

        eventRepo.getEventsFromSession("test")

        coVerify { eventLocalDataSource.loadAllFromSession(eq("test")) }
    }

    @Test
    fun `getEventsJsonFromSession should call local store`() = runTest {
        coEvery { eventLocalDataSource.loadAllEventJsonFromSession(any()) } returns emptyList()

        eventRepo.getEventsJsonFromSession("test")

        coVerify { eventLocalDataSource.loadAllEventJsonFromSession(eq("test")) }
    }

    @Test
    fun `loadAll should call local store`() = runTest {
        coEvery { eventLocalDataSource.loadAll() } returns emptyFlow()

        eventRepo.loadAll()

        coVerify { eventLocalDataSource.loadAll() }
    }

    @Test
    fun `delete should call local store`() = runTest {
        eventRepo.delete(listOf("test"))

        coVerify { eventLocalDataSource.delete(eq(listOf("test"))) }
    }

    @Test
    fun `deleteAll should call local store`() = runTest {
        eventRepo.deleteAll()

        coVerify { eventLocalDataSource.deleteAll() }
    }

    private fun mockSignedId() =
        every { authStore.signedInProjectId } returns DEFAULT_PROJECT_ID

    companion object {
        const val DEVICE_ID = "DEVICE_ID"
        const val APP_VERSION_NAME = "APP_VERSION_NAME"
        const val LIB_VERSION_NAME = "LIB_VERSION_NAME"
        const val LANGUAGE = "en"

        const val N_SESSIONS_DB = 3
        const val NOW = 1000L
    }
}
