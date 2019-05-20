package com.simprints.id.data.analytics.eventdata.controllers.domain

import com.simprints.id.data.analytics.eventdata.controllers.local.SessionEventsLocalDbManager
import com.simprints.id.data.analytics.eventdata.models.domain.events.CandidateReadEvent
import com.simprints.id.data.analytics.eventdata.models.domain.events.Event
import com.simprints.id.data.analytics.eventdata.models.domain.events.MatchEntry
import com.simprints.id.data.analytics.eventdata.models.domain.events.ScannerConnectionEvent
import com.simprints.id.data.analytics.eventdata.models.domain.session.SessionEvents
import com.simprints.id.domain.Person
import io.reactivex.Completable
import io.reactivex.Single

interface SessionEventsManager: SessionEventsLocalDbManager {

    fun signOut()

    fun createSession(libSimprintsVersionName: String): Single<SessionEvents>
    fun getCurrentSession(): Single<SessionEvents>

    fun addEvent(sessionEvent: Event): Completable
    fun addEventInBackground(sessionEvent: Event)

    fun updateSession(block: (sessionEvents: SessionEvents) -> Unit): Completable
    fun updateSessionInBackground(block: (sessionEvents: SessionEvents) -> Unit)

    fun addGuidSelectionEventToLastIdentificationIfExists(selectedGuid: String, sessionId: String): Completable
    fun addPersonCreationEventInBackground(person: Person)
    fun updateHardwareVersionInScannerConnectivityEvent(hardwareVersion: String)
}
