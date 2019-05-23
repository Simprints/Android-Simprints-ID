package com.simprints.fingerprint.controllers.core.eventData

import com.simprints.fingerprint.controllers.core.eventData.model.*
import com.simprints.fingerprint.data.domain.person.Person
import io.reactivex.Completable

interface FingerprintSessionEventsManager {

    fun addEventInBackground(event: Event)
    fun addEvent(event: Event): Completable

    fun addLocationToSessionInBackground(latitude: Double, longitude: Double)
    fun updateHardwareVersionInScannerConnectivityEvent(hardwareVersion: String)
    fun addPersonCreationEventInBackground(person: Person)
}
