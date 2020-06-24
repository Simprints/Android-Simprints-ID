package com.simprints.id.data.db.event.domain.events.callout

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.Event
import com.simprints.id.data.db.event.domain.events.EventLabel
import com.simprints.id.data.db.event.domain.events.EventPayload
import com.simprints.id.data.db.event.domain.events.EventPayloadType
import java.util.*


@Keep
class EnrolmentLastBiometricsCalloutEvent(
    starTime: Long,
    projectId: String,
    userId: String,
    moduleId: String,
    metadata: String?,
    sessionId: String,
    sessionStartTime: Long = 0 //StopShip: to change in PAS-993
) : Event(
    UUID.randomUUID().toString(),
    listOf(EventLabel.SessionId(sessionId)),
    EnrolmentLastBiometricsCalloutPayload(starTime, starTime - sessionStartTime, projectId, userId, moduleId, metadata, sessionId)) {

    @Keep
    class EnrolmentLastBiometricsCalloutPayload(starTime: Long,
                                                relativeStartTime: Long,
                                                val projectId: String,
                                                val userId: String,
                                                val moduleId: String,
                                                val metadata: String?,
                                                val sessionId: String) : EventPayload(EventPayloadType.CALLOUT_LAST_BIOMETRICS, starTime, relativeStartTime)

}
