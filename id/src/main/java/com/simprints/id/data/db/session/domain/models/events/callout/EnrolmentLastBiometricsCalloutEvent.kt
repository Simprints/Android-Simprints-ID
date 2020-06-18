package com.simprints.id.data.db.session.domain.models.events.callout

import androidx.annotation.Keep
import com.simprints.id.data.db.session.domain.models.events.Event
import com.simprints.id.data.db.session.domain.models.events.EventType

@Keep
class EnrolmentLastBiometricsCalloutEvent(starTime: Long,
                                          val projectId: String,
                                          val userId: String,
                                          val moduleId: String,
                                          val metadata: String?,
                                          val sessionId: String) : Event(EventType.CALLOUT_LAST_BIOMETRICS, starTime)
