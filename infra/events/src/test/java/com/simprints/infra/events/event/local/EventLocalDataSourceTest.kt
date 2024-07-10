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
import com.simprints.infra.events.event.domain.models.scope.EventScopeType
import com.simprints.infra.events.event.local.*
import com.simprints.infra.events.event.local.models.DbEvent
import com.simprints.infra.events.event.local.models.DbEventScope
import com.simprints.infra.events.event.local.models.fromDbToDomain
import com.simprints.infra.events.event.local.models.fromDomainToDb
import com.simprints.infra.events.local.*
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
internal class EventLocalDataSourceTest {

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

        eventLocalDataSource = EventLocalDataSource(
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
            eventLocalDataSource.loadAllEvents()
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
            eventLocalDataSource.loadAllEvents()
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
            assertThrows<SQLiteException> { eventLocalDataSource.loadAllEvents() }
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
            assertThrows<Exception> { eventLocalDataSource.loadAllEvents() }
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
            coEvery { eventDao.observeCount() }
                .throws(SQLiteDatabaseCorruptException())
                .andThen(flowOf(1))
            // When
            eventLocalDataSource.observeEventCount().toList()
            // Then
            verify {
                eventDatabaseFactory.deleteDatabase()
                eventDatabaseFactory.recreateDatabaseKey()
                eventDatabaseFactory.build()
            }
            coVerify(exactly = 2) { eventDao.observeCount() }
        }

    @Test
    fun `test handleDatabaseCorruption not called on SQLiteExceptions that don't  contain 'file is not a database' by flow`() =
        runTest {
            //Given
            coEvery { eventDao.observeCount() } throws SQLiteException()
            // When
            assertThrows<SQLiteException> {
                eventLocalDataSource.observeEventCount().toList()
            }
            // Then
            verify(exactly = 0) {
                eventDatabaseFactory.deleteDatabase()
            }
            coVerify(exactly = 1) { eventDao.observeCount() }
        }

    @Test
    fun `test handleDatabaseCorruption gets called on SQLiteExceptions that contains 'file is not a database' by flow`() =
        runTest {
            //Given
            coEvery { eventDao.observeCount() }
                .throws(SQLiteException("file is not a database"))
                .andThen(flowOf(1))
            // When
            eventLocalDataSource.observeEventCount().toList()
            // Then
            verify {
                eventDatabaseFactory.deleteDatabase()
                eventDatabaseFactory.recreateDatabaseKey()
                eventDatabaseFactory.build()
            }
            coVerify(exactly = 2) { eventDao.observeCount() }
        }

    @Test
    fun `test handleDatabaseCorruption not called on other Exceptions by flow`() =
        runTest {
            //Given
            coEvery { eventDao.observeCount() } throws Exception()
            // When
            assertThrows<Exception> {
                eventLocalDataSource.observeEventCount().toList()
            }
            // Then
            verify(exactly = 0) {
                eventDatabaseFactory.deleteDatabase()
            }
            coVerify(exactly = 1) { eventDao.observeCount() }
        }

    @Test
    fun `returns value after handleDatabaseCorruption gets called on SQLiteDatabaseCorruptException by flow`() =
        runTest {
            //Given
            coEvery { eventDao.observeCount() }
                .throws(SQLiteException("file is not a database"))
                .andThen(flowOf(1, 2, 3))
            // When
            val count = eventLocalDataSource.observeEventCount().toList()
            // Then
            verify {
                eventDatabaseFactory.deleteDatabase()
                eventDatabaseFactory.recreateDatabaseKey()
                eventDatabaseFactory.build()
            }
            coVerify(exactly = 2) { eventDao.observeCount() }
            assertThat(count).isEqualTo(listOf(1, 2, 3))
        }

    @Test
    fun `test handleDatabaseCorruption gets called on SQLiteDatabaseCorruptExceptions inside of the flow`() =
        runTest {
            //Given
            coEvery { eventDao.observeCount() }
                .returns(flow { throw SQLiteDatabaseCorruptException() })
                .andThen(flowOf(1))
            // When
            eventLocalDataSource.observeEventCount().toList()
            // Then
            verify {
                eventDatabaseFactory.deleteDatabase()
                eventDatabaseFactory.recreateDatabaseKey()
                eventDatabaseFactory.build()
            }
            coVerify(exactly = 2) { eventDao.observeCount() }
        }

