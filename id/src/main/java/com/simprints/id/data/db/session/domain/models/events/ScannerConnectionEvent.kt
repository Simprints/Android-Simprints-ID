package com.simprints.id.data.db.session.domain.models.events

import androidx.annotation.Keep

@Keep
class ScannerConnectionEvent(startTime: Long,
                             val scannerInfo: ScannerInfo) : Event(EventType.SCANNER_CONNECTION, startTime) {

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
