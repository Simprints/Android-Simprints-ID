package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.BiometricReferenceCreationEvent.BiometricReferenceCreationPayload
import kotlinx.serialization.Serializable

@Keep
@Serializable
internal data class ApiBiometricReferenceCreationPayload(
    override val startTime: ApiTimestamp,
    val id: String,
    val modality: String,
    val captureIds: List<String>,
) : ApiEventPayload() {
    constructor(domainPayload: BiometricReferenceCreationPayload) : this(
        domainPayload.createdAt.fromDomainToApi(),
        domainPayload.id,
        domainPayload.modality.name,
        domainPayload.captureIds,
    )

    override fun getTokenizedFieldJsonPath(tokenKeyType: TokenKeyType): String? = null
}
