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

    @Embedded var labels: EventLabels,
    val type: EventType?,
    var eventJson: String,
    val createdAt: Long,
    val endedAt: Long,
    val sessionIsClosed: Boolean,
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
    val sessionIsClosed =
        if (this is SessionCaptureEvent) this.payload.sessionIsClosed
        else false

    return DbEvent(
        id = id,
        labels = labels,
        type = payload.type,
        eventJson = JsonHelper.toJson(this, module = dbSerializationModule),
        createdAt = payload.createdAt,
        endedAt = payload.endedAt,
        sessionIsClosed = sessionIsClosed
    )
}

internal fun DbEvent.fromDbToDomain(): Event = JsonHelper.fromJson(
    json = this.eventJson,
    module = dbSerializationModule,
    type = object : TypeReference<Event>() {})
