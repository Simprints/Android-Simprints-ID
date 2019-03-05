package com.simprints.id.data.analytics.eventdata.models.remote.events

import com.simprints.id.data.analytics.eventdata.models.domain.events.ScannerConnectionEvent

class ApiScannerConnectionEvent(val relativeStartTime: Long,
                                val scannerInfo: ApiScannerInfo): ApiEvent(ApiEventType.SCANNER_CONNECTION) {

    class ApiScannerInfo(val scannerId: String, val macAddress: String, var hardwareVersion: String) {
        constructor(scannerInfo: ScannerConnectionEvent.ScannerInfo) :
            this(scannerInfo.scannerId, scannerInfo.macAddress, scannerInfo.hardwareVersion)
    }

    constructor(scannerConnectionEvent: ScannerConnectionEvent) :
        this(scannerConnectionEvent.relativeStartTime,
            ApiScannerInfo(scannerConnectionEvent.scannerInfo))
}
