package com.simprints.id.data.db.event

import com.google.common.truth.Truth.assertThat
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.commontesttools.DefaultTestConstants.GUID1
import com.simprints.id.commontesttools.DefaultTestConstants.GUID2
import com.simprints.id.commontesttools.DefaultTestConstants.GUID3
import com.simprints.id.commontesttools.events.createAlertScreenEvent
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.db.event.EventRepositoryImpl.Companion.SESSION_BATCH_SIZE
import com.simprints.id.data.db.event.domain.models.ArtificialTerminationEvent.ArtificialTerminationPayload.Reason.NEW_SESSION
import com.simprints.id.data.db.event.domain.models.ArtificialTerminationEvent.ArtificialTerminationPayload.Reason.TIMED_OUT
import com.simprints.id.data.db.event.domain.models.EventLabels
import com.simprints.id.data.db.event.domain.models.EventType.SESSION_CAPTURE
import com.simprints.id.data.db.event.domain.models.session.SessionCaptureEvent
import com.simprints.id.data.db.event.domain.validators.EventValidator
import com.simprints.id.data.db.event.domain.validators.SessionEventValidatorsFactory
import com.simprints.id.data.db.event.local.EventLocalDataSource
import com.simprints.id.data.db.event.local.models.DbLocalEventQuery
import com.simprints.id.data.db.event.remote.EventRemoteDataSource
import com.simprints.id.data.db.events_sync.up.domain.LocalEventQuery
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.modality.Modality.FACE
import com.simprints.id.domain.modality.Modality.FINGER
import com.simprints.id.tools.time.TimeHelper
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class EventRepositoryImplTest {

    private lateinit var eventRepo: EventRepository

    @MockK lateinit var loginInfoManager: LoginInfoManager
    @MockK lateinit var eventLocalDataSource: EventLocalDataSource
    @MockK lateinit var eventRemoteDataSource: EventRemoteDataSource
    @MockK lateinit var preferencesManager: PreferencesManager
    @MockK lateinit var crashReportManager: CrashReportManager
    @MockK lateinit var timeHelper: TimeHelper
    @MockK lateinit var sessionEventValidatorsFactory: SessionEventValidatorsFactory
    @MockK lateinit var eventValidator: EventValidator

    lateinit var queryToLoadOpenSessions: DbLocalEventQuery
    lateinit var queryToLoadOldOpenSessions: DbLocalEventQuery

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        every { timeHelper.now() } returns NOW
        every { loginInfoManager.getSignedInProjectIdOrEmpty() } returns DEFAULT_PROJECT_ID
        every { preferencesManager.modalities } returns listOf(FACE, FINGER)
        every { preferencesManager.language } returns LANGUAGE

        every { sessionEventValidatorsFactory.build() } returns arrayOf(eventValidator)
        queryToLoadOpenSessions = DbLocalEventQuery(type = SESSION_CAPTURE, endTime = LongRange(0, 0))
        queryToLoadOldOpenSessions = DbLocalEventQuery(
            type = SESSION_CAPTURE,
            endTime = LongRange(0, 0),
            startTime = LongRange(0, timeHelper.now() - EventRepositoryImpl.GRACE_PERIOD),
            projectId = DEFAULT_PROJECT_ID)

        eventRepo = EventRepositoryImpl(
            DEVICE_ID,
            APP_VERSION_NAME,
            loginInfoManager,
            eventLocalDataSource,
            eventRemoteDataSource,
            preferencesManager,
            crashReportManager,
            timeHelper,
            sessionEventValidatorsFactory)

        runBlocking {
            coEvery { eventLocalDataSource.load(queryToLoadOldOpenSessions) } returns emptyFlow()
            mockDbToLoadPersonRecordEvents(0)
        }
    }

    @Test
    fun createSession_shouldHaveTheRightSessionCount() {
        runBlocking {
            mockDbToHaveOneOpenSession()
            coEvery { eventLocalDataSource.count(DbLocalEventQuery(type = SESSION_CAPTURE)) } returns N_SESSIONS_DB

            eventRepo.createSession(LIB_VERSION_NAME)

            coVerify {
                eventLocalDataSource.insertOrUpdate(match<SessionCaptureEvent> {
                    it.payload.databaseInfo.sessionCount == N_SESSIONS_DB
                })
            }
        }
    }

    @Test
    fun createSession_shouldCloseOpenSessionEvents() {
        runBlocking {
            val openSession = mockDbToHaveOneOpenSession()

            eventRepo.createSession(LIB_VERSION_NAME)

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

            eventRepo.createSession(LIB_VERSION_NAME)

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
            val session = mockDbToHaveOneOpenSession()
            val newEvent = createAlertScreenEvent()

            eventRepo.addEventToSession(newEvent, session)

            coVerify {
                eventLocalDataSource.insertOrUpdate(
                    newEvent.copy(labels = EventLabels(sessionId = GUID1, deviceId = DEVICE_ID, projectId = DEFAULT_PROJECT_ID)))
            }
        }
    }

    @Test
    fun addEvent_shouldStoreItIntoTheDbWithRightLabels() {
        runBlocking {
            val newEvent = createAlertScreenEvent()
            newEvent.labels = EventLabels()

            eventRepo.addEvent(newEvent)

            coVerify {
                eventLocalDataSource.insertOrUpdate(
                    newEvent.copy(labels = EventLabels(deviceId = DEVICE_ID, projectId = DEFAULT_PROJECT_ID)))
            }
        }
    }

    @Test
    fun addEventToCurrentSession_shouldAddEventRelatedToCurrentSessionIntoDb() {
        runBlocking {
            mockDbToHaveOneOpenSession(GUID1)
            val newEvent = createAlertScreenEvent()

            eventRepo.addEventToCurrentSession(newEvent)

            coVerify {
                eventLocalDataSource.insertOrUpdate(newEvent.copy(labels = EventLabels(sessionId = GUID1, deviceId = DEVICE_ID, projectId = DEFAULT_PROJECT_ID)))
            }
        }
    }

    @Test
    fun createBatches_shouldSplitEventsCorrectlyIntoBatches() {
        runBlocking {
            mockDbToLoadTwoCloseSessionsWithEvents(2 * SESSION_BATCH_SIZE)
            mockDbToLoadPersonRecordEvents(SESSION_BATCH_SIZE + 3)

            val bathes = (eventRepo as EventRepositoryImpl).createBatches(LocalEventQuery(DEFAULT_PROJECT_ID))

            assertThat(bathes[0].events.size).isEqualTo(SESSION_BATCH_SIZE)
            assertThat(bathes[1].events.size).isEqualTo(3)
            assertThat(bathes[2].events.size).isEqualTo(SESSION_BATCH_SIZE)
            assertThat(bathes[3].events.size).isEqualTo(SESSION_BATCH_SIZE)
        }
    }

    @Test
    fun upload_shouldLoadAllEventsPartOfSessionsToUpload() {
        runBlocking {
            mockDbToLoadTwoCloseSessionsWithEvents(2 * SESSION_BATCH_SIZE, GUID1, GUID2)

            eventRepo.uploadEvents(LocalEventQuery(DEFAULT_PROJECT_ID)).toList()

            coVerify { eventLocalDataSource.load(DbLocalEventQuery(sessionId = GUID1)) }
            coVerify { eventLocalDataSource.load(DbLocalEventQuery(sessionId = GUID2)) }
        }
    }

    @Test
    fun upload_shouldNotUploadOpenSession() {
        runBlocking {
            mockDbToLoadTwoCloseSessionsWithEvents(2 * SESSION_BATCH_SIZE)
            mockDbToLoadOpenSession(GUID3)

            eventRepo.uploadEvents(LocalEventQuery(DEFAULT_PROJECT_ID)).toList()

            verifySessionHasNotGotUploaded(GUID3)
        }
    }

    @Test
    fun upload_shouldUploadOldOpenSession() {
        runBlocking {
            mockDbToLoadTwoCloseSessionsWithEvents(2 * SESSION_BATCH_SIZE)
            mockDbToLoadOldOpenSession(GUID3)

            eventRepo.uploadEvents(LocalEventQuery(DEFAULT_PROJECT_ID)).toList()

            verifySessionHasGotUploaded(GUID3)
            verifyArtificialEventWasAdded(GUID3, TIMED_OUT)
        }
    }

    @Test
    fun upload_succeeds_shouldDeleteEvents() {
        runBlocking {
            val events =
                mockDbToLoadTwoCloseSessionsWithEvents(2 * SESSION_BATCH_SIZE) +
                    mockDbToLoadPersonRecordEvents(SESSION_BATCH_SIZE / 2)

            eventRepo.uploadEvents(LocalEventQuery(DEFAULT_PROJECT_ID)).toList()

            events.forEach {
                coVerify {
                    eventLocalDataSource.delete(DbLocalEventQuery(id = it.id))
                }
            }
        }
    }

    @Test
    fun upload_inProgress_shouldEmitProgress() {
        runBlocking {
            mockDbToLoadTwoCloseSessionsWithEvents(2 * SESSION_BATCH_SIZE)
            mockDbToLoadPersonRecordEvents(SESSION_BATCH_SIZE / 2)

            val progress = eventRepo.uploadEvents(LocalEventQuery(DEFAULT_PROJECT_ID)).toList()

            assertThat(progress[0]).isEqualTo(SESSION_BATCH_SIZE/2)
            assertThat(progress[1]).isEqualTo(SESSION_BATCH_SIZE)
        }
    }

    @Test
    fun upload_succeeds_shouldDeleteUploadedEvents() {
        runBlocking {
            val events =
                mockDbToLoadTwoCloseSessionsWithEvents(2 * SESSION_BATCH_SIZE) +
                    mockDbToLoadPersonRecordEvents(SESSION_BATCH_SIZE / 2)

            eventRepo.uploadEvents(LocalEventQuery(DEFAULT_PROJECT_ID)).toList()

            for (event in events) {
                coVerify { eventLocalDataSource.delete(DbLocalEventQuery(id = event.id)) }
            }
        }
    }

    @Test
    fun upload_fails_shouldNotDeleteEventsAfterNetworkIssues() {
        runBlocking {
            mockDbToLoadTwoCloseSessionsWithEvents(2 * SESSION_BATCH_SIZE)
            coEvery { eventRemoteDataSource.post(any(), any()) } throws Throwable("Network issue")

            eventRepo.uploadEvents(LocalEventQuery(DEFAULT_PROJECT_ID)).toList()

            coVerify(exactly = 0) { eventLocalDataSource.delete(any()) }
        }
    }

    @Test
    fun upload_fails_shouldDeleteSessionEventsAfterIntegrationIssues() {
        runBlocking {
            coEvery { eventRemoteDataSource.post(any(), any()) } throws HttpException(Response.error<String>(404, "".toResponseBody(null)))
            val events = mockDbToLoadTwoCloseSessionsWithEvents(2 * SESSION_BATCH_SIZE)
            val subjectEvents = mockDbToLoadPersonRecordEvents(SESSION_BATCH_SIZE / 2)

            eventRepo.uploadEvents(LocalEventQuery(DEFAULT_PROJECT_ID)).toList()

            for (event in events) {
                coVerify { eventLocalDataSource.delete(DbLocalEventQuery(id = event.id)) }
            }

            for (event in subjectEvents) {
                coVerify(exactly = 0) { eventLocalDataSource.delete(DbLocalEventQuery(id = event.id)) }
            }
        }
    }

    @Test
    fun createSession_shouldAddArtificialTerminationEventToThePreviousOne() {
        runBlocking {
            mockDbToHaveOneOpenSession(GUID1)

            eventRepo.createSession(LIB_VERSION_NAME)

            coVerify { eventLocalDataSource.load(queryToLoadOpenSessions) }
            verifyArtificialEventWasAdded(GUID1, NEW_SESSION)
        }
    }

    @Test
    fun getCurrentOpenSession() {
        runBlocking {
            val session = mockDbToHaveOneOpenSession(GUID1)

            val loadedSession = eventRepo.getCurrentCaptureSessionEvent()
            assertThat(loadedSession).isEqualTo(session)
        }
    }

    @Test
    fun insertEventIntoCurrentOpenSession() {
        runBlocking {
            mockSignedId()
            val session = mockDbToHaveOneOpenSession(GUID1)
            val eventInSession = createAlertScreenEvent().removeLabels()

            eventRepo.addEventToCurrentSession(eventInSession)

            coVerify { eventLocalDataSource.load(queryToLoadOpenSessions) }
            coVerify {
                eventLocalDataSource.insertOrUpdate(
                    eventInSession.copy(labels = EventLabels(deviceId = DEVICE_ID, sessionId = session.id, projectId = DEFAULT_PROJECT_ID)))
            }
        }
    }

    @Test
    fun insertEventIntoCurrentOpenSession_shouldInvokeValidators() {
        runBlocking {
            mockSignedId()
            val session = mockDbToHaveOneOpenSession(GUID1)
            val eventInSession = createAlertScreenEvent().removeLabels()
            coEvery { eventLocalDataSource.load(DbLocalEventQuery(sessionId = session.id)) } returns flowOf(session, eventInSession)
            val newEvent = createAlertScreenEvent().removeLabels()

            eventRepo.addEventToCurrentSession(newEvent)

            verify { eventValidator.validate(listOf(session, eventInSession), newEvent) }
        }
    }

    private fun mockSignedId() = every { loginInfoManager.getSignedInProjectIdOrEmpty() } returns DEFAULT_PROJECT_ID

    companion object {
        const val DEVICE_ID = "DEVICE_ID"
        const val APP_VERSION_NAME = "APP_VERSION_NAME"
        const val LIB_VERSION_NAME = "LIB_VERSION_NAME"
        const val LANGUAGE = "en"

        const val N_SESSIONS_DB = 3
        const val NOW = 1000L
    }
}
