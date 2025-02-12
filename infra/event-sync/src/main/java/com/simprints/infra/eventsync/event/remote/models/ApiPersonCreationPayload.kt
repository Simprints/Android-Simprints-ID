package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.PersonCreationEvent.PersonCreationPayload

@Keep
@Deprecated("Replaced by ApiBiometricReferenceCreationEvent in 2025.1.0")
internal data class ApiPersonCreationPayload(
    override val startTime: ApiTimestamp,
    val fingerprintCaptureIds: List<String>?,
    val fingerprintReferenceId: String?,
    val faceCaptureIds: List<String>?,
    val faceReferenceId: String?,
) : ApiEventPayload(startTime) {
    constructor(domainPayload: PersonCreationPayload) : this(
        domainPayload.createdAt.fromDomainToApi(),
        domainPayload.fingerprintCaptureIds,
        domainPayload.fingerprintReferenceId,
        domainPayload.faceCaptureIds,
        domainPayload.faceReferenceId,
    )

    override fun getTokenizedFieldJsonPath(tokenKeyType: TokenKeyType): String? = null // this payload doesn't have tokenizable fields
}
