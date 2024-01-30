package com.simprints.infra.events.event.local.models

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.module.SimpleModule
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.domain.tokenization.serialization.TokenizationClassNameDeserializer
import com.simprints.core.domain.tokenization.serialization.TokenizationClassNameSerializer
import com.simprints.core.tools.json.JsonHelper
import com.simprints.infra.events.event.domain.models.Event
import com.simprints.infra.events.event.domain.models.EventLabels
import com.simprints.infra.events.event.domain.models.EventType
import com.simprints.infra.events.event.domain.models.session.SessionCaptureEvent
import com.simprints.infra.events.event.local.models.DbEvent.Companion.dbSerializationModule

@Entity
internal data class DbEvent(
    @PrimaryKey var id: String,
    @Embedded("createdAt_") val createdAt: DbTimestamp,
    val type: EventType,
    val projectId: String? = null,
    val sessionId: String? = null,
    var eventJson: String,
) {

    companion object {

        const val DEFAULT_EVENT_VERSION = 0
        val dbSerializationModule = SimpleModule().apply {
            addSerializer(TokenizableString::class.java, TokenizationClassNameSerializer())
            addDeserializer(TokenizableString::class.java, TokenizationClassNameDeserializer())
        }
    }
}

internal fun Event.fromDomainToDb(): DbEvent {
    return DbEvent(
        id = id,
        sessionId = labels.sessionId,
        projectId = labels.projectId,
        type = payload.type,
        eventJson = JsonHelper.toJson(this, module = dbSerializationModule),
        createdAt = DbTimestamp(payload.createdAt),
    )
}

internal fun DbEvent.fromDbToDomain(): Event = JsonHelper.fromJson(
    json = this.eventJson,
    module = dbSerializationModule,
    type = object : TypeReference<Event>() {})
