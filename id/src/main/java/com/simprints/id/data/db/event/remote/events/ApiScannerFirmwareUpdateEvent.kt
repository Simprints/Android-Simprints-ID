package com.simprints.id.data.db.event.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.ScannerFirmwareUpdateEvent
import com.simprints.id.data.db.event.domain.models.ScannerFirmwareUpdateEvent.ScannerFirmwareUpdatePayload

@Keep
class ApiScannerFirmwareUpdateEvent(domainEvent: ScannerFirmwareUpdateEvent) :
    ApiEvent(
        domainEvent.id,
        domainEvent.labels.map { it.fromDomainToApi() }.toMap(),
        domainEvent.payload.fromDomainToApi()) {

    @Keep
    class ApiScannerFirmwareUpdatePayload(createdAt: Long,
                                          eventVersion: Int,
                                          val relativeEndTime: Long,
                                          val chip: String,
                                          val targetAppVersion: String,
                                          val failureReason: String?) : ApiEventPayload(ApiEventPayloadType.SCANNER_FIRMWARE_UPDATE, eventVersion, createdAt) {

        constructor(domainPayload: ScannerFirmwareUpdatePayload) :
            this(domainPayload.createdAt,
                domainPayload.eventVersion,
                domainPayload.endedAt,
                domainPayload.chip,
                domainPayload.targetAppVersion,
                domainPayload.failureReason)
    }
}
