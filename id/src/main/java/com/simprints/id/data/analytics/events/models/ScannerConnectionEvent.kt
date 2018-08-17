package com.simprints.id.data.analytics.events.models

class ScannerConnectionEvent(val relativeStartTime: Long, val scannerInfo: ScannerInfo) : Event(EventType.SCANNER_CONNECTION) {

    class ScannerInfo(val scannerId: String, val macAddress: String, var hardwareVersion: String)
}
