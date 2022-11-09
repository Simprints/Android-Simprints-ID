package com.simprints.fingerprint.controllers.core.eventData.model

import androidx.annotation.Keep
import com.simprints.fingerprint.scanner.domain.ota.AvailableOta
import com.simprints.eventsystem.event.domain.models.ScannerFirmwareUpdateEvent as ScannerFirmwareUpdateEventCore

/**
 * This class represents a event from updating the firmware on the scanner.
 *
 * @property chip  the name of the chip [AvailableOta] undergoing the firmware update
 * @property targetAppVersion  the version the firmware is being updated to
 * @property failureReason  the reason the firmware update failed, if any
 */
@Keep
class ScannerFirmwareUpdateEvent(startTime: Long,
                                 endTime: Long,
                                 val chip: String,
                                 val targetAppVersion: String,
                                 val failureReason: String? = null) : Event(EventType.SCANNER_FIRMWARE_UPDATE, startTime, endTime)

fun ScannerFirmwareUpdateEvent.fromDomainToCore(): ScannerFirmwareUpdateEventCore =
    ScannerFirmwareUpdateEventCore(startTime, endTime, chip, targetAppVersion, failureReason)
