package com.simprints.id.data.db.session

import com.simprints.id.Application
import com.simprints.id.data.db.person.domain.FingerprintSample
import com.simprints.id.data.db.session.domain.models.session.SessionEvents
import com.simprints.id.data.db.session.local.SessionLocalDataSource

interface SessionRepository : SessionLocalDataSource {

    fun signOut()

    suspend fun createSession(libSimprintsVersionName: String): SessionEvents
    suspend fun getCurrentSession(): SessionEvents
    suspend fun addGuidSelectionEvent(selectedGuid: String, sessionId: String)

    fun addPersonCreationEventInBackground(fingerprintSamples: List<FingerprintSample>)
    fun updateHardwareVersionInScannerConnectivityEvent(hardwareVersion: String)

    companion object {
        fun build(app: Application): SessionRepository =
            app.component.getSessionEventsManager()
    }
}
