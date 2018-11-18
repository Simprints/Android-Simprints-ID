package com.simprints.id.shared.sessionEvents

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.anyOrNull
import com.simprints.id.data.analytics.eventData.controllers.local.SessionEventsLocalDbManager
import com.simprints.id.data.analytics.eventData.models.domain.session.Device
import com.simprints.id.data.analytics.eventData.models.domain.session.SessionEvents
import com.simprints.id.exceptions.safe.session.SessionNotFoundException
import com.simprints.id.shared.anyNotNull
import com.simprints.id.shared.whenever
import com.simprints.id.tools.TimeHelper
import io.reactivex.Completable
import io.reactivex.Single
import java.util.*

fun createFakeSession(timeHelper: TimeHelper? = null,
                      projectId: String = "some_project",
                      id: String = UUID.randomUUID().toString(),
                      startTime: Long = timeHelper?.now() ?: 0,
                      fakeRelativeEndTime: Long = 0): SessionEvents =
    SessionEvents(
        id = id,
        projectId = projectId,
        appVersionName = "some_version",
        libVersionName = "some_version",
        language = "en",
        device = Device(),
        startTime = startTime).apply {
        relativeEndTime = fakeRelativeEndTime
    }

fun createFakeOpenSession(timeHelper: TimeHelper,
                          projectId: String = "some_project",
                          id: String = UUID.randomUUID().toString()) =
    createFakeSession(timeHelper, projectId, id, timeHelper.nowMinus(1000))


fun createFakeClosedSession(timeHelper: TimeHelper,
                            projectId: String = "some_project",
                            id: String = UUID.randomUUID().toString()) =
    createFakeSession(timeHelper, projectId, id, timeHelper.nowMinus(1000)).apply {
        relativeEndTime = nowRelativeToStartTime(timeHelper)
    }

fun mockSessionEventsManager(sessionsEventsManager: SessionEventsLocalDbManager,
                             sessionsInFakeDb: MutableList<SessionEvents>) {

    mockSessionEventsMgrLoadSessionsToUseFakeDb(sessionsEventsManager, sessionsInFakeDb)
    mockSessionEventsMgrInsertOrUpdateSessionsToUseFakeDb(sessionsEventsManager, sessionsInFakeDb)
    mockSessionEventsMgrToDeleteSessionByIdToUseFakeDb(sessionsEventsManager, sessionsInFakeDb)
    mockSessionEventsMgrLoadSessionByIdToUseFakeDb(sessionsEventsManager, sessionsInFakeDb)
}

fun mockSessionEventsMgrLoadSessionByIdToUseFakeDb(sessionsEventsManager: SessionEventsLocalDbManager,
                                                   sessionsInFakeDb: MutableList<SessionEvents>) {

    whenever(sessionsEventsManager.loadSessionById(anyNotNull())).thenAnswer { args ->
        val session = sessionsInFakeDb.find { it.id == args.arguments[0] }
        session?.let { Single.just(session) } ?: throw SessionNotFoundException()
    }
}

fun mockSessionEventsMgrToDeleteSessionByIdToUseFakeDb(sessionsEventsManager: SessionEventsLocalDbManager,
                                                       sessionsInFakeDb: MutableList<SessionEvents>) {

    whenever(sessionsEventsManager.deleteSessions(anyOrNull(), anyOrNull(), anyOrNull())).thenAnswer { args ->
        val sessionToDelete = findSessions(
            sessionsInFakeDb,
            args.arguments[0] as String?,
            args.arguments[1] as String?,
            args.arguments[2] as Boolean?)

        sessionsInFakeDb.removeAll(sessionToDelete)
        Completable.complete()
    }
}

fun mockSessionEventsMgrLoadSessionsToUseFakeDb(sessionsEventsManager: SessionEventsLocalDbManager,
                                                sessionsInFakeDb: MutableList<SessionEvents>) {

    whenever(sessionsEventsManager.loadSessions(anyOrNull(), anyOrNull())).thenAnswer { args ->
        val sessions = findSessions(
            sessionsInFakeDb,
            args.arguments[0] as String?,
            null,
            args.arguments[1] as Boolean?)
        Single.just(sessions)
    }
}

private fun findSessions(sessionsInFakeDb: MutableList<SessionEvents>,
                         projectId: String?,
                         sessionId: String?,
                         openSession: Boolean?): List<SessionEvents> {

    return sessionsInFakeDb.filter {
        projectId?.let { projectIdToSelect -> it.projectId == projectIdToSelect } ?: true &&
            sessionId?.let { sessionIdToSelect -> it.id == sessionIdToSelect } ?: true &&
            openSession?.let { openSession -> it.isOpen() == openSession } ?: true
    }
}

private fun mockSessionEventsMgrInsertOrUpdateSessionsToUseFakeDb(sessionsEventsManager: SessionEventsLocalDbManager,
                                                                  sessionsInFakeDb: MutableList<SessionEvents>) {

    whenever(sessionsEventsManager.insertOrUpdateSessionEvents(anyNotNull())).thenAnswer {
        val newSession = it.arguments[0] as SessionEvents
        sessionsInFakeDb.removeIf { session -> session.id == newSession.id }
        sessionsInFakeDb.add(newSession)
        Completable.complete()
    }
}
