package com.simprints.id.data.db.session.domain.models.events.callout

import androidx.annotation.Keep
import com.simprints.id.data.db.session.domain.models.events.Event
import com.simprints.id.data.db.session.domain.models.events.EventLabel
import com.simprints.id.data.db.session.domain.models.events.EventPayload
import com.simprints.id.data.db.session.domain.models.events.EventPayloadType
import java.util.*


@Keep
class EnrolmentLastBiometricsCalloutEvent(
    starTime: Long,
    projectId: String,
    userId: String,
    moduleId: String,
    metadata: String?,
    sessionId: String
) : Event(
    UUID.randomUUID().toString(),
    listOf(EventLabel.SessionId(sessionId)),
    EnrolmentLastBiometricsCalloutPayload(starTime, projectId, userId, moduleId, metadata, sessionId)) {

    @Keep
    class EnrolmentLastBiometricsCalloutPayload(val starTime: Long,
                                                val projectId: String,
                                                val userId: String,
                                                val moduleId: String,
                                                val metadata: String?,
                                                val sessionId: String) : EventPayload(EventPayloadType.CALLOUT_LAST_BIOMETRICS)

}
