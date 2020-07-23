package com.simprints.face.controllers.core.events.model

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.face.FaceOnboardingCompleteEvent as CoreFaceOnboardingCompleteEvent

@Keep
class FaceOnboardingCompleteEvent(
    startTime: Long,
    endTime: Long
) : Event(EventType.FACE_ONBOARDING_COMPLETE, startTime, endTime) {
    fun fromDomainToCore(): CoreFaceOnboardingCompleteEvent = CoreFaceOnboardingCompleteEvent(startTime, endTime)
}
