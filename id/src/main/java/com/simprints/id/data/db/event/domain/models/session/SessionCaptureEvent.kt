package com.simprints.id.data.db.event.domain.models.session

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.EventLabel
import com.simprints.id.data.db.event.domain.models.EventLabel.SessionIdLabel
import com.simprints.id.data.db.event.domain.models.EventPayload
import com.simprints.id.data.db.event.domain.models.EventType
import com.simprints.id.data.db.event.domain.models.EventType.SESSION_CAPTURE
import java.util.*

@Keep
open class SessionCaptureEvent(
    override val id: String = UUID.randomUUID().toString(),
    override val labels: MutableList<EventLabel>,
    override val payload: SessionCapturePayload,
    override val type: EventType
) : Event(id, labels, payload, type) {

    constructor(createdAt: Long,
                id: String = UUID.randomUUID().toString(),
                projectId: String,
                appVersionName: String,
                libVersionName: String = "",
                language: String,
                device: Device,
                databaseInfo: DatabaseInfo,
                uploadTime: Long = 0,
                endTime: Long = 0,
                location: Location? = null,
                analyticsId: String? = null) :
        this(
            id,
            mutableListOf(SessionIdLabel(id)),
            SessionCapturePayload(
                createdAt,
                endTime,
                EVENT_VERSION,
                id,
                projectId,
                appVersionName,
                libVersionName,
                language,
                device,
                databaseInfo,
                uploadTime,
                location,
                analyticsId),
            SESSION_CAPTURE)

    @Keep
    class SessionCapturePayload(
        createdAt: Long,
        endedAt: Long,
        eventVersion: Int,
        val id: String = UUID.randomUUID().toString(),
        var projectId: String,
        val appVersionName: String,
        val libVersionName: String = "",
        val language: String,
        val device: Device,
        val databaseInfo: DatabaseInfo,
        val uploadTime: Long,
        var location: Location? = null,
        var analyticsId: String? = null
    ) : EventPayload(SESSION_CAPTURE, eventVersion, createdAt, endedAt)

    companion object {
        // When the sync starts, any open activeSession started GRACE_PERIOD ms
        // before it will be considered closed
        const val GRACE_PERIOD: Long = 1000 * 60 * 5 // 5 minutes
        const val EVENT_VERSION = DEFAULT_EVENT_VERSION
    }
}
