package com.simprints.id.data.db.session.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.session.domain.events.ScannerConnectionEvent

@Keep
class ApiScannerConnectionEvent(val relativeStartTime: Long,
                                val scannerInfo: ApiScannerInfo): ApiEvent(ApiEventType.SCANNER_CONNECTION) {

    @Keep
    class ApiScannerInfo(val scannerId: String, val macAddress: String, var hardwareVersion: String) {
        constructor(scannerInfo: ScannerConnectionEvent.ScannerInfo) :
            this(scannerInfo.scannerId, scannerInfo.macAddress, scannerInfo.hardwareVersion)
    }

    constructor(scannerConnectionEvent: ScannerConnectionEvent) :
        this(scannerConnectionEvent.relativeStartTime ?: 0,
            ApiScannerInfo(scannerConnectionEvent.scannerInfo))
}
