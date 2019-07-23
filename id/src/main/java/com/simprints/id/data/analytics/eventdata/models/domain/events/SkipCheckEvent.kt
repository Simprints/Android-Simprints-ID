package com.simprints.id.data.analytics.eventdata.models.domain.events

import androidx.annotation.Keep

@Keep
class SkipCheckEvent(starTime: Long,
                     val skipValue: Boolean) : Event(EventType.SKIP_CHECK, starTime)
