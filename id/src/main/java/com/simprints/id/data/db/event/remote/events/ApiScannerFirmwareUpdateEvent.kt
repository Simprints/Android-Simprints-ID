package com.simprints.id.data.db.event.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.ScannerFirmwareUpdateEvent
import com.simprints.id.data.db.event.domain.events.ScannerFirmwareUpdateEvent.ScannerFirmwareUpdatePayload

@Keep
class ApiScannerFirmwareUpdateEvent(domainEvent: ScannerFirmwareUpdateEvent) :
    ApiEvent(
        domainEvent.id,
        domainEvent.labels.fromDomainToApi(),
        domainEvent.payload.fromDomainToApi()) {

    @Keep
    class ApiScannerFirmwareUpdatePayload(val relativeStartTime: Long,
                                          val relativeEndTime: Long,
                                          val chip: String,
                                          val targetAppVersion: String,
                                          val failureReason: String?) : ApiEventPayload(ApiEventPayloadType.SCANNER_FIRMWARE_UPDATE) {

        constructor(domainPayload: ScannerFirmwareUpdatePayload) :
            this(domainPayload.creationTime,
                domainPayload.endTime,
                domainPayload.chip,
                domainPayload.targetAppVersion,
                domainPayload.failureReason)
    }
}
