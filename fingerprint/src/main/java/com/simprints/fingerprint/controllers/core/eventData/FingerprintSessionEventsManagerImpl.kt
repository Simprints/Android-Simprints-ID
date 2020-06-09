package com.simprints.fingerprint.controllers.core.eventData

import com.simprints.core.tools.extentions.completableWithSuspend
import com.simprints.fingerprint.controllers.core.eventData.model.*
import com.simprints.fingerprint.controllers.core.eventData.model.EventType.*
import com.simprints.id.data.db.session.SessionRepository
import com.simprints.id.tools.ignoreException
import io.reactivex.Completable
import kotlinx.coroutines.runBlocking
import com.simprints.id.data.db.session.domain.models.events.Event as CoreEvent

class FingerprintSessionEventsManagerImpl(private val sessionRepository: SessionRepository) : FingerprintSessionEventsManager {

    override fun addEventInBackground(event: Event) {
        fromDomainToCore(event)?.let { sessionRepository.addEventToCurrentSessionInBackground(it) }
    }

    override fun addEvent(event: Event): Completable =
        completableWithSuspend {
            ignoreException {
                fromDomainToCore(event)?.let {
                    runBlocking {
                        sessionRepository.updateCurrentSession { currentSession ->
                            currentSession.addEvent(it)
                        }
                    }
                }
            }
        }

    override fun updateHardwareVersionInScannerConnectivityEvent(hardwareVersion: String) {
        runBlocking {
            ignoreException {
                sessionRepository.updateCurrentSession { session ->
                    val scannerConnectivityEvents = session.getEvents().filterIsInstance(ScannerConnectionEvent::class.java)
                    scannerConnectivityEvents.forEach { it.scannerInfo.hardwareVersion = hardwareVersion }
                }
            }
        }
    }

    private fun fromDomainToCore(event: Event): CoreEvent? =
        when (event.type) {
            REFUSAL_RESPONSE -> (event as RefusalEvent).fromDomainToCore()
            FINGERPRINT_CAPTURE -> (event as FingerprintCaptureEvent).fromDomainToCore()
            ONE_TO_ONE_MATCH -> (event as OneToOneMatchEvent).fromDomainToCore()
            ONE_TO_MANY_MATCH -> (event as OneToManyMatchEvent).fromDomainToCore()
            REFUSAL -> (event as RefusalEvent).fromDomainToCore()
            PERSON_CREATION -> (event as PersonCreationEvent).fromDomainToCore()
            SCANNER_CONNECTION -> (event as ScannerConnectionEvent).fromDomainToCore()
            ALERT_SCREEN -> (event as AlertScreenEvent).fromDomainToCore()
            ALERT_SCREEN_WITH_SCANNER_ISSUE -> (event as AlertScreenEventWithScannerIssue).fromDomainToCore()
            VERO_2_INFO_SNAPSHOT -> (event as Vero2InfoSnapshotEvent).fromDomainToCore()
            SCANNER_FIRMWARE_UPDATE -> (event as ScannerFirmwareUpdateEvent).fromDomainToCore()
        }
}
