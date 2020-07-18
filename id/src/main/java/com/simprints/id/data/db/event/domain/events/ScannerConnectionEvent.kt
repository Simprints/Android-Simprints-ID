package com.simprints.id.data.db.event.domain.events

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.EventLabel.SessionIdLabel
import java.util.*

@Keep
class ScannerConnectionEvent(
    createdAt: Long,
    scannerInfo: ScannerConnectionPayload.ScannerInfo,
    sessionId: String = UUID.randomUUID().toString() //StopShip: to change in PAS-993
) : Event(
    UUID.randomUUID().toString(),
    mutableListOf(SessionIdLabel(sessionId)),
    ScannerConnectionPayload(createdAt, DEFAULT_EVENT_VERSION, scannerInfo)) {


    @Keep
    class ScannerConnectionPayload(createdAt: Long,
                                   eventVersion: Int,
                                   val scannerInfo: ScannerInfo) : EventPayload(EventType.SCANNER_CONNECTION, eventVersion, createdAt) {

        @Keep
        class ScannerInfo(val scannerId: String,
                          val macAddress: String,
                          val generation: ScannerGeneration,
                          var hardwareVersion: String?)

        enum class ScannerGeneration {
            VERO_1,
            VERO_2
        }
    }
}
