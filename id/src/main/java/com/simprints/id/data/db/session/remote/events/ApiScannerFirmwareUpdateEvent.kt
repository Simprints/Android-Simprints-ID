package com.simprints.id.data.db.session.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.session.domain.models.events.ScannerFirmwareUpdateEvent

@Keep
class ApiScannerFirmwareUpdateEvent(val relativeStartTime: Long,
                                    val relativeEndTime: Long,
                                    val chip: String,
                                    val targetAppVersion: String,
                                    val failureReason: String?): ApiEvent(ApiEventType.SCANNER_FIRMWARE_UPDATE) {

    constructor(scannerFirmwareUpdateEvent: ScannerFirmwareUpdateEvent):
        this(scannerFirmwareUpdateEvent.relativeStartTime ?: 0,
            scannerFirmwareUpdateEvent.relativeEndTime ?: 0,
            scannerFirmwareUpdateEvent.chip,
            scannerFirmwareUpdateEvent.targetAppVersion,
            scannerFirmwareUpdateEvent.failureReason)
}
