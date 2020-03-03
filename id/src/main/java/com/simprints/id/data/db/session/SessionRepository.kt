package com.simprints.id.data.db.session

import com.simprints.id.Application
import com.simprints.id.data.db.person.domain.FingerprintSample
import com.simprints.id.data.db.session.domain.models.events.Event
import com.simprints.id.data.db.session.domain.models.session.SessionEvents

interface SessionRepository {

    fun signOut()

    suspend fun createSession(libSimprintsVersionName: String)
    suspend fun getCurrentSession(): SessionEvents

    suspend fun addGuidSelectionEvent(selectedGuid: String, sessionId: String)
    suspend fun addPersonCreationEvent(fingerprintSamples: List<FingerprintSample>)
    suspend fun updateHardwareVersionInScannerConnectivityEvent(hardwareVersion: String)

    fun addEventToCurrentSessionInBackground(event: Event)
    suspend fun updateCurrentSession(updateBlock: (SessionEvents) -> Unit)

    companion object {
        fun build(app: Application): SessionRepository =
            app.component.getSessionEventsManager()
    }
}
