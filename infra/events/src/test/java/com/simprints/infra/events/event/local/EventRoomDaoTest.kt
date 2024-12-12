package com.simprints.infra.events.event.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.utils.randomUUID
import com.simprints.infra.events.event.domain.models.EventType
import com.simprints.infra.events.event.local.models.DbEvent
import com.simprints.infra.events.event.local.models.fromDomainToDb
import com.simprints.infra.events.sampledata.SampleDefaults.CREATED_AT
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.infra.events.sampledata.SampleDefaults.GUID1
import com.simprints.infra.events.sampledata.SampleDefaults.GUID2
import io.mockk.MockKAnnotations
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
        CREATED_AT.fromDomainToDb(),
        EventType.INTENT_PARSING,
        DEFAULT_PROJECT_ID,
        GUID1,
        eventJson,
    )

    private lateinit var db: EventRoomDatabase
    private lateinit var eventDao: EventRoomDao

    @Before
    fun setup() {
        MockKAnnotations.init(this)

        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room
            .inMemoryDatabaseBuilder(context, EventRoomDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        eventDao = db.eventDao
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun loadBySessionId() {
        runTest {
            val wrongEvent = event.copy(id = randomUUID(), scopeId = GUID2)
            addIntoDb(event, wrongEvent)
            verifyEvents(listOf(event), eventDao.loadFromScope(scopeId = GUID1))
        }
    }

    @Test
    fun loadEventJsonFormSession() {
        runTest {
            addIntoDb(event)
            val results = eventDao.loadEventJsonFromScope(GUID1)
            assertThat(results).containsExactlyElementsIn(listOf(eventJson))
        }
    }

    @Test
    fun loadAll() {
        runTest {
            val secondEvent = event.copy(id = randomUUID())
            addIntoDb(event, secondEvent)
            verifyEvents(listOf(event, secondEvent), eventDao.loadAll())
        }
    }

    @Test
    fun deletionBySessionId() {
        runTest {
            val eventSameSession =
                event.copy(id = randomUUID(), scopeId = GUID1)
            val eventDifferentSession =
                event.copy(id = randomUUID(), scopeId = GUID2)
            addIntoDb(event, eventSameSession, eventDifferentSession)
            db.eventDao.deleteAllFromScope(scopeId = GUID1)
            verifyEvents(listOf(eventDifferentSession), eventDao.loadAll())
        }
    }

    private suspend fun addIntoDb(vararg events: DbEvent) {
        events.forEach {
            eventDao.insertOrUpdate(it)
        }
    }

    private fun verifyEvents(
        expectedEvents: List<DbEvent>,
        queryResult: List<DbEvent>,
    ) {
        assertThat(queryResult).containsExactlyElementsIn(expectedEvents)
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }
}
