package com.simprints.id.data.db.session

import android.annotation.SuppressLint
import android.os.Build
import com.simprints.core.tools.EncodingUtils
import com.simprints.core.tools.extentions.completableWithSuspend
import com.simprints.core.tools.extentions.singleWithSuspend
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.db.person.domain.FingerprintSample
import com.simprints.id.data.db.session.domain.models.events.*
import com.simprints.id.data.db.session.domain.models.events.EventType.CALLBACK_IDENTIFICATION
import com.simprints.id.data.db.session.domain.models.events.EventType.GUID_SELECTION
import com.simprints.id.data.db.session.domain.models.session.DatabaseInfo
import com.simprints.id.data.db.session.domain.models.session.Device
import com.simprints.id.data.db.session.domain.models.session.SessionEvents
import com.simprints.id.data.db.session.local.SessionLocalDataSource
import com.simprints.id.data.db.session.local.SessionLocalDataSource.Query
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
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.flow.toList
import timber.log.Timber

// Class to manage the current activeSession
open class SessionRepositoryImpl(private val deviceId: String,
                                 private val appVersionName: String,
                                 private val sessionEventsSyncManager: SessionEventsSyncManager,
                                 private val sessionLocalDataSource: SessionLocalDataSource,
                                 private val preferencesManager: PreferencesManager,
                                 private val timeHelper: TimeHelper,
                                 private val crashReportManager: CrashReportManager) :

    SessionRepository,
    SessionLocalDataSource by sessionLocalDataSource {

    companion object {
        const val PROJECT_ID_FOR_NOT_SIGNED_IN = "NOT_SIGNED_IN"
    }

    // as default, the manager tries to load the last open activeSession
    //
    override suspend fun getCurrentSession(): SessionEvents =
        sessionLocalDataSource.load(Query(openSession = true)).first()

    override suspend fun createSession(libSimprintsVersionName: String): SessionEvents {
        val count = sessionLocalDataSource.count(Query())

        val session = SessionEvents(
            PROJECT_ID_FOR_NOT_SIGNED_IN,
            appVersionName,
            libSimprintsVersionName,
            preferencesManager.language,
            Device(
                Build.VERSION.SDK_INT.toString(),
                Build.MANUFACTURER + "_" + Build.MODEL,
                deviceId),
            timeHelper.now(),
            DatabaseInfo(count))

        closeLastSessionsIfPending()
        sessionLocalDataSource.create(session)
        return session
    }


    private suspend fun closeLastSessionsIfPending() {
        val openSessions = sessionLocalDataSource.load(Query(openSession = true))
        openSessions.collect {
            val artificialTerminationEvent = ArtificialTerminationEvent(timeHelper.now(), ArtificialTerminationEvent.Reason.NEW_SESSION)
            sessionLocalDataSource.addEvent(it.id, listOf(artificialTerminationEvent))
            sessionLocalDataSource.closeSession(it.id)
        }
    }

    override suspend fun addGuidSelectionEvent(selectedGuid: String, sessionId: String) {
        val currentSession = getCurrentSession()

        if (currentSession.isOpen() &&
            !currentSession.hasEvent(GUID_SELECTION) &&
            currentSession.hasEvent(CALLBACK_IDENTIFICATION)) {

            val guidEvent = GuidSelectionEvent(timeHelper.now(), selectedGuid)
            sessionLocalDataSource.addEvent(currentSession.id, listOf(guidEvent))
        }
    }

    override fun updateHardwareVersionInScannerConnectivityEvent(hardwareVersion: String) {
        updateSessionInBackground { session ->
            val scannerConnectivityEvents = session.events.filterIsInstance(ScannerConnectionEvent::class.java)
            scannerConnectivityEvents.forEach { it.scannerInfo.hardwareVersion = hardwareVersion }
        }
    }

    override fun addPersonCreationEventInBackground(fingerprintSamples: List<FingerprintSample>) {
        updateSessionInBackground { session ->
            session.addEvent(PersonCreationEvent(
                timeHelper.now(),
                extractCaptureEventIdsBasedOnPersonTemplate(session, fingerprintSamples.map { EncodingUtils.byteArrayToBase64(it.template) })
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
        completableWithSuspend { sessionLocalDataSource.delete(Query(openSession = false)) }
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
