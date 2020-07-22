package com.simprints.id.data.db.event.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.OneToOneMatchEvent
import com.simprints.id.data.db.event.domain.models.OneToOneMatchEvent.OneToOneMatchPayload

@Keep
class ApiOneToOneMatchEvent(domainEvent: OneToOneMatchEvent) :
    ApiEvent(
        domainEvent.id,
        domainEvent.labels.map { it.fromDomainToApi() }.toMap(),
        domainEvent.payload.fromDomainToApi()) {

    @Keep
    class ApiOneToOneMatchPayload(createdAt: Long,
                                  eventVersion: Int,
                                  val relativeEndTime: Long,
                                  val candidateId: String,
                                  val result: ApiMatchEntry?) : ApiEventPayload(ApiEventPayloadType.ONE_TO_ONE_MATCH, eventVersion, createdAt) {

        constructor(domainPayload: OneToOneMatchPayload) :
            this(domainPayload.createdAt,
                domainPayload.eventVersion,
                domainPayload.endedAt,
                domainPayload.candidateId,
                domainPayload.result?.let { ApiMatchEntry(it) })
    }
}
