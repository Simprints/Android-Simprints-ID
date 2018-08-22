package com.simprints.id.data.analytics.eventData

import android.content.Context
import android.os.Build
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.analytics.eventData.models.events.*
import com.simprints.id.data.analytics.eventData.models.session.Device
import com.simprints.id.data.analytics.eventData.models.session.Location
import com.simprints.id.data.analytics.eventData.models.session.SessionEvents
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.exceptions.safe.session.NoSessionsFoundException
import com.simprints.id.exceptions.safe.session.SessionNotFoundException
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.delegates.lazyVar
import com.simprints.id.tools.extensions.deviceId
import com.simprints.libcommon.Person
import com.simprints.libcommon.Utils
import com.simprints.libsimprints.Identification
import com.simprints.libsimprints.Verification
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.rxkotlin.subscribeBy
import retrofit2.adapter.rxjava2.Result

// Class to manage the current activeSession
open class SessionEventsManagerImpl(private val ctx: Context,
                                    private val sessionEventsLocalDbManager: SessionEventsLocalDbManager,
                                    override val loginInfoManager: LoginInfoManager,
                                    private val preferencesManager: PreferencesManager,
                                    private val timeHelper: TimeHelper,
                                    private val remoteDbManager: RemoteDbManager,
                                    private val analyticsManager: AnalyticsManager) : SessionEventsManager {

    companion object {
        val PROJECT_ID_FOR_USER_NOT_SIGNED_ID = "USER_NOT_SIGNED_ID"
    }

    private var activeSession: SessionEvents? = null

    var sessionsApi: SessionsRemoteInterface by lazyVar {
        remoteDbManager.getSessionsApiClient().blockingGet()
    }

    //as default, the manager tries to load the last open activeSession for a specific project
    override fun getCurrentSession(projectId: String): Single<SessionEvents> = activeSession?.let {
        Single.just(it)
    } ?: sessionEventsLocalDbManager.loadSessions(projectId, true).map { it[0] }.doOnSuccess {
        activeSession = it
    }

    override fun createSession(): Single<SessionEvents> =
        createSessionWithAvailableInfo(PROJECT_ID_FOR_USER_NOT_SIGNED_ID).let {
            activeSession = it
            closeLastSessionsIfPending()
                .andThen(insertOrUpdateSession(it))
                .toSingle { it }
        }

    private fun createSessionWithAvailableInfo(projectId: String): SessionEvents =
        SessionEvents(
            projectId = projectId,
            appVersionName = preferencesManager.appVersionName,
            libVersionName = preferencesManager.libVersionName,
            startTime = timeHelper.now(),
            language = preferencesManager.language,
            device = Device(
                androidSdkVersion = Build.VERSION.SDK_INT.toString(),
                deviceModel = Build.MANUFACTURER + "_" + Build.MODEL,
                deviceId = ctx.deviceId))

    override fun updateSession(block: (sessionEvents: SessionEvents) -> Unit, projectId: String): Completable =
        getCurrentSession(projectId).flatMapCompletable {
            block(it)
            insertOrUpdateSession(it)
        }.doOnError {
            analyticsManager.logThrowable(it)
        }.onErrorComplete() // because events are low priority, it swallows the exception

    override fun updateSessionInBackground(block: (sessionEvents: SessionEvents) -> Unit, projectId: String) {
        updateSession(block, projectId).subscribeBy(onError = { it.printStackTrace() })
    }

    private fun closeLastSessionsIfPending(): Completable =
        sessionEventsLocalDbManager.loadSessions(openSession = true).flatMapCompletable { openSessions ->
            openSessions.forEach {
                it.addArtificialTerminationIfRequired(timeHelper, ArtificialTerminationEvent.Reason.NEW_SESSION)
                it.closeIfRequired(timeHelper)
                insertOrUpdateSession(it).blockingAwait()
            }
            Completable.complete()
        }

    private fun insertOrUpdateSession(session: SessionEvents): Completable =
        sessionEventsLocalDbManager.insertOrUpdateSessionEvents(session).doOnComplete {
            this.activeSession = session
        }

    /** @throws SessionNotFoundException */
    override fun addGuidSelectionEventToLastIdentificationIfExists(selectedGuid: String, sessionId: String): Completable =
        sessionEventsLocalDbManager.loadSessionById(sessionId).flatMapCompletable {
            it.events.add(GuidSelectionEvent(
                it.nowRelativeToStartTime(timeHelper),
                selectedGuid
            ))
            insertOrUpdateSession(it)
        }

    /** @throws NoSessionsFoundException */
    override fun syncSessions(projectId: String): Completable =
        sessionEventsLocalDbManager.loadSessions(projectId).flatMap { sessions ->
            if (sessions.size > 0) {
                closeAnyOpenSessionsAndUpdateUploadTime(sessions)
                uploadClosedSessions(sessions, projectId)
            } else {
                throw NoSessionsFoundException()
            }
        }.flatMapCompletable {
            if (uploadSessionSucceeded(it)) {
                sessionEventsLocalDbManager.deleteSessions(projectId, false).doFinally {
                    sessionEventsLocalDbManager.deleteSessions(PROJECT_ID_FOR_USER_NOT_SIGNED_ID)
                }
            } else {
                Completable.complete()
            }
        }

    private fun closeAnyOpenSessionsAndUpdateUploadTime(sessions: ArrayList<SessionEvents>) {
        sessions.forEach {
            forceSessionToCloseIfOpenAndNotInProgress(it, timeHelper)
            it.relativeUploadTime = it.nowRelativeToStartTime(timeHelper)
            sessionEventsLocalDbManager.insertOrUpdateSessionEvents(it).blockingAwait()
        }
    }

    private fun uploadClosedSessions(sessions: ArrayList<SessionEvents>, projectId: String): Single<Result<Unit>> {
        return sessions.filter { it.isClosed() }.toTypedArray().let {
            sessionsApi.uploadSessions(projectId, hashMapOf("sessions" to it))
        }
    }

    private fun uploadSessionSucceeded(it: Result<Unit>) =
        !it.isError && it.response()?.code() == 200

    private fun forceSessionToCloseIfOpenAndNotInProgress(it: SessionEvents, timeHelper: TimeHelper) {
        if (it.isOpen() && !it.isPossiblyInProgress(timeHelper)) {
            it.addArtificialTerminationIfRequired(timeHelper, ArtificialTerminationEvent.Reason.TIMED_OUT)
            it.closeIfRequired(timeHelper)
        }
    }

    override fun addOneToOneMatchEventInBackground(patientId: String, startTimeVerification: Long, match: Verification?) {
        updateSessionInBackground({ session ->
            session.events.add(OneToOneMatchEvent(
                session.timeRelativeToStartTime(startTimeVerification),
                session.nowRelativeToStartTime(timeHelper),
                preferencesManager.patientId,
                match?.let { MatchCandidate(it.guid, match.confidence) }))
        })
    }

    override fun addOneToManyEventInBackground(startTimeIdentification: Long, matches: List<Identification>, matchSize: Int) {
        updateSessionInBackground({ session ->
            session.events.add(OneToManyMatchEvent(
                session.timeRelativeToStartTime(startTimeIdentification),
                session.nowRelativeToStartTime(timeHelper),
                OneToManyMatchEvent.MatchPool(OneToManyMatchEvent.MatchPoolType.fromConstantGroup(preferencesManager.matchGroup), matchSize),
                matches.map { MatchCandidate(it.guid, it.confidence) }.toList().toTypedArray()))
        })
    }

    override fun addEventForScannerConnectivityInBackground(scannerInfo: ScannerConnectionEvent.ScannerInfo) {
        updateSessionInBackground({
            it.events.add(ScannerConnectionEvent(
                it.nowRelativeToStartTime(timeHelper),
                scannerInfo
            ))
        })
    }

    override fun updateHardwareVersionInScannerConnectivityEvent(hardwareVersion: String) {
        updateSessionInBackground({ session ->
            val scannerConnectivityEvents = session.events.filterIsInstance(ScannerConnectionEvent::class.java)
            scannerConnectivityEvents.forEach { it.scannerInfo.hardwareVersion = hardwareVersion }
        })
    }

    override fun addPersonCreationEventInBackground(person: Person) {
        updateSessionInBackground({ session ->
            session.events.add(PersonCreationEvent(
                session.nowRelativeToStartTime(timeHelper),
                extractCaptureEventIdsBasedOnPersonTemplate(session, person.fingerprints.map { Utils.byteArrayToBase64(it.templateBytes) })
            ))
        })
    }

    override fun addEventForCandidateReadInBackground(guid: String,
                                                      startCandidateSearchTime: Long,
                                                      localResult: CandidateReadEvent.LocalResult,
                                                      remoteResult: CandidateReadEvent.RemoteResult) {
        updateSessionInBackground({
            it.events.add(CandidateReadEvent(
                it.timeRelativeToStartTime(startCandidateSearchTime),
                it.nowRelativeToStartTime(timeHelper),
                guid,
                localResult,
                remoteResult
            ))
        })
    }

    override fun addLocationToSession(latitude: Double, longitude: Double) {
        this.updateSessionInBackground({ sessionEvents ->
            sessionEvents.location = Location(latitude, longitude)
        }, loginInfoManager.getSignedInProjectIdOrEmpty())
    }

    // It extracts CaptureEvents Ids with the templates used to create the "Person" object for
    // identification, verification, enrolment.
    private fun extractCaptureEventIdsBasedOnPersonTemplate(sessionEvents: SessionEvents, personTemplates: List<String>): List<String> =
        sessionEvents.events
            .filterIsInstance(FingerprintCaptureEvent::class.java)
            .filter { it.fingerprint?.template in personTemplates }
            .map { it.id }
}
