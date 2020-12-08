package com.simprints.id.data.db.event.local.models

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fasterxml.jackson.core.type.TypeReference
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.EventLabels
import com.simprints.id.data.db.event.domain.models.EventType

@Entity
data class DbEvent(
    @PrimaryKey var id: String,

    @Embedded var labels: EventLabels,
    val type: EventType?,
    var eventJson: String,
    val createdAt: Long,
    val endedAt: Long
) {

    companion object {
        const val DEFAULT_EVENT_VERSION = 0
    }
}

fun Event.fromDomainToDb(): DbEvent =
    DbEvent(
        id,
        labels,
        payload.type,
        JsonHelper().toJson(this),
        payload.createdAt,
        payload.endedAt
    )

fun DbEvent.fromDbToDomain(): Event =
    JsonHelper().fromJson(this.eventJson, object : TypeReference<Event>() {})
