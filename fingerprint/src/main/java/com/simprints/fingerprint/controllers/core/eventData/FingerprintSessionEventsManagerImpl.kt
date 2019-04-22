package com.simprints.fingerprint.controllers.core.eventData

import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.analytics.eventdata.models.domain.events.CandidateReadEvent
import com.simprints.id.data.analytics.eventdata.models.domain.events.MatchEntry
import com.simprints.id.data.analytics.eventdata.models.domain.events.ScannerConnectionEvent
import com.simprints.id.data.analytics.eventdata.models.domain.session.SessionEvents
import com.simprints.id.domain.fingerprint.Person
import io.reactivex.Completable

class FingerprintSessionEventsManagerImpl(private val sessionEventsManager: SessionEventsManager): FingerprintSessionEventsManager {

    override fun addPersonCreationEventInBackground(person: Person) =
        sessionEventsManager.addPersonCreationEventInBackground(person)

    override fun updateSessionInBackground(block: (sessionEvents: SessionEvents) -> Unit) =
        sessionEventsManager.updateSessionInBackground(block)

    override fun updateHardwareVersionInScannerConnectivityEvent(hardwareVersion: String) =
        sessionEventsManager.updateHardwareVersionInScannerConnectivityEvent(hardwareVersion)

    override fun addEventForCandidateReadInBackground(guid: String,
                                                      startCandidateSearchTime: Long,
                                                      localResult: CandidateReadEvent.LocalResult,
                                                      remoteResult: CandidateReadEvent.RemoteResult?) =
        sessionEventsManager.addEventForCandidateReadInBackground(guid, startCandidateSearchTime, localResult, remoteResult)

    override fun addLocationToSession(latitude: Double, longitude: Double) =
        sessionEventsManager.addLocationToSession(latitude, longitude)

    override fun addEventForScannerConnectivityInBackground(scannerInfo: ScannerConnectionEvent.ScannerInfo) =
        sessionEventsManager.addEventForScannerConnectivityInBackground(scannerInfo)

    override fun addOneToOneMatchEventInBackground(patientId: String, matchStartTime: Long, verificationResult: MatchEntry) =
        sessionEventsManager.addOneToOneMatchEventInBackground(patientId, matchStartTime, verificationResult)

    override fun addOneToManyEventInBackground(matchStartTime: Long, map: List<MatchEntry>, size: Int) =
        sessionEventsManager.addOneToManyEventInBackground(matchStartTime, map, size)

    override fun updateSession(block: (sessionEvents: SessionEvents) -> Unit): Completable =
        sessionEventsManager.updateSession(block)
}
