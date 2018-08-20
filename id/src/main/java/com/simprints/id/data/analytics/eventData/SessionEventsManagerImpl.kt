package com.simprints.id.data.analytics.eventData

import android.content.Context
import android.os.Build
import com.simprints.id.data.analytics.eventData.models.events.*
import com.simprints.id.data.analytics.eventData.models.session.Device
import com.simprints.id.data.analytics.eventData.models.session.SessionEvents
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.session.callout.CalloutAction
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

// Class to manage the current activeSession
open class SessionEventsManagerImpl(private val ctx: Context,
                                    private val sessionEventsLocalDbManager: SessionEventsLocalDbManager,
                                    override val loginInfoManager: LoginInfoManager,
                                    private val preferencesManager: PreferencesManager,
                                    private val timeHelper: TimeHelper,
                                    private val remoteDbManager: RemoteDbManager) : SessionEventsManager {

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

    override fun createSession(projectId: String): Single<SessionEvents> =
        if (projectId.isNotEmpty()) {
            val sessionToSave = SessionEvents(
                projectId = projectId,
                appVersionName = preferencesManager.appVersionName,
                libVersionName = preferencesManager.libVersionName,
                startTime = timeHelper.msSinceBoot(),
                language = preferencesManager.language,
                device = Device(
                    androidSdkVersion = Build.VERSION.SDK_INT.toString(),
                    deviceModel = Build.MANUFACTURER + "_" + Build.MODEL,
                    deviceId = ctx.deviceId)
            ).also { activeSession = it }

            closeLastSessionsIfPending(projectId)
                .andThen(insertOrUpdateSession(sessionToSave))
                .toSingle { sessionToSave }
        } else {
            Single.error<SessionEvents>(Throwable("project ID empty"))
        }

    override fun updateSession(block: (sessionEvents: SessionEvents) -> Unit, projectId: String): Completable =
        getCurrentSession(projectId).flatMapCompletable {
            block(it)
            insertOrUpdateSession(it)
        }.onErrorComplete() // because events are low priority, it swallows the exception

    override fun updateSessionInBackground(block: (sessionEvents: SessionEvents) -> Unit, projectId: String) {
        updateSession(block, projectId).subscribeBy(onError = { it.printStackTrace() })
    }

    private fun closeLastSessionsIfPending(projectId: String): Completable =
        sessionEventsLocalDbManager.loadSessions(projectId, true).flatMapCompletable {
            it.forEach {
                it.addArtificialTerminationIfRequired(timeHelper, ArtificialTerminationEvent.Reason.NEW_SESSION)
                it.closeIfRequired(timeHelper)
                insertOrUpdateSession(it).blockingAwait()
            }
            Completable.complete()
        }

    override fun insertOrUpdateSession(session: SessionEvents): Completable = sessionEventsLocalDbManager.insertOrUpdateSessionEvents(session)

    override fun addGuidSelectionEventToLastIdentificationIfExists(projectId: String, selectedGuid: String): Completable =
        sessionEventsLocalDbManager.loadSessions(projectId, false).flatMapCompletable { sessions ->
            sessions.firstOrNull()?.let { lastSessionClosed ->
                lastSessionClosed.events
                    .filterIsInstance(CalloutEvent::class.java)
                    .filter { it.parameters.action == CalloutAction.IDENTIFY }
                    .take(1)
                    .also {
                        lastSessionClosed.events.add(GuidSelectionEvent(
                            lastSessionClosed.nowRelativeToStartTime(timeHelper),
                            selectedGuid
                        ))
                        return@flatMapCompletable insertOrUpdateSession(lastSessionClosed)
                    }

                return@flatMapCompletable Completable.error(Throwable("No sessions closed"))
            }
        }

    override fun syncSessions(projectId: String): Completable {
        return sessionEventsLocalDbManager.loadSessions(projectId).flatMap { sessions ->
            if(sessions.size > 0 ) {
                sessions.forEach {
                    forceSessionToCloseIfOpenAndNotInProgress(it, timeHelper)
                    it.relativeUploadTime = it.nowRelativeToStartTime(timeHelper)
                    sessionEventsLocalDbManager.insertOrUpdateSessionEvents(it).blockingAwait()
                }

                sessions.filter { it.isClose() }.toTypedArray().let {
                    sessionsApi.uploadSessions(projectId, hashMapOf("sessions" to it))
                }
            } else {
                throw Throwable("No sessions To upload")
            }
        }.flatMapCompletable {
            if (!it.isError && it.response()?.code() == 200) {
                sessionEventsLocalDbManager.deleteSessions(projectId, false)
            } else {
                Completable.complete()
            }
        }
    }

    private fun forceSessionToCloseIfOpenAndNotInProgress(it: SessionEvents, timeHelper: TimeHelper) {
        if (it.isOpen() && !it.isPossiblyInProgress(timeHelper)) {
            it.addArtificialTerminationIfRequired(timeHelper, ArtificialTerminationEvent.Reason.TIMED_OUT)
            it.closeIfRequired(timeHelper)
        }
    }

    override fun addOneToOneMatchEventInBackground(patientId: String, startTimeVerification: Long, match: Verification?) {
        updateSessionInBackground({
            it.events.add(OneToOneMatchEvent(
                it.timeRelativeToStartTime(startTimeVerification),
                it.nowRelativeToStartTime(timeHelper),
                preferencesManager.patientId,
                match?.let { MatchCandidate(it.guid, match.confidence) }))
        })
    }

    override fun addOneToManyEventInBackground(startTimeIdentification: Long, matches: List<Identification>, matchSize: Int) {
        updateSessionInBackground({
            it.events.add(OneToManyMatchEvent(
                it.timeRelativeToStartTime(startTimeIdentification),
                it.nowRelativeToStartTime(timeHelper),
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
        updateSessionInBackground({
            val scannerConnectivityEvents = it.events.filterIsInstance(ScannerConnectionEvent::class.java)
            scannerConnectivityEvents.forEach { it.scannerInfo.hardwareVersion = hardwareVersion }
        })
    }

    override fun addPersonCreationEventInBackground(person: Person) {
        updateSessionInBackground({
            it.events.add(PersonCreationEvent(
                it.nowRelativeToStartTime(timeHelper),
                extractCaptureEventIdsBasedOnPersonTemplate(it, person.fingerprints.map { Utils.byteArrayToBase64(it.templateBytes) })
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

    // It extracts CaptureEvents Ids with the templates used to create the "Person" object for
    // identification, verification, enrolment.
    private fun extractCaptureEventIdsBasedOnPersonTemplate(sessionEvents: SessionEvents, personTemplates: List<String>): List<String> =
        sessionEvents.events
            .filterIsInstance(FingerprintCaptureEvent::class.java)
            .filter { it.fingerprint?.template in personTemplates }
            .map { it.id }

}
