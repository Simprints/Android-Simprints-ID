package com.simprints.id.data.db.event.local

import android.content.Context
import android.os.Build
import android.os.Build.VERSION
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.simprints.id.data.db.event.EventRepositoryImpl
import com.simprints.id.data.db.event.domain.models.session.DatabaseInfo
import com.simprints.id.data.db.event.domain.models.session.Device
import com.simprints.id.data.db.event.domain.models.session.SessionCaptureEvent
import com.simprints.id.data.db.event.local.models.fromDbToDomain
import com.simprints.id.data.db.event.local.models.fromDomainToDb
import com.simprints.id.orchestrator.SOME_GUID
import com.simprints.id.tools.TimeHelper
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.util.*

@RunWith(AndroidJUnit4::class)
class SessionLocalDataSourceImplTest {

    private lateinit var db: EventRoomDatabase
    private lateinit var eventDao: EventRoomDao
    private lateinit var eventLocalDataSource: SessionLocalDataSource

    @RelaxedMockK lateinit var timeHelper: TimeHelper

    @Before
    fun setup() {
        MockKAnnotations.init(this)

        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, EventRoomDatabase::class.java).build()
        eventDao = db.eventDao
        eventLocalDataSource = SessionLocalDataSourceImpl(context, mockk(), timeHelper, eventDao, emptyArray())
    }

    @Test
    fun create_session() {
        runBlocking {
            val sessionId = eventLocalDataSource.create(APP_VERSION_NAME, LIB_VERSION_NAME, LANGUAGE, DEVICE_ID)
            val deviceId = randomGuid
            eventDao.insertOrUpdate(
                SessionCaptureEvent(
                    timeHelper.now(),
                    UUID.randomUUID().toString(),
                    EventRepositoryImpl.PROJECT_ID_FOR_NOT_SIGNED_IN,
                    APP_VERSION_NAME,
                    LIB_VERSION_NAME,
                    LANGUAGE,
                    Device(
                        VERSION.SDK_INT.toString(),
                        Build.MANUFACTURER + "_" + Build.MODEL,
                        deviceId,
                        SOME_GUID),
                    DatabaseInfo(0)).fromDomainToDb())

            val sessionEvent = eventDao.load().first()
            val sessionPayload = (sessionEvent.fromDbToDomain() as SessionCaptureEvent).payload
            Truth.assertThat(sessionPayload.appVersionName).isEqualTo(APP_VERSION_NAME)
            Truth.assertThat(sessionPayload.libVersionName).isEqualTo(LIB_VERSION_NAME)
            Truth.assertThat(sessionPayload.language).isEqualTo(LANGUAGE)
            Truth.assertThat(sessionPayload.device.deviceId).isEqualTo(deviceId)
        }
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    companion object {
        const val APP_VERSION_NAME = "APP_VERSION_NAME"
        const val LIB_VERSION_NAME = "LIB_VERSION_NAME"
        const val DEVICE_ID = "DEVICE_ID"
        const val LANGUAGE = "LANGUAGE"
        val randomGuid
            get() = UUID.randomUUID().toString()
    }
}
