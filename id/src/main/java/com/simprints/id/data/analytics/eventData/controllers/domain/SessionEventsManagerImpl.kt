package com.simprints.id.data.analytics.eventData.controllers.domain

import android.content.Context
import android.os.Build
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.analytics.eventData.controllers.local.SessionEventsLocalDbManager
import com.simprints.id.data.analytics.eventData.models.domain.events.*
import com.simprints.id.data.analytics.eventData.models.domain.session.Device
import com.simprints.id.data.analytics.eventData.models.domain.session.Location
import com.simprints.id.data.analytics.eventData.models.domain.session.SessionEvents
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.exceptions.safe.session.AttemptedToModifyASessionAlreadyClosed
import com.simprints.id.exceptions.safe.session.SessionNotFoundException
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.extensions.deviceId
import com.simprints.libcommon.Person
import com.simprints.libcommon.Utils
import com.simprints.libsimprints.Identification
import com.simprints.libsimprints.Verification
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber

// Class to manage the current activeSession
open class SessionEventsManagerImpl(private val ctx: Context,
                                    private val sessionEventsLocalDbManager: SessionEventsLocalDbManager,
                                    private val preferencesManager: PreferencesManager,
                                    private val timeHelper: TimeHelper,
                                    private val analyticsManager: AnalyticsManager):
    SessionEventsManager,
    SessionEventsLocalDbManager by sessionEventsLocalDbManager {

    companion object {
        const val PROJECT_ID_FOR_NOT_SIGNED_IN = "NOT_SIGNED_IN"
    }

    private var activeSessionId: String? = null

    //as default, the manager tries to load the last open activeSession
    override fun getCurrentSession(): Single<SessionEvents> =
        activeSessionId?.let {
            sessionEventsLocalDbManager.loadSessionById(it)
        } ?: sessionEventsLocalDbManager.loadSessions(openSession = true).map { it[0] }.doOnSuccess {
            activeSessionId = it.id
        }

    override fun createSession(): Single<SessionEvents> =
        createSessionWithAvailableInfo(PROJECT_ID_FOR_NOT_SIGNED_IN).let {
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
                ctx.deviceId),
            timeHelper.now())

    override fun updateSession(block: (sessionEvents: SessionEvents) -> Unit): Completable =
        getCurrentSession().flatMapCompletable {
            if (it.isOpen()) {
                block(it)
                insertOrUpdateSessionEvents(it)
            } else {
                throw AttemptedToModifyASessionAlreadyClosed()
            }
        }.doOnError {
            Timber.e(it)
            analyticsManager.logThrowable(it)
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
            analyticsManager.logThrowable(it)
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

    override fun addOneToOneMatchEventInBackground(patientId: String, startTimeVerification: Long, match: Verification?) {
        updateSessionInBackground { session ->
            session.events.add(OneToOneMatchEvent(
                session.timeRelativeToStartTime(startTimeVerification),
                session.nowRelativeToStartTime(timeHelper),
                preferencesManager.patientId,
                match?.let { MatchEntry(it.guid, match.confidence) }))
        }
    }

    override fun addOneToManyEventInBackground(startTimeIdentification: Long, matches: List<Identification>, matchSize: Int) {
        updateSessionInBackground { session ->
            session.events.add(OneToManyMatchEvent(
                session.timeRelativeToStartTime(startTimeIdentification),
                session.nowRelativeToStartTime(timeHelper),
                OneToManyMatchEvent.MatchPool(OneToManyMatchEvent.MatchPoolType.fromConstantGroup(preferencesManager.matchGroup), matchSize),
                matches.map { MatchEntry(it.guid, it.confidence) }.toList().toTypedArray()))
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

    // It extracts CaptureEvents Ids with the templates used to create the "Person" object for
    // identification, verification, enrolment.
    private fun extractCaptureEventIdsBasedOnPersonTemplate(sessionEvents: SessionEvents, personTemplates: List<String>): List<String> =
        sessionEvents.events
            .filterIsInstance(FingerprintCaptureEvent::class.java)
            .filter { it.fingerprint?.template in personTemplates && it.result != FingerprintCaptureEvent.Result.SKIPPED }
            .map { it.id }
}
