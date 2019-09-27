package com.simprints.id.data.analytics.eventdata.controllers.domain

import android.annotation.SuppressLint
import android.os.Build
import com.simprints.core.tools.EncodingUtils
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.eventdata.controllers.local.SessionEventsLocalDbManager
import com.simprints.id.data.analytics.eventdata.models.domain.events.*
import com.simprints.id.data.analytics.eventdata.models.domain.events.EventType.CALLBACK_IDENTIFICATION
import com.simprints.id.data.analytics.eventdata.models.domain.events.EventType.GUID_SELECTION
import com.simprints.id.data.analytics.eventdata.models.domain.session.DatabaseInfo
import com.simprints.id.data.analytics.eventdata.models.domain.session.Device
import com.simprints.id.data.analytics.eventdata.models.domain.session.SessionEvents
import com.simprints.id.data.db.person.domain.Person
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.exceptions.safe.session.NoSessionsFoundException
import com.simprints.id.exceptions.unexpected.AttemptedToModifyASessionAlreadyClosedException
import com.simprints.id.exceptions.unexpected.InvalidSessionForGuidSelectionEvent
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

    override fun createSession(libSimprintsVersionName: String): Single<SessionEvents> =
        sessionEventsLocalDbManager.getSessionCount().flatMap {
            createSessionWithAvailableInfo(
                PROJECT_ID_FOR_NOT_SIGNED_IN,
                libSimprintsVersionName,
                appVersionName,
                DatabaseInfo(it)).let {
                Timber.d("Created session: ${it.id}")
                sessionEventsSyncManager.scheduleSessionsSync()

                closeLastSessionsIfPending()
                    .andThen(insertOrUpdateSessionEvents(it))
                    .toSingle { it }
            }
        }


    private fun createSessionWithAvailableInfo(projectId: String,
                                               libVersionName: String,
                                               appVersionName: String,
                                               databaseInfo: DatabaseInfo): SessionEvents =
        SessionEvents(
            projectId,
            appVersionName,
            libVersionName,
            preferencesManager.language,
            Device(
                Build.VERSION.SDK_INT.toString(),
                Build.MANUFACTURER + "_" + Build.MODEL,
                deviceId),
            timeHelper.now(),
            databaseInfo)

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
            crashReportManager.logExceptionOrSafeException(it)
        }.onErrorComplete() // because events are low priority, it swallows the exception

    @SuppressLint("CheckResult")
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
            crashReportManager.logExceptionOrSafeException(it)
        }.onErrorComplete()

    override fun addGuidSelectionEvent(selectedGuid: String, sessionId: String): Completable =
        sessionEventsLocalDbManager.loadSessionById(sessionId).flatMapCompletable { session ->

            if (session.isOpen() &&
                !session.hasEvent(GUID_SELECTION) &&
                session.hasEvent(CALLBACK_IDENTIFICATION)) {

                session.addEvent(GuidSelectionEvent(timeHelper.now(), selectedGuid))
                insertOrUpdateSessionEvents(session)
            } else {
                Completable.error(InvalidSessionForGuidSelectionEvent("open: ${session.isOpen()}"))
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
                timeHelper.now(),
                extractCaptureEventIdsBasedOnPersonTemplate(session, person.fingerprintSamples.map { EncodingUtils.byteArrayToBase64(it.template) })
            ))
        }
    }

    override fun addEventInBackground(sessionEvent: Event) {
        updateSessionInBackground {
            it.addEvent(sessionEvent)
        }
    }

    override fun addEvent(sessionEvent: Event): Completable =
        updateSession {
            it.addEvent(sessionEvent)
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
