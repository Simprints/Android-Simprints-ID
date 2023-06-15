package com.simprints.infra.events.event.local

import android.content.Context
import android.database.sqlite.SQLiteDatabaseCorruptException
import android.database.sqlite.SQLiteException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.infra.events.event.domain.models.Event
import com.simprints.infra.events.event.domain.models.EventType.CALLBACK_ENROLMENT
import com.simprints.infra.events.event.domain.models.EventType.SESSION_CAPTURE
import com.simprints.infra.events.event.local.*
import com.simprints.infra.events.event.local.models.DbEvent
import com.simprints.infra.events.event.local.models.fromDbToDomain
import com.simprints.infra.events.event.local.models.fromDomainToDb
import com.simprints.infra.events.local.*
import com.simprints.infra.events.sampledata.SampleDefaults.GUID1
import io.mockk.*
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
internal class EventLocalDataSourceImplTest {

    private lateinit var db: EventRoomDatabase
    private lateinit var eventDao: EventRoomDao
    private lateinit var eventLocalDataSource: EventLocalDataSource

    @RelaxedMockK
    lateinit var eventDatabaseFactory: EventDatabaseFactory

    @Before
    fun setup() {
        MockKAnnotations.init(this)

        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, EventRoomDatabase::class.java)
            .allowMainThreadQueries().build()

