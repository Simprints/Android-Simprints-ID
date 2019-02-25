package com.simprints.id.data.analytics.eventdata.controllers.domain

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.anyOrNull
import com.nhaarman.mockito_kotlin.spy
import com.simprints.id.commontesttools.sessionEvents.createFakeOpenSession
import com.simprints.id.commontesttools.state.mockSessionEventsManager
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.eventdata.controllers.local.SessionEventsLocalDbManager
import com.simprints.id.data.analytics.eventdata.models.domain.events.ArtificialTerminationEvent
import com.simprints.id.data.analytics.eventdata.models.domain.session.SessionEvents
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.services.scheduledSync.sessionSync.SessionEventsSyncManager
import com.simprints.id.testtools.TestApplication
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.TimeHelperImpl
import com.simprints.testtools.common.syntax.*
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class SessionEventsManagerImplTest {

    private val sessionEventsSyncManagerMock: SessionEventsSyncManager = mock()
    private val sessionEventsLocalDbManagerMock: SessionEventsLocalDbManager = mock()
    private val preferencesManagerMock: PreferencesManager = mock()
    private val timeHelper: TimeHelper = TimeHelperImpl()
    private val crashReportManagerMock: CrashReportManager = mock()
    private val sessionsEventsManagerSpy: SessionEventsManager =
        spy(SessionEventsManagerImpl("deviceID", sessionEventsSyncManagerMock, sessionEventsLocalDbManagerMock, preferencesManagerMock, timeHelper, crashReportManagerMock))

    private var sessionsInFakeDb = mutableListOf<SessionEvents>()

    @Before
    fun setUp() {
        ShadowLog.stream = System.out

        sessionsInFakeDb.clear()
        mockSessionEventsManager(sessionEventsLocalDbManagerMock, sessionsInFakeDb)
        mockPreferenceManagerInfo()
    }

    private fun mockPreferenceManagerInfo() {
        whenever(preferencesManagerMock.appVersionName).thenReturn("app_version_name")
        whenever(preferencesManagerMock.libVersionName).thenReturn("lib_version_name")
        whenever(preferencesManagerMock.language).thenReturn("language")
    }

    @Test
    fun signOut_shouldDeleteSessionsAndStopWorkers() {
        Mockito.doReturn(Completable.complete()).`when`(sessionsEventsManagerSpy).deleteSessions()

        sessionsEventsManagerSpy.signOut()

        verifyOnce(sessionsEventsManagerSpy) { deleteSessions(null, null, null, null) }
        verifyOnce(sessionEventsSyncManagerMock) { cancelSyncWorkers() }
    }

    @Test
    fun createSession_shouldCreateASession() {

        sessionsEventsManagerSpy.createSession().blockingGet()

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

        sessionsEventsManagerSpy.createSession().blockingGet()

        assertThat(sessionsInFakeDb.size).isEqualTo(2)
        val newCreatedSession = sessionsInFakeDb.find { it.id != "old_session_id" }
        assertThat(newCreatedSession?.isOpen()).isTrue()
        assertThat(newCreatedSession?.events).hasSize(0)
        val oldOpenSession = sessionsInFakeDb.find { it.id == "old_session_id" }
        assertThat(oldOpenSession?.isClosed()).isTrue()
        assertThat(oldOpenSession?.events?.filterIsInstance(ArtificialTerminationEvent::class.java)).hasSize(1)

        verifyOnce(sessionEventsSyncManagerMock) { scheduleSessionsSync() }
    }

    @Test
    fun closeLastSessionsIfPending_shouldSwallowException() {
        whenever(sessionEventsLocalDbManagerMock.loadSessions(anyOrNull(), anyOrNull())).thenReturn(Single.error(Throwable("error_reading_db")))

        sessionsEventsManagerSpy.createSession().blockingGet()

        verifyOnce(crashReportManagerMock) { logExceptionOrThrowable(anyNotNull()) }
        assertThat(sessionsInFakeDb.size).isEqualTo(1)
    }

    @Test
    fun updateSession_shouldUpdateSession() {
        sessionsEventsManagerSpy.createSession().blockingGet()
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
        verifyOnce(crashReportManagerMock) { logExceptionOrThrowable(anyNotNull()) }
    }

    @Test
    fun getCurrentSessionWithNoOpenSession_shouldThrowException() {
        val tester = sessionsEventsManagerSpy
            .getCurrentSession()
            .test()

        tester.awaitTerminalEvent()
        assertThat(tester.errorCount()).isEqualTo(1)
    }
}
