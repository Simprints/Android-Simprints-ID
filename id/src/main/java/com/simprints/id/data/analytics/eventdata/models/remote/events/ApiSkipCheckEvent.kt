package com.simprints.id.data.analytics.eventdata.models.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.analytics.eventdata.models.domain.events.SkipCheckEvent

@Keep
class ApiSkipCheckEvent(val relativeStartTime: Long,
                        val skipValue: Boolean) : ApiEvent(ApiEventType.SKIP_CHECK) {

    constructor(skipCheckEvent: SkipCheckEvent) :
        this(skipCheckEvent.relativeStartTime ?: 0, skipCheckEvent.skipValue)
}
