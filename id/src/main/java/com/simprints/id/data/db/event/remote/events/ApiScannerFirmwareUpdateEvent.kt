package com.simprints.id.data.db.event.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.ScannerFirmwareUpdateEvent
import com.simprints.id.data.db.event.domain.events.ScannerFirmwareUpdateEvent.ScannerFirmwareUpdatePayload
import com.simprints.id.data.db.session.remote.events.ApiEvent

@Keep
class ApiScannerFirmwareUpdateEvent(val relativeStartTime: Long,
                                    val relativeEndTime: Long,
                                    val chip: String,
                                    val targetAppVersion: String,
                                    val failureReason: String?) : ApiEvent(ApiEventType.SCANNER_FIRMWARE_UPDATE) {

    constructor(scannerFirmwareUpdateEvent: ScannerFirmwareUpdateEvent) :
        this((scannerFirmwareUpdateEvent.payload as ScannerFirmwareUpdatePayload).relativeStartTime,
            scannerFirmwareUpdateEvent.payload.relativeStartTime,
            scannerFirmwareUpdateEvent.payload.chip,
            scannerFirmwareUpdateEvent.payload.targetAppVersion,
            scannerFirmwareUpdateEvent.payload.failureReason)
}
