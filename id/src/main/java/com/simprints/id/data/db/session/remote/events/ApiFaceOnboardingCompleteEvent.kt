package com.simprints.id.data.db.session.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.session.domain.models.events.FaceOnboardingCompleteEvent

@Keep
class ApiFaceOnboardingCompleteEvent(
    val relativeStartTime: Long
) : ApiEvent(ApiEventType.FACE_ONBOARDING_COMPLETE) {

    constructor(
        faceOnboardingCompleteEvent: FaceOnboardingCompleteEvent
    ) : this(faceOnboardingCompleteEvent.relativeStartTime ?: 0)

}
