package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.ScannerFirmwareUpdateEvent.ScannerFirmwareUpdatePayload


@Keep
@JsonInclude(Include.NON_NULL)
internal data class ApiScannerFirmwareUpdatePayload(
    override val startTime: ApiTimestamp,
    override val version: Int,
    val endTime: ApiTimestamp?,
    val chip: String,
    val targetAppVersion: String,
    val failureReason: String?,
) : ApiEventPayload(ApiEventPayloadType.ScannerFirmwareUpdate, version, startTime) {

    constructor(domainPayload: ScannerFirmwareUpdatePayload) : this(
        domainPayload.createdAt.fromDomainToApi(),
        domainPayload.eventVersion,
        domainPayload.endedAt?.fromDomainToApi(),
        domainPayload.chip,
        domainPayload.targetAppVersion,
        domainPayload.failureReason,
    )

    override fun getTokenizedFieldJsonPath(tokenKeyType: TokenKeyType): String? =
        null // this payload doesn't have tokenizable fields
}
