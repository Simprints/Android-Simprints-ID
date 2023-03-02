package com.simprints.eventsystem.event

import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.modality.Modes
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.utils.randomUUID
import com.simprints.eventsystem.event.EventRepositoryImpl.Companion.PROJECT_ID_FOR_NOT_SIGNED_IN
import com.simprints.eventsystem.event.EventRepositoryImpl.Companion.SESSION_BATCH_SIZE
import com.simprints.eventsystem.event.domain.models.ArtificialTerminationEvent.ArtificialTerminationPayload.Reason.NEW_SESSION
import com.simprints.eventsystem.event.domain.models.EventLabels
import com.simprints.eventsystem.event.domain.models.EventType
import com.simprints.eventsystem.event.domain.models.EventType.SESSION_CAPTURE
import com.simprints.eventsystem.event.domain.models.session.SessionCaptureEvent
import com.simprints.eventsystem.event.domain.validators.EventValidator
import com.simprints.eventsystem.event.domain.validators.SessionEventValidatorsFactory
import com.simprints.eventsystem.event.local.EventLocalDataSource
import com.simprints.eventsystem.event.local.SessionDataCache
import com.simprints.eventsystem.event.remote.ApiModes
import com.simprints.eventsystem.event.remote.EventRemoteDataSource
import com.simprints.eventsystem.events_sync.down.domain.RemoteEventQuery
import com.simprints.eventsystem.exceptions.TryToUploadEventsForNotSignedProject
import com.simprints.eventsystem.exceptions.validator.DuplicateGuidSelectEventValidatorException
import com.simprints.eventsystem.sampledata.*
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.eventsystem.sampledata.SampleDefaults.GUID1
import com.simprints.eventsystem.sampledata.SampleDefaults.GUID2
import com.simprints.eventsystem.sampledata.SampleDefaults.GUID3
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.GeneralConfiguration.Modality
import com.simprints.infra.login.LoginManager
import com.simprints.infra.network.exceptions.NetworkConnectionException
import io.kotest.assertions.throwables.shouldThrow
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

internal class EventRepositoryImplTest {

    private lateinit var eventRepo: EventRepository

    @MockK
    lateinit var loginManager: LoginManager

    @MockK
    lateinit var eventLocalDataSource: EventLocalDataSource

