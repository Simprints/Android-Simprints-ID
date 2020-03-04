package com.simprints.id.data.db.session.controllers.domain

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.id.commontesttools.sessionEvents.createFakeOpenSession
import com.simprints.id.commontesttools.state.mockSessionEventsManager
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.db.session.SessionRepository
import com.simprints.id.data.db.session.SessionRepositoryImpl
import com.simprints.id.data.db.session.domain.models.SessionQuery
import com.simprints.id.data.db.session.domain.models.events.ArtificialTerminationEvent
import com.simprints.id.data.db.session.domain.models.events.callback.IdentificationCallbackEvent
import com.simprints.id.data.db.session.domain.models.session.SessionEvents
import com.simprints.id.data.db.session.local.SessionLocalDataSource
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.exceptions.safe.session.SessionDataSourceException
import com.simprints.id.services.scheduledSync.sessionSync.SessionEventsSyncManager
import com.simprints.id.testtools.TestApplication
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.TimeHelperImpl
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import io.kotlintest.shouldThrow
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class SessionRepositoryImplTest {

    val timeHelper: TimeHelper = TimeHelperImpl()
    @MockK private lateinit var sessionEventsSyncManagerMock: SessionEventsSyncManager
    @MockK private lateinit var sessionLocalDataSourceMock: SessionLocalDataSource
    @MockK private lateinit var preferencesManagerMock: PreferencesManager
    @MockK private lateinit var crashReportManagerMock: CrashReportManager
    private lateinit var sessionsRepositorySpy: SessionRepository

    private var sessionsInFakeDb = mutableListOf<SessionEvents>()

    @Before
    fun setUp() {
        ShadowLog.stream = System.out
        MockKAnnotations.init(this, relaxed = true)

        sessionsRepositorySpy = spyk(SessionRepositoryImpl(
            "deviceID",
            "com.simprints.id",
            sessionEventsSyncManagerMock, sessionLocalDataSourceMock, preferencesManagerMock, timeHelper, crashReportManagerMock))

        sessionsInFakeDb.clear()
        mockSessionEventsManager(sessionLocalDataSourceMock, sessionsInFakeDb)
        mockPreferenceManagerInfo()
    }

    private fun mockPreferenceManagerInfo() {
        every { preferencesManagerMock.language } returns "language"
    }

    @Test
    fun signOut_shouldDeleteSessionsAndStopWorkers() {
        runBlockingTest {
            sessionsRepositorySpy.signOut()

            coVerify(exactly = 1) { sessionLocalDataSourceMock.delete(SessionQuery(openSession = false)) }
            coVerify(exactly = 1) { sessionEventsSyncManagerMock.cancelSyncWorkers() }
        }
    }

    @Test
    fun createSession_shouldCreateASession() {
        runBlockingTest {
            sessionsRepositorySpy.createSession("")

            assertThat(sessionsInFakeDb.size).isEqualTo(1)
            val createdSession = sessionsInFakeDb.first()
            assertNotNull(createdSession)
            assertThat(createdSession.isOpen()).isTrue()
        }
    }

    @Test
    fun createSession_shouldCloseAnyOpenSession() {
        runBlockingTest {
            val openSession = createFakeOpenSession(timeHelper, "projectId", "old_session_id")
            sessionsInFakeDb.add(openSession)
            assertThat(openSession.isOpen()).isTrue()

            sessionsRepositorySpy.createSession("")

            assertThat(sessionsInFakeDb.size).isEqualTo(2)
            val newCreatedSession = sessionsInFakeDb.find { it.id != "old_session_id" }
            assertThat(newCreatedSession?.isOpen()).isTrue()
            assertThat(newCreatedSession?.events).hasSize(0)
            val oldOpenSession = sessionsInFakeDb.find { it.id == "old_session_id" }
            assertThat(oldOpenSession?.isClosed()).isTrue()
            assertThat(oldOpenSession?.events?.filterIsInstance(ArtificialTerminationEvent::class.java)).hasSize(1)

            verify(exactly = 1) { sessionEventsSyncManagerMock.scheduleSessionsSync() }
        }
    }

    @Test
    fun closeLastSessionsIfPending_shouldSwallowException() {
        runBlockingTest {
            coEvery { sessionLocalDataSourceMock.load(any()) } throws Throwable("error_reading_db")

            sessionsRepositorySpy.createSession("")

            verify(exactly = 1) { crashReportManagerMock.logExceptionOrSafeException(any()) }
            assertThat(sessionsInFakeDb.size).isEqualTo(1)
        }
    }

    @Test
    fun updateSession_shouldUpdateSession() {
        runBlockingTest {
            sessionsRepositorySpy.createSession("")
            sessionsRepositorySpy.updateCurrentSession {
                it.projectId = "new_project"
            }

            assertThat(sessionsInFakeDb.size).isEqualTo(1)
            assertThat(sessionsInFakeDb.first().projectId).isEqualTo("new_project")
        }
    }

    @Test
    fun updateSession_shouldSwallowException() {
        runBlockingTest {
            sessionsRepositorySpy.updateCurrentSession { it.projectId = "new_project" }
            verify(exactly = 1) { crashReportManagerMock.logExceptionOrSafeException(any()) }
        }
    }

    @Test
    fun getCurrentSessionWithNoOpenSession_shouldThrowException() {
        runBlockingTest {
            shouldThrow<SessionDataSourceException> {
                sessionsRepositorySpy.getCurrentSession()
            }
        }
    }

    @Test
    fun addGuidEventForIdentificationSession_shouldAddBeAdded() {
        runBlockingTest {
            sessionsInFakeDb.add(createFakeOpenSession(timeHelper = timeHelper, id = "some_session_id").apply {
                this.events.add(IdentificationCallbackEvent(0, "some_session_id", emptyList()))
            })

            sessionsRepositorySpy.addGuidSelectionEvent("selected_guid", "some_session_id")
        }
    }

}
