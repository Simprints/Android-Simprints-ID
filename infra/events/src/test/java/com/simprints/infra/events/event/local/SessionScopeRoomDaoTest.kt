package com.simprints.infra.events.event.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.events.event.domain.models.scope.EventScopeType
import com.simprints.infra.events.event.local.models.fromDomainToDb
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.infra.events.sampledata.SampleDefaults.GUID1
import com.simprints.infra.events.sampledata.SampleDefaults.GUID2
import com.simprints.infra.events.sampledata.SampleDefaults.GUID3
import com.simprints.infra.events.sampledata.createSessionScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
internal class SessionScopeRoomDaoTest {
    private lateinit var db: EventRoomDatabase
    private lateinit var scopeDao: SessionScopeRoomDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room
            .inMemoryDatabaseBuilder(context, EventRoomDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        scopeDao = db.scopeDao
    }

    @After
    @Throws(IOException::class)
    fun tearDown() {
        db.close()
    }

    @Test
    fun `loadClosed returns scopes in FIFO order`() = runTest {
        val oldestScope = createSessionScope(
            id = GUID1,
            createdAt = Timestamp(1000L),
            projectId = DEFAULT_PROJECT_ID,
            isClosed = true,
        ).fromDomainToDb()
        val middleScope = createSessionScope(
            id = GUID2,
            createdAt = Timestamp(2000L),
            projectId = DEFAULT_PROJECT_ID,
            isClosed = true,
        ).fromDomainToDb()
        val newestScope = createSessionScope(
            id = GUID3,
            createdAt = Timestamp(3000L),
            projectId = DEFAULT_PROJECT_ID,
            isClosed = true,
        ).fromDomainToDb()
        // Insert in reverse order to ensure ordering isn't insertion-dependent
        scopeDao.insertOrUpdate(newestScope)
        scopeDao.insertOrUpdate(middleScope)
        scopeDao.insertOrUpdate(oldestScope)

        val result = scopeDao.loadClosed(EventScopeType.SESSION, limit = 10)

        assertThat(result.map { it.id }).isEqualTo(listOf(GUID1, GUID2, GUID3))
    }

    @Test
    fun `loadClosed respects the limit parameter`() = runTest {
        val oldestScope = createSessionScope(
            id = GUID1,
            createdAt = Timestamp(1000L),
            projectId = DEFAULT_PROJECT_ID,
            isClosed = true,
        ).fromDomainToDb()
        val middleScope = createSessionScope(
            id = GUID2,
            createdAt = Timestamp(2000L),
            projectId = DEFAULT_PROJECT_ID,
            isClosed = true,
        ).fromDomainToDb()
        val newestScope = createSessionScope(
            id = GUID3,
            createdAt = Timestamp(3000L),
            projectId = DEFAULT_PROJECT_ID,
            isClosed = true,
        ).fromDomainToDb()
        scopeDao.insertOrUpdate(oldestScope)
        scopeDao.insertOrUpdate(middleScope)
        scopeDao.insertOrUpdate(newestScope)

        val result = scopeDao.loadClosed(EventScopeType.SESSION, limit = 2)

        // With FIFO ordering, the oldest 2 are returned
        assertThat(result.map { it.id }).isEqualTo(listOf(GUID1, GUID2))
    }

    @Test
    fun `observeClosedCountsByType returns no rows when there are no scopes`() = runTest {
        val result = scopeDao.observeClosedCountsByType().first()

        assertThat(result).isEmpty()
    }

    @Test
    fun `observeClosedCountsByType counts closed scopes of every type`() = runTest {
        scopeDao.insertOrUpdate(scope(GUID1, EventScopeType.SESSION, isClosed = false))
        scopeDao.insertOrUpdate(scope(GUID2, EventScopeType.SESSION, isClosed = true))
        scopeDao.insertOrUpdate(scope(GUID3, EventScopeType.UP_SYNC, isClosed = true))

        val result = scopeDao.observeClosedCountsByType().first()

        // Only types that are actually present appear here - the data source zero-fills the rest.
        assertThat(result.associate { it.type to it.count }).containsExactlyEntriesIn(
            mapOf(
                EventScopeType.SESSION to 1,
                EventScopeType.UP_SYNC to 1,
            ),
        )
    }

    @Test
    fun `observeClosedCountsByType re-emits after an insert and after a delete`() = runTest {
        scopeDao.observeClosedCountsByType().test {
            assertThat(awaitItem()).isEmpty()

            scopeDao.insertOrUpdate(scope(GUID1, EventScopeType.SESSION, isClosed = true))
            assertThat(awaitItem().associate { it.type to it.count })
                .isEqualTo(mapOf(EventScopeType.SESSION to 1))

            scopeDao.delete(listOf(GUID1))
            assertThat(awaitItem()).isEmpty()

            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun scope(
        id: String,
        type: EventScopeType,
        isClosed: Boolean = false,
    ) = createSessionScope(id = id, projectId = DEFAULT_PROJECT_ID, isClosed = isClosed)
        .copy(type = type)
        .fromDomainToDb()
}
