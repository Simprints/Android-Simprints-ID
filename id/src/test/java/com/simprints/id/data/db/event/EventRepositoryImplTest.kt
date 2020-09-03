package com.simprints.id.data.db.event

import android.os.Build
import android.os.Build.VERSION
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.utils.randomUUID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.commontesttools.DefaultTestConstants.GUID1
import com.simprints.id.commontesttools.DefaultTestConstants.GUID2
import com.simprints.id.commontesttools.DefaultTestConstants.GUID3
import com.simprints.id.commontesttools.events.createAlertScreenEvent
import com.simprints.id.commontesttools.events.createSessionCaptureEvent
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.db.event.EventRepositoryImpl.Companion.PROJECT_ID_FOR_NOT_SIGNED_IN
import com.simprints.id.data.db.event.EventRepositoryImpl.Companion.SESSION_BATCH_SIZE
import com.simprints.id.data.db.event.domain.models.AlertScreenEvent
import com.simprints.id.data.db.event.domain.models.ArtificialTerminationEvent
import com.simprints.id.data.db.event.domain.models.ArtificialTerminationEvent.ArtificialTerminationPayload.Reason.NEW_SESSION
import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.EventLabels
import com.simprints.id.data.db.event.domain.models.EventType.ARTIFICIAL_TERMINATION
import com.simprints.id.data.db.event.domain.models.EventType.SESSION_CAPTURE
import com.simprints.id.data.db.event.domain.models.session.DatabaseInfo
import com.simprints.id.data.db.event.domain.models.session.Device
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
import com.simprints.id.domain.modality.Modes
import com.simprints.id.domain.modality.Modes.FINGERPRINT
import com.simprints.id.tools.time.TimeHelper
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class EventRepositoryImplTest {

    lateinit var eventRepo: EventRepository

    @MockK lateinit var loginInfoManager: LoginInfoManager
    @MockK lateinit var eventLocalDataSource: EventLocalDataSource
    @MockK lateinit var eventRemoteDataSource: EventRemoteDataSource
    @MockK lateinit var preferencesManager: PreferencesManager
    @MockK lateinit var crashReportManager: CrashReportManager
    @MockK lateinit var timeHelper: TimeHelper
    @MockK lateinit var sessionEventValidatorsFactory: SessionEventValidatorsFactory
    @MockK lateinit var eventValidator: EventValidator

    private val openSessionsQuery = DbLocalEventQuery(type = SESSION_CAPTURE, endTime = LongRange(0, 0))

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        every { timeHelper.now() } returns NOW
        every { loginInfoManager.getSignedInProjectIdOrEmpty() } returns DEFAULT_PROJECT_ID
        every { preferencesManager.modalities } returns listOf(FACE, FINGER)
        every { preferencesManager.language } returns LANGUAGE

        every { sessionEventValidatorsFactory.build() } returns arrayOf(eventValidator)

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
    }

    @Test
    fun createSession_shouldCloseOpenSessionEvents() {
        runBlocking {
            val oldOpenSession = createSessionCaptureEvent(randomUUID()).openSession()
            coEvery { eventLocalDataSource.count(any()) } returns 1
            coEvery { eventLocalDataSource.load(any()) } returns flowOf(oldOpenSession)

            eventRepo.createSession(LIB_VERSION_NAME)

            coVerify {
                eventLocalDataSource.insertOrUpdate(match {
                    assertThatArtificialTerminationEventWasAdded(it, oldOpenSession.id)
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
            coEvery { eventLocalDataSource.count(any()) } returns 1
            coEvery { eventLocalDataSource.load(any()) } returns flowOf()

            eventRepo.createSession(LIB_VERSION_NAME)

            coVerify {
                eventLocalDataSource.insertOrUpdate(match {
                    assertANewSessionCaptureWasAdded(it)
                })
            }
        }
    }

    @Test
    fun addEvent_shouldAddEventIntoDb() {
        runBlocking {
            val session = createSessionCaptureEvent(GUID1).openSession()
            with(eventLocalDataSource) {
                coEvery { load(openSessionsQuery) } returns flowOf(session)
                coEvery { load(DbLocalEventQuery(type = SESSION_CAPTURE, id = GUID1)) } returns flowOf(session)
                coEvery { load(DbLocalEventQuery(sessionId = GUID1)) } returns flowOf(session)
            }
            val newEvent = createAlertScreenEvent()

            eventRepo.addEvent(newEvent, GUID1)

            coVerify {
                eventLocalDataSource.insertOrUpdate(
                    newEvent.copy(labels = EventLabels(sessionId = GUID1, deviceId = DEVICE_ID, projectId = DEFAULT_PROJECT_ID)))
            }
        }
    }

    @Test
    fun addEventToCurrentSession_shouldAddEventRelatedToCurrentSessionIntoDb() {
        runBlocking {
            val session = createSessionCaptureEvent(GUID1).openSession()
            with(eventLocalDataSource) {
                coEvery { load(openSessionsQuery) } returns flowOf(session)
                coEvery { load(DbLocalEventQuery(type = SESSION_CAPTURE, id = session.id)) } returns flowOf(session)
                coEvery { load(DbLocalEventQuery(sessionId = session.id)) } returns flowOf(session)
            }
            val newEvent = createAlertScreenEvent()

            eventRepo.addEventToCurrentSession(newEvent)

            coVerify {
                eventLocalDataSource.insertOrUpdate(newEvent.copy(labels = EventLabels(sessionId = GUID1, deviceId = DEVICE_ID, projectId = DEFAULT_PROJECT_ID)))
            }
        }
    }

    @Test
    fun upload_shouldCreateTheRightBatches() {
        runBlocking {
            createMultipleBatches()

            val bathes = (eventRepo as EventRepositoryImpl).createBatches(LocalEventQuery())

            assertThat(bathes.first().events.size).isEqualTo(SESSION_BATCH_SIZE)
            assertThat(bathes[1].events.size).isEqualTo(SESSION_BATCH_SIZE)
        }
    }

    @Test
    fun upload_shouldLoadTheRightEventsForBatches() {
        runBlocking {
            createMultipleBatches()

            eventRepo.uploadEvents(LocalEventQuery()).toList()

            coVerify { eventLocalDataSource.load(DbLocalEventQuery(sessionId = GUID1)) }
            coVerify { eventLocalDataSource.load(DbLocalEventQuery(sessionId = GUID2)) }
            coVerify { eventLocalDataSource.load(DbLocalEventQuery(sessionId = GUID3)) }
        }
    }

    @Test
    fun uploadSucceeds_shouldDeleteEvents() {
        runBlocking {
            val events = createMultipleBatches()

            eventRepo.uploadEvents(LocalEventQuery()).toList()

            events.forEach {
                coVerify {
                    eventLocalDataSource.delete(DbLocalEventQuery(id = it.id))
                }
            }
        }
    }

    @Test
    fun upload_shouldEmitProgress() {
        runBlocking {
            createMultipleBatches()

            val progress = eventRepo.uploadEvents(LocalEventQuery()).toList()

            assertThat(progress[0]).isEqualTo(SESSION_BATCH_SIZE)
            assertThat(progress[1]).isEqualTo(SESSION_BATCH_SIZE)
        }
    }

    @Test
    fun upload_shouldDeleteUploadedEvents() {
        runBlocking {
            val events = createMultipleBatches()

            eventRepo.uploadEvents(LocalEventQuery()).toList()

            for (event in events) {
                coVerify { eventLocalDataSource.delete(DbLocalEventQuery(id = event.id)) }
            }
        }
    }

    @Test
    fun upload_shouldNotDeleteEventsAfterNetworkIssues() {
        runBlocking {
            val events = createMultipleBatches()
            coEvery { eventRemoteDataSource.post(any(), any()) } throws Throwable("Network issue")

            eventRepo.uploadEvents(LocalEventQuery()).toList()

            coVerify(exactly = 0) { eventLocalDataSource.delete(any()) }
        }
    }

    @Test
    fun upload_shouldDeleteEventsAfterIntegrationIssues() {
        runBlocking {
            coEvery { eventRemoteDataSource.post(any(), any()) } throws HttpException(Response.error<String>(404, "".toResponseBody(null)))
            val events = createMultipleBatches()

            eventRepo.uploadEvents(LocalEventQuery()).toList()

            for (event in events) {
                coVerify { eventLocalDataSource.delete(DbLocalEventQuery(id = event.id)) }
            }
        }
    }

    private suspend fun createMultipleBatches(): List<Event> {
        val smallSession1Events = mockSessionWithEvent(GUID1, SESSION_BATCH_SIZE / 2 - 1)
        val smallSession2Events = mockSessionWithEvent(GUID2, SESSION_BATCH_SIZE / 2 - 1)
        val bigSessionEvents = mockSessionWithEvent(GUID3, SESSION_BATCH_SIZE - 1)
        val events = smallSession1Events + smallSession2Events + bigSessionEvents

        coEvery {
            eventLocalDataSource.load(DbLocalEventQuery(projectId = DEFAULT_PROJECT_ID, type = SESSION_CAPTURE, endTime = LongRange(1, Long.MAX_VALUE)))
        } returns events.filterIsInstance<SessionCaptureEvent>().asFlow()

        return events
    }

    private fun mockSessionWithEvent(sessionId: String, nEvents: Int): List<Event> {
        val events = mutableListOf<Event>()
        events.add(createSessionCaptureEvent(sessionId))
        repeat(nEvents) {
            events.add(createAlertScreenEvent().copy(labels = EventLabels(sessionId = GUID1)))
        }

        coEvery { eventLocalDataSource.load(DbLocalEventQuery(sessionId = sessionId)) } returns events.asFlow()
        coEvery { eventLocalDataSource.count(DbLocalEventQuery(sessionId = sessionId)) } returns nEvents + 1
        return events
    }

    private fun assertANewSessionCaptureWasAdded(event: Event): Boolean =
        event is SessionCaptureEvent &&
            event.payload.projectId == PROJECT_ID_FOR_NOT_SIGNED_IN &&
            event.payload.createdAt == NOW &&
            event.payload.modalities == listOf(Modes.FACE, FINGERPRINT) &&
            event.payload.appVersionName == APP_VERSION_NAME &&
            event.payload.language == LANGUAGE &&
            event.payload.device == Device(VERSION.SDK_INT.toString(), Build.MANUFACTURER + "_" + Build.MODEL, DEVICE_ID) &&
            event.payload.databaseInfo == DatabaseInfo(1) &&
            event.payload.endedAt == 0L


    private fun assertThatSessionCaptureEventWasClosed(event: Event): Boolean =
        event is SessionCaptureEvent && event.payload.endedAt > 0

    private fun assertThatArtificialTerminationEventWasAdded(event: Event, id: String): Boolean =
        event is ArtificialTerminationEvent &&
            event.labels == EventLabels(sessionId = id, deviceId = DEVICE_ID, projectId = DEFAULT_PROJECT_ID) &&
            event.payload.reason == NEW_SESSION &&
            event.payload.createdAt == NOW


    @Test
    fun createSession_shouldAddArtificialTerminationEventToThePreviousOne() {
        runBlocking {
            coEvery { eventLocalDataSource.count(any()) } returns 1
            val oldOpenSession = createSessionCaptureEvent().openSession()
            coEvery { eventLocalDataSource.load(any()) } returns flowOf(oldOpenSession)

            eventRepo.createSession(LIB_VERSION_NAME)

            coVerify { eventLocalDataSource.load(openSessionsQuery) }
            coVerify {
                eventLocalDataSource.insertOrUpdate(match {
                    it.type == ARTIFICIAL_TERMINATION && it.labels.sessionId == oldOpenSession.id
                })
            }
        }
    }

    @Test
    fun getCurrentOpenSession() {
        runBlocking {
            val session = createSessionCaptureEvent().openSession()
            with(eventLocalDataSource) {
                coEvery { load(openSessionsQuery) } returns flowOf(session)
                coEvery { load(DbLocalEventQuery(type = SESSION_CAPTURE, id = session.id)) } returns flowOf(session)
                coEvery { load(DbLocalEventQuery(sessionId = session.id)) } returns flowOf(session)
            }

            val loadedSession = eventRepo.getCurrentCaptureSessionEvent()
            assertThat(loadedSession).isEqualTo(session)
        }
    }

    @Test
    fun insertEventIntoCurrentOpenSession() {
        runBlocking {
            mockSignedId()
            val session = createSessionCaptureEvent().openSession()
            with(eventLocalDataSource) {
                coEvery { load(openSessionsQuery) } returns flowOf(session)
                coEvery { load(DbLocalEventQuery(type = SESSION_CAPTURE, id = session.id)) } returns flowOf(session)
                coEvery { load(DbLocalEventQuery(sessionId = session.id)) } returns flowOf(session)
            }
            val event = createAlertScreenEvent().removeLabels()

            eventRepo.addEventToCurrentSession(event)

            coVerify { eventLocalDataSource.load(openSessionsQuery) }
            coVerify {
                eventLocalDataSource.insertOrUpdate(
                    event.copy(labels = EventLabels(deviceId = DEVICE_ID, sessionId = session.id, projectId = DEFAULT_PROJECT_ID)))
            }
        }
    }

    @Test
    fun insertEventIntoCurrentOpenSession_shouldInvokeValidators() {
        runBlocking {
            mockSignedId()
            val session = createSessionCaptureEvent().openSession()
            val eventInSession = createAlertScreenEvent().removeLabels()
            coEvery { eventLocalDataSource.load(openSessionsQuery) } returns flowOf(session)
            coEvery { eventLocalDataSource.load(DbLocalEventQuery(type = SESSION_CAPTURE, id = session.id)) } returns flowOf(session)
            coEvery { eventLocalDataSource.load(DbLocalEventQuery(sessionId = session.id)) } returns flowOf(session, eventInSession)

            val event = createAlertScreenEvent().removeLabels()

            eventRepo.addEventToCurrentSession(event)

            verify { eventValidator.validate(listOf(session, eventInSession), event) }
        }
    }

    private fun mockSignedId() = every { loginInfoManager.getSignedInProjectIdOrEmpty() } returns DEFAULT_PROJECT_ID

    private fun SessionCaptureEvent.openSession(): SessionCaptureEvent =
        this.copy(payload = this.payload.copy(endedAt = 0))

    private fun SessionCaptureEvent.removeLabels(): SessionCaptureEvent =
        this.copy(id = GUID1, labels = EventLabels())

    private fun AlertScreenEvent.removeLabels(): AlertScreenEvent =
        this.copy(id = GUID1, labels = EventLabels())

    companion object {
        const val DEVICE_ID = "DEVICE_ID"
        const val APP_VERSION_NAME = "APP_VERSION_NAME"
        const val LIB_VERSION_NAME = "LIB_VERSION_NAME"
        const val LANGUAGE = "en"

        const val NOW = 1000L
        private val ID = randomUUID()
    }
}
