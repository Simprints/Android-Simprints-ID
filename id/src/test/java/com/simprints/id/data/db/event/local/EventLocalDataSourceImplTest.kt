package com.simprints.id.data.db.event.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.id.data.db.event.domain.models.EventType.SESSION_CAPTURE
import com.simprints.id.testtools.TestApplication
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import io.mockk.*
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
        db = Room.inMemoryDatabaseBuilder(context, EventRoomDatabase::class.java).allowMainThreadQueries().build()

        eventDao = db.eventDao
        every { eventDatabaseFactory.build() } returns db
        mockDaoLoadToMakeNothing()
    }

    @Test
    fun loadWithAQuery() {
        runBlocking {

            eventLocalDataSource.loadAll()

            coVerify {
                eventDao.loadAll()
            }
        }
    }

    @Test
    fun countWithAQuery() {
        runBlocking {
            eventLocalDataSource.count(SESSION_CAPTURE)

            coVerify {
                eventDao.countFromType(type = SESSION_CAPTURE)
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
        eventLocalDataSource = EventLocalDataSourceImpl(eventDatabaseFactory)
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

}