        eventDao = db.eventDao
        every { eventDatabaseFactory.build() } returns db
        mockDaoLoadToMakeNothing()
    }

    @Test(expected = SQLiteDatabaseCorruptException::class)
    fun `test handleDatabaseCorruption gets called on SQLiteDatabaseCorruptExceptions`() =
        runTest {
            //Given
            coEvery { eventDao.loadAll() } throws SQLiteDatabaseCorruptException()
            // When
            eventLocalDataSource.loadAll()
            // Then
            verify {
                eventDatabaseFactory.deleteDatabase()
                eventDatabaseFactory.recreateDatabaseKey()
                eventDatabaseFactory.build()
            }
            coVerify(exactly = 2) { eventDao.loadAll() }
        }

    @Test(expected = SQLiteException::class)
    fun `test handleDatabaseCorruption gets called on SQLiteExceptions that contains 'file is not a database'`() =
        runTest {
            //Given
            coEvery { eventDao.loadAll() } throws SQLiteException("file is not a database")
            // When
            eventLocalDataSource.loadAll()
            // Then
            verify {
                eventDatabaseFactory.deleteDatabase()
                eventDatabaseFactory.recreateDatabaseKey()
                eventDatabaseFactory.build()
            }
            coVerify(exactly = 2) { eventDao.loadAll() }
        }

    @Test(expected = SQLiteException::class)
    fun `test handleDatabaseCorruption not called on SQLiteExceptions that don't  contain 'file is not a database'`() =
        runTest {
            //Given
            coEvery { eventDao.loadAll() } throws SQLiteException()
            // When
            eventLocalDataSource.loadAll()
            // Then
            verify(exactly = 0) {
                eventDatabaseFactory.deleteDatabase()
            }
            coVerify(exactly = 1) { eventDao.loadAll() }
        }

    @Test(expected = Exception::class)
    fun `test handleDatabaseCorruption not called on other Exceptions`() =
        runTest {
            //Given
            coEvery { eventDao.loadAll() } throws Exception()
            // When
            eventLocalDataSource.loadAll()
            // Then
            verify(exactly = 0) {
                eventDatabaseFactory.deleteDatabase()
            }
            coVerify(exactly = 1) { eventDao.loadAll() }
        }

    @Test
    fun loadAll() = runTest {
        eventLocalDataSource.loadAll()

        coVerify { eventDao.loadAll() }
    }

    @Test
    fun loadAllEventJsonFromSession() = runTest {
        val sessionId = GUID1
        eventLocalDataSource.loadAllEventJsonFromSession(sessionId)

        coVerify { eventDao.loadEventJsonFromSession(sessionId) }
    }

    @Test
    fun loadAllFromSession() = runTest {
        val sessionId = GUID1
        eventLocalDataSource.loadAllFromSession(sessionId)

        coVerify { eventDao.loadFromSession(sessionId) }
    }

    @Test
    fun loadOpenedSessions() = runTest {
        mockkStatic("com.simprints.infra.events.event.local.models.DbEventKt")
        val dbSessionCaptureEvent = mockk<DbEvent> {
            every { type } returns SESSION_CAPTURE
            every { fromDbToDomain() } returns mockk()
        }
        coEvery { eventDao.loadOpenedSessions() } returns listOf(dbSessionCaptureEvent)
        eventLocalDataSource.loadOpenedSessions()

        coVerify { eventDao.loadOpenedSessions() }
        verify { dbSessionCaptureEvent.fromDbToDomain() }
    }

    @Test
    fun loadAllClosedSessionIds() = runTest {
        val sessionId = GUID1
        eventLocalDataSource.loadAllClosedSessionIds(sessionId)

        coVerify { eventDao.loadAllClosedSessionIds(sessionId) }
    }

    @Test
    fun countWithEventType() = runTest {
        eventLocalDataSource.count(type = SESSION_CAPTURE)

        coVerify { eventDao.countFromType(type = SESSION_CAPTURE) }
    }


    @Test
    fun countWithAProjectIdQuery() = runTest {
        eventLocalDataSource.count(projectId = "PROJECT_ID")

        coVerify { eventDao.countFromProject(projectId = "PROJECT_ID") }
    }


    @Test
    fun observeCountWithAProjectIdQuery() = runTest {
        eventLocalDataSource.observeCount(projectId = "PROJECT_ID").toList()

        coVerify { eventDao.observeCount(projectId = "PROJECT_ID") }
    }

    @Test
    fun observeCountWithAProjectIdAndTypeQuery() = runTest {
        eventLocalDataSource.observeCount(
            projectId = "PROJECT_ID",
            type = CALLBACK_ENROLMENT,
        ).toList()

        coVerify {
            eventDao.observeCountFromType(
                projectId = "PROJECT_ID",
                type = CALLBACK_ENROLMENT,
            )
        }

    }

    @Test
    fun insertOrUpdate() {
        runBlocking {
            mockkStatic("com.simprints.infra.events.event.local.models.DbEventKt")
            val dbEvent = mockk<DbEvent>()
            val event = mockk<Event> {
                every { fromDomainToDb() } returns dbEvent
            }
            eventLocalDataSource.insertOrUpdate(event)

            coVerify { eventDao.insertOrUpdate(dbEvent) }
        }
    }

    @Test
    fun delete() = runTest {
        eventLocalDataSource.delete(listOf("1", "2"))

        coVerify { eventDao.delete(listOf("1", "2")) }
    }

    @Test
    fun deleteAllFromSession() = runTest {
        eventLocalDataSource.deleteAllFromSession(GUID1)

        coVerify { eventDao.deleteAllFromSession(GUID1) }
    }

    @Test
    fun deleteAll() = runTest {
        eventLocalDataSource.deleteAll()

        coVerify { eventDao.deleteAll() }
    }

    private fun mockDaoLoadToMakeNothing() {
        db = mockk(relaxed = true)
        eventDao = mockk(relaxed = true)
        eventDatabaseFactory = mockk(relaxed = true)
        coEvery { eventDao.loadAll() } returns emptyList()
        coEvery { eventDao.loadFromProject(any()) } returns emptyList()
        coEvery { eventDao.loadFromSession(any()) } returns emptyList()
        coEvery { eventDao.countFromProject(any()) } returns 0
        coEvery { eventDao.countFromType(any()) } returns 0
        coEvery { eventDao.countFromProjectByType(any(), any()) } returns 0
        every { db.eventDao } returns eventDao
        every { eventDatabaseFactory.build() } returns db
        eventLocalDataSource = EventLocalDataSourceImpl(
            eventDatabaseFactory,
            UnconfinedTestDispatcher(),
            UnconfinedTestDispatcher()
        )
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

}
