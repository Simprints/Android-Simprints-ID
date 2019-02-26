package com.simprints.id.data.analytics.eventdata.controllers.domain

import android.os.Build
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.eventdata.controllers.local.SessionEventsLocalDbManager
import com.simprints.id.data.analytics.eventdata.models.domain.events.*
import com.simprints.id.data.analytics.eventdata.models.domain.session.Device
import com.simprints.id.data.analytics.eventdata.models.domain.session.Location
import com.simprints.id.data.analytics.eventdata.models.domain.session.SessionEvents
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.exceptions.unexpected.AttemptedToModifyASessionAlreadyClosedException
import com.simprints.id.exceptions.safe.session.NoSessionsFoundException
import com.simprints.id.exceptions.unexpected.SessionNotFoundException
import com.simprints.id.services.scheduledSync.sessionSync.SessionEventsSyncManager
import com.simprints.id.tools.TimeHelper
import com.simprints.id.domain.fingerprint.Person
import com.simprints.id.domain.fingerprint.Utils
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

// Class to manage the current activeSession
open class SessionEventsManagerImpl(private val deviceId: String,
                                    private val sessionEventsSyncManager: SessionEventsSyncManager,
                                    private val sessionEventsLocalDbManager: SessionEventsLocalDbManager,
                                    private val preferencesManager: PreferencesManager,
                                    private val timeHelper: TimeHelper,
                                    private val crashReportManager: CrashReportManager) :

    SessionEventsManager,
    SessionEventsLocalDbManager by sessionEventsLocalDbManager {

    companion object {
        const val PROJECT_ID_FOR_NOT_SIGNED_IN = "NOT_SIGNED_IN"
    }

    //as default, the manager tries to load the last open activeSession
    override fun getCurrentSession(): Single<SessionEvents> =
        sessionEventsLocalDbManager.loadSessions(openSession = true).map {
            if (it.isEmpty())
                throw NoSessionsFoundException()
            it[0]
        }

    override fun createSession(): Single<SessionEvents> =
        createSessionWithAvailableInfo(PROJECT_ID_FOR_NOT_SIGNED_IN).let {
            Timber.d("Created session: ${it.id}")
            sessionEventsSyncManager.scheduleSessionsSync()

            closeLastSessionsIfPending()
                .andThen(insertOrUpdateSessionEvents(it))
                .toSingle { it }
        }

    private fun createSessionWithAvailableInfo(projectId: String): SessionEvents =
        SessionEvents(
            projectId,
            preferencesManager.appVersionName,
            preferencesManager.libVersionName,
            preferencesManager.language,
            Device(
                Build.VERSION.SDK_INT.toString(),
               Build.MANUFACTURER + "_" + Build.MODEL,
                deviceId),
            timeHelper.now())

    override fun updateSession(block: (sessionEvents: SessionEvents) -> Unit): Completable =
        getCurrentSession().flatMapCompletable {
            if (it.isOpen()) {
                block(it)
                insertOrUpdateSessionEvents(it)
            } else {
                throw AttemptedToModifyASessionAlreadyClosedException()
            }
        }.doOnError {
            Timber.e(it)
            crashReportManager.logExceptionOrThrowable(it)
        }.onErrorComplete() // because events are low priority, it swallows the exception

    override fun updateSessionInBackground(block: (sessionEvents: SessionEvents) -> Unit) {
        updateSession(block).subscribeBy()
    }

    private fun closeLastSessionsIfPending(): Completable =
        sessionEventsLocalDbManager.loadSessions(openSession = true).flatMapCompletable { openSessions ->

            openSessions.forEach {
                it.addArtificialTerminationIfRequired(timeHelper, ArtificialTerminationEvent.Reason.NEW_SESSION)
                it.closeIfRequired(timeHelper)
                insertOrUpdateSessionEvents(it).blockingAwait()
            }
            Completable.complete()
        }.doOnError {
            crashReportManager.logExceptionOrThrowable(it)
        }.onErrorComplete()

    /** @throws SessionNotFoundException */
    override fun addGuidSelectionEventToLastIdentificationIfExists(selectedGuid: String, sessionId: String): Completable =
        sessionEventsLocalDbManager.loadSessionById(sessionId).flatMapCompletable {
            it.events.add(GuidSelectionEvent(
                it.nowRelativeToStartTime(timeHelper),
                selectedGuid
            ))
            insertOrUpdateSessionEvents(it)
        }

    override fun addOneToOneMatchEventInBackground(patientId: String, startTimeVerification: Long, match: VerificationResult?) {
        updateSessionInBackground { session ->
            session.events.add(OneToOneMatchEvent(
                session.timeRelativeToStartTime(startTimeVerification),
                session.nowRelativeToStartTime(timeHelper),
                preferencesManager.patientId,
                match?.let { MatchEntry(it.guidVerified, match.confidence.toFloat()) }))
        }
    }

    override fun addOneToManyEventInBackground(startTimeIdentification: Long, matches: List<IdentificationResult>, matchSize: Int) {
        updateSessionInBackground { session ->
            session.events.add(OneToManyMatchEvent(
                session.timeRelativeToStartTime(startTimeIdentification),
                session.nowRelativeToStartTime(timeHelper),
                OneToManyMatchEvent.MatchPool(OneToManyMatchEvent.MatchPoolType.fromConstantGroup(preferencesManager.matchGroup), matchSize),
                matches.map { MatchEntry(it.guidFound, it.confidence.toFloat()) }.toList().toTypedArray()))
        }
    }

    override fun addEventForScannerConnectivityInBackground(scannerInfo: ScannerConnectionEvent.ScannerInfo) {
        updateSessionInBackground {
            if (it.events.filterIsInstance(ScannerConnectionEvent::class.java).isEmpty()) {
                it.events.add(ScannerConnectionEvent(
                    it.nowRelativeToStartTime(timeHelper),
                    scannerInfo
                ))
            }
        }
    }

    override fun updateHardwareVersionInScannerConnectivityEvent(hardwareVersion: String) {
        updateSessionInBackground { session ->
            val scannerConnectivityEvents = session.events.filterIsInstance(ScannerConnectionEvent::class.java)
            scannerConnectivityEvents.forEach { it.scannerInfo.hardwareVersion = hardwareVersion }
        }
    }

    override fun addPersonCreationEventInBackground(person: Person) {
        updateSessionInBackground { session ->
            session.events.add(PersonCreationEvent(
                session.nowRelativeToStartTime(timeHelper),
                extractCaptureEventIdsBasedOnPersonTemplate(session, person.fingerprints.map { Utils.byteArrayToBase64(it.templateBytes) })
            ))
        }
    }

    override fun addEventForCandidateReadInBackground(guid: String,
                                                      startCandidateSearchTime: Long,
                                                      localResult: CandidateReadEvent.LocalResult,
                                                      remoteResult: CandidateReadEvent.RemoteResult?) {
        updateSessionInBackground {
            it.events.add(CandidateReadEvent(
                it.timeRelativeToStartTime(startCandidateSearchTime),
                it.nowRelativeToStartTime(timeHelper),
                guid,
                localResult,
                remoteResult
            ))
        }
    }

    override fun addLocationToSession(latitude: Double, longitude: Double) {
        this.updateSessionInBackground { sessionEvents ->
            sessionEvents.location = Location(latitude, longitude)
        }
    }

    override fun signOut() {
        deleteSessions()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onComplete = {}, onError = {
                it.printStackTrace()
            })

        sessionEventsSyncManager.cancelSyncWorkers()
    }

    // It extracts CaptureEvents Ids with the templates used to create the "Person" object for
    // identification, verification, enrolment.
    private fun extractCaptureEventIdsBasedOnPersonTemplate(sessionEvents: SessionEvents, personTemplates: List<String>): List<String> =
        sessionEvents.events
            .filterIsInstance(FingerprintCaptureEvent::class.java)
            .filter { it.fingerprint?.template in personTemplates && it.result != FingerprintCaptureEvent.Result.SKIPPED }
            .map { it.id }
}