    @Test
    fun `test handleDatabaseCorruption not called on SQLiteExceptions that don't  contain 'file is not a database' inside of the flow`() =
        runTest {
            //Given
            coEvery { eventDao.observeCount() }.returns(flow { throw SQLiteException() })
            // When
            assertThrows<SQLiteException> {
                eventLocalDataSource.observeEventCount().toList()
            }
            // Then
            verify(exactly = 0) { eventDatabaseFactory.deleteDatabase() }
            coVerify(exactly = 1) { eventDao.observeCount() }
        }

    @Test
    fun `test handleDatabaseCorruption gets called on SQLiteExceptions that contains 'file is not a database' inside of the flow`() =
        runTest {
            //Given
            coEvery { eventDao.observeCount() }
                .returns(flow { throw SQLiteException("file is not a database") })
                .andThen(flowOf(1))
            // When
            eventLocalDataSource.observeEventCount().toList()
            // Then
            verify {
                eventDatabaseFactory.deleteDatabase()
                eventDatabaseFactory.recreateDatabaseKey()
                eventDatabaseFactory.build()
            }
            coVerify(exactly = 2) { eventDao.observeCount() }
        }

    @Test
    fun `test handleDatabaseCorruption not called on other Exceptions inside of the flow`() =
        runTest {
            //Given
            coEvery { eventDao.observeCount() }.returns(flow { throw Exception() })
            // When
            assertThrows<Exception> {
                eventLocalDataSource.observeEventCount().toList()
            }
            // Then
            verify(exactly = 0) { eventDatabaseFactory.deleteDatabase() }
            coVerify(exactly = 1) { eventDao.observeCount() }
        }

    @Test
    fun `returns value after handleDatabaseCorruption gets called on SQLiteDatabaseCorruptException inside of the flow`() =
        runTest {
            //Given
            coEvery { eventDao.observeCount() }
                .returns(flow { throw SQLiteDatabaseCorruptException() })
                .andThen(flowOf(1, 2, 3))
            // When
            val count = eventLocalDataSource.observeEventCount().toList()
            // Then
            verify {
                eventDatabaseFactory.deleteDatabase()
                eventDatabaseFactory.recreateDatabaseKey()
                eventDatabaseFactory.build()
            }
            coVerify(exactly = 2) { eventDao.observeCount() }
            assertThat(count).isEqualTo(listOf(1, 2, 3))
        }

    @Test
    fun `is not stuck when handleDatabaseCorruption gets called on SQLiteDatabaseCorruptException inside of the flow`() =
        runTest {
            //Given
            coEvery { eventDao.observeCount() }
                .returns(flow { throw SQLiteDatabaseCorruptException() })
            // When
            assertThrows<SQLiteDatabaseCorruptException> {
                eventLocalDataSource.observeEventCount().toList()
            }
            // Then
            verify {
                eventDatabaseFactory.deleteDatabase()
                eventDatabaseFactory.recreateDatabaseKey()
                eventDatabaseFactory.build()
            }
            coVerify(exactly = 2) { eventDao.observeCount() }
        }

    @Test
    fun loadAll() = runTest {
        eventLocalDataSource.loadAllEvents()

        coVerify { eventDao.loadAll() }
    }

    @Test
    fun loadAllEventJsonFromEventScope() = runTest {
        val sessionId = GUID1
        eventLocalDataSource.loadEventJsonInScope(sessionId)

        coVerify { eventDao.loadEventJsonFromScope(sessionId) }
    }

    @Test
    fun loadAllFromEventScope() = runTest {
        val sessionId = GUID1
        eventLocalDataSource.loadEventsInScope(sessionId)

        coVerify { eventDao.loadFromScope(sessionId) }
    }

    @Test
    fun countEventScopes() = runTest {
        eventLocalDataSource.countEventScopes(EventScopeType.SESSION)

        coVerify { scopeDao.count(EventScopeType.SESSION) }
    }

    @Test
    fun saveEventScope() = runTest {
        mockkStatic("com.simprints.infra.events.event.local.models.DbEventScopeKt")
        eventLocalDataSource.saveEventScope(mockk())

        coVerify { scopeDao.insertOrUpdate(any()) }
    }

