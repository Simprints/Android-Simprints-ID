package com.simprints.id.data.db.event.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.utils.randomUUID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODULE_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODULE_ID_2
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_USER_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_USER_ID_2
import com.simprints.id.commontesttools.DefaultTestConstants.GUID1
import com.simprints.id.commontesttools.DefaultTestConstants.GUID2
import com.simprints.id.commontesttools.events.CREATED_AT
import com.simprints.id.commontesttools.events.ENDED_AT
import com.simprints.id.data.db.event.domain.models.EventLabels
import com.simprints.id.data.db.event.domain.models.EventType.CONSENT
import com.simprints.id.data.db.event.domain.models.EventType.SESSION_CAPTURE
import com.simprints.id.data.db.event.local.models.DbEvent
import com.simprints.id.domain.modality.Modes
import com.simprints.id.tools.time.TimeHelper
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class EventRoomDaoTest {

    val event = DbEvent(GUID1,
        EventLabels(
            projectId = DEFAULT_PROJECT_ID,
            subjectId = DEFAULT_USER_ID_2,
            attendantId = DEFAULT_USER_ID,
            moduleIds = listOf(DEFAULT_MODULE_ID, DEFAULT_MODULE_ID_2),
            mode = listOf(Modes.FACE, Modes.FINGERPRINT),
            sessionId = GUID1,
            deviceId = GUID1
        ),
        SESSION_CAPTURE, "", CREATED_AT, ENDED_AT)

    private lateinit var db: EventRoomDatabase
    private lateinit var eventDao: EventRoomDao

    @RelaxedMockK lateinit var timeHelper: TimeHelper

    @Before
    fun setup() {
        MockKAnnotations.init(this)

        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, EventRoomDatabase::class.java).allowMainThreadQueries().build()
        eventDao = db.eventDao
    }

    @Test
    fun loadById() {
        runBlocking {
            val wrongEvent = event.copy(id = randomUUID())
            addIntoDb(event, wrongEvent)
            verifyEvents(listOf(event), eventDao.load(id = GUID1))
        }
    }

    @Test
    fun loadByType() {
        runBlocking {
            val wrongEvent = event.copy(id = randomUUID(), type = CONSENT)
            addIntoDb(event, wrongEvent)
            verifyEvents(listOf(event), eventDao.load(type = SESSION_CAPTURE))
        }
    }

    @Test
    fun loadByProjectId() {
        runBlocking {
            val wrongEvent = event.copy(id = randomUUID(), labels = EventLabels(projectId = GUID1))
            addIntoDb(event, wrongEvent)
            verifyEvents(listOf(event), eventDao.load(projectId = DEFAULT_PROJECT_ID))
        }
    }

    @Test
    fun loadBySubjectId() {
        runBlocking {
            val wrongEvent = event.copy(id = randomUUID(), labels = EventLabels(subjectId = GUID1))
            addIntoDb(event, wrongEvent)
            verifyEvents(listOf(event), eventDao.load(subjectId = DEFAULT_USER_ID_2))
        }
    }

    @Test
    fun loadByAttendantId() {
        runBlocking {
            val wrongEvent = event.copy(id = randomUUID(), labels = EventLabels(attendantId = GUID1))
            addIntoDb(event, wrongEvent)
            verifyEvents(listOf(event), eventDao.load(attendantId = DEFAULT_USER_ID))
        }
    }

    @Test
    fun loadBySessionId() {
        runBlocking {
            val wrongEvent = event.copy(id = randomUUID(), labels = EventLabels(sessionId = GUID2))
            addIntoDb(event, wrongEvent)
            verifyEvents(listOf(event), eventDao.load(sessionId = GUID1))
        }
    }

    @Test
    fun loadByDeviceId() {
        runBlocking {
            val wrongEvent = event.copy(id = randomUUID(), labels = EventLabels(deviceId = GUID2))
            addIntoDb(event, wrongEvent)
            verifyEvents(listOf(event), eventDao.load(deviceId = GUID1))
        }
    }

    @Test
    fun loadByCreatedAt() {
        runBlocking {
            val wrongEvent = event.copy(id = randomUUID(), createdAt = CREATED_AT - 100)
            addIntoDb(event, wrongEvent)
            verifyEvents(listOf(event), eventDao.load(createdAtLower = CREATED_AT, createdAtUpper = CREATED_AT))
        }
    }

    @Test
    fun loadByEndedAt() {
        runBlocking {
            val wrongEvent = event.copy(id = randomUUID(), endedAt = ENDED_AT + 100)
            addIntoDb(event, wrongEvent)
            verifyEvents(listOf(event), eventDao.load(endedAtLower = ENDED_AT, endedAtUpper = ENDED_AT))
        }
    }

    @Test
    fun count() {
        runBlocking {
            addIntoDb(event, event.copy(id = randomUUID()), event.copy(id = randomUUID()))
            assertThat(eventDao.count()).isEqualTo(3)
        }
    }

    @Test
    fun deletion() {
        runBlocking {
            db.eventDao.delete(event.id)
            assertThat(eventDao.count()).isEqualTo(0)
        }
    }

    @Test
    fun deletionBySessionId() {
        runBlocking {
            val eventSameSession = event.copy(id = randomUUID(), labels = EventLabels(sessionId = GUID1))
            val eventDifferentSession = event.copy(id = randomUUID(), labels = EventLabels(sessionId = GUID2))
            addIntoDb(event, eventSameSession, eventDifferentSession)
            db.eventDao.delete(sessionId = GUID1)
            verifyEvents(listOf(eventDifferentSession), eventDao.load())
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
