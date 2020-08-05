package com.simprints.id.data.db.event

import android.os.Build
import android.os.Build.VERSION
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.utils.randomUUID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.commontesttools.DefaultTestConstants.GUID1
import com.simprints.id.commontesttools.DefaultTestConstants.GUID2
import com.simprints.id.commontesttools.DefaultTestConstants.GUID3
import com.simprints.id.commontesttools.events.createAlertScreenEvent
import com.simprints.id.commontesttools.events.createSessionCaptureEvent
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.db.event.EventRepositoryImpl.Batch
import com.simprints.id.data.db.event.EventRepositoryImpl.Companion.PROJECT_ID_FOR_NOT_SIGNED_IN
import com.simprints.id.data.db.event.EventRepositoryImpl.Companion.SESSION_BATCH_SIZE
import com.simprints.id.data.db.event.domain.models.ArtificialTerminationEvent
import com.simprints.id.data.db.event.domain.models.ArtificialTerminationEvent.ArtificialTerminationPayload.Reason.NEW_SESSION
import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.EventLabels
import com.simprints.id.data.db.event.domain.models.session.DatabaseInfo
import com.simprints.id.data.db.event.domain.models.session.Device
import com.simprints.id.data.db.event.domain.models.session.SessionCaptureEvent
import com.simprints.id.data.db.event.local.EventLocalDataSource
import com.simprints.id.data.db.event.local.models.DbEventQuery
import com.simprints.id.data.db.event.remote.EventRemoteDataSource
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.modality.Modality.FACE
import com.simprints.id.domain.modality.Modality.FINGER
import com.simprints.id.domain.modality.Modes
import com.simprints.id.domain.modality.Modes.FINGERPRINT
import com.simprints.id.services.scheduledSync.sessionSync.SessionEventsSyncManager
import com.simprints.id.tools.TimeHelper
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EventRepositoryImplTest {

    lateinit var eventRepo: EventRepository

    @MockK lateinit var loginInfoManager: LoginInfoManager
    @MockK lateinit var sessionEventsSyncManager: SessionEventsSyncManager
    @MockK lateinit var eventLocalDataSource: EventLocalDataSource
    @MockK lateinit var eventRemoteDataSource: EventRemoteDataSource
    @MockK lateinit var preferencesManager: PreferencesManager
    @MockK lateinit var crashReportManager: CrashReportManager
    @MockK lateinit var timeHelper: TimeHelper

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        every { timeHelper.now() } returns NOW
        every { loginInfoManager.getSignedInProjectIdOrEmpty() } returns DEFAULT_PROJECT_ID
        every { preferencesManager.modalities } returns listOf(FACE, FINGER)
        every { preferencesManager.language } returns LANGUAGE

        eventRepo = EventRepositoryImpl(
            DEVICE_ID,
            APP_VERSION_NAME,
            loginInfoManager,
            sessionEventsSyncManager,
            eventLocalDataSource,
            eventRemoteDataSource,
            preferencesManager,
            crashReportManager,
            timeHelper)
    }

    @Test
    fun createSession_shouldCloseOpenSessions() {
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
    fun createSession_shouldAddANewSession() {
        runBlocking {
            coEvery { eventLocalDataSource.count(any()) } returns 1
            coEvery { eventLocalDataSource.load(any()) } returns flowOf()

            eventRepo.createSession(LIB_VERSION_NAME)

            coVerify {
                eventLocalDataSource.insertOrUpdate(match {
                    assertThatEventIsAboutANewSession(it)
                })
            }
        }
    }

    @Test
    fun addEvent_shouldAddEventIntoDb() {
        runBlocking {
            coEvery { eventLocalDataSource.load(any()) } returns flowOf(createSessionCaptureEvent(GUID1))
            val newEvent = createAlertScreenEvent()

            eventRepo.addEvent(GUID1, newEvent)

            coVerify { eventLocalDataSource.load(DbEventQuery(id = GUID1)) }
            coVerify {
                eventLocalDataSource.insertOrUpdate(newEvent.copy(labels = EventLabels(sessionId = GUID1, deviceId = DEVICE_ID, projectId = DEFAULT_PROJECT_ID)))
            }
        }
    }

    @Test
    fun upload_shouldCreateTheRightBatches() {
        runBlocking {
            val smallSession = createSessionCaptureEvent(GUID1)
            val mediumSession = createSessionCaptureEvent(GUID2)
            val bigSession = createSessionCaptureEvent(GUID3)

            coEvery { eventLocalDataSource.load(any()) } returns flowOf(smallSession, mediumSession, bigSession)
            coEvery { eventLocalDataSource.count(DbEventQuery(sessionId = GUID1)) } returns SESSION_BATCH_SIZE / 2
            coEvery { eventLocalDataSource.count(DbEventQuery(sessionId = GUID2)) } returns SESSION_BATCH_SIZE / 2 - 1
            coEvery { eventLocalDataSource.count(DbEventQuery(sessionId = GUID3)) } returns SESSION_BATCH_SIZE


            val bathes = (eventRepo as EventRepositoryImpl).createBatchesWithCloseSessions()

            assertThat(bathes).containsExactly(
                Batch(listOf(GUID1, GUID2).toMutableList(), SESSION_BATCH_SIZE - 1),
                Batch(listOf(GUID3).toMutableList(), SESSION_BATCH_SIZE)
            )
        }
    }

    private fun assertThatEventIsAboutANewSession(event: Event): Boolean =
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


    //    @Test
//    fun createSession_shouldAddArtificialTerminationEventToThePreviousOne() {
//        runBlocking {
//            val oldOpenSession = createSessionCaptureEvent().openSession()
//            val newSession = createSessionCaptureEvent().openSession()
//            eventDao.insertOrUpdate(oldOpenSession.fromDomainToDb())
//
//            eventLocalDataSource.create(newSession)
//
//            val eventAssociatedToCloseSession = eventDao.load(sessionId = oldOpenSession.id)
//            assertThat(eventAssociatedToCloseSession.firstOrNull()?.type).isEqualTo(SESSION_CAPTURE)
//            assertThat(eventAssociatedToCloseSession[1].type).isEqualTo(ARTIFICIAL_TERMINATION)
//        }
//    }
//
//    @Test
//    fun getCurrentOpenSession() {
//        runBlocking {
//            val closeSession = createSessionCaptureEvent()
//            val newSession = createSessionCaptureEvent().openSession()
//            eventDao.insertOrUpdate(closeSession.fromDomainToDb())
//            eventDao.insertOrUpdate(newSession.fromDomainToDb())
//
//            val currentOpenSession = eventLocalDataSource.getCurrentSessionCaptureEvent()
//            assertThat(currentOpenSession).isEqualTo(newSession)
//        }
//    }
//
//    @Test
//    fun insertEventIntoCurrentOpenSession() {
//        runBlocking {
//            mockSignedId()
//            val session = createSessionCaptureEvent().openSession()
//            val event = createAlertScreenEvent().removeLabels()
//            eventLocalDataSource.insertOrUpdate(session)
//            eventLocalDataSource.insertOrUpdateInCurrentSession(event)
//
//            val storedEvent = eventLocalDataSource.load(DbEventQuery(id = event.id)).first()
//
//            assertThat(storedEvent.labels).isEqualTo(EventLabels(projectId = DEFAULT_PROJECT_ID, deviceId = Companion.DEVICE_ID, sessionId = session.id))
//        }
//    }
//
//    @Test
//    fun insertEventIntoCurrentOpenSession_shouldInvokeValidators() {
//        runBlocking {
//            mockSignedId()
//            val session = createSessionCaptureEvent().openSession()
//            val eventInSession = createAlertScreenEvent().copy(labels = EventLabels(sessionId = session.id))
//            eventLocalDataSource.insertOrUpdate(session)
//            eventLocalDataSource.insertOrUpdate(eventInSession)
//
//            val newEvent = createAlertScreenEvent().removeLabels()
//            eventLocalDataSource.insertOrUpdateInCurrentSession(newEvent)
//
//            validators.forEach {
//                verify { it.validate(listOf(session, eventInSession), newEvent) }
//            }
//        }
//    }
//
//
//    @Test
//    fun insertNewEvent_notSignedIn() {
//        runBlocking {
//            mockNotSignedId()
//            val event = createSessionCaptureEvent().copy(labels = EventLabels())
//            eventLocalDataSource.insertOrUpdate(event)
//            val storedEvent = eventLocalDataSource.load(DbEventQuery(id = event.id)).first()
//
//            Truth.assertThat(storedEvent.labels).isEqualTo(EventLabels(projectId = EventRepositoryImpl.PROJECT_ID_FOR_NOT_SIGNED_IN, deviceId = EventLocalDataSourceImplTest.DEVICE_ID))
//        }
//    }
//
//    @Test
//    fun insertNewEvent_signedIn() {
//        runBlocking {
//            mockSignedId()
//            val event = createSessionCaptureEvent().copy(labels = EventLabels())
//            eventLocalDataSource.insertOrUpdate(event)
//            val storedEvent = eventLocalDataSource.load(DbEventQuery(id = event.id)).first()
//
//            Truth.assertThat(storedEvent.labels).isEqualTo(EventLabels(projectId = DefaultTestConstants.DEFAULT_PROJECT_ID, deviceId = EventLocalDataSourceImplTest.DEVICE_ID))
//        }
//    }
//
//    private fun mockSignedId() = every { loginInfoManager.getSignedInProjectIdOrEmpty() } returns DefaultTestConstants.DEFAULT_PROJECT_ID
//
    private fun SessionCaptureEvent.openSession(): SessionCaptureEvent =
        this.copy(payload = this.payload.copy(endedAt = 0))

//    private fun SessionCaptureEvent.removeLabels(): SessionCaptureEvent =
//        this.copy(id = EventLocalDataSourceImplTest.ID, labels = EventLabels())
//
//    private fun AlertScreenEvent.removeLabels(): AlertScreenEvent =
//        this.copy(id = EventLocalDataSourceImplTest.ID, labels = EventLabels())

    companion object {
        const val DEVICE_ID = "DEVICE_ID"
        const val APP_VERSION_NAME = "APP_VERSION_NAME"
        const val LIB_VERSION_NAME = "LIB_VERSION_NAME"
        const val LANGUAGE = "en"

        const val NOW = 1000L
        private val ID = randomUUID()
    }
}
