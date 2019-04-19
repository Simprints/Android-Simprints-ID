package com.simprints.id.data.analytics.eventdata.models.domain.events

import androidx.annotation.Keep

@Keep
class ScannerConnectionEvent(val relativeStartTime: Long, val scannerInfo: ScannerInfo) : Event(EventType.SCANNER_CONNECTION) {

    @Keep
    class ScannerInfo(val scannerId: String, val macAddress: String, var hardwareVersion: String)
}
