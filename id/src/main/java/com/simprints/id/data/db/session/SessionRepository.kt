package com.simprints.id.data.db.session

import com.simprints.id.Application
import com.simprints.id.data.db.person.domain.FingerprintSample
import com.simprints.id.data.db.session.domain.models.events.Event
import com.simprints.id.data.db.session.domain.models.session.SessionEvents
import com.simprints.id.data.db.session.local.SessionEventsLocalDbManager
import io.reactivex.Completable
import io.reactivex.Single

interface SessionRepository : SessionEventsLocalDbManager {

    fun signOut()

    fun createSession(libSimprintsVersionName: String): Single<SessionEvents>
    fun getCurrentSession(): Single<SessionEvents>

    fun addEvent(sessionEvent: Event): Completable
    fun addEventInBackground(sessionEvent: Event)

    fun updateSession(block: (sessionEvents: SessionEvents) -> Unit): Completable
    fun updateSessionInBackground(block: (sessionEvents: SessionEvents) -> Unit)

    fun addGuidSelectionEvent(selectedGuid: String, sessionId: String): Completable
    fun addPersonCreationEventInBackground(fingerprintSamples: List<FingerprintSample>)
    fun updateHardwareVersionInScannerConnectivityEvent(hardwareVersion: String)

    companion object {
        fun build(app: Application): SessionRepository =
            app.component.getSessionEventsManager()
    }
}
