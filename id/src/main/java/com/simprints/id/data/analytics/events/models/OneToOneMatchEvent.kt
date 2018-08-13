package com.simprints.id.data.analytics.events.models

import com.simprints.id.data.db.remote.enums.VERIFY_GUID_EXISTS_RESULT

class OneToOneMatchEvent(val relativeStartTime: Long,
                         val relativeEndTime: Long,
                         val candidateId: String,
                         val result: VERIFY_GUID_EXISTS_RESULT,
                         val matchResult: MatchCandidate?) : Event(EventType.ONE_TO_ONE_MATCH)
