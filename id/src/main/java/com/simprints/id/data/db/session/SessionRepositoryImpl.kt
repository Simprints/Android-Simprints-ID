package com.simprints.id.data.db.session

import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.db.session.domain.models.SessionQuery
import com.simprints.id.data.db.session.domain.models.events.Event
import com.simprints.id.data.db.session.domain.models.session.SessionEvents
import com.simprints.id.data.db.session.local.SessionLocalDataSource
import com.simprints.id.data.db.session.remote.SessionRemoteDataSource
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.services.scheduledSync.sessionSync.SessionEventsSyncManager
import com.simprints.id.tools.extensions.bufferedChunks
import com.simprints.id.tools.ignoreException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

// Class to manage the current activeSession
open class SessionRepositoryImpl(private val deviceId: String,
                                 private val appVersionName: String,
                                 private val projectId: String,
                                 private val sessionEventsSyncManager: SessionEventsSyncManager,
                                 private val sessionLocalDataSource: SessionLocalDataSource,
                                 private val sessionRemoteDataSource: SessionRemoteDataSource,
                                 private val preferencesManager: PreferencesManager,
                                 private val crashReportManager: CrashReportManager) : SessionRepository {

    companion object {
        const val PROJECT_ID_FOR_NOT_SIGNED_IN = "NOT_SIGNED_IN"
        const val SESSION_BATCH_SIZE = 20
    }

    // as default, the manager tries to load the last open activeSession
    //
    override suspend fun getCurrentSession(): SessionEvents =
        reportExceptionIfNeeded { sessionLocalDataSource.load(SessionQuery(openSession = true)).first() }

    override suspend fun createSession(libSimprintsVersionName: String) {
        reportExceptionIfNeeded {
            sessionLocalDataSource.create(appVersionName, libSimprintsVersionName, preferencesManager.language, deviceId)
        }
    }

    override fun addEventToCurrentSessionInBackground(event: Event) {
        CoroutineScope(Dispatchers.IO).launch {
            ignoreException {
                reportExceptionIfNeeded {
                    sessionLocalDataSource.addEventToCurrentSession(event)
                }
            }
        }
    }
    override suspend fun uploadSessions() {
        createBatchesFromLocalAndUploadSessions()
    }

    private suspend fun createBatchesFromLocalAndUploadSessions() {
        loadSessionsToUpload()
            .filterClosedSessions()
            .createBatches()
            .startUploadingSessions()
            .deleteSessionsFromDb()
    }

    private suspend fun loadSessionsToUpload() =
        sessionLocalDataSource.load(SessionQuery(projectId = projectId))

    private fun Flow<SessionEvents>.filterClosedSessions() =
        filter { it.isClosed() }

    private suspend fun Flow<SessionEvents>.createBatches() =
        this.bufferedChunks(SESSION_BATCH_SIZE)

    private suspend fun Flow<List<SessionEvents>>.startUploadingSessions(): Flow<List<SessionEvents>> {
        this.collect {
            sessionRemoteDataSource.uploadSessions(projectId, it)
        }
        return this
    }

    private suspend fun Flow<List<SessionEvents>>.deleteSessionsFromDb() {
        this.collect {sessions ->
            sessions.forEach {
                sessionLocalDataSource.delete(SessionQuery(openSession = false))
            }
        }
    }

    override suspend fun updateSession(sessionId: String, updateBlock: (SessionEvents) -> Unit) {
        reportExceptionIfNeeded {
            sessionLocalDataSource.update(sessionId) {
                updateBlock(it)
            }
        }
    }

    override suspend fun updateCurrentSession(updateBlock: (SessionEvents) -> Unit) {
        reportExceptionIfNeeded {
            sessionLocalDataSource.updateCurrentSession(updateBlock)
        }
    }

    override suspend fun signOut() {
        sessionLocalDataSource.delete(SessionQuery(openSession = false))
        sessionEventsSyncManager.cancelSyncWorkers()
    }

    private suspend fun <T> reportExceptionIfNeeded(block: suspend () -> T): T =
        try {
            block()
        } catch (t: Throwable) {
            crashReportManager.logExceptionOrSafeException(t)
            throw t
        }
}
