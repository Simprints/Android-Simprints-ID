package com.simprints.id.commontesttools.state

import com.simprints.id.data.db.session.domain.models.session.SessionEvents
import com.simprints.id.data.db.session.local.SessionLocalDataSource
import com.simprints.id.exceptions.unexpected.SessionNotFoundException
import io.mockk.every
import io.reactivex.Completable
import io.reactivex.Single

fun mockSessionEventsManager(sessionsManager: SessionLocalDataSource,
                             sessionsInFakeDb: MutableList<SessionEvents>) {

    mockSessionEventsMgrLoadSessionsToUseFakeDb(sessionsManager, sessionsInFakeDb)
    mockSessionEventsMgrInsertOrUpdateSessionsToUseFakeDb(sessionsManager, sessionsInFakeDb)
    mockSessionEventsMgrToDeleteSessionsToUseFakeDb(sessionsManager, sessionsInFakeDb)
    mockSessionEventsMgrLoadSessionByIdToUseFakeDb(sessionsManager, sessionsInFakeDb)
    mockSessionEventsMgrCountToUseFakeDb(sessionsManager, sessionsInFakeDb)
}

fun mockSessionEventsMgrLoadSessionByIdToUseFakeDb(sessionsManager: SessionLocalDataSource,
                                                   sessionsInFakeDb: MutableList<SessionEvents>) {

    every { sessionsManager.loadSessionById(any()) } answers {
        val session = sessionsInFakeDb.find { it.id == args[0] }
        session?.let { Single.just(session) } ?: throw SessionNotFoundException()
    }
}

fun mockSessionEventsMgrCountToUseFakeDb(sessionsManager: SessionLocalDataSource,
                                         sessionsInFakeDb: MutableList<SessionEvents>) {

    every { sessionsManager.getSessionCount(any()) } answers {
        Single.just(sessionsInFakeDb.count { it.projectId == args[0] })
    }
}

fun mockSessionEventsMgrToDeleteSessionsToUseFakeDb(sessionsManager: SessionLocalDataSource,
                                                    sessionsInFakeDb: MutableList<SessionEvents>) {

    every { sessionsManager.deleteSessions(any(), any(), any(), any()) } answers {
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

fun mockSessionEventsMgrLoadSessionsToUseFakeDb(sessionsManager: SessionLocalDataSource,
                                                sessionsInFakeDb: MutableList<SessionEvents>) {

    every { sessionsManager.loadSessions(any(), any()) } answers {
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

private fun mockSessionEventsMgrInsertOrUpdateSessionsToUseFakeDb(sessionsManager: SessionLocalDataSource,
                                                                  sessionsInFakeDb: MutableList<SessionEvents>) {

    every { sessionsManager.insertOrUpdateSessionEvents(any()) } answers {
        val newSession = args[0] as SessionEvents
        sessionsInFakeDb.removeIf { session -> session.id == newSession.id }
        sessionsInFakeDb.add(newSession)
        Completable.complete()
    }
}
