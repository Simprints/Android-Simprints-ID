package com.simprints.id.data.db.event.remote.models

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.simprints.id.data.db.event.domain.models.ScannerFirmwareUpdateEvent.ScannerFirmwareUpdatePayload


@Keep
@JsonInclude(Include.NON_NULL)
data class ApiScannerFirmwareUpdatePayload(override val relativeStartTime: Long,
                                           override val version: Int,
                                           val relativeEndTime: Long,
                                           val chip: String,
                                           val targetAppVersion: String,
                                           val failureReason: String?) : ApiEventPayload(ApiEventPayloadType.ScannerFirmwareUpdate, version, relativeStartTime) {

    constructor(domainPayload: ScannerFirmwareUpdatePayload) :
        this(domainPayload.createdAt,
            domainPayload.eventVersion,
            domainPayload.endedAt,
            domainPayload.chip,
            domainPayload.targetAppVersion,
            domainPayload.failureReason)
}
