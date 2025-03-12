package com.simprints.infra.events.event.local

import android.content.Context
import com.google.common.truth.Truth
import com.simprints.infra.security.SecurityManager
import com.simprints.infra.security.keyprovider.LocalDbKey
import io.mockk.coEvery
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

internal class EventDatabaseFactoryTest {
    private val dbName = "dbevents"
    private val localDbKey = LocalDbKey(
        "project1",
        "byte".toByteArray(),
    )
    private val context: Context = mockk()
    private val securityManager: SecurityManager = mockk()

    lateinit var dbEventDatabaseFactory: EventDatabaseFactory

    @Before
    fun setUp() {
        dbEventDatabaseFactory = EventDatabaseFactory(context, securityManager)
    }

    @Test
    fun `test get db success`() = runTest {
        // Given
        coEvery { securityManager.getLocalDbKeyOrThrow(dbName) } returns localDbKey
        mockkObject(EventRoomDatabase)
        val db: EventRoomDatabase = mockk()
        every { EventRoomDatabase.getDatabase(context, any(), dbName) } returns db
        // When
        Truth.assertThat(dbEventDatabaseFactory.get()).isEqualTo(db)
        verify { securityManager.getLocalDbKeyOrThrow(dbName) }
    }

    @Test
    fun `get should return the same db instance on multiple calls`() = runTest {
        // Given
        coEvery { securityManager.getLocalDbKeyOrThrow(dbName) } returns localDbKey
        mockkObject(EventRoomDatabase)
        every { EventRoomDatabase.getDatabase(context, any(), dbName) } returns mockk()
        // When and Then
        val db1 = dbEventDatabaseFactory.get()
        val db2 = dbEventDatabaseFactory.get()
        Truth.assertThat(db1).isSameInstanceAs(db2)
        // Verify that getLocalDbKeyOrThrow is called only once
        verify(exactly = 1) {
            securityManager.getLocalDbKeyOrThrow(dbName)
            EventRoomDatabase.getDatabase(context, any(), dbName)
        }
    }

    @Test
    fun `test get db creates key if not exist`() = runTest {
        // Given
        coEvery { securityManager.getLocalDbKeyOrThrow(dbName) } throws Exception() andThen localDbKey
        justRun { securityManager.createLocalDatabaseKeyIfMissing(dbName) }
        mockkObject(EventRoomDatabase)
        val db: EventRoomDatabase = mockk()
        every { EventRoomDatabase.getDatabase(context, any(), dbName) } returns db
        // When and Then
        Truth.assertThat(dbEventDatabaseFactory.get()).isEqualTo(db)
        verify(exactly = 2) { securityManager.getLocalDbKeyOrThrow(dbName) }
    }

    @Test(expected = Exception::class)
    fun `test get db falure`() = runTest {
        // Given
        coEvery { securityManager.getLocalDbKeyOrThrow(dbName) } throws Exception()
        justRun { securityManager.createLocalDatabaseKeyIfMissing(dbName) }
        mockkObject(EventRoomDatabase)
        val db: EventRoomDatabase = mockk()
        every { EventRoomDatabase.getDatabase(context, any(), dbName) } returns db
        // When calling build it should throw exception
        dbEventDatabaseFactory.get()
    }

    @Test
    fun recreateDatabase() {
        // Given
        justRun { securityManager.recreateLocalDatabaseKey(dbName) }
        coEvery { securityManager.getLocalDbKeyOrThrow(dbName) } returns localDbKey
        justRun { securityManager.createLocalDatabaseKeyIfMissing(dbName) }
        mockkObject(EventRoomDatabase)
        val db: EventRoomDatabase = mockk()
        every { EventRoomDatabase.getDatabase(context, any(), dbName) } returns db
        // When
        dbEventDatabaseFactory.recreateDatabase()
        // Then
        verify { context.deleteDatabase(dbName) }
        verify { securityManager.recreateLocalDatabaseKey(dbName) }
    }
}
