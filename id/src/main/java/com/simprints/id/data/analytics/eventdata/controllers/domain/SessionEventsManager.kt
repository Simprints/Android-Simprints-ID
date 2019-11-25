package com.simprints.id.data.analytics.eventdata.controllers.domain

import com.simprints.id.Application
import com.simprints.id.data.analytics.eventdata.controllers.local.SessionEventsLocalDbManager
import com.simprints.id.data.analytics.eventdata.models.domain.events.Event
import com.simprints.id.data.analytics.eventdata.models.domain.session.SessionEvents
import com.simprints.id.data.db.person.domain.FingerprintSample
import io.reactivex.Completable
import io.reactivex.Single

interface SessionEventsManager : SessionEventsLocalDbManager {

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
        fun build(app: Application): SessionEventsManager =
            app.component.getSessionEventsManager()
    }
}
