package com.simprints.fingerprint.controllers.core.eventData

import com.simprints.fingerprint.controllers.core.eventData.model.*
import com.simprints.fingerprint.data.domain.person.Person
import com.simprints.fingerprint.data.domain.person.fromDomainToCore
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import io.reactivex.Completable
import com.simprints.id.data.analytics.eventdata.models.domain.events.Event as CoreEvent

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
        sessionEventsManager.addLocationToSession(latitude, longitude)

    override fun addPersonCreationEventInBackground(person: Person) =
        sessionEventsManager.addPersonCreationEventInBackground(person.fromDomainToCore())

    private fun fromDomainToCore(event: Event): CoreEvent? =
        when (event) {
            is CandidateReadEvent -> event.fromDomainToCore()
            is ConsentEvent -> event.fromDomainToCore()
            is FingerprintCaptureEvent -> event.fromDomainToCore()
            is ScannerConnectionEvent -> event.fromDomainToCore()
            else -> null
        }

}
