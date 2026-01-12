package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.core.tools.json.AnyPrimitiveSerializer
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.SuspiciousIntentEvent.SuspiciousIntentPayload
import kotlinx.serialization.Serializable

@Keep
@Serializable
internal data class ApiSuspiciousIntentPayload(
    override val startTime: ApiTimestamp,
    val unexpectedExtras: Map<String, @Serializable(with = AnyPrimitiveSerializer::class) Any?>,
) : ApiEventPayload() {
    constructor(domainPayload: SuspiciousIntentPayload) : this(
        domainPayload.createdAt.fromDomainToApi(),
        domainPayload.unexpectedExtras,
    )

    override fun getTokenizedFieldJsonPath(tokenKeyType: TokenKeyType): String? = null // this payload doesn't have tokenizable fields
}
