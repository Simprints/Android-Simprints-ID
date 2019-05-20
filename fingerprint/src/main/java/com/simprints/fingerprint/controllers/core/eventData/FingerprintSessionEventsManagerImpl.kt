package com.simprints.fingerprint.controllers.core.eventData

import com.simprints.fingerprint.controllers.core.eventData.model.*
import com.simprints.fingerprint.data.domain.person.Person
import com.simprints.fingerprint.data.domain.person.fromDomainToCore
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.analytics.eventdata.models.domain.session.Location
import io.reactivex.Completable
import com.simprints.id.data.analytics.eventdata.models.domain.events.Event as CoreEvent
import com.simprints.fingerprint.controllers.core.eventData.model.EventType.*

class FingerprintSessionEventsManagerImpl(private val sessionEventsManager: SessionEventsManager) : FingerprintSessionEventsManager {

    override fun addEventInBackground(event: Event) {
        fromDomainToCore(event)?.let { sessionEventsManager.addEventInBackground(it) }
    }

    override fun addEvent(event: Event): Completable =
        fromDomainToCore(event)
            ?.let { sessionEventsManager.addEvent(it) }
            ?: Completable.complete()

    override fun updateHardwareVersionInScannerConnectivityEvent(hardwareVersion: String) =
        sessionEventsManager.updateHardwareVersionInScannerConnectivityEvent(hardwareVersion)

    override fun addLocationToSessionInBackground(latitude: Double, longitude: Double) =
        sessionEventsManager.updateSessionInBackground {
            it.location = Location(latitude, longitude)
        }

    override fun addPersonCreationEventInBackground(person: Person) =
        sessionEventsManager.addPersonCreationEventInBackground(person.fromDomainToCore())

    private fun fromDomainToCore(event: Event): CoreEvent? =
        when (event.type) {
            CANDIDATE_READ -> (event as CandidateReadEvent).fromDomainToCore()
            REFUSAL_RESPONSE -> (event as RefusalEvent).fromDomainToCore()
            CONSENT -> (event as ConsentEvent).fromDomainToCore()
            FINGERPRINT_CAPTURE -> (event as FingerprintCaptureEvent).fromDomainToCore()
            ONE_TO_ONE_MATCH -> (event as OneToOneMatchEvent).fromDomainToCore()
            ONE_TO_MANY_MATCH -> (event as OneToManyMatchEvent).fromDomainToCore()
            REFUSAL -> (event as RefusalEvent).fromDomainToCore()
            PERSON_CREATION -> (event as PersonCreationEvent).fromDomainToCore()
            SCANNER_CONNECTION -> (event as ScannerConnectionEvent).fromDomainToCore()
            ALERT_SCREEN -> (event as AlertScreenEvent).fromDomainToCore()
        }
}
