package com.simprints.id.data.db.event.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.ScannerConnectionEvent
import com.simprints.id.data.db.event.domain.events.ScannerConnectionEvent.ScannerConnectionPayload
import com.simprints.id.data.db.event.domain.events.ScannerConnectionEvent.ScannerConnectionPayload.ScannerGeneration
import com.simprints.id.data.db.event.domain.events.ScannerConnectionEvent.ScannerConnectionPayload.ScannerInfo

@Keep
class ApiScannerConnectionEvent(val relativeStartTime: Long,
                                val scannerInfo: ApiScannerInfo) : ApiEvent(ApiEventType.SCANNER_CONNECTION) {

    @Keep
    class ApiScannerInfo(val scannerId: String,
                         val macAddress: String,
                         val generation: ApiScannerGeneration?,
                         var hardwareVersion: String?) {
        constructor(scannerInfo: ScannerInfo) :
            this(scannerInfo.scannerId, scannerInfo.macAddress,
                scannerInfo.generation.toApiScannerGeneration(), scannerInfo.hardwareVersion)
    }

    constructor(scannerConnectionEvent: ScannerConnectionEvent) :
        this((scannerConnectionEvent.payload as ScannerConnectionPayload).creationTime,
            ApiScannerInfo(scannerConnectionEvent.payload.scannerInfo))

    enum class ApiScannerGeneration {
        VERO_1,
        VERO_2
    }
}

fun ScannerGeneration.toApiScannerGeneration() =
    when (this) {
        ScannerGeneration.VERO_1 -> ApiScannerConnectionEvent.ApiScannerGeneration.VERO_1
        ScannerGeneration.VERO_2 -> ApiScannerConnectionEvent.ApiScannerGeneration.VERO_2
    }
