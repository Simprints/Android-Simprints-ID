package com.simprints.id.data.db.event.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.OneToOneMatchEvent
import com.simprints.id.data.db.event.domain.events.OneToOneMatchEvent.OneToOneMatchPayload

@Keep
class ApiOneToOneMatchEvent(val relativeStartTime: Long,
                            val relativeEndTime: Long,
                            val candidateId: String,
                            val result: ApiMatchEntry?) : ApiEvent(ApiEventType.ONE_TO_ONE_MATCH) {

    constructor(oneToOneMatchEvent: OneToOneMatchEvent) :
        this((oneToOneMatchEvent.payload as OneToOneMatchPayload).creationTime,
            oneToOneMatchEvent.payload.endTime,
            oneToOneMatchEvent.payload.candidateId,
            oneToOneMatchEvent.payload.result?.let { ApiMatchEntry(it) })
}
