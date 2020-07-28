package com.simprints.id.data.db.event.remote.models

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.ArtificialTerminationEvent.ArtificialTerminationPayload
import com.simprints.id.data.db.event.domain.models.ArtificialTerminationEvent.ArtificialTerminationPayload.Reason
import com.simprints.id.data.db.event.domain.models.ArtificialTerminationEvent.ArtificialTerminationPayload.Reason.NEW_SESSION
import com.simprints.id.data.db.event.domain.models.ArtificialTerminationEvent.ArtificialTerminationPayload.Reason.TIMED_OUT
import com.simprints.id.data.db.event.remote.events.ApiArtificialTerminationPayload.ApiReason

@Keep
class ApiArtificialTerminationPayload(createdAt: Long,
                                      version: Int,
                                      val reason: ApiReason) : ApiEventPayload(ApiEventPayloadType.ARTIFICIAL_TERMINATION, version, createdAt) {

    constructor(domainPayload: ArtificialTerminationPayload) :
        this(domainPayload.createdAt, domainPayload.eventVersion, domainPayload.reason.fromDomainToApi())

    @Keep
    enum class ApiReason {
        TIMED_OUT, NEW_SESSION
    }
}

fun Reason.fromDomainToApi() =
    when (this) {
        TIMED_OUT -> ApiReason.TIMED_OUT
        NEW_SESSION -> ApiReason.NEW_SESSION
    }
