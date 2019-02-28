package com.simprints.id.data.analytics.eventdata.models.domain.events

import com.simprints.id.data.analytics.eventdata.models.domain.EventType
import com.simprints.id.domain.responses.Response

class CallbackEvent(val relativeStartTime: Long,
                    val appResponse: Response) : Event(EventType.CALLBACK) //STOPSHIP: changed Callout in the event. PT?
