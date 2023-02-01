package com.simprints.eventsystem.event.local

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.simprints.infra.security.SecurityManager
import com.simprints.infra.security.keyprovider.LocalDbKey
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class DbEventDatabaseFactoryImplTest {

    private val dbName = "dbevents"
    private val localDbKey = LocalDbKey(
        "project1",
        "byte".toByteArray()
    )
    private val context: Context = mockk()
    private val securityManager: SecurityManager = mockk()
    lateinit var dbEventDatabaseFactory: EventDatabaseFactory

    @Before
    fun setUp() {
        dbEventDatabaseFactory = DbEventDatabaseFactoryImpl(context, securityManager)
    }

    @Test
    fun `test build db success`() = runTest {
        //Given
        coEvery { securityManager.getLocalDbKeyOrThrow(dbName) } returns localDbKey
        mockkObject(EventRoomDatabase)
        val db: EventRoomDatabase = mockk()
        every { EventRoomDatabase.getDatabase(context, any(), dbName) } returns db
        //When
        Truth.assertThat(dbEventDatabaseFactory.build()).isEqualTo(db)
        verify { securityManager.getLocalDbKeyOrThrow(dbName) }

    }

    @Test
    fun `test build db creates key if not exist`() = runTest {
        //Given
        coEvery { securityManager.getLocalDbKeyOrThrow(dbName) } throws Exception() andThen localDbKey
        justRun { securityManager.createLocalDatabaseKeyIfMissing(dbName) }
        mockkObject(EventRoomDatabase)
        val db: EventRoomDatabase = mockk()
        every { EventRoomDatabase.getDatabase(context, any(), dbName) } returns db
        //When and Then
        Truth.assertThat(dbEventDatabaseFactory.build()).isEqualTo(db)
        verify(exactly = 2) { securityManager.getLocalDbKeyOrThrow(dbName) }

    }

    @Test(expected = Exception::class)
    fun `test build db falure`() = runTest {
        //Given
        coEvery { securityManager.getLocalDbKeyOrThrow(dbName) } throws Exception()
        justRun { securityManager.createLocalDatabaseKeyIfMissing(dbName) }
        mockkObject(EventRoomDatabase)
        val db: EventRoomDatabase = mockk()
        every { EventRoomDatabase.getDatabase(context, any(), dbName) } returns db
        //When calling build it should throw exception
        dbEventDatabaseFactory.build()

    }

    @Test
    fun deleteDatabase() {
        //When
        dbEventDatabaseFactory.deleteDatabase()

        //Then
        verify { context.deleteDatabase(dbName) }
    }

    @Test
    fun recreateDatabaseKey() {
        //Given
        justRun { securityManager.recreateLocalDatabaseKey(dbName) }
        //When
        dbEventDatabaseFactory.recreateDatabaseKey()
        //Then
        verify { securityManager.recreateLocalDatabaseKey(dbName) }
    }
}
