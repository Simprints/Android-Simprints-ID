package com.simprints.id.data.db.session

import com.simprints.id.Application
import com.simprints.id.data.db.person.domain.FingerprintSample
import com.simprints.id.data.db.session.domain.models.events.Event
import com.simprints.id.data.db.session.domain.models.session.SessionEvents
import com.simprints.id.exceptions.safe.session.SessionDataSourceException

interface SessionRepository {


    suspend fun createSession(libSimprintsVersionName: String)
    suspend fun addGuidSelectionEvent(selectedGuid: String, sessionId: String)
    suspend fun addPersonCreationEvent(fingerprintSamples: List<FingerprintSample>)
    suspend fun updateHardwareVersionInScannerConnectivityEvent(hardwareVersion: String)

    @Throws(SessionDataSourceException::class)
    suspend fun getCurrentSession(): SessionEvents

    @Throws(SessionDataSourceException::class)
    suspend fun updateCurrentSession(updateBlock: (SessionEvents) -> Unit)

    fun addEventToCurrentSessionInBackground(event: Event)

    suspend fun signOut()


    suspend fun startUploadingSessions()

    companion object {
        fun build(app: Application): SessionRepository =
            app.component.getSessionEventsManager()
    }
}
