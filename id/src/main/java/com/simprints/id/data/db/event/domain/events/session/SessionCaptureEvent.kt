package com.simprints.id.data.db.event.domain.events.session

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.Event
import com.simprints.id.data.db.event.domain.events.EventPayload
import com.simprints.id.data.db.event.domain.events.EventPayloadType
import java.util.*

@Keep
open class SessionCaptureEvent(createdAt: Long,
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
    Event(
        UUID.randomUUID().toString(),
        mutableListOf(EventLabel.SessionId(id)),
        SessionCapturePayload(
            createdAt,
            DEFAULT_EVENT_VERSION,
            id,
            projectId,
            appVersionName,
            libVersionName,
            language,
            device,
            databaseInfo,
            uploadTime,
            endTime,
            location,
            analyticsId)) {

    companion object {
        // When the sync starts, any open activeSession started GRACE_PERIOD ms
        // before it will be considered closed
        const val GRACE_PERIOD: Long = 1000 * 60 * 5 // 5 minutes
    }

    @Keep
    class SessionCapturePayload(
        createdAt: Long,
        eventVersion: Int,
        val id: String = UUID.randomUUID().toString(),
        var projectId: String,
        val appVersionName: String,
        val libVersionName: String = "",
        val language: String,
        val device: Device,
        val databaseInfo: DatabaseInfo,
        val endTime: Long,
        val uploadTime: Long,
        var location: Location? = null,
        var analyticsId: String? = null
    ) : EventPayload(EventPayloadType.SESSION_CAPTURE, eventVersion, createdAt)
}
