package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.ScannerConnectionEvent.ScannerConnectionPayload
import com.simprints.infra.events.event.domain.models.ScannerConnectionEvent.ScannerConnectionPayload.ScannerGeneration
import com.simprints.infra.events.event.domain.models.ScannerConnectionEvent.ScannerConnectionPayload.ScannerGeneration.VERO_1
import com.simprints.infra.events.event.domain.models.ScannerConnectionEvent.ScannerConnectionPayload.ScannerGeneration.VERO_2
import com.simprints.infra.eventsync.event.remote.models.ApiScannerConnectionPayload.ApiScannerGeneration

@Keep
@JsonInclude(Include.NON_NULL)
internal data class ApiScannerConnectionPayload(
    override val startTime: ApiTimestamp,
    val scannerInfo: ApiScannerInfo,
) : ApiEventPayload(startTime) {
    @Keep
    @JsonInclude(Include.NON_NULL)
    data class ApiScannerInfo(
        val scannerId: String,
        val macAddress: String,
        val generation: ApiScannerGeneration?,
        var hardwareVersion: String?,
    )

    constructor(domainPayload: ScannerConnectionPayload) : this(
        domainPayload.createdAt.fromDomainToApi(),
        ApiScannerInfo(
            domainPayload.scannerInfo.scannerId,
            domainPayload.scannerInfo.macAddress,
            domainPayload.scannerInfo.generation.toApiScannerGeneration(),
            domainPayload.scannerInfo.hardwareVersion,
        ),
    )

    enum class ApiScannerGeneration {
        VERO_1,
        VERO_2,
    }

    override fun getTokenizedFieldJsonPath(tokenKeyType: TokenKeyType): String? = null // this payload doesn't have tokenizable fields
}

internal fun ScannerGeneration.toApiScannerGeneration() = when (this) {
    VERO_1 -> ApiScannerGeneration.VERO_1
    VERO_2 -> ApiScannerGeneration.VERO_2
}
