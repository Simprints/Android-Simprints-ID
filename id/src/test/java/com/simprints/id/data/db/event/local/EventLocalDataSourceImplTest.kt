package com.simprints.id.data.db.event.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.core.tools.utils.randomUUID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_USER_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_USER_ID_2
import com.simprints.id.commontesttools.DefaultTestConstants.GUID1
import com.simprints.id.commontesttools.DefaultTestConstants.GUID2
import com.simprints.id.commontesttools.events.CREATED_AT_RANGE
import com.simprints.id.commontesttools.events.ENDED_AT_RANGE
import com.simprints.id.data.db.event.domain.models.EventType.SESSION_CAPTURE
import com.simprints.id.data.db.event.local.models.DbEventQuery
import io.mockk.*
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.runBlocking
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

    @RelaxedMockK lateinit var eventDatabaseFactory: EventDatabaseFactory

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
            val eventQuery = createCompleteEventQuery()

            eventLocalDataSource.load(eventQuery)

            coVerify {
                eventDao.load(ID, SESSION_CAPTURE, DEFAULT_PROJECT_ID, DEFAULT_USER_ID, DEFAULT_USER_ID_2, GUID1, GUID2,
                    CREATED_AT_RANGE.first, CREATED_AT_RANGE.last, ENDED_AT_RANGE.first, ENDED_AT_RANGE.last)
            }
        }
    }

    @Test
    fun countWithAQuery() {
        runBlocking {
            val eventQuery = createCompleteEventQuery()

            eventLocalDataSource.count(eventQuery)

            coVerify {
                eventDao.count(ID, SESSION_CAPTURE, DEFAULT_PROJECT_ID, DEFAULT_USER_ID, DEFAULT_USER_ID_2, GUID1, GUID2,
                    CREATED_AT_RANGE.first, CREATED_AT_RANGE.last, ENDED_AT_RANGE.first, ENDED_AT_RANGE.last)
            }
        }
    }

    @Test
    fun deleteWithAQuery() {
        runBlocking {
            val eventQuery = createCompleteEventQuery()

            eventLocalDataSource.delete(eventQuery)

            coVerify {
                eventDao.delete(ID, SESSION_CAPTURE, DEFAULT_PROJECT_ID, DEFAULT_USER_ID, DEFAULT_USER_ID_2, GUID1, GUID2,
                    CREATED_AT_RANGE.first, CREATED_AT_RANGE.last, ENDED_AT_RANGE.first, ENDED_AT_RANGE.last)
            }
        }
    }

    private fun createCompleteEventQuery() =
        DbEventQuery(
            ID,
            SESSION_CAPTURE,
            DEFAULT_PROJECT_ID,
            DEFAULT_USER_ID,
            DEFAULT_USER_ID_2,
            GUID1,
            GUID2,
            CREATED_AT_RANGE,
            ENDED_AT_RANGE
        )

    private fun mockDaoLoadToMakeNothing() {
        db = mockk(relaxed = true)
        eventDao = mockk(relaxed = true)
        eventDatabaseFactory = mockk(relaxed = true)
        coEvery { eventDao.load(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) } returns emptyList()
        coEvery { eventDao.count(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) } returns 0
        every { db.eventDao } returns eventDao
        every { eventDatabaseFactory.build() } returns db
        eventLocalDataSource = EventLocalDataSourceImpl(eventDatabaseFactory)
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    companion object {
        private val ID = randomUUID()
    }
}
