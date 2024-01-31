package com.simprints.infra.events.event.domain.models.session

import androidx.annotation.Keep
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.config.store.models.GeneralConfiguration.Modality
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.Event
import com.simprints.infra.events.event.domain.models.EventPayload
import com.simprints.infra.events.event.domain.models.EventType
import com.simprints.infra.events.event.domain.models.EventType.SESSION_CAPTURE
import java.util.UUID

@Deprecated("Should be deleted")
@Keep
data class SessionCaptureEvent(
    override val id: String = UUID.randomUUID().toString(),
    override val type: EventType,
    override val payload: SessionCapturePayload,
    override var sessionId: String? = null,
    override var projectId: String? = null,
) : Event() {

    constructor(
        id: String,
        projectId: String,
        createdAt: Timestamp,
        modalities: List<Modality>,
        appVersionName: String,
        libVersionName: String,
        language: String,
        device: Device,
        databaseInfo: DatabaseInfo,
        location: Location? = null,
    ) : this(
        id,
        SESSION_CAPTURE,
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
            databaseInfo = databaseInfo,
            location = location,
        ),
        sessionId = id,
        projectId = projectId,
    )

    override fun getTokenizedFields(): Map<TokenKeyType, TokenizableString> = emptyMap()

    override fun setTokenizedFields(map: Map<TokenKeyType, TokenizableString>) =
        this // No tokenized fields

    @Deprecated("Should be deleted")
    @Keep
    data class SessionCapturePayload(
        override val eventVersion: Int,
        val id: String,
        var projectId: String,
        override val createdAt: Timestamp,
        var modalities: List<Modality>,
        val appVersionName: String,
        val libVersionName: String,
        var language: String,
        val device: Device,
        val databaseInfo: DatabaseInfo,
        var location: Location? = null,
        var uploadedAt: Long = 0,
        override val endedAt: Timestamp? = null,
        override val type: EventType = SESSION_CAPTURE,
        var sessionIsClosed: Boolean = false,
    ) : EventPayload()

    companion object {

        const val EVENT_VERSION = 2
    }
}
