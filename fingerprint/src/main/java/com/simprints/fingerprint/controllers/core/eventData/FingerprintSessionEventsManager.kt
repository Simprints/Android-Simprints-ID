package com.simprints.fingerprint.controllers.core.eventData

import com.simprints.fingerprint.controllers.core.eventData.model.*
import com.simprints.fingerprint.data.domain.alert.FingerprintAlert
import com.simprints.fingerprint.data.domain.person.Person
import io.reactivex.Completable

interface FingerprintSessionEventsManager {

    fun addPersonCreationEventInBackground(person: Person)
    fun addAlertEventInBackground(now: Long, alertType: FingerprintAlert)
    fun addLocationToSessionInBackground(latitude: Double, longitude: Double)
    fun updateHardwareVersionInScannerConnectivityEvent(hardwareVersion: String)
    fun addEventForScannerConnectivityInBackground(scannerInfo: ScannerConnectionEvent.ScannerInfo)
    fun addOneToManyEventInBackground(matchStartTime: Long, matchEntries: List<MatchEntry>, size: Int)
    fun addOneToOneMatchEventInBackground(patientId: String, matchStartTime: Long, verificationResult: MatchEntry)
    fun addRefusalEvent(now: Long, refusalStartTime: Long, answer: RefusalAnswer, otherText: String): Completable

    fun addEventForCandidateReadInBackground(guid: String,
                                             startCandidateSearchTime: Long,
                                             localResult: CandidateReadEvent.LocalResult,
                                             remoteResult: CandidateReadEvent.RemoteResult?)

    fun addFingerprintCaptureEventInBackground(now: Long,
                                               lastCaptureStartedAt: Long,
                                               fingerprintCaptureEvent: FingerprintCaptureEvent)
    fun addConsentEventInBackground(now: Long,
                                    lastCaptureStartedAt: Long,
                                    consentEvent: ConsentEvent)

}
