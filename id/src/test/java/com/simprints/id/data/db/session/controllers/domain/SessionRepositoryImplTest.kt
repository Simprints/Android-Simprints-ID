package com.simprints.id.data.db.session.controllers.domain

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.id.commontesttools.sessionEvents.createFakeClosedSession
import com.simprints.id.commontesttools.sessionEvents.createFakeOpenSession
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.db.session.SessionRepository
import com.simprints.id.data.db.session.SessionRepositoryImpl
import com.simprints.id.data.db.session.domain.models.SessionQuery
import com.simprints.id.data.db.session.domain.models.events.Event
import com.simprints.id.data.db.session.domain.models.session.SessionEvents
import com.simprints.id.data.db.session.local.SessionLocalDataSource
import com.simprints.id.data.db.session.remote.SessionRemoteDataSource
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.services.scheduledSync.sessionSync.SessionEventsSyncManager
import com.simprints.id.testtools.TestApplication
import com.simprints.id.tools.TimeHelperImpl
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import io.kotlintest.shouldThrow
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class SessionRepositoryImplTest {

    @MockK private lateinit var sessionEventsSyncManagerMock: SessionEventsSyncManager
    @MockK private lateinit var sessionLocalDataSourceMock: SessionLocalDataSource
    @MockK private lateinit var sessionRemoteDataSourceMock: SessionRemoteDataSource
    @MockK private lateinit var preferencesManagerMock: PreferencesManager
    @MockK private lateinit var crashReportManagerMock: CrashReportManager
    private lateinit var sessionsRepository: SessionRepository
    private val timeHelper = TimeHelperImpl()

    @Before
    fun setUp() {
        ShadowLog.stream = System.out
        MockKAnnotations.init(this, relaxed = true)

        sessionsRepository = SessionRepositoryImpl(
            DEVICE_ID,
            APP_VERSION_NAME,
            PROJECT_ID,
            sessionEventsSyncManagerMock, sessionLocalDataSourceMock, sessionRemoteDataSourceMock,
            preferencesManagerMock, crashReportManagerMock)
        mockPreferenceManagerInfo()
    }

    private fun mockPreferenceManagerInfo() {
        every { preferencesManagerMock.language } returns LANGUAGE
    }

    @Test
    fun createSession_shouldCreateASession() {
        runBlocking {
            sessionsRepository.createSession(LIB_VERSION_NAME)

            coVerify(exactly = 1) { sessionLocalDataSourceMock.create(APP_VERSION_NAME, LIB_VERSION_NAME, LANGUAGE, DEVICE_ID) }
            coVerify(exactly = 1) { preferencesManagerMock.language }
        }
    }

    @Test
    fun createSession_shouldReportExceptionAndThrow() {
        runBlockingTest {
            every { preferencesManagerMock.language } throws Throwable("Error")

            shouldThrow<Throwable> {
                sessionsRepository.createSession("")
            }

            coVerify(exactly = 1) { crashReportManagerMock.logExceptionOrSafeException(any()) }
        }
    }

    @Test
    fun getCurrentSession_shouldReturnCurrentSession() {
        runBlockingTest {
            val session = createFakeOpenSession(timeHelper)
            coEvery { sessionLocalDataSourceMock.load(any()) } returns flowOf(session)

            val currentSession = sessionsRepository.getCurrentSession()

            assertThat(currentSession).isEqualTo(session)
            coVerify(exactly = 1) { sessionLocalDataSourceMock.load(SessionQuery(openSession = true)) }
        }
    }

    @Test
    fun getCurrentSession_shouldReportExceptionAndThrow() {
        runBlockingTest {
            coEvery { sessionLocalDataSourceMock.updateCurrentSession(any()) } throws Throwable("Error")
            shouldThrow<Throwable> {
                sessionsRepository.getCurrentSession()
            }
            coVerify(exactly = 1) { crashReportManagerMock.logExceptionOrSafeException(any()) }
        }
    }

    @Test
    fun updateCurrentSession_shouldUpdateCurrentSession() {
        runBlockingTest {
            val block: (SessionEvents) -> Unit = {}
            sessionsRepository.updateCurrentSession(block)
            coVerify(exactly = 1) { sessionLocalDataSourceMock.updateCurrentSession(block) }
        }
    }

    @Test
    fun updateCurrentSession_shouldReportExceptionAndThrow() {
        runBlockingTest {
            coEvery { sessionLocalDataSourceMock.updateCurrentSession(any()) } throws Throwable("Error")
            shouldThrow<Throwable> {
                sessionsRepository.updateCurrentSession { }
            }
            coVerify(exactly = 1) { crashReportManagerMock.logExceptionOrSafeException(any()) }
        }
    }

    @Test
    fun addEventToCurrentSessionInBackground_shouldAddEventIntoCurrentSession() {
        runBlockingTest {
            val event = mockk<Event>()
            sessionsRepository.addEventToCurrentSessionInBackground(event)
            coVerify(exactly = 1) { sessionLocalDataSourceMock.addEventToCurrentSession(event) }
        }
    }

    @Test
    fun addEventToCurrentSessionInBackground_shouldReportException() {
        runBlockingTest {
            coEvery { sessionLocalDataSourceMock.addEventToCurrentSession(any()) } throws Throwable("Error")
            sessionsRepository.addEventToCurrentSessionInBackground(mockk())

            coVerify(exactly = 1) { crashReportManagerMock.logExceptionOrSafeException(any()) }
        }
    }


    @Test
    fun signOut_shouldDeleteSessionsAndStopWorkers() {
        runBlockingTest {
            sessionsRepository.signOut()

            coVerify(exactly = 1) { sessionLocalDataSourceMock.delete(SessionQuery(openSession = false)) }
            coVerify(exactly = 1) { sessionEventsSyncManagerMock.cancelSyncWorkers() }
        }
    }

    @Test
    fun uploadSessions_shouldDeleteAfterUploading() {
        runBlockingTest {
            coEvery { sessionLocalDataSourceMock.load(any()) } returns flowOf(createFakeClosedSession(timeHelper))

            sessionsRepository.startUploadingSessions()

            coVerify(exactly = 1) { sessionRemoteDataSourceMock.uploadSessions(PROJECT_ID, any()) }
            coVerify(exactly = 1) { sessionLocalDataSourceMock.delete(any()) }
        }
    }


    companion object {
        private const val DEVICE_ID = "deviceId"
        private const val APP_VERSION_NAME = "v1"
        private const val LIB_VERSION_NAME = "v1"
        private const val LANGUAGE = "en"
        private const val PROJECT_ID = "projectId"
    }
}
