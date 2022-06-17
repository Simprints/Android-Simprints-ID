package com.simprints.eventsystem.event

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.modality.Modes
import com.simprints.core.login.LoginInfoManager
import com.simprints.core.tools.time.TimeHelper
import com.simprints.eventsystem.event.domain.models.EventLabels
import com.simprints.eventsystem.event.domain.models.EventType
import com.simprints.eventsystem.event.domain.validators.SessionEventValidatorsFactory
import com.simprints.eventsystem.event.local.*
import com.simprints.eventsystem.event.local.models.DbEvent
import com.simprints.eventsystem.event.local.models.fromDbToDomain
import com.simprints.eventsystem.event.remote.EventRemoteDataSource
import com.simprints.eventsystem.sampledata.SampleDefaults.CREATED_AT
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_DEVICE_ID
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.eventsystem.sampledata.SampleDefaults.ENDED_AT
import com.simprints.eventsystem.sampledata.SampleDefaults.GUID1
import com.simprints.eventsystem.sampledata.SampleDefaults.GUID2
import com.simprints.eventsystem.sampledata.SampleDefaults.GUID3
import com.simprints.eventsystem.sampledata.SampleDefaults.TIME1
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class EventRepositoryImplIntegrationTest {

    private lateinit var eventRepo: EventRepository

    @MockK
    lateinit var loginInfoManager: LoginInfoManager

    @MockK
    lateinit var eventRemoteDataSource: EventRemoteDataSource

    @MockK
    lateinit var timeHelper: TimeHelper

    @MockK
    lateinit var sessionEventValidatorsFactory: SessionEventValidatorsFactory

    @MockK
    lateinit var sessionDataCache: SessionDataCache

    private lateinit var db: EventRoomDatabase
    private lateinit var eventDao: EventRoomDao
    private lateinit var eventLocalDataSource: EventLocalDataSource

    @MockK
    lateinit var eventDatabaseFactory: EventDatabaseFactory

    @Before
    fun setup() {
        MockKAnnotations.init(this)

        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, EventRoomDatabase::class.java).allowMainThreadQueries().build()
        eventDao = db.eventDao
        eventLocalDataSource = EventLocalDataSourceImpl(eventDatabaseFactory)
        eventRepo = EventRepositoryImpl(
            "",
            "",
            loginInfoManager,
            eventLocalDataSource,
            eventRemoteDataSource,
            timeHelper,
            sessionEventValidatorsFactory,
            "",
            sessionDataCache,
            "",
            listOf(Modes.FACE, Modes.FINGERPRINT)
        )

        every { timeHelper.now() } returns TIME1
        every { eventDatabaseFactory.build() } returns db
        every { loginInfoManager.getSignedInProjectIdOrEmpty() } returns DEFAULT_PROJECT_ID
    }

    @Test
    fun `upload should dump the session containing an invalid events and post the valid session`() = runTest {
        val invalidEventJson = "{\"id\":\"5b28d790-929d-455e-a7c9-7c6cc929275c\",\"labels\":{\"projectId\":\"${DEFAULT_PROJECT_ID}\",\"sessionId\":\"${GUID1}\",\"deviceId\":\"${DEFAULT_DEVICE_ID}\"},\"payload\":{\"id\":\"5b28d790-929d-455e-a7c9-7c6cc929275c\",\"createdAt\":1647511734063,\"endedAt\":1647511734257,\"type\":\"FACE_CAPTURE\",\"eventVersion\":2,\"attemptNb\":0,\"qualityThreshold\":-1.0,\"result\":\"VALID\",\"isFallback\":false,\"fac\u0000e\":{\"yaw\":1.2824339,\"roll\":11.53936,\"quality\":-0.060480837,\"template\":\"template\"}}"
        val invalidDbEvent = DbEvent(
            GUID3,
            EventLabels(
                projectId = DEFAULT_PROJECT_ID,
                sessionId = GUID1,
                deviceId = DEFAULT_DEVICE_ID
            ),
            EventType.FACE_CAPTURE, invalidEventJson, CREATED_AT, ENDED_AT, false
        )
        val sessionCaptureForInvalidEvent = generateSessionCapture(GUID1)
        val sessionCapture2 = generateSessionCapture(GUID2)
        // Adding two sessions one with an invalid event + session capture and one with only a session capture.
        addIntoDb(invalidDbEvent, sessionCaptureForInvalidEvent, sessionCapture2)

        eventRepo.uploadEvents(DEFAULT_PROJECT_ID).toList()

        coVerify(exactly = 1) {
            eventRemoteDataSource.dumpInvalidEvents(DEFAULT_PROJECT_ID, listOf(invalidEventJson, sessionCaptureForInvalidEvent.eventJson))
            eventRemoteDataSource.post(DEFAULT_PROJECT_ID, listOf(sessionCapture2.fromDbToDomain()))
        }

        // The events have been deleted
        assertThat(eventDao.loadAll()).isEqualTo(listOf<DbEvent>())
    }

    private suspend fun addIntoDb(vararg events: DbEvent) {
        events.forEach {
            eventDao.insertOrUpdate(it)
        }
    }

    private fun generateSessionCapture(sessionId: String): DbEvent {
        val eventJson = "{\"id\":\"${sessionId}\",\"type\":\"SESSION_CAPTURE\",\"labels\":{\"projectId\":\"${DEFAULT_PROJECT_ID}\",\"sessionId\":\"${sessionId}\",\"deviceId\":\"${DEFAULT_DEVICE_ID}\"},\"payload\":{\"type\":\"SESSION_CAPTURE\",\"eventVersion\":1,\"createdAt\":31213401430,\"endedAt\":21401438930,\"uploadedAt\":${TIME1},\"id\":\"${sessionId}\",\"projectId\":\"${DEFAULT_PROJECT_ID}\",\"modalities\":[\"FINGERPRINT\",\"FACE\"],\"appVersionName\":\"2022.2.0+1084\",\"libVersionName\":\"2022.2.0\",\"language\":\"en\",\"device\":{\"deviceId\":\"${DEFAULT_DEVICE_ID}\",\"androidSdkVersion\":\"31\",\"deviceModel\":\"Samsung\"},\"databaseInfo\":{\"sessionCount\":102,\"recordCount\":10},\"location\":{\"latitude\":42.24,\"longitude\":24.42}}}"
        return DbEvent(
            sessionId,
            EventLabels(
                projectId = DEFAULT_PROJECT_ID,
                sessionId = sessionId,
                deviceId = DEFAULT_DEVICE_ID
            ),
            EventType.SESSION_CAPTURE, eventJson, CREATED_AT, ENDED_AT, true
        )
    }
}
