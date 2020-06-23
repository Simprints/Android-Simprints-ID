package com.simprints.id.data.db.session.domain.models.events.callout

import androidx.annotation.Keep
import com.simprints.id.data.db.session.domain.models.events.Event
import com.simprints.id.data.db.session.domain.models.events.EventLabel
import com.simprints.id.data.db.session.domain.models.events.EventPayload
import com.simprints.id.data.db.session.domain.models.events.EventPayloadType
import java.util.*

@Keep
class VerificationCalloutEvent(
    startTime: Long,
    projectId: String,
    userId: String,
    moduleId: String,
    verifyGuid: String,
    metadata: String,
    sessionId: String = UUID.randomUUID().toString() //StopShip: to change in PAS-993
) : Event(
    UUID.randomUUID().toString(),
    listOf(EventLabel.SessionId(sessionId)),
    VerificationCalloutPayload(startTime, projectId, userId, moduleId, verifyGuid, metadata)) {

    @Keep
    class VerificationCalloutPayload(
        val startTime: Long,
        val projectId: String,
        val userId: String,
        val moduleId: String,
        val verifyGuid: String,
        val metadata: String
    ) : EventPayload(EventPayloadType.CALLOUT_VERIFICATION)
}
