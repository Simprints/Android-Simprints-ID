package com.simprints.id.data.analytics.eventdata.models.domain.events.callback

import androidx.annotation.Keep
import com.simprints.id.data.analytics.eventdata.models.domain.events.Event
import com.simprints.id.data.analytics.eventdata.models.domain.events.EventType

@Keep
class EnrolmentCallbackEvent(override val starTime: Long,
                             val guid: String): Event(EventType.CALLBACK_ENROLMENT)
