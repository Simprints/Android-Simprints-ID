package com.simprints.id.data.db.event.domain.models.session

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.EventLabels
import com.simprints.id.data.db.event.domain.models.EventPayload
import com.simprints.id.data.db.event.domain.models.EventType
import com.simprints.id.data.db.event.domain.models.EventType.SESSION_CAPTURE
import com.simprints.id.domain.modality.Modes
import java.util.*

@Keep
data class SessionCaptureEvent(
    override val id: String = UUID.randomUUID().toString(),
    override val type: EventType,
    override var labels: EventLabels,
    override val payload: SessionCapturePayload
) : Event() {

    fun isOpen(): Boolean =
        payload.endedAt == 0L

    constructor(id: String,
                projectId: String,
                createdAt: Long,
                modalities: List<Modes>,
                appVersionName: String,
                libVersionName: String,
                language: String,
                device: Device,
                databaseInfo: DatabaseInfo,
                extraLabels: EventLabels = EventLabels()) :
        this(
            id,
            SESSION_CAPTURE,
            extraLabels.copy(sessionId = id, deviceId = device.deviceId, projectId = projectId, mode = modalities),
            SessionCapturePayload(
                EVENT_VERSION,
                id,
                projectId,
                createdAt,
                modalities,
                appVersionName,
                libVersionName,
                language,
                device,
                databaseInfo)) {

        // Ensure that sessionId is equal to the id
        this.labels = labels.copy(sessionId = id)
    }

    @Keep
    data class SessionCapturePayload(
        override val eventVersion: Int,
        val id: String,
        var projectId: String,
        override val createdAt: Long,
        val modalities: List<Modes>,
        val appVersionName: String,
        val libVersionName: String,
        val language: String,
        val device: Device,
        val databaseInfo: DatabaseInfo,
        var location: Location? = null,
        var analyticsId: String? = null,
        override var endedAt: Long = 0,
        var uploadedAt: Long = 0,
        override val type: EventType = SESSION_CAPTURE
    ) : EventPayload()

    companion object {
        // When the sync starts, any open activeSession started GRACE_PERIOD ms
        // before it will be considered closed
        const val GRACE_PERIOD: Long = 1000 * 60 * 5 // 5 minutes
        const val EVENT_VERSION = 1
    }

    fun updateProjectId(projectId: String) {
        payload.projectId = projectId
        labels = labels.copy(projectId = projectId)
    }
}
