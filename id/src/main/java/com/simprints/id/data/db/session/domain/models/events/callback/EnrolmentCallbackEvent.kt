package com.simprints.id.data.db.session.domain.events.callback

import androidx.annotation.Keep
import com.simprints.id.data.db.session.domain.events.Event
import com.simprints.id.data.db.session.domain.events.EventType

@Keep
class EnrolmentCallbackEvent(starTime: Long,
                             val guid: String): Event(EventType.CALLBACK_ENROLMENT, starTime)
