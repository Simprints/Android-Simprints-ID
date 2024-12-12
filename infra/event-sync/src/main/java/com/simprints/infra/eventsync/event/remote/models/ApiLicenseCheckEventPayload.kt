package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.LicenseCheckEvent

@Keep
internal data class ApiLicenseCheckEventPayload(
    override val startTime: ApiTimestamp,
    val status: LicenseCheckEvent.LicenseStatus,
    val vendor: String,
) : ApiEventPayload(startTime) {
    constructor(domainPayload: LicenseCheckEvent.LicenseCheckEventPayload) : this(
        domainPayload.createdAt.fromDomainToApi(),
        domainPayload.status,
        domainPayload.vendor,
    )

    override fun getTokenizedFieldJsonPath(tokenKeyType: TokenKeyType): String? = null // this payload doesn't have tokenizable fields
}
