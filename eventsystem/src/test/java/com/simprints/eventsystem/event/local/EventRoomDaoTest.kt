package com.simprints.eventsystem.event.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.modality.Modes
import com.simprints.core.tools.utils.randomUUID
import com.simprints.eventsystem.event.domain.models.EventLabels
import com.simprints.eventsystem.event.domain.models.EventType.SESSION_CAPTURE
import com.simprints.eventsystem.event.local.models.DbEvent
import com.simprints.eventsystem.event.local.models.fromDbToDomain
import com.simprints.eventsystem.sampledata.SampleDefaults.CREATED_AT
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_MODULE_ID
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_MODULE_ID_2
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_USER_ID
import com.simprints.eventsystem.sampledata.SampleDefaults.ENDED_AT
import com.simprints.eventsystem.sampledata.SampleDefaults.GUID1
import com.simprints.eventsystem.sampledata.SampleDefaults.GUID2
import io.mockk.MockKAnnotations
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class EventRoomDaoTest {

    private val eventJson = """{"id": "anID", "payload": "a payload"}"""
    val event = DbEvent(
        GUID1,
        EventLabels(
            projectId = DEFAULT_PROJECT_ID,
            attendantId = DEFAULT_USER_ID,
            moduleIds = listOf(DEFAULT_MODULE_ID, DEFAULT_MODULE_ID_2),
            mode = listOf(Modes.FACE, Modes.FINGERPRINT),
            sessionId = GUID1,
            deviceId = GUID1
        ),
        SESSION_CAPTURE, eventJson, CREATED_AT, ENDED_AT, false
    )

    private lateinit var db: EventRoomDatabase
    private lateinit var eventDao: EventRoomDao

    @Before
    fun setup() {
        MockKAnnotations.init(this)

        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, EventRoomDatabase::class.java)
            .allowMainThreadQueries().build()
        eventDao = db.eventDao
    }

    @Test
    fun test() {
        runBlocking {
            val eventJson = "{\"id\":\"5b28d790-929d-455e-a7c9-7c6cc929275c\",\"labels\":{\"projectId\":\"ffbEEooqVfzaWmqXbsnc\",\"sessionId\":\"d273b0cb-7c88-4a8a-89bf-a5cb5961b876\",\"deviceId\":\"06156a59428859d7\"},\"payload\":{\"id\":\"5b28d790-929d-455e-a7c9-7c6cc929275c\",\"createdAt\":1647511734063,\"endedAt\":1647511734257,\"eventVersion\":2,\"attemptNb\":0,\"qualityThr\":-1.0,\"result\":\"VALID\",\"isFallback\":false,\"fac\u0000e\":{\"yaw\":1.2824339,\"roll\":11.53936,\"quality\":-0.060480837,\"template\":\"Aixz7/1MkPMDOw1Po+AZIxEjAl8Q8e8gLyBhE98sAiT8FDQmGz/CDhPG8\"}}"
            val event = DbEvent(
                GUID1,
                EventLabels(
                    projectId = DEFAULT_PROJECT_ID,
                    attendantId = DEFAULT_USER_ID,
                    moduleIds = listOf(DEFAULT_MODULE_ID, DEFAULT_MODULE_ID_2),
                    mode = listOf(Modes.FACE, Modes.FINGERPRINT),
                    sessionId = GUID1,
                    deviceId = GUID1
                ),
                SESSION_CAPTURE, eventJson, CREATED_AT, ENDED_AT, false
            )
            eventDao.insertOrUpdate(event)
            val events = eventDao.loadFromSession(GUID1)
            println(events[0].fromDbToDomain())
        }
    }

    @Test
    fun loadByProjectId() {
        runBlocking {
            val wrongEvent = event.copy(id = randomUUID(), labels = EventLabels(projectId = GUID1))
            addIntoDb(event, wrongEvent)
            verifyEvents(listOf(event), eventDao.loadFromProject(projectId = DEFAULT_PROJECT_ID))
        }
    }

    @Test
    fun loadBySessionId() {
        runBlocking {
            val wrongEvent = event.copy(id = randomUUID(), labels = EventLabels(sessionId = GUID2))
            addIntoDb(event, wrongEvent)
            verifyEvents(listOf(event), eventDao.loadFromSession(sessionId = GUID1))
        }
    }

    @Test
    fun loadEventJsonFormSession() {
        runBlocking {
            addIntoDb(event)
            val results = eventDao.loadEventJsonFromSession(GUID1)
            assertThat(results).containsExactlyElementsIn(listOf(eventJson))
        }
    }

    @Test
    fun loadAllSessions() {
        runBlocking {
            val closedEvent = event.copy(
                id = randomUUID(),
                sessionIsClosed = true
            )
            addIntoDb(event, closedEvent)
            verifyEvents(listOf(event), eventDao.loadAllSessions(false))
            verifyEvents(listOf(closedEvent), eventDao.loadAllSessions(true))
        }
    }

    @Test
    fun loadAllClosedSessionIds() {
        runBlocking {
            val otherId = randomUUID()
            val closedEvent = event.copy(
                id = otherId,
                sessionIsClosed = true,
                labels = event.labels.copy(sessionId = otherId)
            )

            addIntoDb(event, closedEvent)
            assertThat(listOf(closedEvent.id)).isEqualTo(
                eventDao.loadAllClosedSessionIds(
                    DEFAULT_PROJECT_ID
                )
            )
        }
    }

    @Test
    fun loadAbandonedEvents() {
        runBlocking {
            val closedEvent = event.copy(
                id = randomUUID(),
                labels = event.labels.copy(sessionId = null)
            )

            addIntoDb(event, closedEvent)
            verifyEvents(listOf(closedEvent), eventDao.loadOldSubjectCreationEvents(DEFAULT_PROJECT_ID))
        }
    }

    @Test
    fun loadAll() {
        runBlocking {
            val secondEvent = event.copy(id = randomUUID(), labels = EventLabels(deviceId = GUID2))
            addIntoDb(event, secondEvent)
            verifyEvents(listOf(event, secondEvent), eventDao.loadAll())
        }
    }

    @Test
    fun count() {
        runBlocking {
            addIntoDb(event, event.copy(id = randomUUID()), event.copy(id = randomUUID()))
            assertThat(eventDao.countFromType(SESSION_CAPTURE)).isEqualTo(3)
        }
    }

    @Test
    fun deletion() {
        runBlocking {
            addIntoDb(event)
            db.eventDao.delete(listOf(event.id))
            assertThat(eventDao.countFromType(SESSION_CAPTURE)).isEqualTo(0)
        }
    }

    @Test
    fun deletionBySessionId() {
        runBlocking {
            val eventSameSession =
                event.copy(id = randomUUID(), labels = EventLabels(sessionId = GUID1))
            val eventDifferentSession =
                event.copy(id = randomUUID(), labels = EventLabels(sessionId = GUID2))
            addIntoDb(event, eventSameSession, eventDifferentSession)
            db.eventDao.deleteAllFromSession(sessionId = GUID1)
            verifyEvents(listOf(eventDifferentSession), eventDao.loadAll())
        }
    }

    private suspend fun addIntoDb(vararg events: DbEvent) {
        events.forEach {
            eventDao.insertOrUpdate(it)
        }
    }

    private fun verifyEvents(expectedEvents: List<DbEvent>, queryResult: List<DbEvent>) {
        assertThat(queryResult).containsExactlyElementsIn(expectedEvents)
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }
}
