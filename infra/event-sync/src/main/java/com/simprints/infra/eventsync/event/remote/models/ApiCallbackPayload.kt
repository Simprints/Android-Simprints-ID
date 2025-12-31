package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.callback.ConfirmationCallbackEvent
import com.simprints.infra.events.event.domain.models.callback.EnrolmentCallbackEvent
import com.simprints.infra.events.event.domain.models.callback.ErrorCallbackEvent
import com.simprints.infra.events.event.domain.models.callback.IdentificationCallbackEvent
import com.simprints.infra.events.event.domain.models.callback.RefusalCallbackEvent
import com.simprints.infra.events.event.domain.models.callback.VerificationCallbackEvent
import kotlinx.serialization.Serializable

@Keep
@Serializable
internal data class ApiCallbackPayload(
    override val startTime: ApiTimestamp,
    val callback: ApiCallback,
) : ApiEventPayload() {
    constructor(domainPayload: EnrolmentCallbackEvent.EnrolmentCallbackPayload) : this(
        domainPayload.createdAt.fromDomainToApi(),
        ApiEnrolmentCallback(domainPayload.guid),
    )

    constructor(domainPayload: IdentificationCallbackEvent.IdentificationCallbackPayload) : this(
        domainPayload.createdAt.fromDomainToApi(),
        ApiIdentificationCallback(
            domainPayload.sessionId,
            domainPayload.scores.map { it.fromDomainToApi(domainPayload.eventVersion) },
        ),
    )

    constructor(domainPayload: VerificationCallbackEvent.VerificationCallbackPayload) : this(
        domainPayload.createdAt.fromDomainToApi(),
        ApiVerificationCallback(domainPayload.score.fromDomainToApi(domainPayload.eventVersion)),
    )

    constructor(domainPayload: ConfirmationCallbackEvent.ConfirmationCallbackPayload) : this(
        domainPayload.createdAt.fromDomainToApi(),
        ApiConfirmationCallback(domainPayload.identificationOutcome),
    )

    constructor(domainPayload: ErrorCallbackEvent.ErrorCallbackPayload) : this(
        domainPayload.createdAt.fromDomainToApi(),
        ApiErrorCallback(domainPayload.reason.fromDomainToApi()),
    )

    constructor(domainPayload: RefusalCallbackEvent.RefusalCallbackPayload) : this(
        domainPayload.createdAt.fromDomainToApi(),
        ApiRefusalCallback(domainPayload.reason, domainPayload.extra),
    )

    override fun getTokenizedFieldJsonPath(tokenKeyType: TokenKeyType): String? = null // this payload doesn't have tokenizable fields

    companion object {
        const val CALLBACK_ENROLMENT_KEY = "ENROLMENT"
        const val CALLBACK_IDENTIFICATION_KEY = "IDENTIFICATION"
        const val CALLBACK_VERIFICATION_KEY = "VERIFICATION"
        const val CALLBACK_CONFIRMATION_KEY = "CONFIRMATION"
        const val CALLBACK_ERROR_KEY = "ERROR"
    }
}
