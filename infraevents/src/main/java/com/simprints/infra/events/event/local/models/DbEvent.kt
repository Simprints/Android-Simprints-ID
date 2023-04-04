package com.simprints.infra.events.event.local.models

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fasterxml.jackson.core.type.TypeReference
import com.simprints.core.tools.json.JsonHelper
import com.simprints.infra.events.event.domain.models.Event
import com.simprints.infra.events.event.domain.models.EventLabels
import com.simprints.infra.events.event.domain.models.EventType
import com.simprints.infra.events.event.domain.models.session.SessionCaptureEvent

@Entity
internal data class DbEvent(
    @PrimaryKey var id: String,

    @Embedded var labels: EventLabels,
    val type: EventType?,
    var eventJson: String,
    val createdAt: Long,
    val endedAt: Long,
    val sessionIsClosed: Boolean
) {

    companion object {
        const val DEFAULT_EVENT_VERSION = 0
    }
}

internal fun Event.fromDomainToDb(): DbEvent {
    val sessionIsClosed =
        if (this is SessionCaptureEvent) this.payload.sessionIsClosed
        else false

    return DbEvent(
        id,
        labels,
        payload.type,
        JsonHelper.toJson(this),
        payload.createdAt,
        payload.endedAt,
        sessionIsClosed
    )
}

internal fun DbEvent.fromDbToDomain(): Event =
    JsonHelper.fromJson(this.eventJson, object : TypeReference<Event>() {})