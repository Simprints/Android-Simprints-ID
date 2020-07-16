package com.simprints.id.data.db.event.domain.events

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.EventLabel.SessionIdLabel
import java.util.*

@Keep
class ScannerFirmwareUpdateEvent(
    createdAt: Long,
    endTime: Long,
    chip: String,
    targetAppVersion: String,
    failureReason: String? = null,
    sessionId: String = UUID.randomUUID().toString() //StopShip: to change in PAS-993
) : Event(
    UUID.randomUUID().toString(),
    mutableListOf(SessionIdLabel(sessionId)),
    ScannerFirmwareUpdatePayload(createdAt, DEFAULT_EVENT_VERSION, endTime, chip, targetAppVersion, failureReason)) {


    @Keep
    class ScannerFirmwareUpdatePayload(createdAt: Long,
                                       eventVersion: Int,
                                       val endTime: Long,
                                       val chip: String,
                                       val targetAppVersion: String,
                                       var failureReason: String? = null)
        : EventPayload(EventPayloadType.SCANNER_FIRMWARE_UPDATE, eventVersion, createdAt)

}
