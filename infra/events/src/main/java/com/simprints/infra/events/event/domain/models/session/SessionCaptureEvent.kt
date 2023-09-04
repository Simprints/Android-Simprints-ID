package com.simprints.infra.events.event.domain.models.session

import androidx.annotation.Keep
import com.simprints.infra.config.domain.models.GeneralConfiguration.Modality
import com.simprints.infra.config.domain.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.Event
import com.simprints.infra.events.event.domain.models.EventLabels
import com.simprints.infra.events.event.domain.models.EventPayload
import com.simprints.infra.events.event.domain.models.EventType
import com.simprints.infra.events.event.domain.models.EventType.SESSION_CAPTURE
import java.util.UUID

@Keep
data class SessionCaptureEvent(
    override val id: String = UUID.randomUUID().toString(),
    override val type: EventType,
    override var labels: EventLabels,
    override val payload: SessionCapturePayload
) : Event() {

    constructor(
        id: String,
        projectId: String,
        createdAt: Long,
        modalities: List<Modality>,
        appVersionName: String,
        libVersionName: String,
        language: String,
        device: Device,
        databaseInfo: DatabaseInfo,
        extraLabels: EventLabels = EventLabels()
    ) :
            this(
                id,
                SESSION_CAPTURE,
                extraLabels.copy(sessionId = id, deviceId = device.deviceId, projectId = projectId),
                SessionCapturePayload(
                    eventVersion = EVENT_VERSION,
                    id = id,
                    projectId = projectId,
                    createdAt = createdAt,
                    modalities = modalities,
                    appVersionName = appVersionName,
                    libVersionName = libVersionName,
                    language = language,
                    device = device,
                    databaseInfo = databaseInfo
                )
            ) {

        // Ensure that sessionId is equal to the id
        this.labels = labels.copy(sessionId = id)
    }

    override fun getTokenizedFields(): Map<TokenKeyType, String> = emptyMap()

    override fun setTokenizedFields(map: Map<TokenKeyType, String>) = this // No tokenized fields

    @Keep
    data class SessionCapturePayload(
        override val eventVersion: Int,
        val id: String,
        var projectId: String,
        override val createdAt: Long,
        var modalities: List<Modality>,
        val appVersionName: String,
        val libVersionName: String,
        val language: String,
        val device: Device,
        val databaseInfo: DatabaseInfo,
        var location: Location? = null,
        override var endedAt: Long = 0,
        var uploadedAt: Long = 0,
        override val type: EventType = SESSION_CAPTURE,
        var sessionIsClosed: Boolean = false
    ) : EventPayload()

    companion object {
        const val EVENT_VERSION = 1
    }

    fun updateProjectId(projectId: String) {
        payload.projectId = projectId
        labels = labels.copy(projectId = projectId)
    }

    fun updateModalities(modalities: List<Modality>) {
        payload.modalities = modalities
    }
}
