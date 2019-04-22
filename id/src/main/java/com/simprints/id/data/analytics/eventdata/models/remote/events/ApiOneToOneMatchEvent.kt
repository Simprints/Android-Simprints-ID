package com.simprints.id.data.analytics.eventdata.models.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.analytics.eventdata.models.domain.events.OneToOneMatchEvent

@Keep
class ApiOneToOneMatchEvent(val relativeStartTime: Long,
                            val relativeEndTime: Long,
                            val candidateId: String,
                            val result: ApiMatchEntry?): ApiEvent(ApiEventType.ONE_TO_ONE_MATCH) {

    constructor(oneToOneMatchEvent: OneToOneMatchEvent) :
        this(oneToOneMatchEvent.relativeStartTime,
            oneToOneMatchEvent.relativeEndTime,
            oneToOneMatchEvent.candidateId,
            oneToOneMatchEvent.result?.let { ApiMatchEntry(it) })
}
