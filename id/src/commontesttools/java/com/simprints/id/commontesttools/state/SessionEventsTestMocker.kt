package com.simprints.id.commontesttools.state

import com.simprints.id.data.db.session.domain.models.SessionQuery
import com.simprints.id.data.db.session.domain.models.session.SessionEvents
import com.simprints.id.data.db.session.local.SessionLocalDataSource
import com.simprints.id.exceptions.unexpected.SessionNotFoundException
import io.mockk.coEvery
import io.reactivex.Completable
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flowOf

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

    coEvery { sessionsManager.load(any()) } answers {
        val session = sessionsInFakeDb.find { it.id == args[0] }
        session?.let { flowOf(session) } ?: throw SessionNotFoundException()
    }
}

fun mockSessionEventsMgrCountToUseFakeDb(sessionsManager: SessionLocalDataSource,
                                         sessionsInFakeDb: MutableList<SessionEvents>) {

    coEvery { sessionsManager.count(any()) } answers {
        sessionsInFakeDb.count { it.projectId == args[0] }
    }
}

fun mockSessionEventsMgrToDeleteSessionsToUseFakeDb(sessionsManager: SessionLocalDataSource,
                                                    sessionsInFakeDb: MutableList<SessionEvents>) {

    coEvery() { sessionsManager.delete(any()) } answers {
        val sessionToDelete = findSessions(
            sessionsInFakeDb,
            args[0] as String?,
            args[1] as String?,
            args[2] as Boolean?,
            args[3] as Long?)

        sessionsInFakeDb.removeAll(sessionToDelete)
    }
}

fun mockSessionEventsMgrLoadSessionsToUseFakeDb(sessionsManager: SessionLocalDataSource,
                                                sessionsInFakeDb: MutableList<SessionEvents>) {

    coEvery { sessionsManager.load(SessionQuery()) } answers {
        val sessions = findSessions(
            sessionsInFakeDb,
            args[0] as String?,
            null,
            args[1] as Boolean?,
            null)
        sessions.toCollection(ArrayList()).asFlow()
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

    coEvery { sessionsManager.addEventToCurrentSession(any()) } answers {
        val newSession = args[0] as SessionEvents
        sessionsInFakeDb.removeIf { session -> session.id == newSession.id }
        sessionsInFakeDb.add(newSession)
        Completable.complete()
    }
}
