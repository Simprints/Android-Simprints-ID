package com.simprints.infra.events.event.local

import android.content.Context
import android.database.sqlite.SQLiteDatabaseCorruptException
import android.database.sqlite.SQLiteException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.json.JsonHelper
import com.simprints.infra.events.event.domain.models.Event
import com.simprints.infra.events.event.domain.models.EventType.CALLBACK_ENROLMENT
import com.simprints.infra.events.event.domain.models.EventType.SESSION_CAPTURE
import com.simprints.infra.events.event.local.*
import com.simprints.infra.events.event.local.models.DbEvent
import com.simprints.infra.events.event.local.models.DbSessionScope
import com.simprints.infra.events.event.local.models.fromDbToDomain
import com.simprints.infra.events.event.local.models.fromDomainToDb
import com.simprints.infra.events.local.*
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.infra.events.sampledata.SampleDefaults.GUID1
import com.simprints.testtools.common.syntax.assertThrows
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import dagger.hilt.android.testing.HiltTestApplication
import io.mockk.*
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.io.IOException

@RunWith(AndroidJUnit4::class)
@Config(application = HiltTestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
internal class EventLocalDataSourceImplTest {

    private lateinit var db: EventRoomDatabase
    private lateinit var eventDao: EventRoomDao
    private lateinit var scopeDao: SessionScopeRoomDao
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
        scopeDao = db.scopeDao

        every { eventDatabaseFactory.build() } returns db
        mockDaoLoadToMakeNothing()

        eventLocalDataSource = EventLocalDataSourceImpl(
            eventDatabaseFactory,
            JsonHelper,
            UnconfinedTestDispatcher(),
            UnconfinedTestDispatcher()
        )
    }

    @Test
    fun `test handleDatabaseCorruption gets called on SQLiteDatabaseCorruptExceptions`() =
        runTest {
            //Given
            coEvery { eventDao.loadAll() }
                .throws(SQLiteDatabaseCorruptException())
                .andThen(emptyList())
            //
            eventLocalDataSource.loadAll()
            // Then
            verify {
                eventDatabaseFactory.deleteDatabase()
                eventDatabaseFactory.recreateDatabaseKey()
                eventDatabaseFactory.build()
            }
            coVerify(exactly = 2) { eventDao.loadAll() }
        }

    @Test
    fun `test handleDatabaseCorruption gets called on SQLiteExceptions that contains 'file is not a database'`() =
        runTest {
            //Given
            coEvery { eventDao.loadAll() }
                .throws(SQLiteException("file is not a database"))
                .andThen(emptyList())
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

    @Test
    fun `test handleDatabaseCorruption not called on SQLiteExceptions that don't  contain 'file is not a database'`() =
        runTest {
            //Given
            coEvery { eventDao.loadAll() } throws SQLiteException()
            // When
            assertThrows<SQLiteException> { eventLocalDataSource.loadAll() }
            // Then
            verify(exactly = 0) {
                eventDatabaseFactory.deleteDatabase()
            }
            coVerify(exactly = 1) { eventDao.loadAll() }
        }

    @Test
    fun `test handleDatabaseCorruption not called on other Exceptions`() =
        runTest {
            //Given
            coEvery { eventDao.loadAll() } throws Exception()
            // When
            assertThrows<Exception> { eventLocalDataSource.loadAll() }
            // Then
            verify(exactly = 0) {
                eventDatabaseFactory.deleteDatabase()
            }
            coVerify(exactly = 1) { eventDao.loadAll() }
        }

    @Test
    fun `test handleDatabaseCorruption gets called on SQLiteDatabaseCorruptExceptions by flow`() =
        runTest {
            //Given
            coEvery { eventDao.observeCount(any()) }
                .throws(SQLiteDatabaseCorruptException())
                .andThen(flowOf(1))
            // When
            eventLocalDataSource.observeCount(DEFAULT_PROJECT_ID).toList()
            // Then
            verify {
                eventDatabaseFactory.deleteDatabase()
                eventDatabaseFactory.recreateDatabaseKey()
                eventDatabaseFactory.build()
            }
            coVerify(exactly = 2) { eventDao.observeCount(any()) }
        }

    @Test
    fun `test handleDatabaseCorruption not called on SQLiteExceptions that don't  contain 'file is not a database' by flow`() =
        runTest {
            //Given
            coEvery { eventDao.observeCount(any()) } throws SQLiteException()
            // When
            assertThrows<SQLiteException> {
                eventLocalDataSource.observeCount(DEFAULT_PROJECT_ID).toList()
            }
            // Then
            verify(exactly = 0) {
                eventDatabaseFactory.deleteDatabase()
            }
            coVerify(exactly = 1) { eventDao.observeCount(any()) }
        }

    @Test
    fun `test handleDatabaseCorruption gets called on SQLiteExceptions that contains 'file is not a database' by flow`() =
        runTest {
            //Given
            coEvery { eventDao.observeCount(any()) }
                .throws(SQLiteException("file is not a database"))
                .andThen(flowOf(1))
            // When
            eventLocalDataSource.observeCount(DEFAULT_PROJECT_ID).toList()
            // Then
            verify {
                eventDatabaseFactory.deleteDatabase()
                eventDatabaseFactory.recreateDatabaseKey()
                eventDatabaseFactory.build()
            }
            coVerify(exactly = 2) { eventDao.observeCount(any()) }
        }

    @Test
    fun `test handleDatabaseCorruption not called on other Exceptions by flow`() =
        runTest {
            //Given
            coEvery { eventDao.observeCount(any()) } throws Exception()
            // When
            assertThrows<Exception> {
                eventLocalDataSource.observeCount(DEFAULT_PROJECT_ID).toList()
            }
            // Then
            verify(exactly = 0) {
                eventDatabaseFactory.deleteDatabase()
            }
            coVerify(exactly = 1) { eventDao.observeCount(any()) }
        }

    @Test
    fun `returns value after handleDatabaseCorruption gets called on SQLiteDatabaseCorruptException by flow`() =
        runTest {
            //Given
            coEvery { eventDao.observeCount(any()) }
                .throws(SQLiteException("file is not a database"))
                .andThen(flowOf(1, 2, 3))
            // When
            val count = eventLocalDataSource.observeCount(DEFAULT_PROJECT_ID).toList()
            // Then
            verify {
                eventDatabaseFactory.deleteDatabase()
                eventDatabaseFactory.recreateDatabaseKey()
                eventDatabaseFactory.build()
            }
            coVerify(exactly = 2) { eventDao.observeCount(any()) }
            assertThat(count).isEqualTo(listOf(1, 2, 3))
        }

    @Test
    fun `test handleDatabaseCorruption gets called on SQLiteDatabaseCorruptExceptions inside of the flow`() =
        runTest {
            //Given
            coEvery { eventDao.observeCount(any()) }
                .returns(flow { throw SQLiteDatabaseCorruptException() })
                .andThen(flowOf(1))
            // When
            eventLocalDataSource.observeCount(DEFAULT_PROJECT_ID).toList()
            // Then
            verify {
                eventDatabaseFactory.deleteDatabase()
                eventDatabaseFactory.recreateDatabaseKey()
                eventDatabaseFactory.build()
            }
            coVerify(exactly = 2) { eventDao.observeCount(any()) }
        }

    @Test
    fun `test handleDatabaseCorruption not called on SQLiteExceptions that don't  contain 'file is not a database' inside of the flow`() =
        runTest {
            //Given
            coEvery { eventDao.observeCount(any()) }.returns(flow { throw SQLiteException() })
            // When
            assertThrows<SQLiteException> {
                eventLocalDataSource.observeCount(DEFAULT_PROJECT_ID).toList()
            }
            // Then
            verify(exactly = 0) { eventDatabaseFactory.deleteDatabase() }
            coVerify(exactly = 1) { eventDao.observeCount(any()) }
        }

    @Test
    fun `test handleDatabaseCorruption gets called on SQLiteExceptions that contains 'file is not a database' inside of the flow`() =
        runTest {
            //Given
            coEvery { eventDao.observeCount(any()) }
                .returns(flow { throw SQLiteException("file is not a database") })
                .andThen(flowOf(1))
            // When
            eventLocalDataSource.observeCount(DEFAULT_PROJECT_ID).toList()
            // Then
            verify {
                eventDatabaseFactory.deleteDatabase()
                eventDatabaseFactory.recreateDatabaseKey()
                eventDatabaseFactory.build()
            }
            coVerify(exactly = 2) { eventDao.observeCount(any()) }
        }

    @Test
    fun `test handleDatabaseCorruption not called on other Exceptions inside of the flow`() =
        runTest {
            //Given
            coEvery { eventDao.observeCount(any()) }.returns(flow { throw Exception() })
            // When
            assertThrows<Exception> {
                eventLocalDataSource.observeCount(DEFAULT_PROJECT_ID).toList()
            }
            // Then
            verify(exactly = 0) { eventDatabaseFactory.deleteDatabase() }
            coVerify(exactly = 1) { eventDao.observeCount(any()) }
        }

    @Test
    fun `returns value after handleDatabaseCorruption gets called on SQLiteDatabaseCorruptException inside of the flow`() =
        runTest {
            //Given
            coEvery { eventDao.observeCount(any()) }
                .returns(flow { throw SQLiteDatabaseCorruptException() })
                .andThen(flowOf(1, 2, 3))
            // When
            val count = eventLocalDataSource.observeCount(DEFAULT_PROJECT_ID).toList()
            // Then
            verify {
                eventDatabaseFactory.deleteDatabase()
                eventDatabaseFactory.recreateDatabaseKey()
                eventDatabaseFactory.build()
            }
            coVerify(exactly = 2) { eventDao.observeCount(any()) }
            assertThat(count).isEqualTo(listOf(1, 2, 3))
        }

    @Test
    fun `is not stuck when handleDatabaseCorruption gets called on SQLiteDatabaseCorruptException inside of the flow`() =
        runTest {
            //Given
            coEvery { eventDao.observeCount(any()) }
                .returns(flow { throw SQLiteDatabaseCorruptException() })
            // When
            assertThrows<SQLiteDatabaseCorruptException> {
                eventLocalDataSource.observeCount(DEFAULT_PROJECT_ID).toList()
            }
            // Then
            verify {
                eventDatabaseFactory.deleteDatabase()
                eventDatabaseFactory.recreateDatabaseKey()
                eventDatabaseFactory.build()
            }
            coVerify(exactly = 2) { eventDao.observeCount(any()) }
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
    fun countSessions() = runTest {
        eventLocalDataSource.countSessions()

        coVerify { scopeDao.count() }
    }

    @Test
    fun saveSessionScope() = runTest {
        mockkStatic("com.simprints.infra.events.event.local.models.DbSessionScopeKt")
        eventLocalDataSource.saveSessionScope(mockk())

        coVerify { scopeDao.insertOrUpdate(any()) }
    }

    @Test
    fun loadOpenedSessions() = runTest {
        mockkStatic("com.simprints.infra.events.event.local.models.DbSessionScopeKt")
        val dbSessionCaptureEvent = mockk<DbSessionScope> {
            every { fromDbToDomain(any()) } returns mockk()
        }
        coEvery { scopeDao.loadOpen() } returns listOf(dbSessionCaptureEvent)
        eventLocalDataSource.loadOpenedSessions()

        coVerify { scopeDao.loadOpen() }
        verify { dbSessionCaptureEvent.fromDbToDomain(any()) }
    }

    @Test
    fun loadClosedSessions() = runTest {
        mockkStatic("com.simprints.infra.events.event.local.models.DbSessionScopeKt")
        val dbSessionCaptureEvent = mockk<DbSessionScope> {
            every { fromDbToDomain(any()) } returns mockk()
        }
        coEvery { scopeDao.loadClosed(any()) } returns listOf(dbSessionCaptureEvent)
        eventLocalDataSource.loadClosedSessions("test")

        coVerify { scopeDao.loadClosed(any()) }
        verify { dbSessionCaptureEvent.fromDbToDomain(any()) }
    }

    @Test
    fun deleteSession() = runTest {
        eventLocalDataSource.deleteSession(GUID1)

        coVerify { scopeDao.delete(listOf(GUID1)) }
    }

    @Test
    fun deleteSessions() = runTest {
        eventLocalDataSource.deleteSessions(listOf(GUID1, GUID1))

        coVerify { scopeDao.delete(listOf(GUID1, GUID1)) }
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
    fun observeCountWithAProjectIdQueryReturns() = runTest {
        coEvery { eventDao.observeCount(any()) } returns flowOf(1, 2, 3)

        val count = eventLocalDataSource.observeCount(projectId = "PROJECT_ID").toList()

        assertThat(count).isEqualTo(listOf(1, 2, 3))
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
        runTest {
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

    @Test
    fun `when loadAllFromProject is called, then events are loaded from the local storage`() =
        runTest {
            val projectId = "projectId"
            coEvery { eventDao.loadFromProject(projectId) } returns mockk()
            eventLocalDataSource.loadAllFromProject(projectId)

            coVerify { eventDao.loadFromProject(projectId) }
        }

    private fun mockDaoLoadToMakeNothing() {
        db = mockk(relaxed = true)
        eventDao = mockk(relaxed = true)
        scopeDao = mockk(relaxed = true)
        eventDatabaseFactory = mockk(relaxed = true)
        coEvery { eventDao.loadAll() } returns emptyList()
        coEvery { eventDao.loadFromProject(any()) } returns emptyList()
        coEvery { eventDao.loadFromSession(any()) } returns emptyList()
        coEvery { eventDao.countFromProject(any()) } returns 0
        coEvery { eventDao.countFromType(any()) } returns 0
        coEvery { eventDao.countFromProjectByType(any(), any()) } returns 0
        coEvery { scopeDao.loadAll() } returns emptyList()
        coEvery { scopeDao.loadOpen() } returns emptyList()
        coEvery { scopeDao.count() } returns 0
        every { db.eventDao } returns eventDao
        every { db.scopeDao } returns scopeDao
        every { eventDatabaseFactory.build() } returns db
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

}
