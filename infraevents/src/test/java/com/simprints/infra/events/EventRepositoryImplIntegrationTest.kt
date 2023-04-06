package com.simprints.infra.events

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.time.TimeHelper
import com.simprints.infra.events.domain.validators.SessionEventValidatorsFactory
import com.simprints.infra.events.event.domain.models.EventLabels
import com.simprints.infra.events.event.domain.models.EventType
import com.simprints.infra.events.event.local.*
import com.simprints.infra.events.event.local.models.DbEvent
import com.simprints.infra.events.event.local.models.fromDbToDomain
import com.simprints.infra.events.local.*
import com.simprints.infra.events.remote.EventRemoteDataSource
import com.simprints.infra.events.sampledata.SampleDefaults.CREATED_AT
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_DEVICE_ID
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.infra.events.sampledata.SampleDefaults.ENDED_AT
import com.simprints.infra.events.sampledata.SampleDefaults.GUID1
import com.simprints.infra.events.sampledata.SampleDefaults.GUID2
import com.simprints.infra.events.sampledata.SampleDefaults.GUID3
import com.simprints.infra.events.sampledata.SampleDefaults.TIME1
import com.simprints.infra.login.LoginManager
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class EventRepositoryImplIntegrationTest {

    @MockK
    lateinit var loginManager: LoginManager

    @MockK
    private lateinit var eventRemoteDataSource: EventRemoteDataSource

    @MockK
    lateinit var timeHelper: TimeHelper

    @MockK
    lateinit var sessionEventValidatorsFactory: SessionEventValidatorsFactory

    @MockK
    lateinit var sessionDataCache: SessionDataCache

    @MockK
    lateinit var eventDatabaseFactory: EventDatabaseFactory

    private lateinit var db: EventRoomDatabase
    private lateinit var eventDao: EventRoomDao
    private lateinit var eventLocalDataSource: EventLocalDataSource
    private lateinit var eventRepo: EventRepository

    private lateinit var eventSyncRepo: EventSyncRepository

    @Before
    fun setup() {
        MockKAnnotations.init(this)

        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, EventRoomDatabase::class.java)
            .allowMainThreadQueries().build()
        eventDao = db.eventDao
        every { timeHelper.now() } returns TIME1
        every { eventDatabaseFactory.build() } returns db
        every { loginManager.getSignedInProjectIdOrEmpty() } returns DEFAULT_PROJECT_ID

        eventLocalDataSource = EventLocalDataSourceImpl(
            eventDatabaseFactory,
            UnconfinedTestDispatcher(),
            UnconfinedTestDispatcher()
        )
        eventRepo = EventRepositoryImpl(
            DEFAULT_DEVICE_ID,
            "1",
            "1",
            loginManager,
            eventLocalDataSource,
            timeHelper,
            sessionEventValidatorsFactory,
            sessionDataCache,
            mockk(),
        )

        eventSyncRepo = EventSyncRepositoryImpl(
            loginManager,
            eventRepo,
            eventRemoteDataSource,
            timeHelper,
        )
    }

    @Test
    fun `upload should dump the session containing an invalid events and post the valid session`() =
        runTest {
            val invalidEventJson =
                "{\"id\":\"5b28d790-929d-455e-a7c9-7c6cc929275c\",\"labels\":{\"projectId\":\"${DEFAULT_PROJECT_ID}\",\"sessionId\":\"${GUID1}\",\"deviceId\":\"${DEFAULT_DEVICE_ID}\"},\"payload\":{\"id\":\"5b28d790-929d-455e-a7c9-7c6cc929275c\",\"createdAt\":1647511734063,\"endedAt\":1647511734257,\"type\":\"FACE_CAPTURE\",\"eventVersion\":2,\"attemptNb\":0,\"qualityThreshold\":-1.0,\"result\":\"VALID\",\"isFallback\":false,\"fac\u0000e\":{\"yaw\":1.2824339,\"roll\":11.53936,\"quality\":-0.060480837,\"template\":\"template\"}}"
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


            eventSyncRepo.uploadEvents(
                DEFAULT_PROJECT_ID,
                canSyncAllDataToSimprints = true,
                canSyncBiometricDataToSimprints = false,
                canSyncAnalyticsDataToSimprints = false
            ).toList()

            coVerify(exactly = 1) {
                eventRemoteDataSource.dumpInvalidEvents(
                    DEFAULT_PROJECT_ID,
                    listOf(invalidEventJson, sessionCaptureForInvalidEvent.eventJson)
                )
                eventRemoteDataSource.post(
                    DEFAULT_PROJECT_ID,
                    listOf(sessionCapture2.fromDbToDomain())
                )
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
        val eventJson =
            "{\"id\":\"${sessionId}\",\"type\":\"SESSION_CAPTURE\",\"labels\":{\"projectId\":\"${DEFAULT_PROJECT_ID}\",\"sessionId\":\"${sessionId}\",\"deviceId\":\"${DEFAULT_DEVICE_ID}\"},\"payload\":{\"type\":\"SESSION_CAPTURE\",\"eventVersion\":1,\"createdAt\":31213401430,\"endedAt\":21401438930,\"uploadedAt\":${TIME1},\"id\":\"${sessionId}\",\"projectId\":\"${DEFAULT_PROJECT_ID}\",\"modalities\":[\"FINGERPRINT\",\"FACE\"],\"appVersionName\":\"2022.2.0+1084\",\"libVersionName\":\"2022.2.0\",\"language\":\"en\",\"device\":{\"deviceId\":\"${DEFAULT_DEVICE_ID}\",\"androidSdkVersion\":\"31\",\"deviceModel\":\"Samsung\"},\"databaseInfo\":{\"sessionCount\":102,\"recordCount\":10},\"location\":{\"latitude\":42.24,\"longitude\":24.42}}}"
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
