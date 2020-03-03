package com.simprints.id.commontesttools.state

import com.simprints.id.data.analytics.eventdata.controllers.local.SessionEventsLocalDbManager
import com.simprints.id.data.analytics.eventdata.models.domain.session.SessionEvents
import com.simprints.id.exceptions.unexpected.SessionNotFoundException
import io.mockk.every
import io.reactivex.Completable
import io.reactivex.Single

fun mockSessionEventsManager(sessionsEventsManager: SessionEventsLocalDbManager,
                             sessionsInFakeDb: MutableList<SessionEvents>) {

    mockSessionEventsMgrLoadSessionsToUseFakeDb(sessionsEventsManager, sessionsInFakeDb)
    mockSessionEventsMgrInsertOrUpdateSessionsToUseFakeDb(sessionsEventsManager, sessionsInFakeDb)
    mockSessionEventsMgrToDeleteSessionsToUseFakeDb(sessionsEventsManager, sessionsInFakeDb)
    mockSessionEventsMgrLoadSessionByIdToUseFakeDb(sessionsEventsManager, sessionsInFakeDb)
    mockSessionEventsMgrCountToUseFakeDb(sessionsEventsManager, sessionsInFakeDb)
}

fun mockSessionEventsMgrLoadSessionByIdToUseFakeDb(sessionsEventsManager: SessionEventsLocalDbManager,
                                                   sessionsInFakeDb: MutableList<SessionEvents>) {

    every { sessionsEventsManager.loadSessionById(any()) } answers {
        val session = sessionsInFakeDb.find { it.id == args[0] }
        session?.let { Single.just(session) } ?: throw SessionNotFoundException()
    }
}

fun mockSessionEventsMgrCountToUseFakeDb(sessionsEventsManager: SessionEventsLocalDbManager,
                                         sessionsInFakeDb: MutableList<SessionEvents>) {

    every { sessionsEventsManager.getSessionCount(any()) } answers {
        Single.just(sessionsInFakeDb.count { it.projectId == args[0] })
    }
}

fun mockSessionEventsMgrToDeleteSessionsToUseFakeDb(sessionsEventsManager: SessionEventsLocalDbManager,
                                                    sessionsInFakeDb: MutableList<SessionEvents>) {

    every { sessionsEventsManager.deleteSessions(any(), any(), any(), any()) } answers {
        val sessionToDelete = findSessions(
            sessionsInFakeDb,
            args[0] as String?,
            args[1] as String?,
            args[2] as Boolean?,
            args[3] as Long?)

        sessionsInFakeDb.removeAll(sessionToDelete)
        Completable.complete()
    }
}

fun mockSessionEventsMgrLoadSessionsToUseFakeDb(sessionsEventsManager: SessionEventsLocalDbManager,
                                                sessionsInFakeDb: MutableList<SessionEvents>) {

    every { sessionsEventsManager.loadSessions(any(), any()) } answers {
        val sessions = findSessions(
            sessionsInFakeDb,
            args[0] as String?,
            null,
            args[1] as Boolean?,
            null)
        Single.just(sessions.toCollection(ArrayList()))
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

    every { sessionsEventsManager.insertOrUpdateSessionEvents(any()) } answers {
        val newSession = args[0] as SessionEvents
        sessionsInFakeDb.removeIf { session -> session.id == newSession.id }
        sessionsInFakeDb.add(newSession)
        Completable.complete()
    }
}
