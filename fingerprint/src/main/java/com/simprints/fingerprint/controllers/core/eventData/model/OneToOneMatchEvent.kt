package com.simprints.fingerprint.controllers.core.eventData.model

import androidx.annotation.Keep

@Keep
class OneToOneMatchEvent(starTime: Long,
                         endTime: Long,
                         val candidateId: String,
                         val result: MatchEntry?) : Event(EventType.ONE_TO_ONE_MATCH, starTime, endTime)
