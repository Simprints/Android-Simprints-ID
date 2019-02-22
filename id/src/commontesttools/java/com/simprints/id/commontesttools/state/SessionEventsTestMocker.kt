package com.simprints.id.commontesttools.state

import com.simprints.id.data.analytics.eventData.controllers.local.SessionEventsLocalDbManager
import com.simprints.id.data.analytics.eventData.models.domain.session.SessionEvents
import com.simprints.id.exceptions.safe.session.SessionNotFoundException
import com.simprints.testtools.common.syntax.anyNotNull
import com.simprints.testtools.common.syntax.anyOrNull
import com.simprints.testtools.common.syntax.whenever
import io.reactivex.Completable
import io.reactivex.Single

fun mockSessionEventsManager(sessionsEventsManager: SessionEventsLocalDbManager,
                             sessionsInFakeDb: MutableList<SessionEvents>) {

    mockSessionEventsMgrLoadSessionsToUseFakeDb(sessionsEventsManager, sessionsInFakeDb)
    mockSessionEventsMgrInsertOrUpdateSessionsToUseFakeDb(sessionsEventsManager, sessionsInFakeDb)
    mockSessionEventsMgrToDeleteSessionsToUseFakeDb(sessionsEventsManager, sessionsInFakeDb)
    mockSessionEventsMgrLoadSessionByIdToUseFakeDb(sessionsEventsManager, sessionsInFakeDb)
}

fun mockSessionEventsMgrLoadSessionByIdToUseFakeDb(sessionsEventsManager: SessionEventsLocalDbManager,
                                                   sessionsInFakeDb: MutableList<SessionEvents>) {

    whenever(sessionsEventsManager.loadSessionById(anyNotNull())).thenAnswer { args ->
        val session = sessionsInFakeDb.find { it.id == args.arguments[0] }
        session?.let { Single.just(session) } ?: throw SessionNotFoundException()
    }
}

fun mockSessionEventsMgrToDeleteSessionsToUseFakeDb(sessionsEventsManager: SessionEventsLocalDbManager,
                                                    sessionsInFakeDb: MutableList<SessionEvents>) {

    whenever(sessionsEventsManager.deleteSessions(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())).thenAnswer { args ->
        val sessionToDelete = findSessions(
            sessionsInFakeDb,
            args.arguments[0] as String?,
            args.arguments[1] as String?,
            args.arguments[2] as Boolean?,
            args.arguments[3] as Long?)

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
            args.arguments[1] as Boolean?,
            null)
        Single.just(sessions)
    }
}

private fun findSessions(sessionsInFakeDb: MutableList<SessionEvents>,
                         projectId: String?,
                         sessionId: String?,
                         openSession: Boolean?,
                         startedBefore: Long?): List<SessionEvents> {

    return sessionsInFakeDb.filter {
        projectId?.let { projectIdToSelect -> it.projectId == projectIdToSelect } ?: true &&
        sessionId?.let { sessionIdToSelect -> it.id == sessionIdToSelect } ?: true &&
        openSession?.let { openSession -> it.isOpen() == openSession } ?: true &&
        startedBefore?.let { startedBefore -> it.startTime < startedBefore } ?: true
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
