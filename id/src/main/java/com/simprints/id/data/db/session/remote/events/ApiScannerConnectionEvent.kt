package com.simprints.id.data.db.session.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.session.domain.models.events.ScannerConnectionEvent

@Keep
class ApiScannerConnectionEvent(val relativeStartTime: Long,
                                val scannerInfo: ApiScannerInfo): ApiEvent(ApiEventType.SCANNER_CONNECTION) {

    @Keep
    class ApiScannerInfo(val scannerId: String,
                         val macAddress: String,
                         val generation: ApiScannerGeneration?,
                         var hardwareVersion: String?) {
        constructor(scannerInfo: ScannerConnectionEvent.ScannerInfo) :
            this(scannerInfo.scannerId, scannerInfo.macAddress,
                scannerInfo.generation.toApiScannerGeneration(), scannerInfo.hardwareVersion)
    }

    constructor(scannerConnectionEvent: ScannerConnectionEvent) :
        this(scannerConnectionEvent.relativeStartTime ?: 0,
            ApiScannerInfo(scannerConnectionEvent.scannerInfo))

    enum class ApiScannerGeneration {
        VERO_1,
        VERO_2
    }
}

fun ScannerConnectionEvent.ScannerGeneration.toApiScannerGeneration() =
    when(this) {
        ScannerConnectionEvent.ScannerGeneration.VERO_1 -> ApiScannerConnectionEvent.ApiScannerGeneration.VERO_1
        ScannerConnectionEvent.ScannerGeneration.VERO_2 -> ApiScannerConnectionEvent.ApiScannerGeneration.VERO_2
    }
