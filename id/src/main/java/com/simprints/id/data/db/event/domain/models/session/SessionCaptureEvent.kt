package com.simprints.id.data.db.event.domain.models.session

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.EventLabels
import com.simprints.id.data.db.event.domain.models.EventPayload
import com.simprints.id.data.db.event.domain.models.EventType
import com.simprints.id.data.db.event.domain.models.EventType.SESSION_CAPTURE
import com.simprints.id.data.db.event.local.models.DbEvent.Companion.DEFAULT_EVENT_VERSION
import com.simprints.id.domain.modality.Modes
import java.util.*

@Keep
data class SessionCaptureEvent(
    override val id: String = UUID.randomUUID().toString(),
    override val type: EventType,
    override var labels: EventLabels,
    override val payload: SessionCapturePayload
) : Event() {

    constructor(projectId: String,
                createdAt: Long,
                modalities: List<Modes>,
                appVersionName: String,
                libVersionName: String = "",
                language: String,
                device: Device,
                databaseInfo: DatabaseInfo,
                location: Location? = null,
                analyticsId: String? = null,
                id: String = UUID.randomUUID().toString(),
                labels: EventLabels = EventLabels(sessionId = id)) :
        this(
            id,
            SESSION_CAPTURE,
            labels,
            SessionCapturePayload(
                EVENT_VERSION,
                id,
                projectId,
                createdAt, 0, 0, 0,
                modalities,
                appVersionName,
                libVersionName,
                analyticsId,
                language,
                device,
                databaseInfo,
                location)) {

        // Ensure that sessionId is equal to the id
        this.labels = labels.copy(sessionId = id)
    }

    @Keep
    data class SessionCapturePayload(
        override val eventVersion: Int,
        val id: String,
        var projectId: String,
        override val createdAt: Long,
        var serverStartTime: Long = 0,
        override var endedAt: Long = 0,
        var relativeUploadTime: Long = 0,
        val modalities: List<Modes>,
        val appVersionName: String,
        val libVersionName: String,
        var analyticsId: String?,
        val language: String,
        val device: Device,
        val databaseInfo: DatabaseInfo,
        var location: Location? = null,
        override val type: EventType = SESSION_CAPTURE
    ) : EventPayload()

    companion object {
        // When the sync starts, any open activeSession started GRACE_PERIOD ms
        // before it will be considered closed
        const val GRACE_PERIOD: Long = 1000 * 60 * 5 // 5 minutes
        const val EVENT_VERSION = DEFAULT_EVENT_VERSION
    }
}