    @MockK
    private lateinit var eventRemoteDataSource: EventRemoteDataSource

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
        every { loginManager.getSignedInProjectIdOrEmpty() } returns DEFAULT_PROJECT_ID
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
            loginManager,
            eventLocalDataSource,
            eventRemoteDataSource,
            timeHelper,
            sessionEventValidatorsFactory,
            sessionDataCache,
            configManager,
        )

        runBlocking {
            coEvery { eventLocalDataSource.loadAll() } returns emptyFlow()
        }
    }

    @Test
    fun `download count correctly passes query arguments`() = runTest {
        coEvery { eventRemoteDataSource.count(any()) }.returns(emptyList())

        eventRepo.countEventsToDownload(
            RemoteEventQuery(
                DEFAULT_PROJECT_ID,
                modes = listOf(Modes.FACE, Modes.FINGERPRINT),
            )
        )

        coVerify {
            eventRemoteDataSource.count(withArg { query ->
                assertThat(query.projectId).isEqualTo(DEFAULT_PROJECT_ID)
                assertThat(query.modes).containsExactly(ApiModes.FACE, ApiModes.FINGERPRINT)
            })
        }
    }

    @Test
    fun `download correctly passes query arguments`() = runTest {
        coEvery {
            eventRemoteDataSource.getEvents(
                any(),
                any()
            )
        }.returns(produce { this@produce.close() })

        eventRepo.downloadEvents(
            this@runTest,
            RemoteEventQuery(
                DEFAULT_PROJECT_ID,
                modes = listOf(Modes.FACE),
            )
        )

        coVerify {
            eventRemoteDataSource.getEvents(withArg { query ->
                assertThat(query.projectId).isEqualTo(DEFAULT_PROJECT_ID)
                assertThat(query.modes).containsExactly(ApiModes.FACE)
            }, any())
        }
    }

    @Test
    fun createSession_shouldHaveTheRightSessionCount() {
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
    fun createSession_ForEmptyProjectId() {
        runBlocking {
            every { loginManager.getSignedInProjectIdOrEmpty() } returns ""
            coEvery { eventLocalDataSource.count(SESSION_CAPTURE) } returns N_SESSIONS_DB

            val session = eventRepo.createSession()

            assertThat(session.payload.projectId).isEqualTo(PROJECT_ID_FOR_NOT_SIGNED_IN)

        }
    }

    @Test(expected = DuplicateGuidSelectEventValidatorException::class)
    fun createSession_ReportDuplicateGuidSelectEventValidatorExceptionException() {
        runBlocking {
            coEvery { eventLocalDataSource.count(SESSION_CAPTURE) } returns N_SESSIONS_DB
            coEvery {
                eventLocalDataSource.insertOrUpdate(any())
            } throws DuplicateGuidSelectEventValidatorException("oops...")
            eventRepo.createSession()
        }
    }

    @Test
    fun createSession_shouldCloseOpenSessionEvents() {
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
    fun createSession_shouldAddANewSessionEvent() {
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
    fun addEventIntoASession_shouldStoreItIntoTheDbWithRightLabels() {
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
    fun addEvent_shouldStoreItIntoTheDbWithRightLabels() {
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
    fun addEventToCurrentSession_shouldAddEventRelatedToCurrentSessionIntoDb() {
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
    fun upload_shouldLoadAllEventsPartOfSessionsToUpload() {
        runBlocking {
            mockDbToLoadTwoClosedSessionsWithEvents(2 * SESSION_BATCH_SIZE, GUID1, GUID2)

            eventRepo.uploadEvents(
                DEFAULT_PROJECT_ID,
                canSyncAllDataToSimprints = false,
                canSyncBiometricDataToSimprints = false,
                canSyncAnalyticsDataToSimprints = false
            ).toList()

            coVerify { eventLocalDataSource.loadAllFromSession(sessionId = GUID1) }
            coVerify { eventLocalDataSource.loadAllFromSession(sessionId = GUID2) }
        }
    }

    @Test
    fun upload_shouldSkipInvalidSessions() {
        runBlocking {
            mockDbToLoadInvalidSessions(2)

            eventRepo.uploadEvents(
                DEFAULT_PROJECT_ID,
                canSyncAllDataToSimprints = true,
                canSyncBiometricDataToSimprints = false,
                canSyncAnalyticsDataToSimprints = false
            ).toList()

            coVerify { eventLocalDataSource.loadAllFromSession(sessionId = GUID1) }
        }
    }

    @Test
    fun upload_shouldFilterBiometricEventsOnUpload() {
        runBlocking {
            coEvery {
                eventLocalDataSource.loadAllClosedSessionIds(any())
            } returns listOf(GUID1)
            coEvery {
                eventLocalDataSource.loadAllFromSession(any())
            } returns listOf(
                createAuthenticationEvent(),
                createAlertScreenEvent(),
                // only following should be uploaded
                createEnrolmentEventV2(),
                createPersonCreationEvent(),
                createFingerprintCaptureBiometricsEvent(),
                createFaceCaptureBiometricsEvent(),
            )

            eventRepo.uploadEvents(
                DEFAULT_PROJECT_ID,
                canSyncAllDataToSimprints = false,
                canSyncBiometricDataToSimprints = true,
                canSyncAnalyticsDataToSimprints = false
            ).toList()

            coVerify {
                eventRemoteDataSource.post(
                    any(),
                    withArg { assertThat(it).hasSize(4) },
                    any()
                )
            }
        }
    }

    @Test
    fun upload_shouldFilterAnalyticsEventsOnUpload() {
        runBlocking {
            coEvery {
                eventLocalDataSource.loadAllClosedSessionIds(any())
            } returns listOf(GUID1)
            coEvery {
                eventLocalDataSource.loadAllFromSession(any())
            } returns listOf(
                createFingerprintCaptureBiometricsEvent(),
                createFaceCaptureBiometricsEvent(),
                // only following should be uploaded
                createPersonCreationEvent(),
                createEnrolmentEventV2(),
                createAlertScreenEvent(),
            )

            eventRepo.uploadEvents(
                DEFAULT_PROJECT_ID,
                canSyncAllDataToSimprints = false,
                canSyncBiometricDataToSimprints = false,
                canSyncAnalyticsDataToSimprints = true
            ).toList()

            coVerify {
                eventRemoteDataSource.post(
                    any(),
                    withArg { assertThat(it).hasSize(3) },
                    any()
                )
            }
        }
    }


    @Test
    fun upload_shouldNotUploadOpenSession() {
        runBlocking {
            mockDbToLoadTwoClosedSessionsWithEvents(2 * SESSION_BATCH_SIZE)
            mockDbToLoadOpenSession(GUID3)

            eventRepo.uploadEvents(
                DEFAULT_PROJECT_ID,
                canSyncAllDataToSimprints = false,
                canSyncBiometricDataToSimprints = false,
                canSyncAnalyticsDataToSimprints = false
            ).toList()

            verifySessionHasNotGotUploaded(GUID3, eventRemoteDataSource)
        }
    }

    @Test
    fun upload_shouldNotUploadSessionsForNotSignedProject() {
        runBlocking {
            shouldThrow<TryToUploadEventsForNotSignedProject> {
                eventRepo.uploadEvents(
                    randomUUID(),
                    canSyncAllDataToSimprints = false,
                    canSyncBiometricDataToSimprints = false,
                    canSyncAnalyticsDataToSimprints = false
                ).toList()
            }
        }
    }

    @Test
    fun upload_succeeds_shouldDeleteEvents() {
        runBlocking {
            val events =
                mockDbToLoadTwoClosedSessionsWithEvents(2 * SESSION_BATCH_SIZE)

            eventRepo.uploadEvents(
                DEFAULT_PROJECT_ID,
                canSyncAllDataToSimprints = true,
                canSyncBiometricDataToSimprints = false,
                canSyncAnalyticsDataToSimprints = false
            ).toList()

            coVerify {
                eventLocalDataSource.delete(events.filter { it.labels.sessionId == GUID1 }
                    .map { it.id })
                eventLocalDataSource.delete(events.filter { it.labels.sessionId == GUID2 }
                    .map { it.id })
            }
        }
    }

    @Test
    fun upload_inProgress_shouldEmitProgress() {
        runBlocking {
            mockDbToLoadTwoClosedSessionsWithEvents(2 * SESSION_BATCH_SIZE)

            val progress = eventRepo.uploadEvents(
                DEFAULT_PROJECT_ID,
                canSyncAllDataToSimprints = false,
                canSyncBiometricDataToSimprints = false,
                canSyncAnalyticsDataToSimprints = false
            ).toList()

            assertThat(progress[0]).isEqualTo(SESSION_BATCH_SIZE)
            assertThat(progress[1]).isEqualTo(SESSION_BATCH_SIZE)
        }
    }

    @Test
    fun upload_succeeds_shouldDeleteUploadedEvents() {
        runBlocking {
            val events = mockDbToLoadTwoClosedSessionsWithEvents(2 * SESSION_BATCH_SIZE)

            eventRepo.uploadEvents(
                DEFAULT_PROJECT_ID,
                canSyncAllDataToSimprints = true,
                canSyncBiometricDataToSimprints = false,
                canSyncAnalyticsDataToSimprints = false
            ).toList()

            coVerify {
                eventLocalDataSource.delete(events.filter { it.labels.sessionId == GUID1 }
                    .map { it.id })
                eventLocalDataSource.delete(events.filter { it.labels.sessionId == GUID2 }
                    .map { it.id })
            }
        }
    }

    @Test
    fun upload_fails_shouldNotDeleteEventsAfterGenericException() {
        runBlocking {
            mockDbToLoadTwoClosedSessionsWithEvents(2 * SESSION_BATCH_SIZE)
            coEvery { eventRemoteDataSource.post(any(), any()) } throws Throwable("")

            eventRepo.uploadEvents(
                DEFAULT_PROJECT_ID,
                canSyncAllDataToSimprints = false,
                canSyncBiometricDataToSimprints = false,
                canSyncAnalyticsDataToSimprints = false
            ).toList()

            coVerify(exactly = 0) { eventLocalDataSource.delete(any()) }
        }
    }

    @Test
    fun upload_fails_shouldNotDeleteEventsAfterNetworkIssues() {
        runBlocking {
            mockDbToLoadTwoClosedSessionsWithEvents(2 * SESSION_BATCH_SIZE)
            coEvery { eventRemoteDataSource.post(any(), any()) } throws NetworkConnectionException(
                cause = Exception()
            )

            eventRepo.uploadEvents(
                DEFAULT_PROJECT_ID,
                canSyncAllDataToSimprints = false,
                canSyncBiometricDataToSimprints = false,
                canSyncAnalyticsDataToSimprints = false
            ).toList()

            coVerify(exactly = 0) { eventLocalDataSource.delete(any()) }
        }
    }

    @Test
    fun upload_fails_shouldNotDeleteSessionEventsAfterError() {
        runBlocking {
            coEvery { eventRemoteDataSource.post(any(), any()) } throws IllegalArgumentException()

            val events = mockDbToLoadTwoClosedSessionsWithEvents(2 * SESSION_BATCH_SIZE)

            eventRepo.uploadEvents(
                DEFAULT_PROJECT_ID,
                canSyncAllDataToSimprints = false,
                canSyncBiometricDataToSimprints = false,
                canSyncAnalyticsDataToSimprints = false
            ).toList()

            coVerify(exactly = 0) {
                eventLocalDataSource.delete(events.filter { it.labels.sessionId == GUID1 }
                    .map { it.id })
                eventLocalDataSource.delete(events.filter { it.labels.sessionId == GUID2 }
                    .map { it.id })
            }
        }
    }

    @Test
    fun `upload should dump invalid events, emit the progress and delete the events`() = runTest {
        val sessions = mockDbToLoadTwoSessionsWithInvalidEvent(GUID1, GUID2)

        val progress = eventRepo.uploadEvents(
            DEFAULT_PROJECT_ID,
            canSyncAllDataToSimprints = true,
            canSyncBiometricDataToSimprints = false,
            canSyncAnalyticsDataToSimprints = false
        ).toList()

        coVerify(exactly = 0) {
            eventRemoteDataSource.post(any(), any())
        }

        coVerify {
            eventRemoteDataSource.dumpInvalidEvents(DEFAULT_PROJECT_ID, sessions[GUID1]!!)
            eventRemoteDataSource.dumpInvalidEvents(DEFAULT_PROJECT_ID, sessions[GUID2]!!)
            eventLocalDataSource.deleteAllFromSession(GUID1)
            eventLocalDataSource.deleteAllFromSession(GUID2)
        }
        assertThat(progress[0]).isEqualTo(sessions[GUID1]!!.size)
        assertThat(progress[1]).isEqualTo(sessions[GUID2]!!.size)
    }

    @Test
    fun `fail dump of invalid events should not delete the events`() = runTest {
        coEvery { eventRemoteDataSource.dumpInvalidEvents(any(), any()) } throws HttpException(
            Response.error<String>(
                503,
                "".toResponseBody(null)
            )
        )
        mockDbToLoadTwoSessionsWithInvalidEvent(GUID1, GUID2)

        eventRepo.uploadEvents(
            DEFAULT_PROJECT_ID,
            canSyncAllDataToSimprints = true,
            canSyncBiometricDataToSimprints = false,
            canSyncAnalyticsDataToSimprints = false
        ).toList()

        coVerify(exactly = 0) {
            eventRemoteDataSource.post(any(), any())
        }

        coVerify(exactly = 0) {
            eventLocalDataSource.deleteAllFromSession(GUID1)
            eventLocalDataSource.deleteAllFromSession(GUID2)
        }
    }


    @Test
    fun createSession_shouldAddArtificialTerminationEventToThePreviousOne() {
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
            every { loginManager.getSignedInProjectIdOrEmpty() } returns "projectId"
            //When
            val loadedSession = eventRepo.getCurrentCaptureSessionEvent()
            //Then
            assertThat(loadedSession.labels.projectId).isEqualTo("projectId")
        }

    @Test
    fun `events loaded to cache on request from session`() = runTest {
        mockDbToLoadOpenSession(GUID1)

        eventRepo.getEventsFromSession(GUID1).toList()

        coVerify { eventLocalDataSource.loadAllFromSession(any()) }
        assertThat(sessionDataCache.eventCache).isNotEmpty()
    }

    @Test
    fun insertEventIntoCurrentOpenSession() {
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
    fun insertEventIntoCurrentOpenSession_shouldInvokeValidators() {
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

    private fun mockSignedId() =
        every { loginManager.getSignedInProjectIdOrEmpty() } returns DEFAULT_PROJECT_ID

    companion object {
        const val DEVICE_ID = "DEVICE_ID"
        const val APP_VERSION_NAME = "APP_VERSION_NAME"
        const val LIB_VERSION_NAME = "LIB_VERSION_NAME"
        const val LANGUAGE = "en"

        const val N_SESSIONS_DB = 3
        const val NOW = 1000L
    }
}
