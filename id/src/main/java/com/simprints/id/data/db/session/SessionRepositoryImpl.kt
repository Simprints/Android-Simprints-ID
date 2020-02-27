package com.simprints.id.data.db.session

import com.simprints.core.tools.EncodingUtils
import com.simprints.core.tools.extentions.completableWithSuspend
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.db.person.domain.FingerprintSample
import com.simprints.id.data.db.session.domain.models.events.EventType.CALLBACK_IDENTIFICATION
import com.simprints.id.data.db.session.domain.models.events.EventType.GUID_SELECTION
import com.simprints.id.data.db.session.domain.models.events.FingerprintCaptureEvent
import com.simprints.id.data.db.session.domain.models.events.GuidSelectionEvent
import com.simprints.id.data.db.session.domain.models.events.PersonCreationEvent
import com.simprints.id.data.db.session.domain.models.events.ScannerConnectionEvent
import com.simprints.id.data.db.session.domain.models.session.SessionEvents
import com.simprints.id.data.db.session.local.SessionLocalDataSource
import com.simprints.id.data.db.session.local.SessionLocalDataSource.Query
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.services.scheduledSync.sessionSync.SessionEventsSyncManager
import com.simprints.id.tools.TimeHelper
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.flow.first

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

    override suspend fun createSession(libSimprintsVersionName: String) =
        sessionLocalDataSource.create(appVersionName, libSimprintsVersionName, preferencesManager.language, deviceId)

    override suspend fun addGuidSelectionEvent(selectedGuid: String, sessionId: String) {
        sessionLocalDataSource.updateCurrentSession { currentSession ->
            if (currentSession.hasEvent(GUID_SELECTION) &&
                currentSession.hasEvent(CALLBACK_IDENTIFICATION)) {

                val guidEvent = GuidSelectionEvent(timeHelper.now(), selectedGuid)
                currentSession.events.add(guidEvent)
            }
        }
    }

    override suspend fun updateHardwareVersionInScannerConnectivityEvent(hardwareVersion: String) {
        sessionLocalDataSource.updateCurrentSession { currentSession ->
            val scannerConnectivityEvents = currentSession.events.filterIsInstance(ScannerConnectionEvent::class.java)
            scannerConnectivityEvents.forEach { it.scannerInfo.hardwareVersion = hardwareVersion }
        }
    }

    override suspend fun addPersonCreationEventInBackground(fingerprintSamples: List<FingerprintSample>) {
        sessionLocalDataSource.updateCurrentSession { currentSession ->
            currentSession.events.add(PersonCreationEvent(
                timeHelper.now(),
                extractCaptureEventIdsBasedOnPersonTemplate(currentSession, fingerprintSamples.map { EncodingUtils.byteArrayToBase64(it.template) })
            ))
        }
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
