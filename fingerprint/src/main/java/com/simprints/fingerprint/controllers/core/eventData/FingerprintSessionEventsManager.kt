package com.simprints.fingerprint.controllers.core.eventData

import com.simprints.fingerprint.controllers.core.eventData.model.Event

interface FingerprintSessionEventsManager {

    fun addEventInBackground(event: Event)
    suspend fun addEvent(event: Event)

    fun updateHardwareVersionInScannerConnectivityEvent(hardwareVersion: String)
}
