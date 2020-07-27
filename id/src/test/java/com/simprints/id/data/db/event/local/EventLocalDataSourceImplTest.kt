package com.simprints.id.data.db.event.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.utils.randomUUID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_USER_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_USER_ID_2
import com.simprints.id.data.db.event.EventRepositoryImpl.Companion.PROJECT_ID_FOR_NOT_SIGNED_IN
import com.simprints.id.data.db.event.domain.models.AlertScreenEvent
import com.simprints.id.data.db.event.domain.models.CREATED_AT_RANGE
import com.simprints.id.data.db.event.domain.models.ENDED_AT_RANGE
import com.simprints.id.data.db.event.domain.models.EventLabels
import com.simprints.id.data.db.event.domain.models.EventType.ARTIFICIAL_TERMINATION
import com.simprints.id.data.db.event.domain.models.EventType.SESSION_CAPTURE
import com.simprints.id.data.db.event.domain.models.session.SessionCaptureEvent
import com.simprints.id.data.db.event.domain.validators.EventValidator
import com.simprints.id.data.db.event.local.EventLocalDataSource.EventQuery
import com.simprints.id.data.db.event.local.models.createAlertScreenEvent
import com.simprints.id.data.db.event.local.models.createSessionCaptureEvent
import com.simprints.id.data.db.event.local.models.fromDbToDomain
import com.simprints.id.data.db.event.local.models.fromDomainToDb
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.orchestrator.SOME_GUID1
import com.simprints.id.orchestrator.SOME_GUID2
import com.simprints.id.tools.TimeHelper
import io.mockk.*
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class EventLocalDataSourceImplTest {

    private lateinit var db: EventRoomDatabase
    private lateinit var eventDao: EventRoomDao
    private lateinit var eventLocalDataSource: EventLocalDataSource

    @RelaxedMockK lateinit var timeHelper: TimeHelper
    @RelaxedMockK lateinit var eventDatabaseFactory: EventDatabaseFactory
    @RelaxedMockK lateinit var loginInfoManager: LoginInfoManager
    lateinit var validators: Array<EventValidator>

    @Before
    fun setup() {
        MockKAnnotations.init(this)

        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, EventRoomDatabase::class.java).build()
        validators = arrayOf(mockk(), mockk())

        eventDao = db.eventDao
        every { eventDatabaseFactory.build() } returns db
        every { timeHelper.now() } returns NOW
        eventLocalDataSource = EventLocalDataSourceImpl(eventDatabaseFactory, loginInfoManager, DEVICE_ID, timeHelper, validators)
    }

    @Test
    fun createSession_shouldCloseOpenSessions() {
        runBlocking {
            val oldOpenSession = createSessionCaptureEvent().openSession()
            val newSession = createSessionCaptureEvent().openSession()
            eventDao.insertOrUpdate(oldOpenSession.fromDomainToDb())

            eventLocalDataSource.create(newSession)

            val storedSessionClose = eventDao.load(id = oldOpenSession.id).firstOrNull()?.fromDbToDomain()
            val storedSessionOpen = eventDao.load(id = newSession.id).firstOrNull()?.fromDbToDomain()

            assertThat(storedSessionClose?.payload?.endedAt).isEqualTo(NOW)
            assertThat(storedSessionOpen?.payload?.endedAt).isEqualTo(0)
        }
    }

    @Test
    fun createSession_shouldAddArtificialTerminationEventToThePreviousOne() {
        runBlocking {
            val oldOpenSession = createSessionCaptureEvent().openSession()
            val newSession = createSessionCaptureEvent().openSession()
            eventDao.insertOrUpdate(oldOpenSession.fromDomainToDb())

            eventLocalDataSource.create(newSession)

            val eventAssociatedToCloseSession = eventDao.load(sessionId = oldOpenSession.id)
            assertThat(eventAssociatedToCloseSession.firstOrNull()?.type).isEqualTo(SESSION_CAPTURE)
            assertThat(eventAssociatedToCloseSession[1].type).isEqualTo(ARTIFICIAL_TERMINATION)
        }
    }

    @Test
    fun getCurrentOpenSession() {
        runBlocking {
            val closeSession = createSessionCaptureEvent()
            val newSession = createSessionCaptureEvent().openSession()
            eventDao.insertOrUpdate(closeSession.fromDomainToDb())
            eventDao.insertOrUpdate(newSession.fromDomainToDb())

            val currentOpenSession = eventLocalDataSource.getCurrentSessionCaptureEvent()
            assertThat(currentOpenSession).isEqualTo(newSession)
        }
    }

    @Test
    fun loadWithAQuery() {
        runBlocking {
            mockDaoLoadToMakeNothing()
            eventLocalDataSource = EventLocalDataSourceImpl(eventDatabaseFactory, loginInfoManager, DEVICE_ID, timeHelper, emptyArray())
            val eventQuery = createCompleteEventQuery()

            eventLocalDataSource.load(eventQuery)
            coVerify {
                eventDao.load(ID, SESSION_CAPTURE, DEFAULT_PROJECT_ID, DEFAULT_USER_ID, DEFAULT_USER_ID_2, SOME_GUID1, SOME_GUID2,
                    CREATED_AT_RANGE.first, CREATED_AT_RANGE.last, ENDED_AT_RANGE.first, ENDED_AT_RANGE.last)
            }
        }
    }

    @Test
    fun countWithAQuery() {
        runBlocking {
            mockDaoLoadToMakeNothing()
            eventLocalDataSource = EventLocalDataSourceImpl(eventDatabaseFactory, loginInfoManager, DEVICE_ID, timeHelper, emptyArray())
            val eventQuery = createCompleteEventQuery()

            eventLocalDataSource.count(eventQuery)

            coVerify {
                eventDao.count(ID, SESSION_CAPTURE, DEFAULT_PROJECT_ID, DEFAULT_USER_ID, DEFAULT_USER_ID_2, SOME_GUID1, SOME_GUID2,
                    CREATED_AT_RANGE.first, CREATED_AT_RANGE.last, ENDED_AT_RANGE.first, ENDED_AT_RANGE.last)
            }
        }
    }

    @Test
    fun deleteWithAQuery() {
        runBlocking {
            mockDaoLoadToMakeNothing()
            mockNotSignedId()
            eventLocalDataSource = EventLocalDataSourceImpl(eventDatabaseFactory, loginInfoManager, DEVICE_ID, timeHelper, emptyArray())
            val eventQuery = createCompleteEventQuery()

            eventLocalDataSource.delete(eventQuery)

            coVerify {
                eventDao.delete(ID, SESSION_CAPTURE, DEFAULT_PROJECT_ID, DEFAULT_USER_ID, DEFAULT_USER_ID_2, SOME_GUID1, SOME_GUID2,
                    CREATED_AT_RANGE.first, CREATED_AT_RANGE.last, ENDED_AT_RANGE.first, ENDED_AT_RANGE.last)
            }
        }
    }

    private fun mockNotSignedId() = every { loginInfoManager.getSignedInProjectIdOrEmpty() } returns ""
    private fun mockSignedId() = every { loginInfoManager.getSignedInProjectIdOrEmpty() } returns DEFAULT_PROJECT_ID

    @Test
    fun insertNewEvent_notSignedIn() {
        runBlocking {
            mockNotSignedId()
            val event = createSessionCaptureEvent().copy(labels = EventLabels())
            eventLocalDataSource.insertOrUpdate(event)
            val storedEvent = eventLocalDataSource.load(EventQuery(id = event.id)).first()

            assertThat(storedEvent.labels).isEqualTo(EventLabels(projectId = PROJECT_ID_FOR_NOT_SIGNED_IN, deviceId = DEVICE_ID))
        }
    }

    @Test
    fun insertNewEvent_signedIn() {
        runBlocking {
            mockSignedId()
            val event = createSessionCaptureEvent().copy(labels = EventLabels())
            eventLocalDataSource.insertOrUpdate(event)
            val storedEvent = eventLocalDataSource.load(EventQuery(id = event.id)).first()

            assertThat(storedEvent.labels).isEqualTo(EventLabels(projectId = DEFAULT_PROJECT_ID, deviceId = DEVICE_ID))
        }
    }

    @Test
    fun insertEventIntoCurrentOpenSession() {
        runBlocking {
            mockSignedId()
            val session = createSessionCaptureEvent().openSession()
            val event = createAlertScreenEvent().removeLabels()
            eventLocalDataSource.insertOrUpdate(session)
            eventLocalDataSource.insertOrUpdateInCurrentSession(event)

            val storedEvent = eventLocalDataSource.load(EventQuery(id = event.id)).first()

            assertThat(storedEvent.labels).isEqualTo(EventLabels(projectId = DEFAULT_PROJECT_ID, deviceId = DEVICE_ID, sessionId = session.id))
        }
    }

    @Test
    fun insertEventIntoCurrentOpenSession_shouldInvokeValidators() {
        runBlocking {
            mockSignedId()
            val session = createSessionCaptureEvent().openSession()
            val eventInSession = createAlertScreenEvent().copy(labels = EventLabels(sessionId = session.id))
            eventLocalDataSource.insertOrUpdate(session)
            eventLocalDataSource.insertOrUpdate(eventInSession)

            val newEvent = createAlertScreenEvent().removeLabels()
            eventLocalDataSource.insertOrUpdateInCurrentSession(newEvent)

            validators.forEach {
                verify { it.validate(listOf(session, eventInSession), newEvent) }
            }
        }
    }

    private fun createCompleteEventQuery() =
        EventQuery(
            ID,
            SESSION_CAPTURE,
            DEFAULT_PROJECT_ID,
            DEFAULT_USER_ID,
            DEFAULT_USER_ID_2,
            SOME_GUID1,
            SOME_GUID2,
            CREATED_AT_RANGE,
            ENDED_AT_RANGE
        )

    private fun mockDaoLoadToMakeNothing() {
        db = mockk(relaxed = true)
        eventDao = mockk(relaxed = true)
        eventDatabaseFactory = mockk(relaxed = true)
        coEvery { eventDao.load(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) } returns emptyList()
        coEvery { eventDao.count(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) } returns 0
        every { db.eventDao } returns eventDao
        every { eventDatabaseFactory.build() } returns db
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    private fun SessionCaptureEvent.openSession(): SessionCaptureEvent =
        this.copy(payload = this.payload.copy(endedAt = 0))

    private fun SessionCaptureEvent.removeLabels(): SessionCaptureEvent =
        this.copy(id = ID, labels = EventLabels())

    private fun AlertScreenEvent.removeLabels(): AlertScreenEvent =
        this.copy(id = ID, labels = EventLabels())

    companion object {
        const val DEVICE_ID = "DEVICE_ID"
        const val NOW = 1000L
        private val ID = randomUUID()
    }
}
