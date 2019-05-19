package com.simprints.fingerprint.controllers.core.eventData.model

import androidx.annotation.Keep
import com.simprints.id.data.analytics.eventdata.models.domain.events.ScannerConnectionEvent as ScannerConnectionEventCore
import com.simprints.id.data.analytics.eventdata.models.domain.events.ScannerConnectionEvent.ScannerInfo as ScannerInfoCore

@Keep
class ScannerConnectionEvent(startTime: Long,
                             val scannerInfo: ScannerInfo) : Event(EventType.SCANNER_CONNECTION, startTime) {

    @Keep
    class ScannerInfo(val scannerId: String, val macAddress: String, var hardwareVersion: String)
}

fun ScannerConnectionEvent.fromDomainToCore() =
    ScannerConnectionEventCore(starTime, scannerInfo.fromDomainToCore())

fun ScannerConnectionEvent.ScannerInfo.fromDomainToCore() =
    ScannerInfoCore(scannerId, macAddress, hardwareVersion)
