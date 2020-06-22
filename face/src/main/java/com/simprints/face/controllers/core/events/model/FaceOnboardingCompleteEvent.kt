package com.simprints.face.controllers.core.events.model

import androidx.annotation.Keep

@Keep
class FaceOnboardingCompleteEvent(
    startTime: Long,
    endTime: Long
) : Event(EventType.FACE_ONBOARDING_COMPLETE, startTime, endTime)
