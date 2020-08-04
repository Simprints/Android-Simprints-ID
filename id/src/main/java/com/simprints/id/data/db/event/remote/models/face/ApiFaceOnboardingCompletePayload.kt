package com.simprints.id.data.db.event.remote.models.face

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.face.FaceOnboardingCompleteEvent.FaceOnboardingCompletePayload
import com.simprints.id.data.db.event.remote.models.ApiEventPayload
import com.simprints.id.data.db.event.remote.models.ApiEventPayloadType.FaceOnboardingComplete

@Keep
data class ApiFaceOnboardingCompletePayload(override val relativeStartTime: Long, //Not added on API yet
                                       val relativeEndTime: Long,
                                       override val version: Int) : ApiEventPayload(FaceOnboardingComplete, version, relativeStartTime) {

    constructor(domainPayload: FaceOnboardingCompletePayload) : this(
        domainPayload.createdAt,
        domainPayload.endedAt,
        domainPayload.eventVersion)
}
