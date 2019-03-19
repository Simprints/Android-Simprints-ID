package com.simprints.id.data.analytics.eventdata.models.domain.events

import com.simprints.id.data.analytics.eventdata.models.domain.EventType

class ScannerConnectionEvent(val relativeStartTime: Long, val scannerInfo: ScannerInfo) : Event(EventType.SCANNER_CONNECTION) {

    class ScannerInfo(val scannerId: String, val macAddress: String, var hardwareVersion: String)
}
