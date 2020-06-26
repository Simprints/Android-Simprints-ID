package com.simprints.id.data.db.session.domain.models.events

import androidx.annotation.Keep

@Keep
class FaceOnboardingCompleteEvent(
    startTime: Long,
    endTime: Long
) : Event(EventType.FACE_ONBOARDING_COMPLETE, startTime, endTime)
