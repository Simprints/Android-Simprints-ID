package com.simprints.id.data.analytics.eventdata.controllers.domain

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.id.commontesttools.sessionEvents.createFakeClosedSession
import com.simprints.id.commontesttools.sessionEvents.createFakeOpenSession
import com.simprints.id.commontesttools.sessionEvents.createFakeSession
import com.simprints.id.commontesttools.state.mockSessionEventsManager
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.eventdata.controllers.local.SessionEventsLocalDbManager
import com.simprints.id.data.analytics.eventdata.models.domain.events.ArtificialTerminationEvent
import com.simprints.id.data.analytics.eventdata.models.domain.events.callback.IdentificationCallbackEvent
import com.simprints.id.data.analytics.eventdata.models.domain.session.SessionEvents
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.exceptions.unexpected.InvalidSessionForGuidSelectionEvent
import com.simprints.id.services.scheduledSync.sessionSync.SessionEventsSyncManager
import com.simprints.id.testtools.TestApplication
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.TimeHelperImpl
import com.simprints.testtools.common.syntax.awaitAndAssertSuccess
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.spyk
import io.mockk.verify
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class SessionEventsManagerImplTest {

    val timeHelper: TimeHelper = TimeHelperImpl()
    @MockK private lateinit var sessionEventsSyncManagerMock: SessionEventsSyncManager
    @MockK private lateinit var sessionEventsLocalDbManagerMock: SessionEventsLocalDbManager
    @MockK private lateinit var preferencesManagerMock: PreferencesManager
    @MockK private lateinit var crashReportManagerMock: CrashReportManager
    private lateinit var sessionsEventsManagerSpy: SessionEventsManager

    private var sessionsInFakeDb = mutableListOf<SessionEvents>()

    @Before
    fun setUp() {
        ShadowLog.stream = System.out
        MockKAnnotations.init(this, relaxed = true)

        sessionsEventsManagerSpy = spyk(SessionEventsManagerImpl(
            "deviceID",
            "com.simprints.id",
            sessionEventsSyncManagerMock, sessionEventsLocalDbManagerMock, preferencesManagerMock, timeHelper, crashReportManagerMock))

        sessionsInFakeDb.clear()
        mockSessionEventsManager(sessionEventsLocalDbManagerMock, sessionsInFakeDb)
        mockPreferenceManagerInfo()
    }

    private fun mockPreferenceManagerInfo() {
        every { preferencesManagerMock.language } returns "language"
    }

    @Test
    fun signOut_shouldDeleteSessionsAndStopWorkers() {
        every { sessionsEventsManagerSpy.deleteSessions() } returns Completable.complete()

        sessionsEventsManagerSpy.signOut()

        verify(exactly = 1) { sessionsEventsManagerSpy.deleteSessions(null, null, null, null) }
        verify(exactly = 1) { sessionEventsSyncManagerMock.cancelSyncWorkers() }
    }

    @Test
    fun createSession_shouldCreateASession() {

        sessionsEventsManagerSpy.createSession("").blockingGet()

        assertThat(sessionsInFakeDb.size).isEqualTo(1)
        val createdSession = sessionsInFakeDb.first()
        assertNotNull(createdSession)
        assertThat(createdSession.isOpen()).isTrue()
    }

    @Test
    fun createSession_shouldCloseAnyOpenSession() {
        val openSession = createFakeOpenSession(timeHelper, "projectId", "old_session_id")
        sessionsInFakeDb.add(openSession)
        assertThat(openSession.isOpen()).isTrue()

        sessionsEventsManagerSpy.createSession("").blockingGet()

        assertThat(sessionsInFakeDb.size).isEqualTo(2)
        val newCreatedSession = sessionsInFakeDb.find { it.id != "old_session_id" }
        assertThat(newCreatedSession?.isOpen()).isTrue()
        assertThat(newCreatedSession?.events).hasSize(0)
        val oldOpenSession = sessionsInFakeDb.find { it.id == "old_session_id" }
        assertThat(oldOpenSession?.isClosed()).isTrue()
        assertThat(oldOpenSession?.events?.filterIsInstance(ArtificialTerminationEvent::class.java)).hasSize(1)

        verify(exactly = 1) { sessionEventsSyncManagerMock.scheduleSessionsSync() }
    }

    @Test
    fun closeLastSessionsIfPending_shouldSwallowException() {
        every { sessionEventsLocalDbManagerMock.loadSessions(any(), any()) } returns (Single.error(Throwable("error_reading_db")))

        sessionsEventsManagerSpy.createSession("").blockingGet()

        verify(exactly = 1) { crashReportManagerMock.logExceptionOrSafeException(any()) }
        assertThat(sessionsInFakeDb.size).isEqualTo(1)
    }

    @Test
    fun updateSession_shouldUpdateSession() {
        sessionsEventsManagerSpy.createSession("").blockingGet()
        sessionsEventsManagerSpy.updateSession {
            it.projectId = "new_project"
        }.blockingAwait()

        assertThat(sessionsInFakeDb.size).isEqualTo(1)
        assertThat(sessionsInFakeDb.first().projectId).isEqualTo("new_project")
    }

    @Test
    fun updateSession_shouldSwallowException() {
        val tester = sessionsEventsManagerSpy.updateSession { it.projectId = "new_project" }.test()
        tester.awaitAndAssertSuccess()
        verify(exactly = 1) { crashReportManagerMock.logExceptionOrSafeException(any()) }
    }

    @Test
    fun getCurrentSessionWithNoOpenSession_shouldThrowException() {
        val tester = sessionsEventsManagerSpy
            .getCurrentSession()
            .test()

        tester.awaitTerminalEvent()
        assertThat(tester.errorCount()).isEqualTo(1)
    }

    @Test
    fun addGuidEventForANoIdentificationSession_shouldThrowException() {
        sessionsInFakeDb.add(createFakeSession(id = "some_session_id"))
        addGuidEventAndVerifyErrorResult()
    }

    @Test
    fun addGuidEventForACloseSession_shouldThrowException() {
        sessionsInFakeDb.add(createFakeClosedSession(timeHelper = timeHelper, id = "some_session_id"))
        addGuidEventAndVerifyErrorResult()
    }

    private fun addGuidEventAndVerifyErrorResult() {
        val tester = sessionsEventsManagerSpy
            .addGuidSelectionEvent("selected_guid", "some_session_id")
            .test()

        tester.awaitTerminalEvent()
        assertThat(tester.errorCount()).isEqualTo(1)
        assertThat(tester.errors().first()).isInstanceOf(InvalidSessionForGuidSelectionEvent::class.java)
    }

    @Test
    fun addGuidEventForIdentificationSession_shouldAddBeAdded() {
        sessionsInFakeDb.add(createFakeOpenSession(timeHelper = timeHelper, id = "some_session_id").apply {
            this.addEvent(IdentificationCallbackEvent(0, "some_session_id", emptyList()))
        })
        val tester = sessionsEventsManagerSpy
            .addGuidSelectionEvent("selected_guid", "some_session_id")
            .test()

        tester.awaitAndAssertSuccess()
    }
}
