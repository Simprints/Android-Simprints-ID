package com.simprints.id.data.db.session.domain.models.events.callout

import androidx.annotation.Keep
import com.simprints.id.data.db.session.domain.models.events.Event
import com.simprints.id.data.db.session.domain.models.events.EventLabel
import com.simprints.id.data.db.session.domain.models.events.EventPayload
import com.simprints.id.data.db.session.domain.models.events.EventPayloadType
import java.util.*

@Keep
class ConfirmationCalloutEvent(
    startTime: Long,
    projectId: String,
    selectedGuid: String,
    sessionId: String = UUID.randomUUID().toString() //StopShip: to change in PAS-993
) : Event(
    UUID.randomUUID().toString(),
    listOf(EventLabel.SessionId(sessionId)),
    ConfirmationCalloutPayload(startTime, projectId, selectedGuid, sessionId)) {

    @Keep
    class ConfirmationCalloutPayload(
        val startTime: Long,
        val projectId: String,
        val selectedGuid: String,
        val sessionId: String
    ) : EventPayload(EventPayloadType.CALLOUT_CONFIRMATION)
}
