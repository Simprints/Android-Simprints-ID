package com.simprints.id.data.db.session

import com.simprints.core.tools.EncodingUtils
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.db.person.domain.FingerprintSample
import com.simprints.id.data.db.session.domain.models.SessionQuery
import com.simprints.id.data.db.session.domain.models.events.*
import com.simprints.id.data.db.session.domain.models.events.EventType.CALLBACK_IDENTIFICATION
import com.simprints.id.data.db.session.domain.models.events.EventType.GUID_SELECTION
import com.simprints.id.data.db.session.domain.models.session.SessionEvents
import com.simprints.id.data.db.session.local.SessionLocalDataSource
import com.simprints.id.data.db.session.remote.SessionRemoteDataSource
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.services.scheduledSync.sessionSync.SessionEventsSyncManager
import com.simprints.id.tools.TimeHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

// Class to manage the current activeSession
open class SessionRepositoryImpl(private val deviceId: String,
                                 private val appVersionName: String,
                                 private val sessionEventsSyncManager: SessionEventsSyncManager,
                                 private val sessionLocalDataSource: SessionLocalDataSource,
                                 private val sessionRemoteDataSource: SessionRemoteDataSource,
                                 private val preferencesManager: PreferencesManager,
                                 private val loginInfoManager: LoginInfoManager,
                                 private val timeHelper: TimeHelper,
                                 private val crashReportManager: CrashReportManager) : SessionRepository {

    companion object {
        const val PROJECT_ID_FOR_NOT_SIGNED_IN = "NOT_SIGNED_IN"
        const val SESSION_BATCH_SIZE = 20
    }

    // as default, the manager tries to load the last open activeSession
    //
    override suspend fun getCurrentSession(): SessionEvents =
        reportAndRethrowException { sessionLocalDataSource.load(SessionQuery(openSession = true)).first() }

    override suspend fun createSession(libSimprintsVersionName: String) {
        reportAndSilentException { sessionLocalDataSource.create(appVersionName, libSimprintsVersionName, preferencesManager.language, deviceId) }
    }

    override suspend fun addGuidSelectionEvent(selectedGuid: String, sessionId: String) {
        reportAndSilentException {
            sessionLocalDataSource.updateCurrentSession { currentSession ->
                if (currentSession.hasEvent(GUID_SELECTION) &&
                    currentSession.hasEvent(CALLBACK_IDENTIFICATION)) {

                    val guidEvent = GuidSelectionEvent(timeHelper.now(), selectedGuid)
                    currentSession.events.add(guidEvent)
                }
            }
        }
    }

    override suspend fun updateHardwareVersionInScannerConnectivityEvent(hardwareVersion: String) {
        reportAndSilentException {
            sessionLocalDataSource.updateCurrentSession { currentSession ->
                val scannerConnectivityEvents = currentSession.events.filterIsInstance(ScannerConnectionEvent::class.java)
                scannerConnectivityEvents.forEach { it.scannerInfo.hardwareVersion = hardwareVersion }
            }
        }
    }

    override suspend fun addPersonCreationEvent(fingerprintSamples: List<FingerprintSample>) {
        reportAndSilentException {
            sessionLocalDataSource.updateCurrentSession { currentSession ->
                currentSession.events.add(PersonCreationEvent(
                    timeHelper.now(),
                    extractCaptureEventIdsBasedOnPersonTemplate(currentSession, fingerprintSamples.map { EncodingUtils.byteArrayToBase64(it.template) })
                ))
            }
        }
    }

    override fun addEventToCurrentSessionInBackground(event: Event) {
        CoroutineScope(Dispatchers.IO).launch {
            reportAndSilentException {
                sessionLocalDataSource.addEventToCurrentSession(event)
            }
        }
    }
    override suspend fun startUploadingSessions() {
        createBatchesFromLocalAndUploadSessions()
    }

    private suspend fun createBatchesFromLocalAndUploadSessions() {
        loadSessionsToUpload()
            .createBatches()
            .startUploadingSessions()
            .deleteSessionsFromDb()
    }

    private suspend fun loadSessionsToUpload() =
        sessionLocalDataSource.load(SessionQuery(projectId = loginInfoManager.getSignedInProjectIdOrEmpty()))

    private suspend fun Flow<SessionEvents>.createBatches() =
        this.bufferedChunks(SESSION_BATCH_SIZE)

    private suspend fun Flow<List<SessionEvents>>.startUploadingSessions(): Flow<List<SessionEvents>> {
        this.collect {
            sessionRemoteDataSource.uploadSessions(loginInfoManager.getSignedInProjectIdOrEmpty(), it)
        }
        return this
    }

    private suspend fun Flow<List<SessionEvents>>.deleteSessionsFromDb() {
        this.collect {sessions ->
            sessions.forEach {
                sessionLocalDataSource.delete(SessionQuery(id = it.id, openSession = false))
            }
        }
    }


    override suspend fun updateCurrentSession(updateBlock: (SessionEvents) -> Unit) {
        reportAndRethrowException {
            sessionLocalDataSource.updateCurrentSession(updateBlock)
        }
    }

    override suspend fun signOut() {
        sessionLocalDataSource.delete(SessionQuery(openSession = false))
        sessionEventsSyncManager.cancelSyncWorkers()
    }

    // It extracts CaptureEvents Ids with the templates used to create the "Person" object for
    // identification, verification, enrolment.
    private fun extractCaptureEventIdsBasedOnPersonTemplate(sessionEvents: SessionEvents, personTemplates: List<String>): List<String> =
        sessionEvents.events
            .filterIsInstance(FingerprintCaptureEvent::class.java)
            .filter { it.fingerprint?.template in personTemplates && it.result != FingerprintCaptureEvent.Result.SKIPPED }
            .map { it.id }

    private suspend fun <T> reportAndSilentException(block: suspend () -> T): T? =
        try {
            block()
        } catch (t: Throwable) {
            crashReportManager.logExceptionOrSafeException(t)
            null
        }

    private suspend fun <T> reportAndRethrowException(block: suspend () -> T): T =
        try {
            block()
        } catch (t: Throwable) {
            crashReportManager.logExceptionOrSafeException(t)
            throw t
        }

}
