package com.simprints.id.data.db.event.domain.events

import androidx.annotation.Keep
import java.util.*

@Keep
class ScannerFirmwareUpdateEvent(
    startTime: Long,
    endTime: Long,
    chip: String,
    targetAppVersion: String,
    failureReason: String? = null,
    sessionId: String = UUID.randomUUID().toString() //StopShip: to change in PAS-993
) : Event(
    UUID.randomUUID().toString(),
    listOf(EventLabel.SessionId(sessionId)),
    ScannerFirmwareUpdatePayload(startTime, endTime, chip, targetAppVersion, failureReason)) {


    @Keep
    class ScannerFirmwareUpdatePayload(startTime: Long,
                                       val endTime: Long,
                                       val chip: String,
                                       val targetAppVersion: String,
                                       var failureReason: String? = null)
        : EventPayload(EventPayloadType.SCANNER_FIRMWARE_UPDATE, startTime)

}
