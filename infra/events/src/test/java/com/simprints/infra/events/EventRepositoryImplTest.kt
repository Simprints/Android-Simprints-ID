package com.simprints.infra.events

import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.time.TimeHelper
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.GeneralConfiguration.Modality
import com.simprints.infra.events.EventRepositoryImpl.Companion.PROJECT_ID_FOR_NOT_SIGNED_IN
import com.simprints.infra.events.domain.validators.EventValidator
import com.simprints.infra.events.domain.validators.SessionEventValidatorsFactory
import com.simprints.infra.events.event.domain.models.EventLabels
import com.simprints.infra.events.event.domain.models.EventType
import com.simprints.infra.events.event.domain.models.EventType.CALLBACK_ENROLMENT
import com.simprints.infra.events.event.domain.models.session.SessionCaptureEvent
import com.simprints.infra.events.event.domain.models.session.SessionScope
import com.simprints.infra.events.event.local.EventLocalDataSource
import com.simprints.infra.events.event.local.SessionDataCache
import com.simprints.infra.events.exceptions.validator.DuplicateGuidSelectEventValidatorException
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.infra.events.sampledata.SampleDefaults.GUID1
import com.simprints.infra.events.sampledata.createAlertScreenEvent
import com.simprints.infra.events.sampledata.createSessionScope
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
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
    lateinit var configRepository: ConfigRepository

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        every { timeHelper.now() } returns NOW
        every { authStore.signedInProjectId } returns DEFAULT_PROJECT_ID
        every { sessionDataCache.eventCache } returns mutableMapOf()
        every { sessionDataCache.sessionScope } returns null
        every { sessionEventValidatorsFactory.build() } returns arrayOf(eventValidator)
        coEvery { configRepository.getProjectConfiguration() } returns mockk {
            every { general } returns mockk {
                every { modalities } returns listOf(Modality.FINGERPRINT, Modality.FACE)
            }
        }
        coEvery { configRepository.getDeviceConfiguration() } returns mockk {
            every { language } returns LANGUAGE
        }

        eventRepo = EventRepositoryImpl(
            deviceId = DEVICE_ID,
            appVersionName = APP_VERSION_NAME,
            libSimprintsVersionName = LIB_VERSION_NAME,
            authStore = authStore,
            eventLocalDataSource = eventLocalDataSource,
            timeHelper = timeHelper,
            validatorsFactory = sessionEventValidatorsFactory,
            sessionDataCache = sessionDataCache,
            configRepository = configRepository,
        )
    }

    @Test
    fun `create session should have the right session count`() {
        runTest {
            mockDbToHaveOneOpenSession()
            coEvery { eventLocalDataSource.countSessions() } returns N_SESSIONS_DB

            eventRepo.createSession()

            coVerify {
                eventLocalDataSource.saveSessionScope(match {
                    it.payload.databaseInfo.sessionCount == N_SESSIONS_DB
                })
            }
        }
    }

    @Test
    fun `create session for empty project id`() {
        runTest {
            every { authStore.signedInProjectId } returns ""

            val session = eventRepo.createSession()

            assertThat(session.projectId).isEqualTo(PROJECT_ID_FOR_NOT_SIGNED_IN)
        }
    }

    @Test(expected = DuplicateGuidSelectEventValidatorException::class)
    fun `create session report duplicate GUID select EventValidatorExceptionException`() {
        runTest {
            coEvery { eventLocalDataSource.countSessions() } returns N_SESSIONS_DB
            coEvery {
                eventLocalDataSource.saveSessionScope(any())
            } throws DuplicateGuidSelectEventValidatorException("oops...")
            eventRepo.createSession()
        }
    }

    @Test
    fun `create session should close open session events`() {
        runTest {
            mockDbToHaveOneOpenSession()

            eventRepo.createSession()

            coVerify {
                eventLocalDataSource.saveSessionScope(match {
                    assertThatSessionScopeClosed(it)
                })
            }
        }
    }

    @Test
    fun `create session should add a new session event`() {
        runTest {
            mockDbToBeEmpty()

            eventRepo.createSession()

            coVerify {
                eventLocalDataSource.saveSessionScope(match { assertANewSessionCaptureWasAdded(it) })
            }
        }
    }

    @Test
    fun `returns true if there is open session in cache`() = runTest {
        every { sessionDataCache.sessionScope } returns createSessionScope()
        assertThat(eventRepo.hasOpenSession()).isTrue()
    }

    @Test
    fun `returns true if there is open session in local store`() = runTest {
        every { sessionDataCache.sessionScope } returns null
        mockDbToHaveOneOpenSession()
        assertThat(eventRepo.hasOpenSession()).isTrue()
    }

    @Test
    fun `returns false if there is no session in cache or local store`() = runTest {
        every { sessionDataCache.sessionScope } returns null
        coEvery { eventLocalDataSource.loadOpenedSessions() } returns emptyList()

        assertThat(eventRepo.hasOpenSession()).isFalse()
    }

    @Test
    fun `add event into a session should store it into the DB with right labels`() {
        runTest {
            mockDbToHaveOneOpenSession()
            val newEvent = createAlertScreenEvent()

            eventRepo.addOrUpdateEvent(newEvent)

            coVerify {
                eventLocalDataSource.saveEvent(
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
        runTest {
            mockDbToHaveOneOpenSession()
            val newEvent = createAlertScreenEvent()
            newEvent.labels = EventLabels()

            eventRepo.addOrUpdateEvent(newEvent)

            coVerify {
                eventLocalDataSource.saveEvent(
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
        runTest {
            mockDbToHaveOneOpenSession(GUID1)
            val newEvent = createAlertScreenEvent()

            eventRepo.addOrUpdateEvent(newEvent)

            coVerify {
                eventLocalDataSource.saveEvent(
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
    fun `test getCurrentSessionScope from db`() = runTest {

        val oldSessionScope = mockk<SessionScope> {
            every { endedAt } returns 2
            every { createdAt } returns 1
        }
        val recentSessionScope = mockk<SessionScope> {
            every { endedAt } returns null
            every { createdAt } returns 2
        }
        coEvery { eventLocalDataSource.loadOpenedSessions() } returns listOf(
            recentSessionScope,
            oldSessionScope
        )
        val loadedSession = eventRepo.getCurrentSessionScope()
        assertThat(loadedSession).isEqualTo(recentSessionScope)
    }

    @Test
    fun `test getCurrentSessionScope should create new CaptureSessionEvent is not exist`() =
        runTest {
            //Given
            coEvery { eventLocalDataSource.countSessions() } returns N_SESSIONS_DB
            every { authStore.signedInProjectId } returns "projectId"
            //When
            val loadedSession = eventRepo.getCurrentSessionScope()
            //Then
            assertThat(loadedSession.projectId).isEqualTo("projectId")
        }

    @Test
    fun `events loaded to cache on request from session`() = runTest {
        mockDbToLoadOpenSession(GUID1)

        eventRepo.observeEventsFromSession(GUID1).toList()

        coVerify { eventLocalDataSource.loadEventsInSession(any()) }
        assertThat(sessionDataCache.eventCache).isNotEmpty()
    }

    @Test
    fun `insert event into current open session`() {
        runTest {
            mockSignedId()
            val session = mockDbToHaveOneOpenSession(GUID1)
            val eventInSession = createAlertScreenEvent().removeLabels()

            eventRepo.addOrUpdateEvent(eventInSession)

            coVerify { eventLocalDataSource.loadOpenedSessions() }
            coVerify {
                eventLocalDataSource.saveEvent(
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
        runTest {
            mockSignedId()
            val sessionScope = mockDbToHaveOneOpenSession(GUID1)
            val eventInSession = createAlertScreenEvent().removeLabels()
            coEvery { eventLocalDataSource.loadEventsInSession(sessionId = sessionScope.id) } returns listOf(
                eventInSession
            )
            val newEvent = createAlertScreenEvent().removeLabels()

            eventRepo.addOrUpdateEvent(newEvent)

            verify { eventValidator.validate(listOf(eventInSession), newEvent) }
        }
    }

    @Test
    fun `should close current session correctly`() = runTest(StandardTestDispatcher()) {
        val sessionScope = mockDbToHaveOneOpenSession(GUID1)
        eventRepo.closeCurrentSession(null)

        assertThatSessionScopeClosed(sessionScope)
        coVerify(exactly = 0) {
            eventLocalDataSource.saveEvent(match { it.type == EventType.ARTIFICIAL_TERMINATION })
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
        coVerify(exactly = 0) { eventLocalDataSource.saveEvent(sessionCaptureEvent) }
    }

    @Test
    fun `test removeLocationDataFromCurrentSession remove location if location exist`() = runTest {
        // Given
        val sessionScope = createSessionScope()
        every { sessionDataCache.sessionScope } returns sessionScope
        //When
        eventRepo.removeLocationDataFromCurrentSession()
        //Then
        coVerify {
            eventLocalDataSource.saveSessionScope(match { it.payload.location == null })
        }
    }

    @Test
    fun `when observeEventCount called with null type return all events`() = runTest {
        coEvery { eventLocalDataSource.observeEventCount(any()) } returns flowOf(7)

        assertThat(eventRepo.observeEventCount("test", null).firstOrNull()).isEqualTo(7)

        coVerify(exactly = 1) { eventLocalDataSource.observeEventCount(any()) }
        coVerify(exactly = 0) { eventLocalDataSource.observeEventCount(any(), any()) }
    }

    @Test
    fun `when observeEventCount called with type return events of type`() = runTest {
        coEvery { eventLocalDataSource.observeEventCount(any(), any()) } returns flowOf(7)

        assertThat(
            eventRepo.observeEventCount("test", CALLBACK_ENROLMENT)
                .firstOrNull()
        ).isEqualTo(7)

        coVerify(exactly = 0) { eventLocalDataSource.observeEventCount(any()) }
        coVerify(exactly = 1) { eventLocalDataSource.observeEventCount(any(), any()) }
    }

    @Test
    fun `deleteSession should call local store`() = runTest {
        eventRepo.deleteSession("test")

        coVerify { eventLocalDataSource.deleteSession(eq("test")) }
        coVerify { eventLocalDataSource.deleteEventsInSession(eq("test")) }
    }

    @Test
    fun `getAllClosedSessionIds should call local store`() = runTest {
        coEvery { eventLocalDataSource.loadClosedSessions(any()) } returns emptyList()

        eventRepo.getAllClosedSessions("test")

        coVerify { eventLocalDataSource.loadClosedSessions(eq("test")) }
    }

    @Test
    fun `getEventsFromSession should call local store`() = runTest {
        coEvery { eventLocalDataSource.loadEventsInSession(any()) } returns emptyList()

        eventRepo.getEventsFromSession("test")

        coVerify { eventLocalDataSource.loadEventsInSession(eq("test")) }
    }

    @Test
    fun `getEventsJsonFromSession should call local store`() = runTest {
        coEvery { eventLocalDataSource.loadEventJsonInSession(any()) } returns emptyList()

        eventRepo.getEventsJsonFromSession("test")

        coVerify { eventLocalDataSource.loadEventJsonInSession(eq("test")) }
    }

    @Test
    fun `loadAll should call local store`() = runTest {
        coEvery { eventLocalDataSource.loadAllEvents() } returns emptyFlow()

        eventRepo.loadAll()

        coVerify { eventLocalDataSource.loadAllEvents() }
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
