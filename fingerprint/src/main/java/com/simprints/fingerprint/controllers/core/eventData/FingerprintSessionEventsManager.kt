package com.simprints.fingerprint.controllers.core.eventData

import com.simprints.id.data.analytics.eventdata.models.domain.events.CandidateReadEvent
import com.simprints.id.data.analytics.eventdata.models.domain.events.MatchEntry
import com.simprints.id.data.analytics.eventdata.models.domain.events.ScannerConnectionEvent
import com.simprints.id.data.analytics.eventdata.models.domain.session.SessionEvents
import com.simprints.id.domain.fingerprint.Person

//TODO: Not use shared model class.
interface FingerprintSessionEventsManager {

    fun addOneToManyEventInBackground(matchStartTime: Long, map: List<MatchEntry>, size: Int)
    fun addOneToOneMatchEventInBackground(patientId: String, matchStartTime: Long, verificationResult: MatchEntry)
    fun addPersonCreationEventInBackground(person: Person)
    fun updateSessionInBackground(block: (sessionEvents: SessionEvents) -> Unit)
    fun updateHardwareVersionInScannerConnectivityEvent(hardwareVersion: String)
    fun addEventForCandidateReadInBackground(guid: String,
                                             startCandidateSearchTime: Long,
                                             localResult: CandidateReadEvent.LocalResult,
                                             remoteResult: CandidateReadEvent.RemoteResult?)

    fun addLocationToSession(latitude: Double, longitude: Double)
    fun addEventForScannerConnectivityInBackground(scannerInfo: ScannerConnectionEvent.ScannerInfo)
}
