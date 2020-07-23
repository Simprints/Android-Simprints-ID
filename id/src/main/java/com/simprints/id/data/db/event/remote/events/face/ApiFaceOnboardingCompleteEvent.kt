package com.simprints.id.data.db.event.remote.events.face

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.face.FaceOnboardingCompleteEvent
import com.simprints.id.data.db.event.domain.models.face.FaceOnboardingCompleteEvent.FaceOnboardingCompletePayload
import com.simprints.id.data.db.event.remote.events.ApiEvent
import com.simprints.id.data.db.event.remote.events.ApiEventPayload
import com.simprints.id.data.db.event.remote.events.ApiEventPayloadType.FACE_ONBOARDING_COMPLETE
import com.simprints.id.data.db.event.remote.events.fromDomainToApi

@Keep
class ApiFaceOnboardingCompleteEvent(
    val domainEvent: FaceOnboardingCompleteEvent
) : ApiEvent(
    domainEvent.id,
    domainEvent.labels.fromDomainToApi(),
    domainEvent.payload.fromDomainToApi()) {

    @Keep
    class ApiFaceOnboardingCompletePayload(createdAt: Long,
                                           val endedAt: Long,
                                           version: Int) : ApiEventPayload(FACE_ONBOARDING_COMPLETE, version, createdAt) {

        constructor(domainPayload: FaceOnboardingCompletePayload) : this(
            domainPayload.createdAt,
            domainPayload.endedAt,
            domainPayload.eventVersion)
    }
}
