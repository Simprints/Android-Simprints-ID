package com.simprints.fingerprint.controllers.core.eventData.model

import androidx.annotation.Keep

@Keep
class OneToOneMatchEvent(override val starTime: Long,
                         override val endTime: Long,
                         val candidateId: String,
                         val result: MatchEntry?) : Event(EventType.ONE_TO_ONE_MATCH)
