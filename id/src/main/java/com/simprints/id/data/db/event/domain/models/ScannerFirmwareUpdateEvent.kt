package com.simprints.id.data.db.event.domain.models

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.EventLabel.SessionIdLabel
import com.simprints.id.data.db.event.domain.models.EventType.SCANNER_FIRMWARE_UPDATE
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
    ScannerFirmwareUpdatePayload(createdAt, EVENT_VERSION, endTime, chip, targetAppVersion, failureReason),
    SCANNER_FIRMWARE_UPDATE) {


    @Keep
    class ScannerFirmwareUpdatePayload(createdAt: Long,
                                       eventVersion: Int,
                                       endTimeAt: Long,
                                       val chip: String,
                                       val targetAppVersion: String,
                                       var failureReason: String? = null)
        : EventPayload(SCANNER_FIRMWARE_UPDATE, eventVersion, createdAt, endTimeAt)

    companion object {
        const val EVENT_VERSION = DEFAULT_EVENT_VERSION
    }
}