    @Test
    fun loadOpenedEventScope() = runTest {
        mockkStatic("com.simprints.infra.events.event.local.models.DbEventScopeKt")
        val dbSessionCaptureEvent = mockk<DbEventScope> {
            every { fromDbToDomain(any()) } returns mockk()
        }
        coEvery { scopeDao.loadOpen(EventScopeType.SESSION) } returns listOf(dbSessionCaptureEvent)
        eventLocalDataSource.loadOpenedScopes(EventScopeType.SESSION)

        coVerify { scopeDao.loadOpen(any()) }
        verify { dbSessionCaptureEvent.fromDbToDomain(any()) }
    }

    @Test
    fun loadClosedEventScope() = runTest {
        mockkStatic("com.simprints.infra.events.event.local.models.DbEventScopeKt")
        val dbSessionCaptureEvent = mockk<DbEventScope> {
            every { fromDbToDomain(any()) } returns mockk()
        }
        coEvery { scopeDao.loadClosed(EventScopeType.SESSION, limit = 10) } returns listOf(dbSessionCaptureEvent)
        eventLocalDataSource.loadClosedScopes(EventScopeType.SESSION, limit = 10)

        coVerify { scopeDao.loadClosed(EventScopeType.SESSION, limit = 10) }
        verify { dbSessionCaptureEvent.fromDbToDomain(any()) }
    }

    @Test
    fun loadEventScope() = runTest {
        mockkStatic("com.simprints.infra.events.event.local.models.DbEventScopeKt")
        val dbSessionCaptureEvent = mockk<DbEventScope> {
            every { fromDbToDomain(any()) } returns mockk()
        }
        coEvery { scopeDao.loadScope(GUID1) } returns dbSessionCaptureEvent
        eventLocalDataSource.loadEventScope(GUID1)

        coVerify { scopeDao.loadScope(any()) }
        verify { dbSessionCaptureEvent.fromDbToDomain(any()) }
    }

    @Test
    fun deleteEventScope() = runTest {
        eventLocalDataSource.deleteEventScope(GUID1)

        coVerify { scopeDao.delete(listOf(GUID1)) }
    }

    @Test
    fun observeCountWithAProjectIdQueryReturns() = runTest {
        coEvery { eventDao.observeCount() } returns flowOf(1, 2, 3)

        val count = eventLocalDataSource.observeEventCount().toList()

        assertThat(count).isEqualTo(listOf(1, 2, 3))
    }

    @Test
    fun observeCountWithAProjectIdQuery() = runTest {
        eventLocalDataSource.observeEventCount().toList()

        coVerify { eventDao.observeCount() }
    }

    @Test
    fun observeCountWithAProjectIdAndTypeQuery() = runTest {
        eventLocalDataSource.observeEventCount(
            type = CALLBACK_ENROLMENT,
        ).toList()

        coVerify {
            eventDao.observeCountFromType(type = CALLBACK_ENROLMENT)
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
            eventLocalDataSource.saveEvent(event)

            coVerify { eventDao.insertOrUpdate(dbEvent) }
        }
    }

    @Test
    fun deleteAllFromSession() = runTest {
        eventLocalDataSource.deleteEventsInScope(GUID1)

        coVerify { eventDao.deleteAllFromScope(GUID1) }
    }

    @Test
    fun deleteAll() = runTest {
        eventLocalDataSource.deleteAll()

        coVerify {
            eventDao.deleteAll()
            scopeDao.deleteAll()
        }
    }

    private fun mockDaoLoadToMakeNothing() {
        db = mockk(relaxed = true)
        eventDao = mockk(relaxed = true)
        scopeDao = mockk(relaxed = true)
        eventDatabaseFactory = mockk(relaxed = true)
        coEvery { eventDao.loadAll() } returns emptyList()
        coEvery { eventDao.loadFromScope(any()) } returns emptyList()
        coEvery { scopeDao.loadOpen(any()) } returns emptyList()
        coEvery { scopeDao.loadClosed(any(), any()) } returns emptyList()
        coEvery { scopeDao.count(any()) } returns 0
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
