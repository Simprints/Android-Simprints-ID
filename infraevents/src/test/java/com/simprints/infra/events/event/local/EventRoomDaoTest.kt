package com.simprints.infra.events.event.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.utils.randomUUID
import com.simprints.infra.events.event.domain.models.EventLabels
import com.simprints.infra.events.event.domain.models.EventType.SESSION_CAPTURE
import com.simprints.infra.events.event.local.EventRoomDatabase
import com.simprints.infra.events.event.local.models.DbEvent
import com.simprints.infra.events.sampledata.SampleDefaults.CREATED_AT
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.infra.events.sampledata.SampleDefaults.ENDED_AT
import com.simprints.infra.events.sampledata.SampleDefaults.GUID1
import com.simprints.infra.events.sampledata.SampleDefaults.GUID2
import io.mockk.MockKAnnotations
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
internal class EventRoomDaoTest {

    private val eventJson = """{"id": "anID", "payload": "a payload"}"""
    val event = DbEvent(
        GUID1,
        EventLabels(
            projectId = DEFAULT_PROJECT_ID,
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
    fun `test loadOpenedSessions`() = runTest {
        addIntoDb(event)
        val result = eventDao.loadOpenedSessions()
        assertThat(result).containsExactlyElementsIn(listOf(event))
    }

    @Test
    fun `test loadOpenedSessions return nothing if all sessions are closed`() = runTest {
        val closedSessionEvent = event.copy(sessionIsClosed = true)
        addIntoDb(closedSessionEvent)
        val result = eventDao.loadOpenedSessions()
        assertThat(result).isEmpty()
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
