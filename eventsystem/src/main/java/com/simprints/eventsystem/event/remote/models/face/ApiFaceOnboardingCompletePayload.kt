package com.simprints.eventsystem.event.remote.models.face

import androidx.annotation.Keep
import com.simprints.eventsystem.event.domain.models.face.FaceOnboardingCompleteEvent.FaceOnboardingCompletePayload
import com.simprints.eventsystem.event.remote.models.ApiEventPayload
import com.simprints.eventsystem.event.remote.models.ApiEventPayloadType.FaceOnboardingComplete

@Keep
data class ApiFaceOnboardingCompletePayload(override val startTime: Long, //Not added on API yet
                                            val endTime: Long,
                                            override val version: Int) : ApiEventPayload(FaceOnboardingComplete, version, startTime) {

    constructor(domainPayload: FaceOnboardingCompletePayload) : this(
        domainPayload.createdAt,
        domainPayload.endedAt,
        domainPayload.eventVersion)
}
