package com.simprints.fingerprint.controllers.core.eventData

import com.simprints.fingerprint.controllers.core.eventData.model.Event
import io.reactivex.Completable

interface FingerprintSessionEventsManager {

    fun addEventInBackground(event: Event)
    fun addEvent(event: Event): Completable

    fun updateHardwareVersionInScannerConnectivityEvent(hardwareVersion: String)
}
