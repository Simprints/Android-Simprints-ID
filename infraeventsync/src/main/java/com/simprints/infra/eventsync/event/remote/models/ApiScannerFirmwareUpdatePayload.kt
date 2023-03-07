package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.simprints.infra.events.event.domain.models.ScannerFirmwareUpdateEvent.ScannerFirmwareUpdatePayload
import com.simprints.infra.eventsync.event.remote.models.ApiEventPayload
import com.simprints.infra.eventsync.event.remote.models.ApiEventPayloadType


@Keep
@JsonInclude(Include.NON_NULL)
data class ApiScannerFirmwareUpdatePayload(
    override val startTime: Long,
    override val version: Int,
    val endTime: Long,
    val chip: String,
    val targetAppVersion: String,
    val failureReason: String?,
) : ApiEventPayload(ApiEventPayloadType.ScannerFirmwareUpdate, version, startTime) {

    constructor(domainPayload: ScannerFirmwareUpdatePayload) :
        this(domainPayload.createdAt,
            domainPayload.eventVersion,
            domainPayload.endedAt,
            domainPayload.chip,
            domainPayload.targetAppVersion,
            domainPayload.failureReason)
}
