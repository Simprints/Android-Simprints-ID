package com.simprints.fingerprint.controllers.core.eventData.model

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.ScannerFirmwareUpdateEvent as ScannerFirmwareUpdateEventCore

@Keep
class ScannerFirmwareUpdateEvent(startTime: Long,
                                 endTime: Long,
                                 val chip: String,
                                 val targetAppVersion: String,
                                 val failureReason: String? = null) : Event(EventType.SCANNER_FIRMWARE_UPDATE, startTime, endTime)

fun ScannerFirmwareUpdateEvent.fromDomainToCore(): ScannerFirmwareUpdateEventCore =
    ScannerFirmwareUpdateEventCore(startTime, endTime, chip, targetAppVersion, failureReason)
