package com.simprints.id.sampledata

import com.simprints.id.data.db.event.domain.models.EventLabels
import com.simprints.id.data.db.event.domain.models.ScannerConnectionEvent
import com.simprints.id.sampledata.DefaultTestConstants.CREATED_AT
import com.simprints.id.sampledata.DefaultTestConstants.GUID1

object ScannerConnectionEventSample : SampleEvent() {
    override fun getEvent(
        sessionId: String,
        subjectId: String,
        isClosed: Boolean
    ): ScannerConnectionEvent {
        val labels = EventLabels(sessionId = GUID1)
        val scannerInfoArg = ScannerConnectionEvent.ScannerConnectionPayload.ScannerInfo(
            "scanner_id",
            "mac_address",
            ScannerConnectionEvent.ScannerConnectionPayload.ScannerGeneration.VERO_1,
            "hardware_version"
        )
        return ScannerConnectionEvent(CREATED_AT, scannerInfoArg, labels)
    }
}
