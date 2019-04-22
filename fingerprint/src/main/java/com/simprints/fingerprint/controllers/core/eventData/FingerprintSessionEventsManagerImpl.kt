package com.simprints.fingerprint.controllers.core.eventData

import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.analytics.eventdata.models.domain.events.MatchEntry

class FingerprintSessionEventsManagerImpl(private val sessionEventsManager: SessionEventsManager): FingerprintSessionEventsManager {

    override fun addOneToOneMatchEventInBackground(patientId: String, matchStartTime: Long, verificationResult: MatchEntry) =
        sessionEventsManager.addOneToOneMatchEventInBackground(patientId, matchStartTime, verificationResult)

    override fun addOneToManyEventInBackground(matchStartTime: Long, map: List<MatchEntry>, size: Int) =
        sessionEventsManager.addOneToManyEventInBackground(matchStartTime, map, size)
}
