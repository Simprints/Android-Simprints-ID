package com.simprints.id.data.db.session

import com.simprints.id.Application
import com.simprints.id.data.db.person.domain.FingerprintSample
import com.simprints.id.data.db.session.domain.models.SessionQuery
import com.simprints.id.data.db.session.domain.models.events.Event
import com.simprints.id.data.db.session.domain.models.session.SessionEvents
import kotlinx.coroutines.flow.Flow

interface SessionRepository {

    fun signOut()

    suspend fun createSession(libSimprintsVersionName: String)
    suspend fun getCurrentSession(): SessionEvents

    suspend fun addGuidSelectionEvent(selectedGuid: String, sessionId: String)
    suspend fun addPersonCreationEvent(fingerprintSamples: List<FingerprintSample>)
    suspend fun updateHardwareVersionInScannerConnectivityEvent(hardwareVersion: String)

    fun addEventToCurrentSessionInBackground(event: Event)
    suspend fun updateCurrentSession(updateBlock: (SessionEvents) -> Unit)
    @Deprecated("gonna remove it soon")
    suspend fun insertOrUpdateSessionEvents(sessionEvents: SessionEvents)
    suspend fun delete(query: SessionQuery)
    suspend fun load(query: SessionQuery): Flow<SessionEvents>


    suspend fun startUploadingSessions()

    companion object {
        fun build(app: Application): SessionRepository =
            app.component.getSessionEventsManager()
    }
}
