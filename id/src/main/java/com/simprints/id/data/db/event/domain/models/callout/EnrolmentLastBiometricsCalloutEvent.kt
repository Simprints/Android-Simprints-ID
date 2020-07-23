package com.simprints.id.data.db.event.domain.models.callout

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.EventLabel
import com.simprints.id.data.db.event.domain.models.EventLabel.SessionIdLabel
import com.simprints.id.data.db.event.domain.models.EventPayload
import com.simprints.id.data.db.event.domain.models.EventType
import com.simprints.id.data.db.event.domain.models.EventType.CALLOUT_LAST_BIOMETRICS
import java.util.*

@Keep
class EnrolmentLastBiometricsCalloutEvent(
    override val id: String = UUID.randomUUID().toString(),
    override val labels: MutableList<EventLabel>,
    override val payload: EnrolmentLastBiometricsCalloutPayload,
    override val type: EventType
) : Event(id, labels, payload, type) {

    constructor(
        createdAt: Long,
        projectId: String,
        userId: String,
        moduleId: String,
        metadata: String?,
        sessionId: String
    ) : this(
        UUID.randomUUID().toString(),
        mutableListOf(SessionIdLabel(sessionId)),
        EnrolmentLastBiometricsCalloutPayload(createdAt, EVENT_VERSION, projectId, userId, moduleId, metadata, sessionId),
        CALLOUT_LAST_BIOMETRICS)

    @Keep
    class EnrolmentLastBiometricsCalloutPayload(createdAt: Long,
                                                eventVersion: Int,
                                                val projectId: String,
                                                val userId: String,
                                                val moduleId: String,
                                                val metadata: String?,
                                                val sessionId: String) : EventPayload(CALLOUT_LAST_BIOMETRICS, eventVersion, createdAt)

    companion object {
        const val EVENT_VERSION = DEFAULT_EVENT_VERSION
    }
}
