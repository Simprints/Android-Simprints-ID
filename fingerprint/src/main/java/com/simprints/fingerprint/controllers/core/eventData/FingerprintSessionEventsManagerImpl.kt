package com.simprints.fingerprint.controllers.core.eventData

import com.simprints.core.tools.extentions.inBackground
import com.simprints.eventsystem.event.EventRepository
import com.simprints.fingerprint.controllers.core.eventData.model.*
import com.simprints.fingerprint.controllers.core.eventData.model.EventType.*
import com.simprints.id.tools.ignoreException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.runBlocking
import com.simprints.eventsystem.event.domain.models.Event as CoreEvent

class FingerprintSessionEventsManagerImpl(private val eventRepository: EventRepository,
                                          private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) :
    FingerprintSessionEventsManager {

    override fun addEventInBackground(event: Event) {
        inBackground(dispatcher) {
            fromDomainToCore(event).let {
                eventRepository.addOrUpdateEvent(it)
            }
        }
    }

    override suspend fun addEvent(event: Event) {
        ignoreException {
            fromDomainToCore(event).let {
                eventRepository.addOrUpdateEvent(it)
            }
        }
    }

    override fun updateHardwareVersionInScannerConnectivityEvent(hardwareVersion: String) {
        runBlocking {
            ignoreException {
                val currentSession = eventRepository.getCurrentCaptureSessionEvent()
                val scannerConnectivityEvents = eventRepository.getEventsFromSession(currentSession.id).filterIsInstance<ScannerConnectionEvent>()
                scannerConnectivityEvents.collect { it.scannerInfo.hardwareVersion = hardwareVersion }
            }
        }
    }

    private fun fromDomainToCore(event: Event): CoreEvent =
        when (event.type) {
            REFUSAL_RESPONSE -> (event as RefusalEvent).fromDomainToCore()
            FINGERPRINT_CAPTURE -> (event as FingerprintCaptureEvent).fromDomainToCore()
            FINGERPRINT_CAPTURE_BIOMETRICS -> (event as FingerprintCaptureBiometricsEvent).fromDomainToCore()
            ONE_TO_ONE_MATCH -> (event as OneToOneMatchEvent).fromDomainToCore()
            ONE_TO_MANY_MATCH -> (event as OneToManyMatchEvent).fromDomainToCore()
            REFUSAL -> (event as RefusalEvent).fromDomainToCore()
            SCANNER_CONNECTION -> (event as ScannerConnectionEvent).fromDomainToCore()
            ALERT_SCREEN -> (event as AlertScreenEvent).fromDomainToCore()
            ALERT_SCREEN_WITH_SCANNER_ISSUE -> (event as AlertScreenEventWithScannerIssue).fromDomainToCore()
            VERO_2_INFO_SNAPSHOT -> (event as Vero2InfoSnapshotEvent).fromDomainToCore()
            SCANNER_FIRMWARE_UPDATE -> (event as ScannerFirmwareUpdateEvent).fromDomainToCore()
        }
}
