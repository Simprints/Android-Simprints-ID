package com.simprints.id.data.db.event.remote.models

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.ScannerFirmwareUpdateEvent.ScannerFirmwareUpdatePayload


@Keep
class ApiScannerFirmwareUpdatePayload(override val relativeStartTime: Long,
                                      override val version: Int,
                                      val relativeEndTime: Long,
                                      val chip: String,
                                      val targetAppVersion: String,
                                      val failureReason: String?) : ApiEventPayload(ApiEventPayloadType.SCANNER_FIRMWARE_UPDATE, version, relativeStartTime) {

    constructor(domainPayload: ScannerFirmwareUpdatePayload) :
        this(domainPayload.createdAt,
            domainPayload.eventVersion,
            domainPayload.endedAt,
            domainPayload.chip,
            domainPayload.targetAppVersion,
            domainPayload.failureReason)
}
