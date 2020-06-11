package com.simprints.id.data.db.session.domain.models.events

import androidx.annotation.Keep

@Keep
class ScannerConnectionEvent(
    startTime: Long,
    val scannerInfo: ScannerInfo
) : Event(EventType.SCANNER_CONNECTION, startTime) {

    @Keep
    class ScannerInfo(val scannerId: String, val macAddress: String, var hardwareVersion: String)
}
