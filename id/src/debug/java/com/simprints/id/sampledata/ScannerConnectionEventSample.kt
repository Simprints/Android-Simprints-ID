package com.simprints.id.sampledata

import com.simprints.id.data.db.event.domain.models.EventLabels
import com.simprints.id.data.db.event.domain.models.ScannerConnectionEvent
import com.simprints.id.sampledata.SampleDefaults.CREATED_AT

object ScannerConnectionEventSample : SampleEvent() {
    override fun getEvent(
        labels: EventLabels,
        isClosed: Boolean
    ): ScannerConnectionEvent {
        val scannerInfoArg = ScannerConnectionEvent.ScannerConnectionPayload.ScannerInfo(
            "scanner_id",
            "mac_address",
            ScannerConnectionEvent.ScannerConnectionPayload.ScannerGeneration.VERO_1,
            "hardware_version"
        )
        return ScannerConnectionEvent(CREATED_AT, scannerInfoArg, labels)
    }
}
