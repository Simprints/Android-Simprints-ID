package com.simprints.fingerprint.controllers.core.eventData.model

import androidx.annotation.Keep
import com.simprints.fingerprint.scanner.domain.ScannerGeneration as DomainScannerGeneration
import com.simprints.id.data.db.event.domain.events.ScannerConnectionEvent as ScannerConnectionEventCore
import com.simprints.id.data.db.event.domain.events.ScannerConnectionEvent.ScannerConnectionPayload.ScannerGeneration as ScannerGenerationCore
import com.simprints.id.data.db.event.domain.events.ScannerConnectionEvent.ScannerConnectionPayload.ScannerInfo as ScannerInfoCore

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
        VERO_2;

        companion object {
            fun get(generation: DomainScannerGeneration) = when (generation) {
                DomainScannerGeneration.VERO_1 -> VERO_1
                DomainScannerGeneration.VERO_2 -> VERO_2
            }
        }
    }
}

fun ScannerConnectionEvent.fromDomainToCore(): ScannerConnectionEventCore =
    ScannerConnectionEventCore(startTime, scannerInfo.fromDomainToCore())

fun ScannerConnectionEvent.ScannerInfo.fromDomainToCore(): ScannerInfoCore =
    ScannerInfoCore(scannerId, macAddress, generation.fromDomainToCore(), hardwareVersion)

fun ScannerConnectionEvent.ScannerGeneration.fromDomainToCore(): ScannerGenerationCore =
    when (this) {
        ScannerConnectionEvent.ScannerGeneration.VERO_1 -> ScannerGenerationCore.VERO_1
        ScannerConnectionEvent.ScannerGeneration.VERO_2 -> ScannerGenerationCore.VERO_2
    }
