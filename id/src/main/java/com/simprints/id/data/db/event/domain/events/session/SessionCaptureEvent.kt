package com.simprints.id.data.db.event.domain.events.session

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.Event
import com.simprints.id.data.db.event.domain.events.EventLabel
import com.simprints.id.data.db.event.domain.events.EventLabel.SessionIdLabel
import com.simprints.id.data.db.event.domain.events.EventPayload
import com.simprints.id.data.db.event.domain.events.EventType
import java.util.*

@Keep
open class SessionCaptureEvent(override val id: String,
                               override val labels: MutableList<EventLabel>,
                               override val payload: SessionCapturePayload) : Event(id, labels, payload) {

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
            UUID.randomUUID().toString(),
            mutableListOf(SessionIdLabel(id)),
            SessionCapturePayload(
                createdAt,
                endTime,
                DEFAULT_EVENT_VERSION,
                id,
                projectId,
                appVersionName,
                libVersionName,
                language,
                device,
                databaseInfo,
                uploadTime,
                location,
                analyticsId))

    companion object {
        // When the sync starts, any open activeSession started GRACE_PERIOD ms
        // before it will be considered closed
        const val GRACE_PERIOD: Long = 1000 * 60 * 5 // 5 minutes
    }

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
    ) : EventPayload(EventType.SESSION_CAPTURE, eventVersion, createdAt, endedAt)
}
