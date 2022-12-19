package com.simprints.eventsystem.event.local

import android.content.Context
import android.database.sqlite.SQLiteDatabaseCorruptException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.eventsystem.event.domain.models.Event
import com.simprints.eventsystem.event.domain.models.EventType.SESSION_CAPTURE
import com.simprints.eventsystem.event.local.models.DbEvent
import com.simprints.eventsystem.event.local.models.fromDbToDomain
import com.simprints.eventsystem.event.local.models.fromDomainToDb
import com.simprints.eventsystem.sampledata.SampleDefaults.GUID1
import io.mockk.*
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import net.sqlcipher.database.SQLiteException
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
    fun loadWithAQuery() = runTest {
        eventLocalDataSource.loadAll()

        coVerify {
            eventDao.loadAll()
        }
    }

    @Test
    fun loadOpenedSessions() = runTest {
        mockkStatic("com.simprints.eventsystem.event.local.models.DbEventKt")
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
    fun dumpInvalidEvents() = runTest {
        val sessionId = GUID1
        eventLocalDataSource.loadAllEventJsonFromSession(sessionId)

        coVerify {
            eventDao.loadEventJsonFromSession(sessionId)
        }
    }

    @Test
    fun countWithATypeQuery() {
        runBlocking {
            eventLocalDataSource.count(SESSION_CAPTURE)

            coVerify {
                eventDao.countFromType(type = SESSION_CAPTURE)
            }
        }
    }

    @Test
    fun countWithAProjectIdQuery() {
        runBlocking {
            eventLocalDataSource.count(projectId = "PROJECT_ID")

            coVerify {
                eventDao.countFromProject(projectId = "PROJECT_ID")
            }
        }
    }

    @Test
    fun insertOrUpdate() {
        runBlocking {
            mockkStatic("com.simprints.eventsystem.event.local.models.DbEventKt")
            val dbEvent = mockk<DbEvent>()
            val event = mockk<Event> {
                every { fromDomainToDb() } returns dbEvent
            }
            eventLocalDataSource.insertOrUpdate(event)

            coVerify {
                eventDao.insertOrUpdate(dbEvent)
            }
        }
    }

    @Test
    fun countWithATypeAndProjectIdQuery() {
        runBlocking {
            eventLocalDataSource.count(type = SESSION_CAPTURE, projectId = "PROJECT_ID")

            coVerify {
                eventDao.countFromProjectByType(type = SESSION_CAPTURE, projectId = "PROJECT_ID")
            }
        }
    }

    @Test
    fun deleteWithAQuery() {
        runBlocking {
            eventLocalDataSource.deleteAll()

            coVerify {
                eventDao.deleteAll()
            }
        }
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
