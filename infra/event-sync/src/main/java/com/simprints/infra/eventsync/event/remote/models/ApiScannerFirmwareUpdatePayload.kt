package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.ScannerFirmwareUpdateEvent.ScannerFirmwareUpdatePayload
import kotlinx.serialization.Serializable

@Keep
@Serializable
internal data class ApiScannerFirmwareUpdatePayload(
    override val startTime: ApiTimestamp,
    val endTime: ApiTimestamp?,
    val chip: String,
    val targetAppVersion: String,
    val failureReason: String?,
) : ApiEventPayload() {
    constructor(domainPayload: ScannerFirmwareUpdatePayload) : this(
        domainPayload.createdAt.fromDomainToApi(),
        domainPayload.endedAt?.fromDomainToApi(),
        domainPayload.chip,
        domainPayload.targetAppVersion,
        domainPayload.failureReason,
    )

    override fun getTokenizedFieldJsonPath(tokenKeyType: TokenKeyType): String? = null // this payload doesn't have tokenizable fields
}
