package com.simprints.id.data.db.event.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.id.data.db.event.local.models.createSessionCaptureEvent
import com.simprints.id.data.db.event.local.models.fromDbToDomain
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.tools.TimeHelper
import io.mockk.MockKAnnotations
import io.mockk.every
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
    private lateinit var eventDao: DbEventRoomDao
    private lateinit var eventLocalDataSource: EventLocalDataSource

    @RelaxedMockK lateinit var timeHelper: TimeHelper
    @RelaxedMockK lateinit var dbEventDatabaseFactory: DbEventDatabaseFactory
    @RelaxedMockK lateinit var loginInfoManager: LoginInfoManager

    @Before
    fun setup() {
        MockKAnnotations.init(this)

        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, EventRoomDatabase::class.java).build()
        eventDao = db.eventDao
        every { dbEventDatabaseFactory.build() } returns db
        every { timeHelper.now() } returns NOW
        eventLocalDataSource = EventLocalDataSourceImpl(dbEventDatabaseFactory, loginInfoManager, DEVICE_ID, timeHelper, emptyArray())
    }

    @Test
    fun createSession_shouldCloseOpenSessions() {
        runBlocking {
            val openSession = createSessionCaptureEvent().let {
                it.copy(payload = it.payload.copy(endedAt = 0))
            }
            val newSession = createSessionCaptureEvent()

            eventLocalDataSource.create(newSession)

            val eventStored = eventDao.load(sessionId = openSession.id).firstOrNull()?.fromDbToDomain()
            assertThat(eventStored.payload.endedAt).isNotEqualTo(0)
        }
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    companion object {
        const val DEVICE_ID = "DEVICE_ID"
        const val NOW = 1000L
    }
}
