package com.simprints.fingerprint.controllers.core.eventData

import com.simprints.fingerprint.controllers.core.eventData.model.*
import com.simprints.fingerprint.data.domain.alert.FingerprintAlert
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.analytics.eventdata.models.domain.events.AlertScreenEvent
import com.simprints.id.data.analytics.eventdata.models.domain.events.RefusalEvent
import com.simprints.id.domain.fingerprint.Person
import io.reactivex.Completable

class FingerprintSessionEventsManagerImpl(private val sessionEventsManager: SessionEventsManager) : FingerprintSessionEventsManager {

    override fun addPersonCreationEventInBackground(person: Person) =
        sessionEventsManager.addPersonCreationEventInBackground(person)

    override fun updateHardwareVersionInScannerConnectivityEvent(hardwareVersion: String) =
        sessionEventsManager.updateHardwareVersionInScannerConnectivityEvent(hardwareVersion)

    override fun addEventForCandidateReadInBackground(guid: String,
                                                      startCandidateSearchTime: Long,
                                                      localResult: CandidateReadEvent.LocalResult,
                                                      remoteResult: CandidateReadEvent.RemoteResult?) =
        sessionEventsManager.addEventForCandidateReadInBackground(
            guid,
            startCandidateSearchTime,
            localResult.fromDomainToCore(),
            remoteResult?.fromDomainToCore())

    override fun addLocationToSessionInBackground(latitude: Double, longitude: Double) =
        sessionEventsManager.addLocationToSession(latitude, longitude)

    override fun addEventForScannerConnectivityInBackground(scannerInfo: ScannerConnectionEvent.ScannerInfo) =
        sessionEventsManager.addEventForScannerConnectivityInBackground(scannerInfo.fromDomainToCore())

    override fun addOneToOneMatchEventInBackground(patientId: String, matchStartTime: Long, verificationResult: MatchEntry) =
        sessionEventsManager.addOneToOneMatchEventInBackground(patientId, matchStartTime, verificationResult.fromDomainToCore())

    override fun addOneToManyEventInBackground(matchStartTime: Long, matchEntries: List<MatchEntry>, size: Int) =
        sessionEventsManager.addOneToManyEventInBackground(matchStartTime, matchEntries.map { it.fromDomainToCore() }, size)

    override fun addRefusalEvent(now: Long, refusalStartTime: Long, answer: RefusalAnswer, otherText: String): Completable =
        sessionEventsManager.updateSession {
            it.addEvent(RefusalEvent(
                it.timeRelativeToStartTime(refusalStartTime),
                it.timeRelativeToStartTime(now),
                answer.fromDomainToCore(),
                otherText))
        }

    override fun addFingerprintCaptureEventInBackground(now: Long,
                                                        lastCaptureStartedAt: Long,
                                                        fingerprintCaptureEvent: FingerprintCaptureEvent) {

        sessionEventsManager.updateSessionInBackground {
            it.addEvent(fingerprintCaptureEvent.fromDomainToCore(
                it.timeRelativeToStartTime(lastCaptureStartedAt),
                it.timeRelativeToStartTime(now)))
        }
    }

    override fun addConsentEventInBackground(now: Long,
                                             lastCaptureStartedAt: Long,
                                             consentEvent: ConsentEvent) {
        sessionEventsManager.updateSessionInBackground {
            it.addEvent(consentEvent.fromDomainToCore(
                it.timeRelativeToStartTime(lastCaptureStartedAt),
                it.timeRelativeToStartTime(now)))

            if (consentEvent.result == ConsentEvent.Result.DECLINED || consentEvent.result == ConsentEvent.Result.NO_RESPONSE) {
                it.location = null
            }
        }
    }

    override fun addAlertEventInBackground(now: Long,
                                           alertType: FingerprintAlert) {
        sessionEventsManager.updateSessionInBackground {
            it.addEvent(AlertScreenEvent(it.timeRelativeToStartTime(now), alertType.name))
        }
    }

}
