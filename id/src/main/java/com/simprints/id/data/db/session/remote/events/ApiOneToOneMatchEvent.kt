package com.simprints.id.data.db.session.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.session.domain.models.events.OneToOneMatchEvent

@Keep
class ApiOneToOneMatchEvent(val relativeStartTime: Long,
                            val relativeEndTime: Long,
                            val candidateId: String,
                            val result: ApiMatchEntry?): ApiEvent(ApiEventType.ONE_TO_ONE_MATCH) {

    constructor(oneToOneMatchEvent: OneToOneMatchEvent) :
        this(oneToOneMatchEvent.relativeStartTime ?: 0,
            oneToOneMatchEvent.relativeEndTime ?: 0,
            oneToOneMatchEvent.candidateId,
            oneToOneMatchEvent.result?.let { ApiMatchEntry(it) })
}
