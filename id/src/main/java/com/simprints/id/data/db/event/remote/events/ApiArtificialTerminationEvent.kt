package com.simprints.id.data.db.event.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.ArtificialTerminationEvent
import com.simprints.id.data.db.event.domain.events.ArtificialTerminationEvent.ArtificialTerminationPayload
import com.simprints.id.data.db.event.domain.events.ArtificialTerminationEvent.ArtificialTerminationPayload.Reason
import com.simprints.id.data.db.event.domain.events.ArtificialTerminationEvent.ArtificialTerminationPayload.Reason.NEW_SESSION
import com.simprints.id.data.db.event.domain.events.ArtificialTerminationEvent.ArtificialTerminationPayload.Reason.TIMED_OUT
import com.simprints.id.data.db.event.remote.events.ApiArtificialTerminationEvent.ApiArtificialTerminationPayload.ApiReason

@Keep
class ApiArtificialTerminationEvent(domainEvent: ArtificialTerminationEvent) :
    ApiEvent(
        domainEvent.id,
        domainEvent.labels.fromDomainToApi(),
        domainEvent.payload.fromDomainToApi()) {

    @Keep
    class ApiArtificialTerminationPayload(val relativeStartTime: Long,
                                          val reason: ApiReason) : ApiEventPayload(ApiEventPayloadType.ARTIFICIAL_TERMINATION) {

        constructor(domainPayload: ArtificialTerminationPayload) :
            this(domainPayload.creationTime, domainPayload.reason.fromDomainToApi())

        @Keep
        enum class ApiReason {
            TIMED_OUT, NEW_SESSION
        }
    }
}

fun Reason.fromDomainToApi() =
    when (this) {
        TIMED_OUT -> ApiReason.TIMED_OUT
        NEW_SESSION -> ApiReason.NEW_SESSION
    }
