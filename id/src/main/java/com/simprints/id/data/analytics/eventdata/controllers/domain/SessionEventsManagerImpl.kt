package com.simprints.id.data.analytics.eventdata.controllers.domain

import android.os.Build
import com.simprints.core.tools.EncodingUtils
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.eventdata.controllers.local.SessionEventsLocalDbManager
import com.simprints.id.data.analytics.eventdata.models.domain.events.*
import com.simprints.id.data.analytics.eventdata.models.domain.session.Device
import com.simprints.id.data.analytics.eventdata.models.domain.session.Location
import com.simprints.id.data.analytics.eventdata.models.domain.session.SessionEvents
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.Person
import com.simprints.id.exceptions.safe.session.NoSessionsFoundException
import com.simprints.id.exceptions.unexpected.AttemptedToModifyASessionAlreadyClosedException
import com.simprints.id.exceptions.unexpected.SessionNotFoundException
import com.simprints.id.services.scheduledSync.sessionSync.SessionEventsSyncManager
import com.simprints.id.tools.TimeHelper
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

// Class to manage the current activeSession
open class SessionEventsManagerImpl(private val deviceId: String,
                                    private val appVersionName: String,
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
        createSessionWithAvailableInfo(PROJECT_ID_FOR_NOT_SIGNED_IN, appVersionName).let {
            Timber.d("Created session: ${it.id}")
            sessionEventsSyncManager.scheduleSessionsSync()

            closeLastSessionsIfPending()
                .andThen(insertOrUpdateSessionEvents(it))
                .toSingle { it }
        }

    private fun createSessionWithAvailableInfo(projectId: String, appVersionName: String): SessionEvents =
        SessionEvents(
            projectId,
            appVersionName,
            /* preferencesManager.libVersionName */ "", //StopShip: do we need libVersionName?
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
            it.addEvent(GuidSelectionEvent(
                it.timeRelativeToStartTime(timeHelper.now()),
                selectedGuid
            ))
            insertOrUpdateSessionEvents(it)
        }

    override fun addOneToOneMatchEventInBackground(patientId: String, startTimeVerification: Long, match: MatchEntry?) {
        updateSessionInBackground { session ->
            session.addEvent(OneToOneMatchEvent(
                session.timeRelativeToStartTime(startTimeVerification),
                session.timeRelativeToStartTime(timeHelper.now()),
                patientId,
                match))
        }
    }

    override fun addOneToManyEventInBackground(startTimeIdentification: Long, matches: List<MatchEntry>, matchSize: Int) {
        updateSessionInBackground { session ->
            session.addEvent(OneToManyMatchEvent(
                session.timeRelativeToStartTime(startTimeIdentification),
                session.timeRelativeToStartTime(timeHelper.now()),
                OneToManyMatchEvent.MatchPool(OneToManyMatchEvent.MatchPoolType.fromConstantGroup(preferencesManager.matchGroup), matchSize),
                matches))
        }
    }

    override fun addEventForScannerConnectivityInBackground(scannerInfo: ScannerConnectionEvent.ScannerInfo) {
        updateSessionInBackground {
            if (it.events.filterIsInstance(ScannerConnectionEvent::class.java).isEmpty()) {
                it.addEvent(ScannerConnectionEvent(
                    it.timeRelativeToStartTime(timeHelper.now()),
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
            session.addEvent(PersonCreationEvent(
                session.timeRelativeToStartTime(timeHelper.now()),
                extractCaptureEventIdsBasedOnPersonTemplate(session, person.fingerprints.map { EncodingUtils.byteArrayToBase64(it.templateBytes) })
            ))
        }
    }

    override fun addEventForCandidateReadInBackground(guid: String,
                                                      startCandidateSearchTime: Long,
                                                      localResult: CandidateReadEvent.LocalResult,
                                                      remoteResult: CandidateReadEvent.RemoteResult?) {
        updateSessionInBackground {
            it.addEvent(CandidateReadEvent(
                it.timeRelativeToStartTime(startCandidateSearchTime),
                it.timeRelativeToStartTime(timeHelper.now()),
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

    override fun addSessionEvent(sessionEvent: Event) {
        updateSessionInBackground {
            it.addEvent(sessionEvent)
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
