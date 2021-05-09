package com.simprints.id.data.db.event.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.utils.randomUUID
import com.simprints.id.sampledata.SampleDefaults.DEFAULT_MODULE_ID
import com.simprints.id.sampledata.SampleDefaults.DEFAULT_MODULE_ID_2
import com.simprints.id.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.id.sampledata.SampleDefaults.DEFAULT_USER_ID
import com.simprints.id.sampledata.SampleDefaults.DEFAULT_USER_ID_2
import com.simprints.id.sampledata.SampleDefaults.GUID1
import com.simprints.id.sampledata.SampleDefaults.GUID2
import com.simprints.id.sampledata.SampleDefaults.CREATED_AT
import com.simprints.id.sampledata.SampleDefaults.ENDED_AT
import com.simprints.id.data.db.event.domain.models.EventLabels
import com.simprints.id.data.db.event.domain.models.EventType.SESSION_CAPTURE
import com.simprints.id.data.db.event.local.models.DbEvent
import com.simprints.id.domain.modality.Modes
import com.simprints.id.testtools.TestApplication
import com.simprints.id.tools.time.TimeHelper
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.io.IOException

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class EventRoomDaoTest {

    val event = DbEvent(
        GUID1,
        EventLabels(
            projectId = DEFAULT_PROJECT_ID,
            subjectId = DEFAULT_USER_ID_2,
            attendantId = DEFAULT_USER_ID,
            moduleIds = listOf(DEFAULT_MODULE_ID, DEFAULT_MODULE_ID_2),
            mode = listOf(Modes.FACE, Modes.FINGERPRINT),
            sessionId = GUID1,
            deviceId = GUID1
        ),
        SESSION_CAPTURE, "", CREATED_AT, ENDED_AT, false
    )

    private lateinit var db: EventRoomDatabase
    private lateinit var eventDao: EventRoomDao

    @RelaxedMockK
    lateinit var timeHelper: TimeHelper

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
            verifyEvents(listOf(closedEvent), eventDao.loadAbandonedEvents(DEFAULT_PROJECT_ID))
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
            db.eventDao.delete(event.id)
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
